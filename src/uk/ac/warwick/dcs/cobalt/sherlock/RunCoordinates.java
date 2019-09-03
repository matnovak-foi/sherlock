/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * RunCoordinates is used to hold the information about the start and end point
 * of runs - there are start and end objects in each Run.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class RunCoordinates
    implements Serializable {

  /**
   * Line number of the line of data in the first file.
   *
   * @serial
   */
  private int lineNoInFile1 = 0;

  /**
   * Line number of the line of data in the second file.
   *
   * @serial
   */
  private int lineNoInFile2 = 0;

  /**
   * Original line number of the line of data in the first file.
   *
   * @serial
   */
  private int origLineNoInFile1 = 0;

  /**
   * Original line number of the line of data in the second file.
   *
   * @serial
   */
  private int origLineNoInFile2 = 0;

  /**
   * Empty constructor
   */
  RunCoordinates() {
    set(0, 0, 0, 0);
  }

  /**
   * Constructor for a new RunCoordinates.
   *
   * @param lineNoInFile1 Line number of the line of data in the first file
   * @param lineNoInFile2 Line number of the line of data in the second file
   * @param origLineNoInFile1 Original line number of the line of data
   *  in the first file
   * @param origLineNoInFile2 Original line number of the line of data
   *  in the second file
   */
  RunCoordinates(int lineNoInFile1, int lineNoInFile2, int origLineNoInFile1,
                 int origLineNoInFile2) {
    set(lineNoInFile1, lineNoInFile2, origLineNoInFile1, origLineNoInFile2);
  }

  /**
   * Set the parameters for an existing RunCoordinates
   *
   * @param lineNoInFile1 Line number of the line of data in the first file
   * @param lineNoInFile2 Line number of the line of data in the second file
   * @param origLineNoInFile1 Original line number of the line of data
   * in the first file
   * @param origLineNoInFile2 Original line number of the line of data
   *  in the second file
   */
  void set(int lineNoInFile1, int lineNoInFile2, int origLineNoInFile1,
           int origLineNoInFile2) {
    this.lineNoInFile1 = lineNoInFile1;
    this.lineNoInFile2 = lineNoInFile2;
    this.origLineNoInFile1 = origLineNoInFile1;
    this.origLineNoInFile2 = origLineNoInFile2;
  }

  /**
   * Return the line number of the line of data in the first file.
   *
   * @return The line number of the line of data in the first file
   */
  public int getLineNoInFile1() {
    return lineNoInFile1;
  }

  /**
   * Return the line number of the line of data in the second file.
   *
   * @return The line number of the line of data in the second file
   */
  public int getLineNoInFile2() {
    return lineNoInFile2;
  }

  /**
   * Return the original line number of the line of data in the first file.
   *
   * @return The original line number of the line of data in the first file
   */
  public int getOrigLineNoInFile1() {
    return origLineNoInFile1;
  }

  /**
   * Return the original line number of the line of data in the second file.
   *
   * @return the original line number of the line of data in the second file
   */
  public int getOrigLineNoInFile2() {
    return origLineNoInFile2;
  }

  /**
   * Returns a string that displays and identifies this object's properties.
   *
   * @return a String representation of this object
   */
  public String toString() {
    return "Line number in file 1: " + String.valueOf(lineNoInFile1) +
        ". Line number in file 2: " + String.valueOf(lineNoInFile2) +
        ". Original line number in file 1: " +
        String.valueOf(origLineNoInFile1) +
        ". Original line number in file 2: " +
        String.valueOf(origLineNoInFile2) + ".";
  } // toString

}
