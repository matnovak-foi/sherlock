/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * It holds the line of input, and a vector of FileLineReferences
     * which in turn holds a record of the location of any occurrences of this line.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
class LineOfData
    implements Serializable {

  /**
   * The string read in.
   */
  private String data = "";

  /**
   * List of occurrences of this line.
   */
  private List occurrences = null;

  /**
   * Keep track of the index of the last returned LineOfDataOccurrence from
   * the vector.
   */
  private int lastGivenIndex = -1;

  /**
   * Empty constructor - initialises the occurrences vector.
   */
  LineOfData() {
    data = "";
    occurrences = new ArrayList();
    lastGivenIndex = -1;
  }

  /**
   * Creates new LineOfData object.
   *
   * @param data - the line of data just read in.
   */
  LineOfData(String data) {
    this.data = data;
    occurrences = new ArrayList();
    lastGivenIndex = -1;
  }

  /**
   * Add an occurrence of this line of data to the vector recording that.
   *
   * @param lODO A LineOfDataOccurrence holding the file and line number
   *  information
   */
  void addOccurrence(LineOfDataOccurrence lODO) {
    occurrences.add(lODO);
  }

  /**
   * Returns the first stored occurrence of this line of data.
   *
   * @return A LineOfDataOccurrence object, or null if it doesn't exist
   */
  LineOfDataOccurrence getFirstOccurrence() {
    lastGivenIndex = 0;
    return getLODO();
  }

  /**
   * Returns the next stored occurrence of this line of data. This will
   * return
   * the first element if it is called before getFirstOccurrence().
   *
   * @return A LineOfDataOccurrence object, or null if it doesn't exist
   */
  LineOfDataOccurrence getNextOccurrence() {
    lastGivenIndex++;
    return getLODO();
  }

  /**
   * Used by the first and next get methods. This method returns null if a
   * LineOfDataOccurrence does not exist.
   */
  private LineOfDataOccurrence getLODO() {
    try {
      return (LineOfDataOccurrence) occurrences.get(lastGivenIndex);
    }
    catch (IndexOutOfBoundsException e) {
      return null;
    }
  }

  /**
   * Returns this LineOfData's data.
   *
   * @return this LineOfData's data
   */
  String getData() {
    return data;
  }

}
