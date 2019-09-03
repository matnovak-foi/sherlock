/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

/**
 * <p>Class used to store sentences from documents. Can also store links
 * to other similar sentences once the comparison algorithm has run its
 * course.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 *
 * @author Daniel White
 * @version 4
 */
public class Sentence
    implements Serializable, Cloneable {

  /**
   * dictionary is a hashtable of every word that has been parsed that is not
   * in the commonWords hashtable.
   */
  private static MyHashtable dictionary, commonWords = new MyHashtable(37);

  /**
   * Used by the constructor to initialise static variables.
   */
  private static boolean initialised = false;

  /**
   * The first element indicates line number, the second indicates column for
   * first letter of the sentence in the original text. The last two elements
   * serve the same purpose for the end of the sentence.
   */
  private int[] sentenceCoords = new int[4];
  final static int START_LINE = 0, START_COL = 1, END_LINE = 2, END_COL = 3;

  /**
   * Words that are common in English, used to populate the commonWords table
   * which filters out all words that are too common to be useful.
   */
  private static String[] common = {
      "the", "a", "an", "its", "this", "that",
      "to", "in", "his", "hers", "these", "we", "they", "do", "so", "be", "as",
      "if", "of", "and", "for", "any", "on", "or", "is", "was", "out", "are"};

  /**
   * Percentage similarity used to determine if sentences should be marked as
       * similar. Default value is 80 but this is replaced by whatever is set by the
   * user in Sherlock.
   */
  public static int SIM_THRESHOLD = 80;

  /**
   * If two sentences contain more than this amount of words in common then
   * they are marked as similar regardless of the similarity threshold. Default
       * value is 6 but this is replaced by whatever is set by the user in Sherlock.
   */
  public static int COMMON_THRESHOLD = 6;

  /**
   * Used to determine if this sentence is part of some exclude file. False by
   * default.
   */
  private boolean inExcludeFile = false;

  private boolean ignored = false;

  /**
   * Stores the words in this sentence. Each word should only appear once.
   */
  private MyHashtable words = new MyHashtable();

  /**
   * The line number this sentence starts on in the original text.
   */
  private int lineNo, id;

  private String fileName = "";

  /**
   * List of similar sentences. Any sentence stored in this list should also
   * have this sentence stored in its own list.
   */
  private SentencePair pairs;

  /**
   * Constructor. Initialises static variables if this is the first Sentence
   * object to be constructed.
   * @param lineNo The line number this sentence starts on.
   * @param col The column on the line that this sentence starts on.
   * @param id A unique id number for this sentence, used to link to other
   * sentences.
   */
  public Sentence(int lineNo, int col, int id) {
    if (dictionary == null) {
      dictionary = new MyHashtable(2000);
    }
    this.lineNo = lineNo;
    sentenceCoords[START_LINE] = lineNo - 1;
    sentenceCoords[START_COL] = col;
    this.id = id;
    pairs = new SentencePair(this);
    if (!initialised) {
      // place words from the common array in the commonWords hashtable.
      for (int i = 0; i < common.length; i++) {
        commonWords.put(hash(common[i]), common[i]);
      }
      initialised = true;
    }
  }

  /**
   * Returns a clone of the Sentence, for use when two different subsets
   * of the results are needed. The pairs variable is set to an empty
   * SentencePair object so that different ersults can be assigned to the
   * Sentence. This allows more than one different view of the document at one
   * time.
   * @return A shallow copy of this Sentence, pairs is reinitialised to an
   * empty SentencePair, but the table of words is left.
   */
  public Object clone() {
    try {
      Sentence clone = (Sentence)super.clone();
      clone.pairs = new SentencePair(clone);
      return clone;
    }
    catch (CloneNotSupportedException ex) {
      return null;
    }
  }

  /**
   * Adds the given word to this sentence, if it is not already in the sentence
   * and the word is not in the list of common words.
   * @param word The word that will be added.
   */
  public void addWord(String word) {
    // all words are stored in lower case.
    word = word.toLowerCase();

    // shouldn't happen, but just in case.
    if (word.equals("") || word.equals(" ")) {
      return;
    }
    Integer hashVal = hash(word);
    Integer origHashVal = hashVal;
    String test = commonWords.get(hashVal);

    while (test != null) {
      if (test.equals(word)) {
        return;
      }
      hashVal = new Integer(hashVal.intValue() + 1);
      test = commonWords.get(hashVal);
    }
    // saves calling the hashCode method again, should be more efficient.
    hashVal = origHashVal;

    String dictWord = dictionary.get(hashVal);
    while (dictWord != null) {
      if (dictWord.equals(word)) {
        /* word is in the dictionary so try adding it to the words table.
         * I added dictWord instead of word because this is the object already
         * in the dictionary hashtable so it will save on memory. */
        Integer wordHash = origHashVal;
        String temp = words.get(wordHash);
        while (temp != null) {
          /* return if the word is in the dictionary and in the words table for
           * this sentence. */
          if (temp.equals(dictWord)) {
            return;
          }
          wordHash = new Integer(wordHash.intValue() + 1);
          temp = words.get(wordHash);
        }
        /* To reach this line, dictWord is not in the words table for this
         * sentence and wordHash is a blank space in the table. */
        words.put(wordHash, dictWord);
        return;
      }
      hashVal = new Integer(hashVal.intValue() + 1);
      dictWord = dictionary.get(hashVal);
    }
    // word is not in the dictionary so add it.
    dictionary.put(hashVal, word);
    Object obj = words.put(origHashVal, word);
    assert obj == null;

  } //addWord

  /**
   * Compares this sentence to the given sentence. Commutative so there is no
   * need to call it in the reverse order as well. Stores sentence similarities
   * in both sentences.
   *
   * @param other The sentence to be compared to.
   */
  public void compareToSentence(Sentence other) {
    MyHashtable otherWords = other.words;
    int numInCommon = 0;
    Iterator i = words.entrySet().iterator();
    Set j = otherWords.entrySet();
    while (i.hasNext()) {
      Map.Entry ie = (Map.Entry) i.next();
      if (j.contains(ie)) {
        numInCommon++;
      }
      /** Object iValue = ie.getValue();
       Iterator j = otherWords.entrySet().iterator();
       while(j.hasNext()){
         Map.Entry je = (Map.Entry) j.next();
         Object jValue = je.getValue();
         if(iValue.equals(jValue)){
           numInCommon++;
           // Can break here because no word is repeated in the tables.
           break;
         }
       }*/
    }

    /* similarity is the average of numInCommon as a percentage of both
       sentence sizes. */
    double similarity = ( ( (numInCommon / (double) words.size()) +
                           (numInCommon / (double) otherWords.size())) * 100) /
        2;

    if (similarity > SIM_THRESHOLD || numInCommon > COMMON_THRESHOLD) {
      addSimilarity(other, (byte) similarity);
    }

  } //compareTo

  /**
   * Used during detection. Equivalent to calling addSimilarity(other, score,
   * true).
   * @param other The sentence that is similar to this one.
   * @param score The similarity score
   */
  private void addSimilarity(Sentence other, byte score) {
    addSimilarity(other, score, true);
  }

  /**
   * Used during detection to record similarities between sentences.
   * @param other The sentence that is similar to this one.
   * @param score The similarity score between the two sentences.
   * @param callOther Whether to call this method for the other sentence.
   */
  private void addSimilarity(Sentence other, byte score, boolean callOther) {
    pairs.addSimilarity(other, score);
    if (callOther) {
      other.addSimilarity(this, score, false);
    }
  }

  /**
   * Gives the string showing which sentences this sentence is similar to.
   * Useful for debugging.
   * @return The similarity string.
   */
  public String similarityString() {
    String out = toString() + "\nIs similar to:\n" + pairs.toString();
    return out;
  }

  /**
   * The words contained in this sentence.
   * @return A hash table of non-common words in this sentence.
   */
  public MyHashtable getWords() {
    return words;
  }

  /**
   * The line number this sentence starts at in the original text.
   * @return The sentence's line number.
   */
  public int getLineNo() {
    return lineNo;
  }

  /**
   * The list of sentence pair objects which links this sentence to all similar
   * sentences.
   * @return A Vector containing SentencePair objects.
   */
  public SentencePair getSentencePairs() {
    return pairs;
  }

  /**
   * Replace the current sentence pair object with a new one.
   * @param pair The new SentencePair object.
   */
  public void setSentencePair(SentencePair pair) {
    pairs = pair;
  }

  /**
   * The Dictionary for all documents that have been parsed so far.
   * @return A MyHashtable containing unique, non-common words.
   */
  public static MyHashtable getDictionary() {
    return dictionary;
  }

  /**
       * Convenience method used to get an Integer object holding the given object's
   * hashcode.
   * @param obj The object for which you require the hash code.
   * @return An Integer object representing the given object's hash code.
   */
  public static Integer hash(Object obj) {
    return new Integer(obj.hashCode());
  }

  /**
   * Sets the file name of the document that this sentence originated from.
   * @param name The file name.
   */
  public void setFileName(String name) {
    fileName = name;
    pairs.name = name;
  }

  /**
   * Gets the file name that this sentence originated from.
   * @return A file name in String format.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Set the identifier for this sentence.
   * @param id The identifier.
   */
  public void setId(int id) {
    this.id = id;
    pairs.id = id;
  }

  /**
   * The identifier for this sentence
   * @return The identifier for this sentence.
   */
  public int getId() {
    return id;
  }

  /**
   * The coordinates at which this sentence ends in the original text.
   * @param line The line of the coordinates.
   * @param col The column of the coordinates.
   */
  public void setEndCoords(int line, int col) {
    sentenceCoords[END_LINE] = line - 1;
    sentenceCoords[END_COL] = col;
  }

  /**
   * The start and end coordinates of this sentence. Use the constants to get
   * the desired number.
   * @return The coordinates array for this sentence.
   */
  public int[] getCoords() {
    return sentenceCoords;
  }

  /**
   * Useful for debugging.
   * @return The string representation of this sentence.
   */
  public String toString() {
    String temp = "Starts at line " + lineNo + " ";
    Iterator i = words.entrySet().iterator();
    boolean first = true;
    while (i.hasNext()) {
      Map.Entry e = (Map.Entry) i.next();
      Object value = e.getValue();
      if (first) {
        first = false;
        temp += value.toString();
      }
      else {
        temp += ", " + value.toString();
      }
    }
    return temp;
  }

  /**
   * Two sentences are equal iff they contain equal hashtables of words.
   * @param o The object to be compared.
   * @return True if the above condition is met, false otherwise.
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (! (o instanceof Sentence)) {
      return false;
    }
    Sentence tmp = (Sentence) o;
    MyHashtable testWords = tmp.getWords();
    if (sentenceCoords.length != tmp.sentenceCoords.length) {
      return false;
    }
    for (int i = 0; i < sentenceCoords.length; i++) {
      if (sentenceCoords[i] != tmp.sentenceCoords[i]) {
        return false;
      }
    }
    return tmp.id == id && tmp.fileName.equals(fileName) &&
        testWords.equals(words);
  }

  /**
   * Whether this sentence should be excluded from the comparison on the
   * grounds that is part of the exclude file for this dataset.
   * @return true if it should be ignored, false otherwise.
   */
  public boolean isInExcludeFile() {
    return inExcludeFile;
  }

  /**
   * Change whether this sentence is in the exclude file or not. Default value
   * is false.
   * @param exclude Whether the sentence is in the exclude file.
   */
  public void setInExcludeFile(boolean exclude) {
    inExcludeFile = exclude;
  }

  /**
   * Change the array of common words for sentences.
   * @param commonArray The new common words array.
   */
  public static void setCommon(String[] commonArray) {
    common = commonArray;
    commonWords.clear();
    for (int i = 0; i < commonArray.length; i++) {
      commonWords.put(hash(commonArray[i]), commonArray[i]);
    }
  }

  /**
   * Called when all links involving this sentence should be ignored.
   */
  public void ignoreAllScores() {
    pairs.ignoreAllScores();
  }
}
