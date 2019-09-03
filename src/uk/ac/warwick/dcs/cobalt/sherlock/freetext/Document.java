/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

/**
 *
 * <P>Class designed to store data about individual documents. Each of these
 * objects stores sentence objects, which are added by the parser. Provides
 * access to the comparison algorithm which detects sentences likely to
 * be plagiarised from other documents within the data set.</P>
 *
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */
public class Document
    implements Serializable, Cloneable {
  /**
   * Sentences shorter than this constant value will be discarded.
   */
  public static final int MIN_SENTENCE_LENGTH = 4;

  /**
   * Stores all the Sentence objects this file contains.
   */
  private Vector sentences;

  /**
   * The file name of the file this document object originated from.
   */
  private String fileName;

  /**
   * Constructor.
   */
  public Document() {
    sentences = new Vector();
  }

  /**
   * Creates a clone of this Document. All Sentence objects are cloned, as is
   * the Vector used to store them. See the clone() method in Sentence for
   * further details.
   * @return A clone of this object.
   */
  public Object clone() {
    Object temp;
    try {
      temp = super.clone();
    }
    catch (CloneNotSupportedException ex) {
      return null;
    }
    Document cloneDoc = (Document) temp;
    cloneDoc.sentences = (Vector) cloneDoc.sentences.clone();
    Vector cloneSent = cloneDoc.sentences;
    for (int i = 0; i < cloneSent.size(); i++) {
      cloneSent.set(i, ( (Sentence) cloneSent.get(i)).clone());
    }
    return cloneDoc;
  }

  /**
   * Tells the object that a new Sentence is required at the given line number.
   * @param lineNo The line number in the original document that the sentence
   * starts at.
   * @param col The column that the parser says the sentence starts at.
   * @return The new sentence object so that words can be placed into it.
   */
  public Sentence startSentence(int lineNo, int col) {
    // create new sentence, add it to the sentences vector and return it to
    // the calling method.
    Sentence temp = new Sentence(lineNo, col, sentences.size());
    sentences.add(temp);
    return temp;
  }

  /**
   * Call this when the current sentence has been completely parsed. Only call
   * this method if startSentence has been called before it.
   * @param lineNo The line number in the original document that the current
   * sentence ends on.
   * @param col The column number in the original document that the sentence
   * ends on.
   */
  public void endSentence(int lineNo, int col) {
    // last element in vector is current sentence.
    Sentence temp = (Sentence) sentences.get(sentences.size() - 1);
    temp.setEndCoords(lineNo, col);
  }

  /**
   * The sentences contained within this object.
   * @return A vector of Sentence objects.
   */
  public Vector getSentences() {
    return sentences;
  }

  /**
   * Compares each sentence in this Document with every sentence in the given
   * Document. The method is commutative so there is no need to call it from
   * the other document. No output is produced but the state of the Sentence
   * objects is altered to indicate which sentences they are similar to.
   * @param other The document to be compared.
   */
  public void compareToDocument(Document other) {
    Vector otherSent = other.sentences;
    for (int i = 0; i < sentences.size(); i++) {
      Sentence temp = (Sentence) sentences.get(i);
      if (!temp.isInExcludeFile()) {
        for (int j = 0; j < otherSent.size(); j++) {
          Sentence temp2 = (Sentence) otherSent.get(j);
          if (!temp2.isInExcludeFile()) {
            temp.compareToSentence(temp2);
          }
        }
      }
    }
  }

  /**
   * Return an array of SentencePair objects. There is exactly one for every
   * sentence in this document and the order of the returned array matches the
   * order of the sentences returned by getSentences().
   * @return An array of SentencePair[] objects relating this document to
   * others in the comparison group.
   */
  public SentencePair[] getPairsArray() {
    SentencePair[] pairs = new SentencePair[sentences.size()];
    for (int i = 0; i < sentences.size(); i++) {
      pairs[i] = ( (Sentence) sentences.get(i)).getSentencePairs();
    }
    return pairs;
  }

  /**
   * Sets the sentence pairs for this object. The document is only saved by
   * Sherlock in its state before the comparison, its sentence pair array
   * contains the data from the comparison. Therefore, use this method to
   * place the document in its post-comparison state.
   * @param pairs The array of SentencePair objects linking this document
   * to other similar ones.
   */
  public void setPairsArray(SentencePair[] pairs) {
    assert pairs.length == sentences.size();
    for (int i = 0; i < sentences.size(); i++) {
      ( (Sentence) sentences.get(i)).setSentencePair(pairs[i]);
    }
  }

  /**
   * Returns a string showing the sentences of this document grouped together
   * to show similarity. Used for debugging purposes.
   * @return Returns this document's similarityString.
   */
  public String similarityString() {
    String out = this.fileName + "\n";
    for (int i = 0; i < sentences.size(); i++) {
      Sentence temp = (Sentence) sentences.get(i);
      if (temp.getSentencePairs().scores.size() > 0) {
        out += "Sentence " + temp.getId() + ": \n";
        out += temp.similarityString();
      }
    }
    return out;
  }

  /**
   * Used primarily for debugging purposes.
   * @return The string representation of the object.
   */
  public String toString() {
    String temp = "Size: " + sentences.size() + ", ";
    for (int i = 0; i < sentences.size(); i++) {
      temp += "Sentence " + i + ": " +
          ( (Sentence) sentences.get(i)).toString() + "\n";
    }
    return temp + Sentence.getDictionary().toString();
  }

  /**
   * Two documents are equal if they contain equal sentences in the same order.
   * Used for debugging only as it is quite slow.
   * @param o The object to compare to.
   * @return True if the objects are equal, false otherwise.
   */
  public boolean equals(Object o) {
    if (! (o instanceof Document)) {
      return false;
    }
    if (o == this) {
      return true;
    }
    Vector testSent = ( (Document) o).getSentences();
    return fileName.equals( ( (Document) o).fileName) &&
        testSent.equals(sentences);
  }

  /**
       *  Tidys the vectors used to store sentences by removing the short sentences.
   *  Reorders sentence ID's as it removes sentences so that there are no gaps
   *  in the sequence. This should not be called once comparisons of documents
   *  have started.
   */
  public void finishedParsing() {
    for (int i = 0; i < sentences.size(); i++) {
      Sentence temp = (Sentence) sentences.get(i);
      if (temp.getWords().size() < MIN_SENTENCE_LENGTH) {
        // Decrement all following sentence ID's.
        for (int j = i + 1; j < sentences.size(); j++) {
          ( (Sentence) sentences.get(j)).setId(j - 1);
        }
        sentences.removeElementAt(i);
        i--;
      }
    }
    sentences.trimToSize();
  }

  /**
   * Sets the file name that this document object is originated from. Also sets
   * the filename in the sentence objects.
   * @param fileName The file name of the file that this document is originated
   * from.
   */
  public void setFileName(String fileName) {
    this.fileName = fileName;
    for (int i = 0; i < sentences.size(); i++) {
      ( (Sentence) sentences.get(i)).setFileName(fileName);
    }
  }

  /**
   * The filename this object was constructed from.
   * @return The original file's file name.
   */
  public String getFileName() {
    return fileName;
  }
}