/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

/**
 * Any process involved in Sherlock provides a concrete implementation of this
 * abstract class. Both TokeniseFiles and Samelines are such processes.
 * The methods and variables defined here are used to send messages to either
 * the GUI or Sherlock to tell them how progress is going.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Terri Mak
 * @author Weiliang Zhang
 * @version 19 Aug 2002
 */
public abstract class SherlockProcess
    extends Thread {
  /**
   * Constant representing TokeniseFile processes.
   */
  public final static int TOKENISE = 0;

  /**
   * Constant representing Samelines processes.
   */
  public final static int SAMELINES = 1;

  /**
   * KIND number representing which kind of child process this object is.
   */
  protected Integer KIND;

  /**
   * Total number of stages there are to do.
   */
  int stagesToDo = 0;

  /**
   * Number of stages that have been completed.
   */
  int stagesDone = 0;

  /**
   * Number of matches that have been found.
   */
  protected int matchesFound = 0;

  /**
   * If true, pause processing, else continue as normal.
   */
  boolean pause = false;

  /**
       * This can me made true whilst the process is paused. If so, break out of any
   * loops and allow this thread to die.
   * Only needed because Thread.destroy() is not implemented yet.
   */
  boolean letDie = false;

  private long timeOfFirstCall;
  public SherlockProcess() {
    timeOfFirstCall = System.currentTimeMillis();
  }

  public long getTimeOfFirstCall() {
    return timeOfFirstCall;
  }

  private boolean natural = false;

  /**
   * Sets whether this is a natural language detection.
   * @param natural true if only natural language texts are being compared.
   */
  public void setNatural(boolean natural) {
    this.natural = natural;
  }

  public boolean getNatural() {
    return natural;
  }

  /**
   * Called if the user chooses to cancel processing - this thread must
   * be killed, or let die.
   */
  public void letProcessDie() {
    letDie = true;
  } // letProcessDie

  /**
   * Called if the user cancels the process before it is finished. All files
   * and directories that have been created must be deleted.
   */
  public abstract void deleteWorkDone();

  /**
   * Prompts whether to Save the current process to a file or not.
   * The file contains 3 serialised objects in the order below:
   *   1. Sherlock general settings, including settings for Sherlock, exclude
   *      file map & number of matches found,
   *   2. An Integer object indicates which kind of process it is,
   *   3. The hashtable of the names of processed files,
   *
   * @param option Indicate the types of buttoms provided. Should be either
   * JOptionPane.YES_NO_CANCEL_OPTION or JOptionPane.YES_NO_OPTION, the
   * former is used in normal saving operations, the latter is used when an
   * Exception has been caught.
   */
  public int save(int option, Component component) {
    //prepare data to save.
    SettingStore data = new SettingStore();
    //set up exclude file map.
    if (KIND.intValue() == SAMELINES) {
      data.excludeMap = getExcludeMap();
      data.matches = getMatchesFound();
    }
    Map progress = getProcessedFiles();

    //Ask user if he/she wants to save the current work and then quit.
    int status = JOptionPane.showConfirmDialog
        (component, "Do you wish to save the current operation?"
         + "\n(All processed data will be lost if you say NO.)",
         "Save current work", option);

    //if yes, than save the current work to a file.
    if (status == JOptionPane.YES_OPTION) {
      boolean choosing = true;
      while (choosing) {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Saving process...");
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int choice = jfc.showSaveDialog(component);

        if (choice == JFileChooser.APPROVE_OPTION) {
          File file = jfc.getSelectedFile();
          try {
            //if it's a directory, try again.
            if (file.isDirectory()) {
              JOptionPane.showMessageDialog
                  (component, file.getName() +
                   " is a directory, try again please.",
                   "Cannot write directories",
                   JOptionPane.ERROR_MESSAGE);
              continue;
            }

            //confirm overwrite operation if file exists.
            if (file.exists()) {
              int overwrite = JOptionPane.showConfirmDialog
                  (component, "File selected already exists, " +
                   "are you sure to overwrite this file?",
                   "Overwrite existing file?",
                   JOptionPane.YES_NO_OPTION);
              if (overwrite == JOptionPane.NO_OPTION) {
                continue;
              }
            }

            //output to file selected.
            ObjectOutputStream out = new ObjectOutputStream
                (new FileOutputStream(file));

            out.writeObject(data);
            out.writeObject(KIND);
            out.writeObject(progress);
            out.close();

            JOptionPane.showMessageDialog
                (component, "File successfully saved.",
                 "File saved", JOptionPane.INFORMATION_MESSAGE);

            choosing = false;
            letProcessDie();
          }
          catch (IOException e) {
            e.printStackTrace();
            int again = JOptionPane.showConfirmDialog
                (component, "File cannot be saved, try again?\n" +
                 "(Please ensure you have enough disk space.)"
                 + "\n(All processed data will be lost if you "
                 + "say NO)",
                 "Error occurred which saving file",
                 JOptionPane.YES_NO_CANCEL_OPTION);
            if (again == JOptionPane.NO_OPTION) {
              choosing = false;
              deleteWorkDone();
              letProcessDie();
              return JOptionPane.NO_OPTION;
            }
            else if (again == JOptionPane.CANCEL_OPTION) {
              choosing = false;
              continueProcessing();
              return again;
            }
          }
        }
        else {
          int again = JOptionPane.showConfirmDialog
              (component, "Are you sure to quit without saving?" +
               "\n(All processed data will be lost if you say YES)",
               "Quit without saving?",
               JOptionPane.YES_NO_CANCEL_OPTION);
          if (again == JOptionPane.YES_OPTION) {
            choosing = false;
            deleteWorkDone();
            letProcessDie();
            return JOptionPane.NO_OPTION;
          }
          else if (again == JOptionPane.CANCEL_OPTION) {
            choosing = false;
            continueProcessing();
            return again;
          }
        }
      }
      return JOptionPane.YES_OPTION;
    }
    else if (status == JOptionPane.NO_OPTION) {
      deleteWorkDone();
      letProcessDie();
      //processedOK = false;
      //setVisible(false);
      return JOptionPane.NO_OPTION;
    }
    else {
      // If get here, cancel was pressed so keep going.
      continueProcessing();
      return JOptionPane.CANCEL_OPTION;
    }
  }

  /**
   * Called to return the current processed file list maintained by the
   * process.
   */
  abstract Map getProcessedFiles();

  /**
   * Return the hashtable of a exclude file specified.
   */
  abstract Map getExcludeMap();

  /**
   * Causes processing to pause while the user decides whether to cancel
   * the process or not.
   */
  public void pauseProcessing() {
    pause = true;
    //yield();
  } // pauseProcessing

  public boolean getPause() {
    if (pause) {
      try {
        sleep(100);
      }
      catch (InterruptedException e) {}
    }
    return pause;
  }

  public boolean getLetDie() {
    return letDie;
  }

  /**
   * Causes processing to resume once it has been paused.
   */
  public void continueProcessing() {
    pause = false;
  } // continueProcessing

  /**
   * Tell the GUI or Sherlock the number of stages to do.
   *
   * @return the number of stages to do
   */
  public int getStagesToDo() {
    return stagesToDo;
  } // getStagesToDo

  /**
   * Tell the GUI or Sherlock how many stages have been done.
   *
   * @return the number of stages done
   */
  public int getStagesDone() {
    return stagesDone;
  } // getStagesDone

  /**
   * Tell the GUI or Sherlock how many matches have been found.
   *
   * @return the number of matches found
   */
  public int getMatchesFound() {
    return matchesFound;
  } // getMatchesFound

}
