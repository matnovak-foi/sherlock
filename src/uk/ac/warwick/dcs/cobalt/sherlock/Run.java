/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * This Run object holds a run of similar code. It records the start and end
 * point of the run of lines, the number of lines that are in this run, and
 * a value (anomolies) indicating how contiguous the run of lines is.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class Run
    implements Serializable {

  /**
   * The RunCoordinates of the start of this run.
   *
   * @serial
   */
  private RunCoordinates start = null;

  /**
   * The RunCoordinates of the end of this run.
   *
   * @serial
   */
  private RunCoordinates end = null;

  /**
   * The number of similar lines that make up this run.
   *
   * @serial
   */
  private int running = 0;

  /**
       * The degree to which this Run is contiguous. The higher this value, the fewer
   * adjacent lines there are in this Run.
   *
   * @serial
   */
  private int anomolies = 0;

  /**
   * Empty constructor.
   */
  Run() {
    start = new RunCoordinates();
    end = new RunCoordinates();
  }

  /**
   * Constructor for a new Run. As it is a new run, the value of running and
   * anomolies will be 1 and 0 respectively.
   *
   * @param start the RunCoordinates of the start of the run
   * @param end the RunCoordinates of the end of the run
   */
  Run(RunCoordinates start, RunCoordinates end) {
    this.start = start;
    this.end = end;
    running = 1;
    anomolies = 0;
  }

  /**
   * Extend this run. This is called when a new match is added to the run, so
   * the end coordinates, running and anomolies values are all changed.
   *
   * @param newEnd the new end RunCoordinates
   * @param increaseRunning the amount to increase the running value by
   * @param increaseAnomolies the amount to increase the anomolies value by
   */
  void extend(RunCoordinates newEnd, int increaseRunning, int increaseAnomolies) {
    end = newEnd;
    running += increaseRunning;
    anomolies += increaseAnomolies;
  }

  /**
   * Returns the RunCoordinates of the start of this run.
   *
   * @return the RunCoordinates of the start of this run.
   */
  public RunCoordinates getStartCoordinates() {
    return start;
  }

  /**
   * Sets the RunCoordinates of the start of this run.
   *
   * @param newCoordinates the new RunCoordinates of the start of this run
   */
  public void setStartCoordinates(RunCoordinates newCoordinates) {
    start = newCoordinates;
  }

  /**
   * Returns the RunCoordinates of the end of this run.
   *
   * @return the RunCoordinates of the end of this run
   */
  public RunCoordinates getEndCoordinates() {
    return end;
  }

  /**
   * Sets the RunCoordinates of the end of this run.
   *
   * @param newCoordinates the new RunCoordinates of the end of this run
   */
  public void setEndCoordinates(RunCoordinates newCoordinates) {
    end = newCoordinates;
  }

  /**
   * Returns this Run's running value.
   *
   * @return this Run's running value
   */
  public int getRunning() {
    return running;
  }

  /**
   * Set this Run's running value to the passed value
   *
   * @param newRunning - the new running value
   */
  public void setRunning(int newRunning) {
    running = newRunning;
  }

  /**
   * Returns this Run's anomolies value.
   *
   * @return this Run's anomolies value
   */
  public int getAnomolies() {
    return anomolies;
  }

  /**
   * Returns a string that displays and identifies this object's properties.
   *
   * @return a String representation of this object
   */
  public String toString() {
    return "Run details:\n" + "\tStart run coordinates:\n\t\t" + start +
        "\n\tEnd run coordinates:\n\t\t" + end + "\n\tRunning: " +
        String.valueOf(running) + ".\n\tAnomolies: " +
        String.valueOf(anomolies) + ".";
  } // toString

}
