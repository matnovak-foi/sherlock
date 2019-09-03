/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.util.*;

import java.awt.*;
import javax.swing.text.*;

/**
 *
 * <p>Extension of DefaultStyledDocument used for displaying results in a user
 * friendly form. Results are displayed so that suspicous sentences are
 * highlighted while ignored sentences are paler.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */

class MyStyledDocument
    extends DefaultStyledDocument {
  // Different formatting styles.
  static final int DEFAULT_STYLE = 0, SUSPICOUS = 1, IGNORED = 2,
      HIGHLIGHTED = 3,
      HIGHLIGHT_IGNORE = 4;
  // change this if it proves inaccurate.
  private static final int TAB_LENGTH = 8;

  private Document doc;
  private String[] origText;
  private static SimpleAttributeSet[] attrs = initAttributes();
  private SortVector locations = new SortVector();

  /**
   * Constructs a MyStyledDocument using the given data.
   * @param doc The parsed document object it will represent.
   * @param origText The original text that doc was parsed from.
   */
  public MyStyledDocument(Document doc, String[] origText) {
    super();
    this.doc = doc;
    this.origText = origText;

    try {

      formatDoc();

    }
    catch (BadLocationException ble) {
      // shouldn't happen.
      ble.printStackTrace();
    }
  }

  /**
       * Insert the string between the given co-ordinates with the given formatting.
   * @param coords A co-ordinates array.
   * @param attr A set of attributes for the characters in the string.
   */
  private void insertString(int coords[], AttributeSet attr) {
    int currentCol = coords[Sentence.START_COL];
    for (int i = coords[Sentence.START_LINE]; i < coords[Sentence.END_LINE]; i++) {
      try {
        insertString(getLength(), origText[i].substring(currentCol), attr);
      }
      catch (BadLocationException ex) {
        // won't happen.
      }
      currentCol = 0;
    }
    try {
      if (coords[Sentence.END_COL] > origText[coords[Sentence.END_LINE]].length()) {
        coords[Sentence.END_COL] = origText[coords[Sentence.END_LINE]].length();
      }
      if (currentCol > coords[Sentence.END_COL]) {
        int u = 0;
      }
      insertString(getLength(),
                   origText[coords[Sentence.END_LINE]].substring(currentCol,
          coords[Sentence.END_COL]), attr);
    }
    catch (BadLocationException ex) {
      // won't happen.
    }

  }

  /**
   * Insert the original text, formatting suspicous sentences as appropriate.
   * @throws BadLocationException Shouldn't happen.
   */
  private void formatDoc() throws BadLocationException {
    // format the original text so that tabs are replaced with spaces.
    for (int i = 0; i < origText.length; i++) {
      origText[i] = undoTabbing(origText[i]);
    }
    Vector sentences = doc.getSentences();

    int coords[] = {
        0, 0, 0, 0};
    for (int i = 0; i < sentences.size(); i++) {
      Sentence sentence = (Sentence) sentences.get(i);
      // if this sentence needs different formatting
      if (sentence.getSentencePairs().scores.size() > 0 ||
          sentence.getSentencePairs().isIgnored()) {
        SentenceLocation tempLoc = new SentenceLocation(sentence, this);
        locations.add(tempLoc);
        // insert up to the sentence's start.
        coords[Sentence.END_LINE] =
            sentence.getCoords()[Sentence.START_LINE];
        coords[Sentence.END_COL] =
            sentence.getCoords()[Sentence.START_COL];
        insertString(coords, attrs[DEFAULT_STYLE]);

        System.arraycopy(sentence.getCoords(), 0, coords, 0, coords.length);

        // insert up to the end of this sentence
        if (sentence.getSentencePairs().isIgnored()) {
          insertString(coords, attrs[IGNORED]);
        }
        else {
          insertString(coords, attrs[SUSPICOUS]);
        }

        coords[Sentence.START_LINE] = coords[Sentence.END_LINE];
        coords[Sentence.START_COL] = coords[Sentence.END_COL];
      }
      // if this is the last sentence, insert the rest of the text.
      if (i == sentences.size() - 1) {
        coords[Sentence.END_LINE] = origText.length - 1;
        coords[Sentence.END_COL] = origText[origText.length - 1].length();
        insertString(coords, attrs[DEFAULT_STYLE]);
      }
    }
  }

  /**
   * Defines the formatting styles for the document.
   * @return An array of attribute sets.
   */
  static SimpleAttributeSet[] initAttributes() {
    SimpleAttributeSet[] temp = new SimpleAttributeSet[5];

    temp[DEFAULT_STYLE] = new SimpleAttributeSet();
    StyleConstants.setFontFamily(temp[DEFAULT_STYLE], "SansSerif");
    StyleConstants.setFontSize(temp[DEFAULT_STYLE], 14);

    temp[SUSPICOUS] = new SimpleAttributeSet(temp[DEFAULT_STYLE]);
    StyleConstants.setForeground(temp[SUSPICOUS], Color.white);
    StyleConstants.setBackground(temp[SUSPICOUS], Color.red);

    temp[IGNORED] = new SimpleAttributeSet(temp[DEFAULT_STYLE]);
    StyleConstants.setForeground(temp[IGNORED], Color.orange);

    temp[HIGHLIGHTED] = new SimpleAttributeSet(temp[DEFAULT_STYLE]);
    StyleConstants.setBackground(temp[HIGHLIGHTED], Color.blue);
    StyleConstants.setForeground(temp[HIGHLIGHTED], Color.white);
    //StyleConstants.setItalic(temp[HIGHLIGHTED], true);
    //StyleConstants.setBold(temp[HIGHLIGHTED], true);

    temp[HIGHLIGHT_IGNORE] = new SimpleAttributeSet(temp[IGNORED]);
    StyleConstants.setForeground(temp[HIGHLIGHT_IGNORE], Color.red);
    //StyleConstants.setItalic(temp[HIGHLIGHT_IGNORE], true);
    //StyleConstants.setBold(temp[HIGHLIGHT_IGNORE], true);

    return temp;
  }

  /**
   * Returns the formatting information at the given index in the array.
   * @param index Should be one of the formatting types (ie. DEFAULT,
   * HIGHLIGHTED etc.)
   * @return A set of formatting attributes.
   */
  static SimpleAttributeSet getAttribute(int index) {
    return attrs[index];
  }

  /**
   * Returns the array of all possible formatting styles in this document.
   * @return An array of formatting styles.
   */
  static SimpleAttributeSet[] getAttributes() {
    return attrs;
  }

  /**
   * Given an attribute set it returns the style it is equal to, defaulting to
   * the default style if one is not found.
   * @param attrSet The set to compare.
   * @return An index into the attribute set array.
   */
  int getLineStyle(AttributeSet attrSet) {
    for (int i = 0; i < attrs.length; i++) {
      if (attrSet.isEqual(attrs[i])) {
        return i;
      }
    }
    return 0;
  }

  /**
   * Given a location, representing a number of characters since the start of
   * the document, this emthod returns the sentence which contains that
   * location.
   * @param loc A location within the document.
   * @return The sentence at this location.
   */
  Sentence getSentence(int loc) {
    SentenceLocation locater = findLocater(loc);
    return locater.sentence;
  }

  /**
   * Return the document object.
   * @return A Document object.
   */
  Document getDocument() {
    return doc;
  }

  /**
   * The vector of locations objects, representing a mapping to sentences.
   * @return A vector of SentenceLocation objects.
   */
  SortVector getLocations() {
    return locations;
  }

  /**
   * An array containing the text as it appeared in the original file.
   * @return A String array, each entry representing a line from the original
   * file.
   */
  String[] getOriginalText() {
    return origText;
  }

  /**
   * Given a String, this method attempts to replace the tabs with spaces.
   * @param str The string to be examined.
   * @return The input String, with tabs replaced by spaces.
   */
  String undoTabbing(String str) {
    StringTokenizer tok = new StringTokenizer(str, "\t\n\r\f", true);
    String ret = new String();
    int pos = 0;
    while (tok.hasMoreTokens()) {
      String temp = tok.nextToken();
      if (temp.equals("\t")) {
        int length = TAB_LENGTH - (pos % TAB_LENGTH);
        StringBuffer buff = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
          buff.append(" ");
        }
        ret += buff.toString();
        pos = ret.length();
      }
      else {
        ret += temp;
        pos = ret.length();
      }
    }
    return ret;
  }

  /**
   * Given a location in the text this finds the SentenceLocation object for
   * that character.
   * @param loc A distance from the start of the object.
   * @return The SentenceLocation object that spans that location.
   */
  SentenceLocation findLocater(int loc) {
    SentenceLocation temp = null;
    for (int i = 0; i < locations.size(); i++) {
      temp = (SentenceLocation) locations.get(i);
      if (temp.startLoc <= loc && temp.endLoc >= loc) {
        break;
      }

      // reaching here on the last iteration means that the locater has not been
      // created yet. Shouldn't reach here.
      if (i == locations.size() - 1) {
        temp = null;
      }
    }
    if (temp != null) {
      return temp;
    }

    temp = new SentenceLocation(loc, this);
    locations.add(temp);
    return temp;
  }

  /**
   * Given a sentence object, finds the SentenceLocation object which maps
   * that sentence to the formatted text.
   * @param sentence A Sentence object.
   * @return A SentenceLocation object for that sentence.
   */
  SentenceLocation findLocater(Sentence sentence) {
    SentenceLocation temp = null;
    for (int i = 0; i < locations.size(); i++) {
      temp = (SentenceLocation) locations.get(i);
      if (temp.sentence.equals(sentence)) {
        return temp;
      }
    }
    // shouldn't reach this point.
    temp = new SentenceLocation(sentence, this);
    locations.add(temp);
    return temp;
  }

}
