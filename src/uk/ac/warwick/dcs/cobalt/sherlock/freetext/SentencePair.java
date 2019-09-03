/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

/**
 * <p>Simple class used to store links between sentences which are similar.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 *
 * @author Daniel White
 * @version 4
 */

public class SentencePair
    implements Serializable {
  /** The list of links to another sentence */
  Vector scores;
  /** The name of the document this SentencePair belongs in. */
  String name;
  /** The ID of the sentence this pair belongs to */
  int id;
  private boolean ignored = false;
  private boolean changed = false;
  private int numNotIgnored = 0;

  /**
   * Constructor.
   * @param parent The sentence which owns this SentencePair
   */
  public SentencePair(Sentence parent) {
    this.name = parent.getFileName();
    this.id = parent.getId();
    scores = new Vector();
  }

  /**
   * Add a link to a sentence which is similar to the sentence owning this
   * object. Equivalent to calling addSimilarity(similar,score,false).
   * @param similar The sentence which is similar.
   * @param score The similarity score.
   */
  public void addSimilarity(Sentence similar, byte score) {
    addSimilarity(similar, score, false);
  }

  /**
   * Add a link to a sentence which is similar to the sentence owning this
   * object.
   * @param similar The sentence which is similar.
   * @param score The similarity score.
   * @param ignore Whether the link should be ignored or not.
   */
  public void addSimilarity(Sentence similar, byte score, boolean ignore) {
    String addedName = similar.getFileName();
    SentenceScore sentScore =
        new SentenceScore(addedName, similar.getId(), score);
    // a false value of ignore does not need to be set since that is the
    // default value for new sentence scores.
    if (ignore) {
      sentScore.setIgnored(ignore);
    }
    else {
      numNotIgnored++;

    }
    scores.add(sentScore);
  }

  /**
   * Used by the GUI to add scores which relate only to some specific document.
   * By adding scores from some master record it means that all changes are
   * added to the master record automatically, meaning they get saved
   * automatically too, should the user choose to do this. If the user
   * does not choose to save changes then the original record is still kept
   * on disk and can be reloaded.
   * @param sentScore The score to add to this object.
   */
  void addSimilarity(SentenceScore sentScore) {
    scores.add(sentScore);
    if (sentScore.isIgnored() != ignored) {
      ignored = allIgnored();
    }
    if (!sentScore.isIgnored()) {
      numNotIgnored++;
    }
  }

  /**
   * Useful for debugging.
   * @return The string representation of this object.
   */
  public String toString() {
    StringBuffer out = new StringBuffer();
    for (int i = 0; i < scores.size(); i++) {
      SentenceScore temp = (SentenceScore) scores.get(i);
      out.append("\t" + temp.name + ", sentence: " + temp.id + ", with score: " +
                 temp.score + "\n");
    }
    return out.toString();
  }

  /**
   * Two SentencePair objects are equal iff they have equal id, name and scores
   * vectors.
   * @param o The object to compare.
   * @return True if they are equal, false otherwise.
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (! (o instanceof SentencePair)) {
      return false;
    }
    SentencePair temp = (SentencePair) o;
    return temp.id == id && temp.name.equals(name) &&
        temp.scores.equals(scores);
  }

  /**
   * Marks all scores as ignored, so that they will be displayed differently
   * and the similarity scores will not be taken into account.
   */
  public void ignoreAllScores() {
    ignored = true;
    for (int i = 0; i < scores.size(); i++) {
      setIgnored(i, true);
    }
    numNotIgnored = 0;
  }

  /**
   * Set all links back to unignored in this sentence pair.
   */
  public void unIgnoreAllScores() {
    ignored = false;
    for (int i = 0; i < scores.size(); i++) {
      setIgnored(i, false);
    }
    numNotIgnored = scores.size();
  }

  /**
   * Sets a specific score to be ignored. Also determines whether the whole
   * sentence should now be ignored as a result of the change. This method
   * should be used in preference to the version when an object is passed
   * instead of the index as it is much quicker.
   * @param index The index of the score in the scores vector.
   * @param val The new value of its ignored flag.
   */
  public void setIgnored(int index, boolean val) {
    SentenceScore temp = (SentenceScore) scores.get(index);
    if (temp.isIgnored() == val) {
      return;
    }

    temp.setIgnored(val);
    changed = true;
    if (!val) {
      ignored = false;
      numNotIgnored++;
      return;
    }
    else {
      numNotIgnored--;

    }
    assert numNotIgnored > -1 && numNotIgnored <= scores.size();

    if (ignored != val) {
      ignored = allIgnored();
    }
  }

  /**
   * Sets a specific score to be ignored. Also determines whether the whole
   * sentence should now be ignored as a result of the change.
   * @param obj A reference to the object you want to change the ignored value
   * for.
   * @param val The new value of its ignored flag.
   */
  public void setIgnored(SentenceScore obj, boolean val) {
    for (int i = 0; i < scores.size(); i++) {
      if (scores.get(i).equals(obj)) {
        setIgnored(i, val);
        break;
      }
    }

  }

  /**
       * Finds a SentenceScore which has the given name and id, changing its ignored
   * value to val.
   * @param name The name of the file the score links to.
   * @param id The id of the sentence the score links to within that file.
   * @param val The value of its new ignored flag.
   */
  public void setIgnored(String name, int id, boolean val) {
    for (int i = 0; i < scores.size(); i++) {
      SentenceScore temp = (SentenceScore) scores.get(i);
      if (temp.name.equals(name) && temp.id == id) {
        setIgnored(i, val);
      }
    }
  }

  /**
   * Whether this whole sentence should be ignored.
   * @return True if the sentence should be ignored. False otherwise.
   */
  public boolean isIgnored() {
    return ignored;
  }

  /**
   * Whether the score at <code>index</code> should be ignored.
   * @param index The index where the score can be found.
   * @return True if that score is to be ignored. False otherwise.
   */
  public boolean isIgnored(int index) {
    return ( (SentenceScore) scores.get(index)).isIgnored();
  }

  /**
   * Whether all scores within this sentence are being ignored.
   * @return True iff all scores are being ignored.
   */
  private boolean allIgnored() {
    for (int i = 0; i < scores.size(); i++) {
      if (! ( (SentenceScore) scores.get(i)).isIgnored()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Asks whether any links in this object are ignored.
   * @return True if there is one or more ignored link here. False if all
   * links are enabled.
   */
  public boolean someSentenceIgnored() {
    for (int i = 0; i < scores.size(); i++) {
      if ( ( (SentenceScore) scores.get(i)).isIgnored()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets whether any ignored values have changed since this pair was saved.
   * @param val The new changed value.
   */
  public void setChanged(boolean val) {
    changed = val;
  }

  /**
   * Whether any ignored values have changed since this pair was saved.
   * @return Whether the object's state has been changed.
   */
  public boolean isChanged() {
    return changed;
  }

  /**
   * The number of links in this object which are not ignored.
   * @return The number of links in this object which are not ignored.
   */
  public int getNumNotIgnored() {
    return numNotIgnored;
  }

}

/**
 * <p>A class representing a link between a sentence and another sentence.
 * Can be sorted on the similarity score. Can be marked as ignored if the
 * results should be displayed without regard to this score.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */
class SentenceScore
    implements Serializable, Comparable, Cloneable {
  /** The name of the document this object links to */
  String name;
  /** The ID of the sentence this object links to */
  int id;
  /** The score of the link represented by this object */
  byte score;
  // Whether the link is ignored.
  private boolean ignore = false;

  /**
   * Constructor.
   * @param name The name of the file linked to.
   * @param id The id of the sentence within that file.
   * @param score The similarity score between these two sentences.
   */
  SentenceScore(String name, int id, byte score) {
    this.name = name;
    this.id = id;
    this.score = score;
  }

  /**
       * Returns a shallow clone of this object. There are no mutable fields in this
   * Object.
   * @return A shallow clone of this object.
   */
  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException ex) {
      return null;
    }
  }

  /**
   * Used to sort links in order of their score.
   * @param o The object to compare to.
   * @return A negative score if this object is larger, zero if they are the
   * same and a positive score if this object is smaller.
   */
  public int compareTo(Object o) {
    SentenceScore temp = (SentenceScore) o;
    // if(ignore && !temp.ignore)
    //  return -1;
    if (temp.ignore && !ignore) {
      return -1;
    }
    if (temp.score < score) {
      return -1;
    }
    else if (temp.score == score) {
      return 0;
    }
    else { // if temp.score > score in other words.
      return 1;
    }

  }

  /**
   * Two SentenceScore objects are equal iff they have equal name, id, score
   * and ignore fields.
   * @param o The object to compare to this one.
   * @return true if they are equal, false otherwise
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (! (o instanceof SentenceScore)) {
      return false;
    }

    SentenceScore temp = (SentenceScore) o;
    return name.equals(temp.name) && temp.id == id && temp.score == score &&
        temp.ignore == ignore;
  }

  /**
   * Is this link ignored?
   * @return Whether the links is ignored or not
   */
  public boolean isIgnored() {
    return ignore;
  }

  /**
   * A new value for the ignored field.
   * @param val The new value for the ignored field.
   */
  protected void setIgnored(boolean val) {
    ignore = val;
  }
}