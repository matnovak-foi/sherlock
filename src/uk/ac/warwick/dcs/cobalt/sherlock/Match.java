/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * Save any runs that have been deemed a copy - a match!
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class Match
    implements Serializable, Comparable {

  /**
   * The first file that this match is comprised of.
   *
   * @serial
   */
  private String file1 = "";

  /**
   * The second file that this match is comprised of.
   *
   * @serial
   */
  private String file2 = "";

  /**
   * The Run of this match - the line number details,
   * running and anomolies values.
   *
   * @serial
   */
  private Run run = null;

  /**
   * The type of tokenised files that this was matched over.
   *
   * @serial
   */
  private int fileType = 0;

  /**
   * The degree of similarity between the files concerned.
   *
   * @serial
   */
  private int similarity = 0;

///////////////////
// for testing only
///////////////////
  Match(String s1, String s2, int i1, int i2) {
    file1 = s1;
    file2 = s2;
    fileType = i1;
    similarity = i2;
  }

///////////////////
// for testing only
///////////////////

  /**
   * Match constructor.
   *
   * @param matchedFile1 the first file that this match is comprised of
   * @param matchedFile2 the second file that this match is comprised of
   * @param matchedRun the run that is a match (similar enough to be a copy)
   * @param matchedFileType the type of files that have been compared
   * @param sim the degree of similarity shown between the files concerned
   */
  Match(String matchedFile1, String matchedFile2, Run matchedRun,
        int matchedFileType, int sim) {
    file1 = matchedFile1;
    file2 = matchedFile2;
    run = matchedRun;
    fileType = matchedFileType;
    similarity = sim;
  } // Match(...

  /**
   * Returns the name and path of the first file of this match.
   *
   * @return the name and path of the first file of this match
   */
  public String getFile1() {
    return file1;
  } // getFile1

  /**
   * Returns the name and path of the second of this match.
   *
   * @return the name and path of the second file of this match
   */
  public String getFile2() {
    return file2;
  } // getFile1

  /**
   * Returns the run holding the details of this match.
   *
   * @return the run holding the details of this match
   */
  public Run getRun() {
    return run;
  } // getRun

  /**
   * Used to sort the matches into ascending order of similarity
   * @param o The object to compare
   * @return negative if this is less than the parameter, 0 if equal, and
   * positive if this is greater than the parameter.
   */
  public int compareTo(Object o) {
    if (! (o instanceof Match)) {
      throw new ClassCastException("Parameter object not of type match!");
    }
    Match other = (Match) o;
    if (getSimilarity() < other.getSimilarity()) {
      return -1;
    }
    if (getSimilarity() == other.getSimilarity()) {
      return 0;
    }
    return 1;
  }

  /**
   * Returns the fileType of this match - original, nowhite etc.
   *
   * @return the type of match
   */
  public int getFileType() {
    return fileType;
  } // getFileType

  /**
   * Returns the similarity of this match.
   *
   * @return the similarity of this match
   */
  public int getSimilarity() {
    return similarity;
  } // getSimilarity

  /**
   * Returns a string that displays and identifies this object's properties.
   *
   * @return a String representation of this object
   */
  public String toString() {
    return "File1: " + file1 + ".\nFile2: " + file2 + ".\n" + run +
        "\nFile type: " + String.valueOf(fileType) + ".\nSimilarity: " +
        String.valueOf(similarity) + ".";
  } // toString

  /**
   * Generate a string tag for this match. Used in saving & loading markings.
   * This string should be unique. It consists of name of the 2 files, line
   * number ranges, file type & percentage of similarity.
   */
  public String output() {
    RunCoordinates start = run.getStartCoordinates();
    RunCoordinates end = run.getEndCoordinates();
    int f1start = start.getLineNoInFile1();
    int f1end = end.getLineNoInFile1();
    int f2start = start.getLineNoInFile2();
    int f2end = end.getLineNoInFile2();
    return file1 + " " + file2 + " " + f1start + "-" + f1end + " " +
        f2start + "-" + f2end + " " + Settings.fileTypes[fileType]
        .getDescription() + similarity;
  }
}
