/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.ac.warwick.dcs.cobalt.sherlock.freetext.*;

/**
 * This is the GUI for Sherlock 2002.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @version 6 Sep 2002
 */
public class GUI
    extends MyGUI
    implements ActionListener {

  /**
   * These constants refer to menu items' and buttons' array indices that
   * are greyed out when Samelines hasn't been run.
   */
  private final static int DETECT = 2, MATCHES = 3, GRAPH = 4;
  private final static int VIEWLOG = 5, FREE_TEXT = 6;

  /**
   * The menu items that need to greyed out when we haven't got a
   * source directory.
   *
   * @serial
   */
  private JMenuItem menuItems[] = new JMenuItem[7];

  /**
   * The buttons that need to be greyed out when we haven't got a
   * source directory.
   *
   * @serial
   */
  private JButton buttons[] = new JButton[7];

  /**
   * The on-screen source directory label.
   *
   * @serial
   */
  private JLabel jlSourceDirectory = new JLabel
      ("No source directory selected.");

  /**
   * The on-screen label telling whether pre-processing has occurred or not.
   *
   * @serial
   */
  private JLabel jlTokeniseFiles = new JLabel("");

  /**
   * The on-screen label telling whether detection has occurred or not.
   *
   * @serial
   */
  private JLabel jlSamelines = new JLabel("");

  /**
   * Status bar.
   */
  private JLabel statusBar;

  /**
   * Stores the markings
   */
  private Marking marking;

  public static void main(String args[]) {
    System.getProperties().list(System.out);
    try {
      String homeDir = System.getProperty("user.home");
      PrintStream outStream = new PrintStream(new BufferedOutputStream(new
          FileOutputStream(new File(homeDir,
                                    "sherlock.out"))), true);
      //System.setOut(outStream);
      PrintStream errStream = new PrintStream(new BufferedOutputStream(new
          FileOutputStream(new File(homeDir,
                                    "sherlock.err"))), true);
      System.setErr(errStream);

      if (!Settings.logFile.exists()) {
        Settings.logFile.createNewFile();

      }
    }
    catch (FileNotFoundException fnfe) {
      System.out.println(fnfe.toString());
    }
    catch (SecurityException se) {
      System.out.println("Security Exception: " + se.toString());
    }
    catch (IOException ioe) {
      System.out.println("Cannot create log flie: " + ioe.toString());
    }

    GUI g;
    if (args.length == 0 || args == null) {
      g = new GUI();
      g.setSize(640, 480);
      g.setVisible(true);
    }
    else {
      boolean ssh = false;
      int i;
      for (i = 0; i < args.length; i++) {
        if (args[i].equals("-ssh")) {
          ssh = true;
          break;
        }
      }
      RepaintManager.currentManager(null).setDoubleBufferingEnabled(!ssh);

      if ( (i + 1) < args.length || !ssh) {
        File sourceDir = new File(args[ssh ? i + 1 : 0]);
        if (!sourceDir.exists()) {
          System.out.println("Invalid directory provided as an argument");
          g = new GUI();
          g.setVisible(true);
        }
        else {
          g = new GUI(sourceDir);
          g.pack();
          g.setVisible(true);
        }
      }
      else {
        g = new GUI();
        g.pack();
        g.setVisible(true);
      }
    }

  }

  /**
   * Create a new GUI.
   */
  public GUI() {
    this(null);
  } // GUI

  /**
   * Create a new GUI with the passed directory as the source directory.
   *
   * @param sourceDirectory the initial source directory
   */
  public GUI(File sourceDirectory) {
    super("Sherlock 2003");

    addWindow(this);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionPerformed(new ActionEvent
                        (new JMenuItem("Exit"), 0, ""));
      }
    });

    setUpMenus();
    setUpScreen();
    setSelectionsStatus(false);

    // Just check before changing the display if a source directory is
    //chosen.
    if (sourceDirectory != null) {
      Settings.sourceDirectory = sourceDirectory;
      Settings.init();
      setSelectionsStatus(true);
      updateScreen();
    }

    //set up current marking, empty/clean initially.
    marking = new Marking();

    pack();
  } // GUI

  /**
   * Set up the menus and add listeners as necessary.
   */
  private void setUpMenus() {
    JMenuBar jmb = getJMenuBar();

    // Get File menu.
    JMenu fileMenu = jmb.getMenu(FILE_MENU);

    JMenuItem jmi = new JMenuItem("Choose source directory...",
                                  KeyEvent.VK_C);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    jmi = new JMenuItem("Load Saved Session...", KeyEvent.VK_L);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    fileMenu.addSeparator();

    jmi = new JMenuItem("Start complete search...", KeyEvent.VK_S);
    jmi.addActionListener(this);
    fileMenu.add(jmi);
    menuItems[0] = jmi;

    jmi = new JMenuItem("Pre-process files...", KeyEvent.VK_P);
    jmi.addActionListener(this);
    fileMenu.add(jmi);
    menuItems[1] = jmi;

    jmi = new JMenuItem("Detect over pre-processed files...",
                        KeyEvent.VK_D);
    jmi.addActionListener(this);
    fileMenu.add(jmi);
    menuItems[DETECT] = jmi;

    fileMenu.addSeparator();

    jmi = new JMenuItem("Examine stored matches...", KeyEvent.VK_E);
    jmi.addActionListener(this);
    fileMenu.add(jmi);
    menuItems[MATCHES] = jmi;

    jmi = new JMenuItem("View matches graph...", KeyEvent.VK_V);
    jmi.addActionListener(this);
    fileMenu.add(jmi);
    menuItems[GRAPH] = jmi;

    jmi = new JMenuItem("View Free-text Results...", KeyEvent.VK_T);
    jmi.addActionListener(this);
    jmi.setEnabled(false);
    fileMenu.add(jmi);
    menuItems[FREE_TEXT] = jmi;

    fileMenu.addSeparator();

    jmi = new JMenuItem("Save Marking", KeyEvent.VK_A);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    jmi = new JMenuItem("Load Marking", KeyEvent.VK_R);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    fileMenu.addSeparator();

    jmi = new JMenuItem("Exit", KeyEvent.VK_X);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    //Get Edit meuu.
    JMenu editMenu = jmb.getMenu(EDIT_MENU);
    editMenu.setEnabled(false);

    // Get Options menu.
    JMenu optionsMenu = jmb.getMenu(OPTIONS_MENU);

    jmi = new JMenuItem("General settings", KeyEvent.VK_G);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);

    jmi = new JMenuItem("Detection settings", KeyEvent.VK_D);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);
    menuItems[6] = jmi;

    jmi = new JMenuItem("View Log", KeyEvent.VK_O);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);
    menuItems[VIEWLOG] = jmi;

    jmi = new JMenuItem("Show message window", KeyEvent.VK_M);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);

    jmi = new JMenuItem("Clear message window", KeyEvent.VK_E);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);

  } // setUpMenus

  /**
   * Enables or disables those menu items/buttons only available when
   * have a source directory available.
   *
   * @param status the status (enabled/disabled) to make all
   *  applicable menu items and buttons
   */
  private void setSelectionsStatus(boolean status) {
    for (int x = 0; x < menuItems.length; x++) {
      menuItems[x].setEnabled(status);

    }
    for (int x = 0; x < buttons.length; x++) {
      buttons[x].setEnabled(status);

      //enable view log file if file exists.
    }
    if (Settings.logFile.exists()) {
      menuItems[VIEWLOG].setEnabled(true);
      buttons[VIEWLOG].setEnabled(true);
    }
  } // setSelectionsStatus

  /**
   * Create the panel in the middle of the screen that shows
   *  the state of the current project.
   */
  private void setUpScreen() {

    // Create the main panel with border.
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mainPanel.setPreferredSize(new Dimension(580, 280));
    GridBagConstraints mainCon = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(5, 5, 5, 5), 0, 0);

    // The GridBagConstraints used.
    GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0.0,
        0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
        new Insets(5, 5, 5, 5), 5, 5);

    // Create the source directory panel.
    JPanel sourcePanel = new JPanel(new GridBagLayout());
    sourcePanel.setBorder(BorderFactory.createTitledBorder
                          ("Source directory"));

    constraints.gridx = 0;
    constraints.gridy = 0;
    jlSourceDirectory.setHorizontalAlignment(JLabel.CENTER);
    sourcePanel.add(jlSourceDirectory, constraints);

    constraints.gridy++;
    JButton button = new JButton("Choose source directory...");
    button.addActionListener(this);
    sourcePanel.add(button, constraints);
    mainPanel.add(sourcePanel, mainCon);
    mainCon.gridy++;

    // Create the panel for the rest of the buttons.
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Detection status"));

    button = new JButton("Start complete search...");
    button.addActionListener(this);
    buttons[0] = button;
    panel.add(button, BorderLayout.NORTH);

    JPanel detectionPanel = new JPanel(new GridBagLayout());

    constraints.gridx = 0;
    constraints.gridy = 0;
    detectionPanel.add(jlTokeniseFiles, constraints);

    constraints.gridx++;
    button = new JButton("Pre-process files...");
    button.addActionListener(this);
    buttons[1] = button;
    detectionPanel.add(button, constraints);

    constraints.gridx = 0;
    constraints.gridy++;
    detectionPanel.add(jlSamelines, constraints);

    constraints.gridx++;
    button = new JButton("Detect over pre-processed files...");
    button.addActionListener(this);
    buttons[DETECT] = button;
    detectionPanel.add(button, constraints);

    panel.add(detectionPanel, BorderLayout.CENTER);
    mainPanel.add(panel, mainCon);
    mainCon.gridy++;

    // Matches panel.
    JPanel matchesPanel = new JPanel(new GridBagLayout());
    matchesPanel.setBorder(BorderFactory.createTitledBorder
                           ("Examine matches"));

    constraints.gridx = 0;
    constraints.gridy = 0;
    button = new JButton("Examine stored matches...");
    button.addActionListener(this);
    buttons[MATCHES] = button;
    matchesPanel.add(button, constraints);

    constraints.gridx++;
    button = new JButton("View matches graph...");
    button.addActionListener(this);
    buttons[GRAPH] = button;
    matchesPanel.add(button, constraints);

    constraints.gridx = 0;
    constraints.gridy++;
    button = new JButton("View Free-text Results...");
    button.addActionListener(this);
    buttons[FREE_TEXT] = button;
    matchesPanel.add(button, constraints);

    constraints.gridx++;
    button = new JButton("View Log");
    button.addActionListener(this);
    buttons[VIEWLOG] = button;
    matchesPanel.add(button, constraints);

    mainPanel.add(matchesPanel, mainCon);

    // Show the whole thing.
    getContentPane().add(mainPanel, BorderLayout.CENTER);

    statusBar = new JLabel("Welcome to Sherlock 2003, Version 5");
    statusBar.setHorizontalAlignment(JLabel.RIGHT);
    statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

    getContentPane().add(statusBar, BorderLayout.SOUTH);
    pack();
  } // setUpScreen

  /**
   * Updates the screen when a source directory has been chosen.
   */
  private void updateScreen() {
    // By default assume nothing has been done..
    String sourceDirectoryText = "No source directory selected.";
    String tokeniseFilesNot = "NOT ";
    String samelinesNot = "NOT ";

    // If the source directory has been set, check for what's been done.
    if (Settings.sourceDirectory != null) {
      sourceDirectoryText = Settings.sourceDirectory.getAbsolutePath();

      // See if the files have been pre-processed.
      for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
        if (Settings.fileTypes[x].isInUse() &&
            (new File(Settings.sourceDirectory,
                      Settings.fileTypes[x].getDirectory())).exists()) {
          tokeniseFilesNot = "";
          break;
        }
      }

      // See if samelines has been run.
      if ( (new File(Settings.sourceDirectory,
                     Settings.sherlockSettings.getMatchDirectory()))
          .exists()) {
        samelinesNot = "";
      }
    }

    jlSourceDirectory.setText(sourceDirectoryText);
    jlTokeniseFiles.setText("Pre-processing has " + tokeniseFilesNot +
                            "occurred.");
    jlSamelines.setText("Detection has " + samelinesNot + "occurred.");

    if (tokeniseFilesNot.equals("NOT ")) {
      menuItems[DETECT].setEnabled(false);
      buttons[DETECT].setEnabled(false);
    }
    else {
      menuItems[DETECT].setEnabled(true);
      buttons[DETECT].setEnabled(true);
    }

    // If detection has been run, allow to look at matches (listing or
    //graph).
    // if (samelinesNot == "") {
    if (samelinesNot.equals("")) {
      menuItems[MATCHES].setEnabled(true);
      menuItems[GRAPH].setEnabled(true);
      menuItems[FREE_TEXT].setEnabled(true);
      buttons[MATCHES].setEnabled(true);
      buttons[GRAPH].setEnabled(true);
      buttons[FREE_TEXT].setEnabled(true);
    }
    // If it hasn't, disable any match viewing buttons or menus.
    else {
      menuItems[MATCHES].setEnabled(false);
      menuItems[GRAPH].setEnabled(false);
      menuItems[FREE_TEXT].setEnabled(false);
      buttons[MATCHES].setEnabled(false);
      buttons[GRAPH].setEnabled(false);
      buttons[FREE_TEXT].setEnabled(false);
    }

    //enable view log button & menu item, log file is created in main()
    if (Settings.logFile.exists()) {
      menuItems[VIEWLOG].setEnabled(true);
      buttons[VIEWLOG].setEnabled(true);
    }
  } // updateScreen

  /**
   * Choose the exclude file - it must reside in the source directory.
   *
   * @param parent the parent of the file chooser dialog
   * @return the exclude file
   */
  String chooseExcludeFile(Component parent) {
    // Get the list of valid files from the source directory - no
    // directories or .ini files.
    File[] files = Settings.sourceDirectory.listFiles
        (new TextFileFilter());

    // Get the current exclude file so that that is the one
    // selected (if it exists).
    String current = Settings.sherlockSettings.getExcludeFile();
    int currentIndex = 0;

    // Add the No exclude file option to the array.
    String options[] = new String[files.length + 1];
    options[0] = "No exclude file";

    for (int x = 0; x < files.length; x++) {
      options[x + 1] = files[x].getName();
      if (options[x + 1].equals(current)) {
        currentIndex = x + 1;
      }
    }

    // Get and then return the new exclude file selection
    String excludeFile = (String) JOptionPane.showInputDialog
        (parent, "Select the exclude file:", "Choose the exclude file:",
         JOptionPane.QUESTION_MESSAGE, null, options,
         options[currentIndex]);

    return excludeFile;

  } // chooseExcludeFile

  /**
   * Runs the tokenisers and comparisons as required from a saved session.
   *
   * @param preProcess if true run files through the tokenisers
   * @param compare if true run files through Samelines
   * @param tokenisedAlready if true, the user CAN'T select which file
   *  types to use
   */
  private void runSherlock(boolean preProcess, boolean compare,
                           boolean tokenisedAlready) {
    statusBar.setText("Detection Settings");
    // The progress window to use.
    ProgressWindow pw;

    // If tokenising took place and an error occurred, this flag
    // will stop Samelines running.
    boolean tokenisedOK = true;

    // Choose detection options.
    DetectionSettings ds = new DetectionSettings(this, tokenisedAlready);
    //ds.setLocationRelativeTo(gui);
    ds.setVisible(true);

    // Only run Sherlock if the user doesn't press cancel
    // on the detection settings screen.
    if (ds.okPressed()) {
      //unzip files in sub-directories if necessory and generate the
      //file list of files need to be processed.
      statusBar.setText("Examining directories...");
      Settings.fileList = processDirectory(Settings.sourceDirectory);
      statusBar.setText("Directory processing completed.");

      //make sure filenames are unique, this operation is necessary
      //only when the source directory does not contain flat files.
      int choice = JOptionPane.showConfirmDialog
          (this, "Would you like to let Sherlock rename the submissions"
           + "\nto make sure that there won't be 2 files having the same"
           + "\nfile name to avoid collisions?\n"
           + "Note: Only say NO if your source directory contains ONLY "
           + "\nflat files.", "Rename files?",
           JOptionPane.YES_NO_OPTION);
      if (choice == JOptionPane.YES_OPTION) {
        statusBar.setText("Making sure filenames are unique...");
        Settings.fileList = renameFiles(Settings.fileList);
        statusBar.setText("Filenames confirmed.");
      }

      // Determine if this is a natural language mode detection
      FileTypeProfile[] profs = Settings.getFileTypes();
      boolean natural = true;
      for (int i = 0; i < profs.length; i++) {
        if (i == Settings.SEN || i == Settings.ORI) {
          natural = natural && profs[i].isInUse();
        }
        else {
          natural = natural && !profs[i].isInUse();
        }
      }
      if (natural) {
        int reply = JOptionPane.showConfirmDialog(this, "Are you " +
                                                  "comparing prose files?",
                                                  "Natural Language",
                                                  JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.NO_OPTION) {
          natural = false;
        }
      }
      // Tokenise the files:
      if (preProcess) {
        statusBar.setText("Pre-processing files...");
        pw = new ProgressWindow(this, "Pre-processing...",
                                (preProcess && compare), natural);
        pw.setVisible(true);
        tokenisedOK = pw.finishedSuccessfully();
      }

      // Run Samelines:
      if (compare && tokenisedOK) {
        statusBar.setText("Detecting plagiarism...");
        pw = new ProgressWindow(this, "Detecting plagiarism...",
                                false, natural);
        pw.setVisible(true);
      }
      statusBar.setText("Process finished.");
    } // if (status)

    // Update the screen
    updateScreen();

  } // runSherlock

  /**
   * Runs the tokenisers and comparisons as required.
   *
   * @param preProcess if true run files through the tokenisers, otherwise
   * run through detection.
   * @param fileProcessed file processed, supplied by a saved session, files
   * in the list is ignored to avoid duplication.
   * @param excludeMap hash table of a exclude file supplied by a saved
   * @param matches number of matches found in saved session
   * session.
   */
  private void runSherlock(boolean preProcess, Map fileProcessed,
                           Map excludeMap, int matches) {

    // The progress window to use.
    ProgressWindow pw;

    //unzip files in sub-directories if necessory and generate the
    //file list of files need to be processed.
    Settings.fileList = processDirectory(Settings.sourceDirectory);

    // Tokenise the files:
    if (preProcess) {
      statusBar.setText("Pre-processing files...");
      pw = new ProgressWindow(this, "Pre-processing...",
                              false, fileProcessed, null, matches);
      pw.setVisible(true);
      //	    tokenisedOK = pw.finishedSuccessfully();
    }
    else {
      // Run Samelines:
      //System.out.println("run detection");
      statusBar.setText("Detecting plagiarism...");
      pw = new ProgressWindow(this, "Detecting plagiarism...",
                              false, fileProcessed, excludeMap, matches);
      pw.setVisible(true);
    }

    statusBar.setText("Process finished.");
    // Update the screen
    updateScreen();

  } // runSherlock

  /**
   * Show either the messages outputted by Sherlock, or the exclude file.
   *
   * @param messageWindow if true, show the message window, otherwise try
   *  and show the exclude file (if set)
   */
  private void showMessageWindowOrExcludeFile(boolean messageWindow) {

    // Show message window.
    if (messageWindow) {

      // If the message window is already open, give it the focus.
      for (int x = 0; x < openWindows.size(); x++) {
        String title = ( (MyFrame) openWindows.get(x)).getTitle();
        if (title.equals("Messages")) {
          ( (MyFrame) openWindows.get(x)).requestFocus();
          return;
        }
      }

      // If not, create it.
      TextWindow tw = new TextWindow(this, "Messages");
      tw.setVisible(true);
    }

    // Show exclude file.
    else {
      // But check if one has been chosen first!
      if (Settings.sherlockSettings.getExcludeFile().equals("")) {
        JOptionPane.showMessageDialog(this, "Exclude file not set.",
                                      "View exclude file error.",
                                      JOptionPane.OK_OPTION);
      }
      else {
        TextWindow tw = new TextWindow
            (this, "Exclude file: " +
             Settings.sherlockSettings.getExcludeFile());
        tw.setVisible(true);
      }
    }

  } // showMessageWindow

  /**
   * Prompt the user to choose the source directory.
   */
  private void chooseSourceDirectory() {
    //if current marking is dirty, prompt user to save.
    if (!marking.isClean()) {
      int ch = JOptionPane.showConfirmDialog
          (this, "Would you like to save the current marking?",
           "Save Marking?", JOptionPane.YES_NO_CANCEL_OPTION);
      if (ch == JOptionPane.YES_OPTION) {
        try {
          save();
        }
        catch (IOException ie) {
          int c = JOptionPane.showConfirmDialog
              (this, "File cannot be saved."
               + "\nLeave with out saving?",
               "File not saved",
               JOptionPane.YES_NO_OPTION);
          if (c == JOptionPane.NO_OPTION) {
            return;
          }
        }
      }
      else if (ch == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }

    // New source directory
    statusBar.setText("Choosing source directory...");
    File newSourceDirectory = null;
    boolean choosing = true;
    while (choosing) {
      JFileChooser jfc = new JFileChooser();
      jfc.setDialogTitle
          ("Choose source directory containing files to be examined.");
      jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      int status = jfc.showDialog(this, "OK");

      // If user presses cancel exit the loop, leaving
      //newSourceDirectory = null;
      if (status == JFileChooser.CANCEL_OPTION) {
        choosing = false;

        // User pressed OK, so check that we have a directory
        // before leaving the loop.
      }
      else if (status == JFileChooser.APPROVE_OPTION) {
        newSourceDirectory = jfc.getSelectedFile();

        if (!newSourceDirectory.exists()) {
          JOptionPane.showMessageDialog
              (this,
               "The directory given does not exist.",
               "Source directory selection error",
               JOptionPane.ERROR_MESSAGE);
          newSourceDirectory = null;
        }
        // If have chosen a file, go round again after the error
        //message.
        else if (newSourceDirectory.isFile()) {
          JOptionPane.showMessageDialog
              (this,
               "A directory must be selected - not a file.",
               "Source directory selection error",
               JOptionPane.ERROR_MESSAGE);
          newSourceDirectory = null;
        }
        // Otherwise have got the new source directory.
        else {
          choosing = false;
        }
      } // APPROVE_OPTION
    } // while

    // Save any currently open profiles, and clear any
    // settings that have been changed from defaults.
    if (Settings.sourceDirectory != null &&
        newSourceDirectory != null) {
      // Close any open windows and reset the GUI's Window menu.
      for (int x = 1; x < openWindows.size(); x++) {
        ( (MyFrame) openWindows.get(x)).dispose();
      }
      openWindows.clear();
      addWindow(this);

      Settings.sourceDirectory = null;
      Settings.sherlockSettings.store();
      Settings.sherlockSettings.clear();
      for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
        Settings.fileTypes[x].store();
        Settings.fileTypes[x].clear();
      }

      // Blank the menus that can't be accessed without source files.
      setSelectionsStatus(false);
    }

    if (newSourceDirectory != null) {
      Settings.sourceDirectory = newSourceDirectory;
      setSelectionsStatus(true);
      Settings.init();
      statusBar.setText("Source Directory: " +
                        newSourceDirectory.getAbsolutePath());
    }

    // Update the screen even if a source directory hasn't been chosen.
    updateScreen();
  } // chooseSourceDirectory()

  /**
   * Load a saved session from a file and continue processing.
   */
  private void loadSession() {
    //set status bar.
    statusBar.setText("Loading session...");
    boolean choosing = true;
    while (choosing) {
      JFileChooser jfc = new JFileChooser();
      jfc.setDialogTitle("Loading process...");
      jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      int choice = jfc.showOpenDialog(this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        File file = jfc.getSelectedFile();
        try {
          //if it's a directory, try again.
          if (file.isDirectory()) {
            JOptionPane.showMessageDialog
                (this, file.getName() +
                 " is a directory, try again please.",
                 "File selected is a directory.",
                 JOptionPane.ERROR_MESSAGE);
            continue;
          }

          //confirm overwrite operation if file exists.
          if (!file.exists()) {
            JOptionPane.showMessageDialog
                (this, file.getName() +
                 " does not exist, try again please.",
                 "Cannot find file.",
                 JOptionPane.ERROR_MESSAGE);
            continue;
          }

          //reading data from file in order.
          ObjectInputStream in = new ObjectInputStream
              (new FileInputStream(file));
          SettingStore data = (SettingStore) in.readObject();
          final Integer whichProcess = (Integer) in.readObject();
          Map fileProcessed = (Map) in.readObject();
          Map excludeMap = null;
          int matches = 0;
          if (whichProcess.intValue() == SherlockProcess.SAMELINES) {
            excludeMap = data.excludeMap;
            matches = data.matches;
          }
          in.close();

          data.restore();
          //setup & fire up processes
          if (whichProcess.intValue() == SherlockProcess.SAMELINES) {
            runSherlock(false, fileProcessed, excludeMap, matches);
          }
          else {
            runSherlock(true, fileProcessed, null, 0);
          }
        }
        catch (Exception e) {
          JOptionPane.showMessageDialog
              (this, "Failed to restore a saved session. " +
               "The reasons can be the following:\n" +
               "1. The file selected is corrupted;\n" +
               "2. The source files are corrupted or missing;\n" +
               "3. The preprocessed files are corrupted or missing."
               + "\nOperation aborted.",
               "Fail to resume session from file " + file.getName(),
               JOptionPane.ERROR_MESSAGE);
        }
        //stop looping.
        choosing = false;
        statusBar.setText("Loading completed.");
      }
      else {
        statusBar.setText("Loading Cancelled.");
        choosing = false;
      }
    }
  }

  /**
   * Handle menu and button events.
   */
  public void actionPerformed(ActionEvent e) {

    // Get the text of the button or menu selected.
    String choice = "";

    // Menu item selected.
    if (e.getSource()instanceof JMenuItem) {
      JMenuItem jmi = (JMenuItem) e.getSource();
      choice = jmi.getText();
    }

    // Button on screen clicked.
    else {
      JButton jb = (JButton) e.getSource();
      choice = jb.getText();
    }

    // File menu
    if (choice.equals("Choose source directory...")) {
      chooseSourceDirectory();

    }
    else if (choice.equals("Load Saved Session...")) {
      loadSession();

    }
    else if (choice.equals("Load Marking")) {
      try {
        //if current marking has been changed, prompt user whether
        //to save or not.
        if (!marking.isClean()) {
          int ch = JOptionPane.showConfirmDialog
              (this, "Would you like to save the current marking?",
               "Save Marking?", JOptionPane.YES_NO_CANCEL_OPTION);
          if (ch == JOptionPane.YES_OPTION) {
            try {
              save();
              load();
            }
            catch (IOException ie) {
              int c = JOptionPane.showConfirmDialog
                  (this, "File cannot be saved."
                   + "\nLoad new marking with out saving?",
                   "File not saved",
                   JOptionPane.YES_NO_OPTION);
              if (c == JOptionPane.YES_OPTION) {
                load();
                return;
              }
              else {
                return;
              }
            }
          }
          else if (ch == JOptionPane.CANCEL_OPTION) {
            return;
          }
          else {
            load();
          }
        }
        //marking is clean, load is safe.
        else {
          load();
        }
      }
      catch (IOException ie) {
        JOptionPane.showMessageDialog
            (this, "Cannot load file. Loading failed.",
             "Failed", JOptionPane.ERROR_MESSAGE);
      }
    }

    else if (choice.equals("Save Marking")) {
      try {
        if (marking != null) {
          save();
        }
      }
      catch (IOException ie) {
        statusBar.setText("Saving failed");
        JOptionPane.showMessageDialog
            (this, "Cannot save file. Saving failed.",
             "Failed", JOptionPane.ERROR_MESSAGE);
      }
    }

    else if (choice.equals("Start complete search...")) {
      //also delete any directory Sherlock has created to avoid
      //duplication.
      int reply = JOptionPane.showConfirmDialog(this, "Are you sure?\n" +
          "This will delete all previous processed data and results",
          "Delete old data?", JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE);
      if (reply == JOptionPane.NO_OPTION) {
        return;
      }
      for (int i = 0; i < Settings.NUMBEROFFILETYPES; i++) {
        try {
          File file = new File(Settings.sourceDirectory,
                               Settings.fileTypes[i].getDirectory());
          if (file.exists()) {
            deleteDir(file);
          }
        }
        catch (IOException ioe) {
          File file = new File
              (Settings.sourceDirectory,
               Settings.fileTypes[i].getDirectory());
          Date day = new Date(System.currentTimeMillis());
          statusBar.setText(day + "-Cannot delete directory: "
                            + file.getAbsolutePath()
                            + " File skipped.");
          try {
            BufferedWriter out = new BufferedWriter
                (new FileWriter
                 (Settings.logFile.getAbsolutePath(), true));
            out.write(day + "-Cannot delete directory: "
                      + file.getAbsolutePath()
                      + " File skipped.");
            out.newLine();
            out.close();
          }
          catch (IOException ioe2) {
            //if failed to write to log file, write to stderr
            System.err.println
                (day + "-Cannot write to log file. "
                 + "Cannot delete directory: "
                 + file.getAbsolutePath()
                 + " File skipped.");
          }
        }
      }

      //deletes match directory
      File matchDir = new File
          (Settings.sourceDirectory,
           Settings.sherlockSettings.getMatchDirectory());
      if (matchDir.exists()) {
        try {
          deleteDir(matchDir);
        }
        catch (IOException ioe) {
          Date day = new Date(System.currentTimeMillis());
          statusBar.setText(day + "-Cannot delete directory: "
                            + matchDir.getAbsolutePath()
                            + " File skipped.");
          try {
            BufferedWriter out = new BufferedWriter
                (new FileWriter
                 (Settings.logFile.getAbsolutePath(), true));
            out.write(day + "-Cannot delete directory: "
                      + matchDir.getAbsolutePath()
                      + " File skipped.");
            out.newLine();
            out.close();
          }
          catch (IOException ioe2) {
            //if failed to write to log file, write to stderr
            System.err.println
                (day + "-Cannot write to log file. "
                 + "Cannot delete directory: "
                 + matchDir.getAbsolutePath()
                 + " File skipped.");
          }
        }
      }

      runSherlock(true, true, false);
    }

    else if (choice.equals("Pre-process files...")) {
      runSherlock(true, false, false);

    }
    else if (choice.equals("Detect over pre-processed files...")) {
      runSherlock(false, true, true);

    }
    else if (choice.equals("Examine stored matches...")) {
      //DisplayMatches dm = new DisplayMatches(this);
      statusBar.setText("Loading Match Table...");
      MatchTable mt = new MatchTable(this, marking);
      statusBar.setText("Match Table Loaded");
    }

    else if (choice.equals("View matches graph...")) {
      statusBar.setText("Loading Match Graph...");
// 	    MatchesGraph mg = new MatchesGraph(this);
      MatchGraphFrame mgf = new MatchGraphFrame(this, marking);
      statusBar.setText("Match Graph Loaded");
      //JOptionPane.showMessageDialog(this, "Matches graph still in
      //development!",
      // "View matches graph...", JOptionPane.WARNING_MESSAGE);
    }

    else if (choice.equals("View Free-text Results...")) {
      statusBar.setText("Loading Free-text Results");
      GroupResults gr = new GroupResults(this);
      statusBar.setText("Loaded Results!");
    }

    else if (choice.equals("Exit")) {
      // Save settings if necessary before exiting.
      if (Settings.sourceDirectory != null) {
        Settings.sherlockSettings.store();
        for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
          Settings.fileTypes[x].store();
        }
      }

      if (!marking.isClean()) {
        int ch = JOptionPane.showConfirmDialog
            (this, "Would you like to save the current marking?",
             "Save Marking?", JOptionPane.YES_NO_CANCEL_OPTION);
        if (ch == JOptionPane.YES_OPTION) {
          try {
            save();
          }
          catch (IOException ie) {
            int c = JOptionPane.showConfirmDialog
                (this, "File cannot be saved."
                 + "\nQuit with out saving?",
                 "File not saved",
                 JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
              System.exit(0);
            }
            else {
              return;
            }
          }
        }
        else if (ch == JOptionPane.CANCEL_OPTION) {
          return;
        }
        else {
          System.exit(0);
        }
      }

      System.exit(0);
    }

    // Options menu
    else if (choice.equals("General settings")) {
      GeneralSettings gs = new GeneralSettings(this);
      gs.setLocationRelativeTo(gui);
      gs.setVisible(true);
    }

    else if (choice.equals("Detection settings")) {
      DetectionSettings ds = new DetectionSettings
          (this,
           jlTokeniseFiles.getText().equals
           ("Pre-processing has occurred."));
      ds.setLocationRelativeTo(gui);
      ds.setVisible(true);
    }

    else if (choice.equals("View Log")) {
      if (Settings.logFile.exists()) {
        LogFrame frame = new LogFrame(this, Settings.logFile);
        frame.setVisible(true);
      }
      else {
        JOptionPane.showMessageDialog
            (this, "Log file does not exist.",
             "File not found.", JOptionPane.INFORMATION_MESSAGE);
      }
    }

    else if (choice.equals("Show message window")) {
      showMessageWindowOrExcludeFile(true);

    }
    else if (choice.equals("Clear message window")) {
      TextWindow.messages.setText("");

    }
  } // actionPerformed

  /*
   * Unpack ZIP/GZIP files within each sub-directories in the given source
   * directory.
   */
  public File[] processDirectory(File dir) {
    DirectoryFilter dirfilter = new DirectoryFilter();
    ZipFilenameFilter zipfilter = new ZipFilenameFilter();
    GzipFilenameFilter gzipfilter = new GzipFilenameFilter();
    TextFileFilter textfilefilter = new TextFileFilter();

    //for each sub-directory, expand any zip/gzip files in it and its
    //sub-directories if any.
    File[] subdir;
    File[] zipfiles = dir.listFiles(zipfilter);
    File[] gzipfiles = dir.listFiles(gzipfilter);
    File[] list;
    File[] tmpList;
    File[] subdirfiles;
    LinkedList l = new LinkedList();

    //add files in current directory
    File[] files = dir.listFiles(textfilefilter);
    for (int i = 0; i < files.length; i++) {
      l.add(files[i]);

      //for each zip file in this directory if any
    }
    for (int i = 0; i < zipfiles.length; i++) {
      try {
        ZipHandler.unzip(new ZipFile(zipfiles[i]));
      }
      catch (IOException e1) {
        //write error log, skip this file and continue.
        Date day = new Date(System.currentTimeMillis());
        try {
          BufferedWriter out = new BufferedWriter
              (new FileWriter
               (Settings.logFile.getAbsolutePath(), true));
          statusBar.setText(day + "-Cannont extract file: "
                            + zipfiles[i].getAbsolutePath()
                            + " File skipped.");
          out.write(day + "-Cannont extract file: "
                    + zipfiles[i].getAbsolutePath()
                    + " File skipped.");
          out.newLine();
          out.close();
        }
        catch (IOException e2) {
          //if failed to write to log, write to stderr
          statusBar.setText
              (day + "-Cannot write to log file. "
               + "Cannont extract file: "
               + zipfiles[i].getAbsolutePath()
               + " File skipped.");
          System.err.println
              (day + "-Cannot write to log file. "
               + "Cannont extract file: "
               + zipfiles[i].getAbsolutePath()
               + " File skipped.");
        }
        continue;
      }
    }

    //for each gzip file in this directory if any
    for (int i = 0; i < gzipfiles.length; i++) {
      try {
        GzipHandler.gunzip(gzipfiles[i]);
      }
      catch (IOException e1) {
        //write error log, skip this file and continue.
        Date day = new Date(System.currentTimeMillis());
        try {
          BufferedWriter out = new BufferedWriter
              (new FileWriter(Settings.logFile.getAbsolutePath(),
                              true));
          statusBar.setText(day + "-Cannont extract file: "
                            + gzipfiles[i].getAbsolutePath()
                            + " File skipped.");
          out.write(day + "-Cannont extract file: "
                    + gzipfiles[i].getAbsolutePath()
                    + " File skipped.");
          out.newLine();
          out.close();
        }
        catch (IOException e2) {
          //if failed to write to log file, write to stderr
          statusBar.setText
              (day + "-Cannot write to log file. "
               + "Cannont extract file: "
               + gzipfiles[i].getAbsolutePath()
               + " File skipped.");
          System.err.println
              (day + "-Cannot write to log file. "
               + "Cannont extract file: "
               + gzipfiles[i].getAbsolutePath()
               + " File skipped.");
        }
        continue;
      }
    }

    //for each sub-directory in this directory if any
    subdir = dir.listFiles(dirfilter);
    int count = Settings.filterSherlockDirs(subdir);
    File[] newDirs = new File[count];
    int pos = 0;
    for (int i = 0; i < subdir.length; i++) {
      if (subdir[i] != null) {
        newDirs[pos] = subdir[i];
        pos++;
      }
    }
    subdir = newDirs;
    for (int i = 0; i < subdir.length; i++) {
      subdirfiles = processDirectory(subdir[i]);
      for (int j = 0; j < subdirfiles.length; j++) {
        l.add(subdirfiles[j]);
      }
    }

    //store result in a File array and return.
    list = new File[l.size()];
    for (int i = 0; i < l.size(); i++) {
      list[i] = (File) l.get(i);

    }
    return list;
  }

  /**
   * Rename files, concatenates filenames with their parent
   * directories' names.i.e. 9945423/a.java to 9945423/9945423a.java
   * this operation ensures that filenames are unique and it is
   * strongely coupled with BOSS, as the parent directories' names
   * are assumed to be named by the student ID numbers.
   *
   *@param l array of files to rename
   */
  private File[] renameFiles(File[] l) {
    File[] list = new File[l.length];
    for (int i = 0; i < l.length; i++) {
      File file = (File) l[i];
      File parent = file.getParentFile();
      File newfile = new File(parent + Settings.fileSep
                              + parent.getName() + file.getName());
      boolean successful = file.renameTo(newfile);

      if (successful) {
        list[i] = newfile;
      }
      else {
        list[i] = file;
      }
    }
    return list;
  }

  /**
   * Save current marking.
   *
   * @return true if file saved, false otherwise.
   */
  public void save() throws IOException {
    boolean choosing = true;
    while (choosing) {
      JFileChooser jfc = new JFileChooser();
      jfc.setDialogTitle("Save marking...");
      jfc.setFileSelectionMode
          (JFileChooser.FILES_AND_DIRECTORIES);
      int choice = jfc.showSaveDialog(this);

      if (choice == JFileChooser.APPROVE_OPTION) {
        File outfile = jfc.getSelectedFile();
        if (!outfile.exists()) {
          outfile.createNewFile();
        }
        else if (outfile.isDirectory()) {
          JOptionPane.showMessageDialog
              (this, outfile.getName() +
               " is a directory, try again please.",
               "Cannot write to directories",
               JOptionPane.ERROR_MESSAGE);
          continue;
        }
        //confirm overwrite operation if file exists.
        else {
          int overwrite =
              JOptionPane.showConfirmDialog
              (this, "File selected already exists, " +
               "are you sure to overwrite?",
               "Overwrite existing file?",
               JOptionPane.YES_NO_OPTION);
          if (overwrite == JOptionPane.NO_OPTION) {
            continue;
          }
        }

        choosing = false;
        marking.save(outfile);
        statusBar.setText("File saved");
      }
      else {
        break;
      }
    }
  }

  /**
   * Load from a marking file.
   *
   * @return true if successfully loaded, false otherwise.
   */
  public void load() throws IOException {
    boolean choosing = true;
    while (choosing) {
      JFileChooser jfc = new JFileChooser();
      jfc.setDialogTitle("Load marking...");
      jfc.setFileSelectionMode
          (JFileChooser.FILES_AND_DIRECTORIES);
      int choice = jfc.showOpenDialog(this);

      if (choice == JFileChooser.APPROVE_OPTION) {
        statusBar.setText("Loading marking file...");

        File infile = jfc.getSelectedFile();
        if (!infile.exists()) {
          JOptionPane.showMessageDialog
              (this, infile.getName() +
               " does not exist, try again please.",
               "File Not Found",
               JOptionPane.ERROR_MESSAGE);
          continue;
        }
        else if (infile.isDirectory()) {
          JOptionPane.showMessageDialog
              (this, infile.getName() +
               " is a directory, try again please.",
               "Cannot write to directories",
               JOptionPane.ERROR_MESSAGE);
          continue;
        }

        choosing = false;

        marking.clear();
        marking.load(infile);
      }
      else {
        break;
      }
    }
  }

  /**
   * Delete directory.
   */
  private static void deleteDir(File dir) throws IOException {
    File[] files = dir.listFiles();
    //delete files in this directory
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDir(files[i]);
      }
      else {
        files[i].delete();
      }
    }

    //delete this directory
    dir.delete();
  }
} // GUI
