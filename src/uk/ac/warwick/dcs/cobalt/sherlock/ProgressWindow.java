/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Shows the progress of tokenising and plagiarism detecting.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Terri Mak
 * @author Weiliang Zhang
 * @version 19 Aug 2002
 */
class ProgressWindow
    extends JDialog
    implements ActionListener, SherlockProcessCallback {

  /**
   * The process that is to be run: TokeniseFiles or Samelines.
   *
   * @serial
   */
  private SherlockProcess process;

  /**
   * The Container to hold the following components:
   * JProgressBar, JScrollBar and JButton
   *
   * @serial
   */
  private Container contentPane = getContentPane();

  /**
   * The box that holds the process bar and the process information.
   *
   * @serial
   */
  private Box box = new Box(BoxLayout.Y_AXIS);

  /**
   * The JProgressBar to show how things are going.
   *
   * @serial
   */
  private JProgressBar bar = new JProgressBar();

  /**
       * The JLabel totalFileLabel to show the total number of files to be processed.
   *
   * @serial
   */
  private JLabel totalFileLabel = new JLabel();

  /**
   * The JLabel fileDoneLabel to show the number of files/comparisons has been processed.
   *
   * @serial
   */
  private JLabel fileDoneLabel = new JLabel();

  /**
   * The JLabel matchedFileLabel to show the number of matched files so far.
   *
   * @serial
   */
  private JLabel matchedFileLabel = new JLabel();

  private JLabel timeRemaining = new JLabel();

  /**
   * Cancel/OK button.
   *
   * @serial
   */
  private JButton command = new JButton("Cancel");

  /**
   * The box that holds all the on-screen objects.
   *
   * @serial
   */
  //private Box box = new Box(BoxLayout.Y_AXIS);
  private JScrollPane jsp = new JScrollPane(TextWindow.messages);

  /**
   * Timer that controls when the screen is updated.
   *
   * @serial
   */
  private javax.swing.Timer timer = new javax.swing.Timer(1000, this);

  /**
   * The GUI that's running.
   *
   * @serial
   */
  private MyGUI gui;

  /**
   * The title of the Progress Window
   *
   * @serial
   */
  private String title;

  /**
       * If true, dialog shuts automatically if it the process finishes successfully.
   *
   * @serial
   */
  private boolean autoClose = false;

  /**
   * Set to true once the process finishes successfully - false until then.
   *
   * @serial
   */
  private boolean processedOK = false;

  /**
   * Set to true if the process has skipped one or more files which caused
   * error or exceptions.
   */
  private boolean hasSkippedError = false;

  /**
   * Creates a new modal progress window with the specified GUI owner.
   *
   * @param gui the GUI owner of this progress window
   * @param title the title of the dialog, which also determines whether this
   *  ProgressWindow is pre-processing, or detecting plagiarism.
   * @param autoClose if true, the dialog will close automatically when
   *  the process has finished. This is used for tokenising if the user has
   *  selected a complete search.
   */
  ProgressWindow(MyGUI gui, String title, boolean autoClose, boolean natural) {
    super(gui, title, true);
    this.gui = gui;
    this.title = title;
    this.autoClose = autoClose;

    // Add a window listener. If still processing, closing is the same
    // as pressing cancel.
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (command.getText().equals("Cancel")) {
          cancelPressed();
        }
        else {
          setVisible(false);
        }
      }
    });

    // Create the appropriate process.
    if (title.equals("Pre-processing...")) {
      process = new TokeniseFiles(this);
    }
    else {
      process = new Samelines(this);
    }
    process.setNatural(natural);

    // Set up and display the screen.
    setUpScreen();
    pack();

    // Run the process at hand.
    process.setPriority(Thread.NORM_PRIORITY - 1);
    process.start();
    timer.start();

  } // ProgressWindow

  /**
   * Creates a new modal progress window with the specified GUI owner.
   *
   * @param gui the GUI owner of this progress window
   * @param title the title of the dialog, which also determines whether this
   *  ProgressWindow is pre-processing, or detecting plagiarism.
   * @param autoClose if true, the dialog will close automatically when
   *  the process has finished. This is used for tokenising if the user has
   *  selected a complete search.
   * @param fileProcessed hashtable storing pairs of processed filenames.
   * @param excludeMap hashtable of the exclude file.
   * @param matches number of matches found in saved session.
   */
  ProgressWindow(MyGUI gui, String title, boolean autoClose,
                 Map fileProcessed, Map excludeMap, int matches) {
    super(gui, title, true);
    this.gui = gui;
    this.title = title;
    this.autoClose = autoClose;

    // Add a window listener. If still processing, closing is the same
    // as pressing cancel.
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        if (command.getText().equals("Cancel")) {
          cancelPressed();
        }
        else {
          setVisible(false);
        }
      }
    });

    // Create the appropriate process.
    if (title.equals("Pre-processing...")) {
      process = new TokeniseFiles(this, fileProcessed);
    }
    else {
      process = new Samelines(this, fileProcessed, excludeMap, matches);

      // Set up and display the screen.
    }
    setUpScreen();
    pack();

    // Run the process at hand. Need to set priority of the thread to normal
    // as it is created by the event-dispatching thread, which is high
    // priority. This ensures that the computer will have reasonable
    // performance if the user wants to do something else while Sherlock
    // is running -DRW 08/04/03
    process.setPriority(Thread.NORM_PRIORITY);
    process.start();
    timer.start();

  } // ProgressWindow

  /**
   * Set up the objects of this progress window.
   */
  private void setUpScreen() {

    //Add the progress bar.
    JPanel barJP = new JPanel();
    bar.setPreferredSize(new Dimension(240, 25));
    bar.setMaximum(process.getStagesToDo());
    bar.setStringPainted(true);
    barJP.add(bar);

    //Add three processing information labels

    if (title.equals("Pre-processing...")) {
      totalFileLabel.setText("Total Number of files: "
                             + process.getStagesToDo());
      fileDoneLabel.setText("No. of processed files: "
                            + process.getStagesDone());
    }
    else {
      totalFileLabel.setText("Total Number of comparisons: "
                             + process.getStagesToDo());
      fileDoneLabel.setText("No. of processed comparisons: "
                            + process.getStagesDone());
      matchedFileLabel.setText("No. of matched comparisons so far: "
                               + process.getMatchesFound());
      timeRemaining.setText("Estimated Time Remaining: "
                            + time);
    }
    //Add the progress bar, and the three progressing information labels
    //in the box
    box.add(barJP);
    box.add(totalFileLabel);
    box.add(fileDoneLabel);
    box.add(matchedFileLabel);
    box.add(timeRemaining);

    // Add the message text area.
    jsp.setPreferredSize(new Dimension(260, 160));

    // Add the OK/Cancel button.
    JPanel buttonJP = new JPanel();
    command.addActionListener(this);
    buttonJP.add(command);

    // Add everything to the dialog.
    contentPane.add(box, BorderLayout.NORTH);
    contentPane.add(jsp, BorderLayout.CENTER);
    contentPane.add(buttonJP, BorderLayout.SOUTH);
  } // setUpScreen

  private String retString = "";
  private String calcEstimatedTime() {
    long timeOfFirstCall = process.getTimeOfFirstCall();
    long timeOfThisCall = System.currentTimeMillis();
    int stagesLeft = process.getStagesToDo()
        - process.getStagesDone();
    int secsSinceCall = (int) ( (timeOfThisCall - timeOfFirstCall) / 1000);

    int secsLeft = (int) ( (secsSinceCall / (double) process.getStagesDone())
                          * stagesLeft);
    int minutesLeft = secsLeft / 60;
    secsLeft %= 60;
    String hoursLeft = "";
    if (minutesLeft > 60) {
      hoursLeft = minutesLeft / 60 + "";
      minutesLeft %= 60;
    }

    retString = minutesLeft + " minutes " + secsLeft + " seconds";
    if (hoursLeft.length() > 0) {
      retString = hoursLeft + " hours " + retString;

    }
    return retString;
  }

  private String time = "";
  private int count = 0;
  /**
   * Invoked when an action occurs.
   *
   * @param e the actionevent
   */
  public void actionPerformed(ActionEvent e) {

    // Timer event.
    if (e.getSource()instanceof javax.swing.Timer) {

      // If a process is still running, update its progress bar and
      // the progressing information labels.
      if (process.isAlive()) {
        bar.setValue(process.getStagesDone());
        if (title.equals("Pre-processing...")) {
          fileDoneLabel.setText("No. of processed files: "
                                + process.getStagesDone());
        }
        else {
          fileDoneLabel.setText("No. of processed comparisons: "
                                + process.getStagesDone());
          matchedFileLabel.setText
              ("No. of matched comparisons so "
               + "far: " + process.getMatchesFound());
          count++;
          if ( (count %= 5) == 0) {
            time = calcEstimatedTime();

          }
          timeRemaining.setText("Estimated Time Remaining: "
                                + time);
        }
      }
      // If it has finished, change the text on the button and
      // ensure that the bar is at 100%.
      // also the progressing information labels
      else {
        bar.setValue(bar.getMaximum());
        if (title.equals("Pre-processing...")) {
          fileDoneLabel.setText("No. of processed files: "
                                + process.getStagesDone());
        }
        else {
          fileDoneLabel.setText("No. of processed comparisons: "
                                + process.getStagesDone());
          matchedFileLabel.setText("No. of matched comparisons: "
                                   + process.getMatchesFound());

          timeRemaining.setText("Estimated Time Remaining: "
                                + "");
        }
        command.setText("OK");

        if (process.getStagesToDo() == process.getStagesDone()) {
          processedOK = true;
        }
        else {
          processedOK = false;

          // Close this window automatically if that is required.
        }
        if (autoClose) {
          setVisible(false);
        }
      }
    }

    // Button pressed.
    else {

      String text = ( (JButton) e.getSource()).getText();

      // Check that we really want to cancel.
      //// if (text == "Cancel")
      if (text.equals("Cancel")) {
        cancelPressed();

        // OK pressed, so must have finished everything.
        //// else if (text == "OK")
      }
      else if (text.equals("OK")) {
        setVisible(false);

      }
    } // if Timer
  } // actionPerformed

  /**
   * Shows the "are you sure?" box when the cancel button or close
   * window button is pressed.
   */
  private void cancelPressed() {

    // Stop the timer and process.
    process.pauseProcessing();
    timer.stop();

    // Ask user if they really want to cancel.
    //   int status = JOptionPane.showConfirmDialog(this, "Cancel "
    //+ getTitle() + "?", "Cancel", JOptionPane.OK_CANCEL_OPTION);

    int state = process.save(JOptionPane.YES_NO_CANCEL_OPTION, this);
    if (state == JOptionPane.CANCEL_OPTION) {
      timer.start();
    }
    else {
      setVisible(false);
      processedOK = false;
    }
  } // cancelPressed

  /**
   * Signals whether the SherlockProcess that has run completed successfully.
   *
   * @return true if the process finished successfully
   */
  boolean finishedSuccessfully() {
    return processedOK;
  } // finishedSuccessfully

  /**
   * Handle an exception that has occurred during a SherlockProcess's
   * processing.
   *
   * @param e the SherlockProcessException thrown
   */
  public void exceptionThrown(SherlockProcessException spe) {
// 	process.deleteWorkDone();
// 	process.letProcessDie();
    hasSkippedError = true;
    spe.getOriginalException().printStackTrace();
    String msg = spe.getMessage() + "\n"
        + spe.getOriginalException().toString();
    Date day = new Date(System.currentTimeMillis());

// 	JOptionPane.showMessageDialog
// 	    (this, msg + "\n(The error message will be kept in the file "
// 	     + "sherlock.log in your home directory.)",
// 	     "Error " + getTitle(), JOptionPane.ERROR_MESSAGE);
    try {
      String file = new String
          (System.getProperty("user.home") + Settings.fileSep
           + "sherlock.log");
      BufferedWriter out = new BufferedWriter
          (new FileWriter(file, true));
      out.write(day + "-" + msg);
      out.newLine();
      out.close();
    }
    catch (IOException e) {
      System.err.println("Cannot save log file. " + msg);
// 	    JOptionPane.showMessageDialog
// 		(this, "Cannot save log file! Press ok to abort.",
// 		 "Log file not saved", JOptionPane.ERROR_MESSAGE);
    }
    //prompt whether to save or not.
    //process.save(JOptionPane.YES_NO_OPTION, this);
    //setVisible(false);
  } // exceptionThrown

}
