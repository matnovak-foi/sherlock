package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * Stores markings for a given data set, i.e. which pairs have been marked as
 * suspicious. It stores the indices of these suspicious matches in an linked
 * list.<p>
 * This class handles the savings & loadings of marks. The marking are saved
 * as lines of strings where each line represent a match, these lines are
 * obtained from the output() method in Match class. When loaded, these lines
 * will be stored in a linked list, after the matches is assigned for this
 * object, the generate() method will find out the indices of the matches
 * which have the same string output from the output() method. These indices
 * points to the suspicious matches.
 *
 * @author Weiliang Zhang
 * @version 23 Sep 2002
 */
public class Marking {
  /**
   * Matches for this data set.
   */
  private Match[] matches;

  /**
   * Linked list holding suspicious matches' indices.
   */
  private LinkedList indices;

  /**
   * Linked list holding entries in marking file loaded. Only used in loading
   * files.
   */
  private LinkedList tags;

  /**
   * Whether this marking has been changed since last save.
   */
  private boolean clean;

  /**
   * Construct a clean marking with every match set to unsuspicious.
   *
   * @param ms all matches for this data set, should be obtained from
   * MatchScreen class.
   */
  public Marking() {
    clean = true;
    indices = new LinkedList();
    tags = new LinkedList();
  }

  /**
   * Set the corresponding matches for the marking data. Must be called
   * after instantiation and before any other operation.
   *
   * @param ms Matches array, must be obtained from MatchesScreen.
   */
  public void setMatches(Match[] ms) {
    matches = ms;
  }

  /**
   * Mark a match as suspicious.
   *
   * @param index index of this match in the matches array in MatchScreen
   * class.
   */
  public void add(int index) {
    //make sure there are no duplicates
    boolean exists = false;
    ListIterator itr = indices.listIterator();
    while (itr.hasNext()) {
      if (index == ( (Integer) itr.next()).intValue()) {
        exists = true;
        break;
      }
    }

    if (!exists) {
      indices.add(new Integer(index));

      //marking changed.
    }
    clean = false;
  }

  /**
   * Mark a match as suspicious.
   *
   * @param index index of this match in the matches array in MatchScreen
   * class.
   */
  public void add(String tag) {
    if (matches != null) {
      for (int i = 0; i < matches.length; i++) {
        if (tag.equals(matches[i].output())) {
          add(i);
          break;
        }
      }
    }
  }

  /**
   * Mark a match as suspicious.
   *
   * @param index index of this match in the matches array in MatchScreen
   * class.
   */
  public void remove(int index) {
    for (int i = indices.size() - 1; i >= 0; i--) {
      if ( ( (Integer) indices.get(i)).intValue() == index) {
        indices.remove(i);
        break;
      }
    }

    //marking changed.
    clean = false;
  }

  /**
   * Mark a match as suspicious.
   *
   * @param index index of this match in the matches array in MatchScreen
   * class.
   */
  public void remove(String tag) {
    if (matches != null) {
      for (int i = 0; i < matches.length; i++) {
        if (tag.equals(matches[i].output())) {
          remove(i);
          //marking changed.
          clean = false;
          break;
        }
      }
    }
  }

  /**
   * Clear current marking.
   */
  public void clear() {
    indices.clear();
    tags.clear();
    matches = null;
  }

  /**
   * Whether the marking has been changed since last save.
   */
  public boolean isClean() {
    return clean;
  }

  /**
   * Called to indicate this marking is clean, i.e. does not need to be save
   * again.
   */
  public void setClean() {
    clean = true;
  }

  /**
   * Called to indicate this marking has been changed.
   */
  public void setDirty() {
    clean = false;
  }

  /**
   * List of indices of the suspicious matches.
   */
  public LinkedList getIndices() {
    return indices;
  }

  /**
   * Whether a match is marked as suspicious.
   *
   * @param index this match's index in the matches array in MatchScreen
   * class.
   */
  public boolean isSuspicious(int index) {
    for (int i = indices.size() - 1; i >= 0; i--) {
      if ( ( (Integer) indices.get(i)).intValue() == index) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether a match is marked as suspicious.
   *
   * @param tag output of a match's output() method.
   */
  public boolean isSuspicious(String tag) {
    ListIterator itr = indices.listIterator();
    while (itr.hasNext()) {
      int index = ( (Integer) itr.next()).intValue();
      if (tag.equals(matches[index].output())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Save current marking to file. The calling class should prvoide the file
   * to save in.
   *
   * @param file file to save current marking.
   * @throws IOException IOException should be caught in calling class which
   * decide what to do & generate interactive dialogs.
   */
  public void save(File file) throws IOException {
    BufferedWriter bw = new BufferedWriter
        (new FileWriter(file));
    ListIterator itr = indices.listIterator();
    while (itr.hasNext()) {
      int index = ( (Integer) itr.next()).intValue();
      System.out.println(matches[index].output());
      bw.write(matches[index].output());
      bw.newLine();
    }

    bw.close();
    //marking has been saved
    clean = true;
  }

  /**
   * Load marking from a file. The calling class should prvoide the file
   * to load up.
   *
   * @param file file to load marking from.
   * @throws IOException IOException should be caught in calling class which
   * decide what to do & generate interactive dialogs.
   */
  public void load(File file) throws IOException {
    indices.clear();
    BufferedReader br = new BufferedReader
        (new FileReader(file));
    String line = null;
    //read in all entries in saved file.
    while ( (line = br.readLine()) != null) {
      tags.add(line);
    }
    br.close();
    //successfully loaded, must be clean
    clean = true;
  }

  /**
   * Generate indices from the marking file loaded & matches array given.
   * As the indices are generated from the given matches array, they will
   * never be out of bound.
   */
  public void generate() {
    //compare these tags with all matches, if equal
    //then set this node to be suspicious.
    ListIterator itr;
    for (int i = 0; i < matches.length; i++) {
      itr = tags.listIterator();
      String ml = matches[i].output();
      while (itr.hasNext()) {
        String line = (String) itr.next();
        if (line.equals(ml)) {
          indices.add(new Integer(i));
          break;
        }
      }
    }
  }

  /**
   * Number of matches marked suspicious.
   */
  public int size() {
    return indices.size();
  }
}
