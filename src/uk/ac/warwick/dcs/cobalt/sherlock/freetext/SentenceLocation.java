/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.util.*;

/**
 * Holds the locations of Sentence objects within the MyStyledDocument that is
 * displaying them. Basically it is just a convenient way of converting between
 * the coordinate system used by the parser and that used by standard Swing
 * Text Components. In the parser, coordinates are given as a line number and
 * a column number on that line. In text components, the coordinate is simply
 * a number of characters from the start of the document.
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */
class SentenceLocation
    implements Comparable {
  /** The sentence held by this Object. */
  Sentence sentence;
  /** Coordinates within the text component displaying this sentence */
  int startLoc, endLoc;
  private MyStyledDocument parent;

  private String[] origText;
  private Document doc;

  /**
   * Given a location within the given document, constructs a SentenceLocation
   * object whose coordinates are the beginning and end of the sentence
   * covering that place in the document.
   * @param loc A position within the document.
   * @param parentDoc The document being searched.
   */
  SentenceLocation(int loc, MyStyledDocument parentDoc) {
    setParent(parentDoc);
    sentence = getSentence(loc);

    int[] coords = sentence.getCoords();
    startLoc = getLoc(coords[Sentence.START_LINE], coords[Sentence.START_COL]);
    endLoc = getLoc(coords[Sentence.END_LINE], coords[Sentence.END_COL]);
  }

  /**
   * Given a sentence within the given document, constructs a SentenceLocation
   * object whose coordinates are the beginning and end of that sentence
   * within the document.
   * @param sentence
   * @param parentDoc
   */
  SentenceLocation(Sentence sentence, MyStyledDocument parentDoc) {
    setParent(parentDoc);
    this.sentence = sentence;

    int[] coords = sentence.getCoords();
    startLoc = getLoc(coords[Sentence.START_LINE], coords[Sentence.START_COL]);
    endLoc = getLoc(coords[Sentence.END_LINE], coords[Sentence.END_COL]);
  }

  private void setParent(MyStyledDocument parent) {
    this.parent = parent;
    origText = parent.getOriginalText();
    doc = parent.getDocument();
  }

  private Sentence getSentence(int loc) {
    // need to find the line and column number, this lets us find the sentence
    // this position occurs in.
    // col represents how far loc is from the start of the line.
    int currentPos = 0, line = 0, col = 0;
    for (int i = 0; i < origText.length; i++) {
      int newPos = currentPos + origText[i].length();
      if (newPos > loc) {
        line = i;
        col = loc - currentPos;
        break;
      }
      currentPos = newPos;
    }
    Vector sentences = doc.getSentences();
    // now we have to determine the first sentence whose end co-ordinates are
    // after the line and column number.
    if (sentences.size() == 0) {
      return null;
    }

    currentPos = 0;
    Sentence current = (Sentence) sentences.get(currentPos);
    int[] coords = current.getCoords();
    currentPos++;
    while (coords[Sentence.END_LINE] < line) {
      current = (Sentence) sentences.get(currentPos);
      coords = current.getCoords();
      currentPos++;
    }
    if (coords[Sentence.END_LINE] == line && coords[Sentence.END_COL] < col) {
      current = (Sentence) sentences.get(currentPos);
      coords = current.getCoords();
      currentPos++;
      while (coords[Sentence.END_LINE] == line &&
             coords[Sentence.END_COL] <= col) {
        current = (Sentence) sentences.get(currentPos);
        coords = current.getCoords();
        currentPos++;
      }
    }

    return current;
  }

  private int getLoc(int line, int col) {
    int totalChars = 0;
    for (int i = 0; i < line; i++) {
      totalChars += origText[i].length();
    }
    return totalChars + col;
  }

  /**
   * Allows for sorting of lists of SentenceLocation objects so that they are
   * placed in the order they occur within the parent document. Must be careful
   * to only compare SentenceLocation objects with the same parent or the
   * results become meaningless.
   * @param o The object to compare to.
   * @return Standard return values for the compareTo method, as decreed in the
   * interface <code>Comparable</code>.
   */
  public int compareTo(Object o) {
    SentenceLocation temp = (SentenceLocation) o;
    if (endLoc < temp.startLoc) {
      return -1;
    }
    else if (startLoc > temp.endLoc) {
      return 1;
    }
    else {
      return 0;
    }
  }

  /**
   * Two SentenceLocation objects are equal iff they both have the same
   * coordinates and the same sentence.
   * @param o The object to compare to.
   * @return <code>true</true> if the objects are equal, <code>false</code>
   * otherwise.
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (! (o instanceof SentenceLocation)) {
      return false;
    }

    SentenceLocation temp = (SentenceLocation) o;
    if (temp.startLoc == startLoc && temp.endLoc == endLoc &&
        temp.sentence.equals(sentence)) {
      return true;
    }
    return false;
  }
}