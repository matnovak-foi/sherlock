/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Screen which displays the matches.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @version 6 Sep 2002
 */
class MatchesScreen
    extends MyFrame {

  /**
   * Structure that contains the matches deserialised from files.
   *
   * @serial
   */
  Match matches[] = null;

  /**
   * The text field that displays the % number.
   *
   * @serial
   */
  private JTextField simText = new JTextField("1", 3);

  /**
   * The slider used to choose the Similarity cut-off value.
   *
   * @serial
   */
  private JSlider simSlider = null;

  /**
   * Used to create message dialogues.
   */
  private static MyGUI parent;

  /**
   * Current marking. Must be static as it's shared by both table view &
   * graph view.
   */

  /**
   * Status bar.
   */
  protected JLabel statusBar;

  /**
   * Stores the marking.
   */
  public static Marking marking;

  /**
   * Constructor for a new Display Matches screen.
   *
   * @param gui the GUI that's running
   * @param title the title of this window
   */
  MatchesScreen(MyGUI gui, String title) {
    super(gui, title);
    parent = gui;

    // Read the matches stored in the match directory into the matches
    //array.
    matches = loadMatches();

    // If there are no matches to display, tell the user and kill this
    // window.
    if (matches == null || matches.length == 0) {
      JOptionPane.showMessageDialog(this,
                                    "There are no matches to show.",
                                    "Display Matches",
                                    JOptionPane.INFORMATION_MESSAGE);
      gui.removeWindow(this);
      closeMe();
    }

    // Update all windows' Window menus.
    gui.addWindow(this);

// 	addWindowListener(new WindowAdapter() {
// 		public void windowClosing(WindowEvent e) {
// 		    closeMe();
// 		}
// 	    });
  } // MatchesScreen

  /**
   * Deserialises all matches stored in the match directory into the
   * returned array, listed in order of similarity
   *
   * @return an array of Matches
   */
  static Match[] loadMatches() {

    // Get the match files, and create array for them as matches.
    File md = new File(Settings.sourceDirectory,
                       Settings.sherlockSettings.getMatchDirectory());
    File matchFiles[] = md.listFiles(new MatchFilenameFilter());
    Match storedMatches[] = new Match[matchFiles.length];

    // If there are no matches, return a null value
    if (storedMatches.length == 0) {
      return null;
    }

    // Deserialise the matches, loading the match into the matches array
    // and details into the data array.
    for (int x = 0; x < storedMatches.length; x++) {
      try {
        FileInputStream fis = new FileInputStream(matchFiles[x]);
        ObjectInputStream ois = new ObjectInputStream(fis);

        // Add this match to the array.
        storedMatches[x] = (Match) ois.readObject();

        ois.close();
        fis.close();
      }
      catch (IOException e) {
        e.printStackTrace();
        // If have an exception then this file does not contain a
        // valid match; set the storedMatches[x] entry to null.
        storedMatches[x] = null;

        JOptionPane.showMessageDialog
            (parent, "The following file does not contain a valid "
             + "match:\n" + matchFiles[x].getAbsolutePath()
             + "\nFile skipped."
             + "\nError logged in file sherlock.log in your HOME"
             + " directory.", "Error", JOptionPane.ERROR_MESSAGE);

        //write error log, skip this file and continue.
        String logname = new String(System.getProperty("user.home")
                                    + Settings.fileSep
                                    + "sherlock.log");
        Date day = new Date(System.currentTimeMillis());
        try {
          BufferedWriter out = new BufferedWriter
              (new FileWriter(logname, true));
          out.write(day + "-The following file does not contain a "
                    + "valid match:\n"
                    + matchFiles[x].getAbsolutePath()
                    + "\nFile skipped.");
          out.newLine();
          out.close();
        }
        catch (IOException e2) {
          JOptionPane.showMessageDialog
              (parent, "Cannot write to log file. write to STDERR",
               "Error", JOptionPane.ERROR_MESSAGE);
          System.err.println(day + "-The following file does not "
                             + "contain a valid match:\n"
                             + matchFiles[x].getAbsolutePath()
                             + "File skipped.");

        }
        continue;
      }
      catch (java.lang.ClassNotFoundException f) {
        // If have an exception then this file does not contain a
        // valid match; set the storedMatches[x] entry to null.
        storedMatches[x] = null;

        //write error log, skip this file and continue.
        String logname = new String(System.getProperty("user.home")
                                    + Settings.fileSep
                                    + "sherlock.log");
        JOptionPane.showMessageDialog
            (parent, "The following file does not contain a valid "
             + "match:\n" + matchFiles[x].getAbsolutePath()
             + "\nFile skipped."
             + "\nError logged in file sherlock.log in your HOME"
             + " directory.", "Error", JOptionPane.ERROR_MESSAGE);
        Date day = new Date(System.currentTimeMillis());
        try {
          BufferedWriter out = new BufferedWriter
              (new FileWriter(logname, true));
          out.write(day + "-The following file does not contain a "
                    + "valid match:\n"
                    + matchFiles[x].getAbsolutePath()
                    + "\nFile skipped.");
          out.newLine();
          out.close();
        }
        catch (IOException e2) {
          JOptionPane.showMessageDialog
              (parent, "Cannot write to log file, write to STDERR.",
               "Error", JOptionPane.ERROR_MESSAGE);
          System.err.println(day + "-The following file does not "
                             + "contain a valid match:\n"
                             + matchFiles[x].getAbsolutePath()
                             + "\nFile skipped.");
        }
        continue;
      }
    } // for

    // Now sort the array of matches into descending order, according to
    // similarity.
    // BUBBLE SORT! GENIUS!
    /*for (int i = 0; i < storedMatches.length; i++)
        for (int j = i+1; j < storedMatches.length; j++)
     if (storedMatches[i].getSimilarity() < storedMatches[j].
         getSimilarity()) {
         Match temp = storedMatches[i];
         storedMatches[i] = storedMatches[j];
         storedMatches[j] = temp;
     }*/
    //Sorting moved to subclasses
    //Arrays.sort(storedMatches,comp);

    // Return the matches.
    return storedMatches;
  } // loadMatches

  /**
   * Returns the value of the slider that the user uses to dictate the
   * similarity of the matches to be shown.
   *
   * @return the similarity
   */
  int getSimilarityToShow() {
    return simSlider.getValue();
  } // getSimilarityToShow

  /**
   * Create a panel that lets the user select the which matches to display.
   *
   * @param mouseListener the mouse listener to add to the slider
   * @return the panel with the necessary components on
   */
  JPanel createSliderPanel(MouseListener mouseListener) {
    JPanel jp = new JPanel();
    jp.add(new JLabel("Only show matches with more than xx% similarity:"));
    // The slider
    simSlider = new JSlider(JSlider.HORIZONTAL, 0,
                            matches[0].getSimilarity(), 1);
    simSlider.setPreferredSize(new Dimension(200, 16));
    simSlider.setMajorTickSpacing(simSlider.getMaximum() / 5);
    simSlider.setMinorTickSpacing(1);
    simSlider.setPaintTicks(true);
    simSlider.setSnapToTicks(true);
    simSlider.addMouseListener(mouseListener);
    // Have the slider update the text field whenever it changes.
    simSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        simText.setText(String.valueOf(simSlider.getValue()));
      }
    });
    // Change it to set the text!
    simSlider.setValue(simSlider.getMinimum());
    jp.add(simSlider);

    // The text field.
    simText.setEditable(false);
    jp.add(simText);
    // Return the panel.
    return jp;
  } // createSliderPanel()

  /**
   * Update table to reflect any external changes.
   */
  public void update() {}
}
