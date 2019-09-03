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
 * Display selected match pair for examination.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @version  25 Sep 2000
 */
class ComparePane
    extends MyFrame
    implements ActionListener {

  /**
   * The match that is being displayed.
   *
   * @serial
   */
  private Match match = null;

  /**
   * Matches for this pair of files.
   */
  private Match[] matches;

  /**
   * The menu option whether to show all of the submitted files or not.
   *
   * @serial
   */
  private JCheckBoxMenuItem showWholeSubFiles = null;

  /**
   * The menu option whether to show all of the tokenised files or not.
   *
   * @serial
   */
  private JCheckBoxMenuItem showWholeTokFiles = null;

  /**
   * The four indices to the different panes:
   *    LEFTORIG : left pane with original content.
   *    RIGHTORIG : right pane with original content
   *    LEFT : left pane with tokenised content.
   *    RIGHT : right pane with tokenised content.
   */
  private final static int LEFTORIG = 0, RIGHTORIG = 1, LEFT = 2, RIGHT = 3;

  /**
   * JTextArea that holds the tokenised code of the left file.
   *
   * @serial
   */
  private JTextArea panes[] = new JTextArea[4];

  /**
   * Labels that hold details about what is displayed in each pane.
   *
   * @serial
   */
  private JLabel fileDetails[] = new JLabel[4];

  /**
   * Labels that hold the results of searches.
   *
   * @serial
   */
  private JLabel findFeedback[] = new JLabel[4];

  /**
   * The FindDialog used by this ComparePane.
   *
   * @serial
   */
  private FindDialog finddialog = null;

  /**
   * The currently selected text - or "" if there is none. This is
   * passed to the find dialog.
   *
   * @serial
   */
  private String selectedText = "";

  /**
   * Used to make sure files are not overwritten if make lots of saves of
   * this
   * match. A similar technique is used in Samelines (serialID is the
   * variable there too). However, the values of serialID from there and
   * that used here are unrelated.
   *
   * @serial
   */
  private int serialID = 0;

  /**
   * Used to view original files and matches.
   */
  private JScrollPane spanes[] = new JScrollPane[4];

  /**
   * Navigation Pane
   */
  private JTabbedPane navigator;

  /**
   * Parent component which fired up this frame.
   */
  MatchesScreen caller;

  /**
   * Used to link the scroll bars in the four JScrollPanes.
   */
  private class MovementDetector
      extends MouseInputAdapter {
    private int previousBarValue = 0;
    private int delta = 0;
    private boolean linked = false;

    public void mouseDragged(MouseEvent e) {
      int newBarValue = ( (JScrollBar) e.getSource()).getValue();
      delta = newBarValue - previousBarValue;
      previousBarValue = newBarValue;

      Object source = e.getSource();
      if (linked) {
        // scroll 2 linked bars.
        if (source == spanes[0].getVerticalScrollBar()) {
          int ori = spanes[1].getVerticalScrollBar().getValue();
          spanes[1].getVerticalScrollBar().setValue(ori + delta);
        }
        else if (source == spanes[1].getVerticalScrollBar()) {
          int ori = spanes[0].getVerticalScrollBar().getValue();
          spanes[0].getVerticalScrollBar().setValue(ori + delta);
        }
        else if (source == spanes[2].getVerticalScrollBar()) {
          int ori = spanes[3].getVerticalScrollBar().getValue();
          spanes[3].getVerticalScrollBar().setValue(ori + delta);
        }
        else if (source == spanes[3].getVerticalScrollBar()) {
          int ori = spanes[2].getVerticalScrollBar().getValue();
          spanes[2].getVerticalScrollBar().setValue(ori + delta);
        }
      }
      else if (e.isControlDown()) {
        //scroll all 4 bars.
        for (int i = 0; i < 4; i++) {
          if (source != spanes[i].getVerticalScrollBar()) {
            int ori = spanes[i].getVerticalScrollBar().getValue();
            spanes[i].getVerticalScrollBar().setValue(ori + delta);
          }
        }
      }
    }

    public void mousePressed(MouseEvent e) {
      previousBarValue = ( (JScrollBar) e.getSource()).getValue();
      // The following line is for J2SDK 1.4
      if (e.getButton() == MouseEvent.BUTTON2) {

// 	    if (SwingUtilities.isMiddleMouseButton(e))
        linked = true;
      }
    }

    public void mouseReleased(MouseEvent e) {
      //The following line works in J2SDK 1.4
      if (e.getButton() == MouseEvent.BUTTON2) {

// 	    if (SwingUtilities.isMiddleMouseButton(e))
        linked = false;
      }
    }
  }

  /**
   * Constructor for a ComparePane.
   *
   * @param gui the parent component.
   * @param matchToDisplaythe match to be displayed in this ComparePane
   */
  ComparePane(MyGUI gui, Match[] ms, Match matchToDisplay,
              MatchesScreen caller) {
    // Ensure keep track of the window numbers.
    super(gui,
          String.valueOf(matchToDisplay.getSimilarity()) + "% similarity, "
          + Settings.fileTypes[matchToDisplay.getFileType()]
          .getDescription() +
          " version, " + matchToDisplay.getFile1() + ", "
          + matchToDisplay.getFile2()
          );

    // Update all windows' Window menus.
    gui.addWindow(this);

    // Set the match of this comparepane.
    matches = ms;
    match = matchToDisplay;
    String file1 = truncate(match.getFile1());
    String file2 = truncate(match.getFile2());

    //construct navigator panel
    //filter matches, put each type of match in its respective linked list.
    LinkedList[] mt = new LinkedList[Settings.NUMBEROFFILETYPES];
    for (int i = 0; i < mt.length; i++) {
      mt[i] = new LinkedList();
    }
    for (int i = 0; i < matches.length; i++) {
      int k = matches[i].getFileType();
      mt[k].add(matches[i]);
    }

    navigator = new JTabbedPane();
    for (int i = Settings.ORI; i < Settings.NUMBEROFFILETYPES; i++) {
      if (Settings.fileTypes[i].isInUse()) {
        OverviewPanel pair = new OverviewPanel
            (mt[i], file1, file2, this);
        navigator.addTab(Settings.fileTypes[i].getDescription(), pair);
      }
    }

    this.caller = caller;

    // Set up the menus and the screen.
    setUpMenus();
    setUpScreen();

    // Load the matched excerpts of files to be displayed.
    loadMatchedCode();

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeMe();
      }
    });

    setSize(950, 700);
    setVisible(true);
  } // ComparePane

  /**
   * Extract original file name from preprocessed filenames. It also removes
   * the directory information.
   */
  private String truncate(String arg) {
    File file = new File(arg);
    String str = file.getName();
    int index = str.lastIndexOf(".");
    str = str.substring(0, index);
    return str;
  }

  /**
   * Load a given match in the current ComparePane.
   */
  public void load(Match m) {
    match = m;
    setTitle(String.valueOf(m.getSimilarity()) + "% similarity, "
             + Settings.fileTypes[m.getFileType()].getDescription() +
             " version, " + m.getFile1() + ", " + m.getFile2());

    loadMatchedCode();
  }

  /**
   * Load code that is similar into this ComparePane.
   */
  private void loadMatchedCode() {
    // Left original file.
    String filename = match.getFile1();
    int slashindex = filename.lastIndexOf(Settings.fileSep);
    int dotindex = filename.lastIndexOf('.');
    filename = filename.substring(slashindex + 1, dotindex);
    File inputFile = new File(Settings.sourceDirectory + Settings.fileSep
                              + Settings.fileTypes[Settings.ORI]
                              .getDirectory(),
                              filename + "." +
                              Settings.fileTypes[Settings.ORI]
                              .getExtension());
    int start = match.getRun().getStartCoordinates()
        .getOrigLineNoInFile1();
    int end = match.getRun().getEndCoordinates().getOrigLineNoInFile1();
    Settings.message("leftorig, " + inputFile.getName() + ", " +
                     String.valueOf(start) + ", " + String.valueOf(end));
    loadPane(LEFTORIG, inputFile, start, end);

    // Right original file.
    filename = match.getFile2();
    slashindex = filename.lastIndexOf(Settings.fileSep);
    dotindex = filename.lastIndexOf('.');
    filename = filename.substring(slashindex + 1, dotindex);
    inputFile = new File(Settings.sourceDirectory + Settings.fileSep
                         + Settings.fileTypes[Settings.ORI]
                         .getDirectory(),
                         filename + "." +
                         Settings.fileTypes[Settings.ORI].getExtension());
    start = match.getRun().getStartCoordinates().getOrigLineNoInFile2();
    end = match.getRun().getEndCoordinates().getOrigLineNoInFile2();
    Settings.message("rightorig, " + inputFile.getAbsolutePath() + ", " +
                     String.valueOf(start) + ", " + String.valueOf(end));
    loadPane(RIGHTORIG, inputFile, start, end);

    // Left tokenised file.
    int fileType = match.getFileType();
    //   inputFile = new File(Settings.sourceDirectory + Settings.fileSep +
    //     Settings.fileTypes[fileType].getDirectory(),
    //     match.getFile1() + "." + Settings.fileTypes[fileType]
    //  .getExtension());
    inputFile = new File(Settings.getSourceDirectory(), match.getFile1());
    start = match.getRun().getStartCoordinates().getLineNoInFile1();
    end = match.getRun().getEndCoordinates().getLineNoInFile1();
    Settings.message("left, " + inputFile.getName() + ", " +
                     String.valueOf(start) + ", " + String.valueOf(end));
    System.out.println("left, " + inputFile.getAbsolutePath() + ", " +
                     String.valueOf(start) + ", " + String.valueOf(end));
    loadPane(LEFT, inputFile, start, end);

    // Right tokenised file.
    //   inputFile= new File(Settings.sourceDirectory + Settings.fileSep +
    //     Settings.fileTypes[fileType].getDirectory(),
    //     match.getFile2() + "." + Settings.fileTypes[fileType]
    //.getExtension());
    inputFile = new File(Settings.getSourceDirectory(), match.getFile2());
    start = match.getRun().getStartCoordinates().getLineNoInFile2();
    end = match.getRun().getEndCoordinates().getLineNoInFile2();
    Settings.message("right, " + inputFile.getName() + ", " +
                     String.valueOf(start) + ", " + String.valueOf(end));
    loadPane(RIGHT, inputFile, start, end);
  } // loadFiles

  /**
   * Load the specified pane with the code between the given points in
   * the given file.
   *
   * @param panethe pane to load code into
   * @param inputFilethe file to load code from
   * @param startLinethe line to start displaying from
   * @param endLinethe line to stop displaying after
   */
  private void loadPane(int pane, File inputFile, int startLine,
                        int endLine) {
    // The pane we are outputting to.
    JTextArea jta = panes[pane];
    jta.setText("");

    // Establish whether to display all the file or not, and get
    // how many lines to show either side of the
    // suspicious code.
    boolean showWholeFile = true;
    int lines = 0;
    // If orig file:
    if (pane < 2) {
      showWholeFile = Settings.sherlockSettings.getShowWholeSub();
      lines = Settings.sherlockSettings.getSubLinesToShow();
    }
    // If tok file:
    else {
      showWholeFile = Settings.sherlockSettings.getShowWholeTok();
      lines = Settings.sherlockSettings.getTokLinesToShow();
    }

    // Keep track of the lines in the file - so we know when to display
    // or not.
    int lineNo = 1;

    // Used to read from the file.
    String inputString = "";

    try {
      BufferedReader readFromFile = new BufferedReader
          (new FileReader(inputFile));

      inputString = readFromFile.readLine();

      while (inputString != null) {

        // Read in all of the file. Only output the lines that aren't
        // #line xxx
        // However, make sure that update the line number count.
        if (inputString.startsWith("#line ")) {
          //lineNo = Integer.parseInt(inputString.substring(6));
          inputString = readFromFile.readLine();
          continue;
        }

        // If at start of matched suspicious section:
        if (lineNo == startLine) {
          jta.append("*****BEGIN SUSPICIOUS SECTION*****\n");
          jta.append(inputString + "\n");
        }

        // If at end of matched suspicious section:
        else if (lineNo == endLine) {
          jta.append(inputString + "\n");
          jta.append("*****END SUSPICIOUS SECTION*****\n");
        }

        // If displaying whole file, within three lines of the
        // limits, or between matched section:
        else if (showWholeFile || (lineNo > startLine
                                   && lineNo < endLine) ||
                 (lineNo > (startLine - lines) && lineNo < startLine) ||
                 (lineNo > endLine && lineNo < (endLine + lines))) {
          jta.append(inputString + "\n");
        }

        // Read the next line.
        inputString = readFromFile.readLine();
        lineNo++;
      } // while reading in the file.
    }
    catch (FileNotFoundException a) {
      // no worries as if the file doesn't exist, we won't get this far.
    }
    catch (IOException f) {
      JOptionPane.showMessageDialog
          (this, "Failed to read from file.", "Error",
           JOptionPane.ERROR_MESSAGE);
      return;
    }
    // Put cursor at top of pane.
    jta.setCaretPosition(0);
  } // loadPane

  /**
   * Set up the screen and its layout.
   */
  private void setUpScreen() {
    // Objects used to create the screen.
    Box boxes[] = new Box[4];

    // Set the labels' text.
    int index = 0;
    File f = new File(Settings.getSourceDirectory(), match.getFile1());
    String left = f.getName();
    index = left.lastIndexOf('.');
    String oriLeft = left.substring(0, index);
    f = new File(Settings.getSourceDirectory(), match.getFile2());
    String right = f.getName();
    index = right.lastIndexOf('.');
    String oriRight = right.substring(0, index);
    fileDetails[LEFTORIG] = new JLabel("Submitted file, " + oriLeft);
    fileDetails[RIGHTORIG] = new JLabel("Submitted file, " + oriRight);
    fileDetails[LEFT] = new JLabel(Settings.fileTypes[match.getFileType()]
                                   .getDescription() + ", " + left);
    fileDetails[RIGHT] = new JLabel(Settings.fileTypes[match.getFileType()]
                                    .getDescription() + ", " + right);

    //create mouse listener to link scroll bars.
    MovementDetector linker = new MovementDetector();

    // Create the 4 quarters of the screen.
    for (int x = 0; x < 4; x++) {
      panes[x] = new JTextArea();
      // When the user double clicks a pane, save the selected text to
      // use in the find dialog.
      // Clear selectedText when they just click normally though.
      panes[x].addCaretListener(new CaretListener() {
        public void caretUpdate(CaretEvent e) {
          if (e.getDot() == e.getMark()) {
            selectedText = "";
          }
          else {
            JTextArea jta = (JTextArea) e.getSource();
            int start = Math.min(e.getDot(), e.getMark());
            int end = Math.max(e.getDot(), e.getMark());
            selectedText = jta.getText().substring(start, end);
          }
        }
      });
      panes[x].setEditable(false);

      spanes[x] = new JScrollPane(panes[x]);
      spanes[x].getVerticalScrollBar().setToolTipText
          ("Hold down both mouse keys to link horizonal scroll bars. "
           + "Hold CTRL to scroll all 4 bars");
      //add mouse listeners, first one handles drag motion.
      spanes[x].getVerticalScrollBar().addMouseMotionListener(linker);
      //second one handles mouse buttons & key presses.
      spanes[x].getVerticalScrollBar().addMouseListener(linker);

      findFeedback[x] = new JLabel("");
      boxes[x] = new Box(BoxLayout.Y_AXIS);
      boxes[x].add(fileDetails[x]);
      boxes[x].add(spanes[x]);
      boxes[x].add(findFeedback[x]);
    }

    // Create the two horizontal splits to hold each file type's pair of
    // files.
    JSplitPane origSplit = new JSplitPane
        (JSplitPane.HORIZONTAL_SPLIT, boxes[LEFTORIG], boxes[RIGHTORIG]);
    JSplitPane tokSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                         boxes[LEFT], boxes[RIGHT]);

    // Add these to the main vertically split pane.
    JSplitPane codeSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                          origSplit, tokSplit);
    codeSplit.setOneTouchExpandable(true);

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                          navigator, codeSplit);
    mainSplit.setOneTouchExpandable(true);
    getContentPane().add(mainSplit);

    // buttons for marking.
    JPanel bpane = new JPanel();
    JButton button = new JButton("Suspicious");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //mark this match as suspicisou.
        MatchesScreen.marking.add(match.output());
        //parent MatchesScreens need to be updated to reflect
        //this change.
        caller.update();
      }
    });
    bpane.add(button, BorderLayout.WEST);
    button = new JButton("Innocent");
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //mark this match as innocent.
        MatchesScreen.marking.remove(match.output());
        //parent MatchesScreens need to be updated to reflect
        //this change.
        caller.update();
      }
    });
    bpane.add(button, BorderLayout.EAST);
    getContentPane().add(bpane, BorderLayout.SOUTH);

    this.pack();

    // Note size of this entire window is set by the call to setSize in
    // the constructor.

    // More interested in the bottom (processed files) pane by default
    mainSplit.setDividerLocation(100);

    // Set the horizontal splits half way across
    int min, max;
    JSplitPane splitP = tokSplit;
    min = splitP.getMinimumDividerLocation();
    max = splitP.getMaximumDividerLocation();
    splitP.setDividerLocation(max - min / 2 + min);

    splitP = origSplit;
    min = splitP.getMinimumDividerLocation();
    max = splitP.getMaximumDividerLocation();
    splitP.setDividerLocation(max - min / 2 + min);

  } // setUpScreen

  /**
   * Find a string in 1-4 of the compare panes.
   *
   * @param prompt if true, show the find dialog box; if false, find with the
   *  same options as last time
   */
  private void find(boolean prompt) {

    // If not finding again or haven't found before, prompt for user
    // choice.
    if (prompt || finddialog == null) {
      // If haven't shown the dialog before, create one with the
      // correct details.
      if (finddialog == null) {
        File f = new File(match.getFile1());
        String first = f.getName();
        f = new File(match.getFile2());
        String second = f.getName();
        finddialog = new FindDialog
            (this, Settings.fileTypes[match.getFileType()]
             .getDescription(), first, second);
      }
      // If text is selected, pass that as the string to be searched for.
      if (!selectedText.equals("")) {
        finddialog.setStringToFind(selectedText);
        // Show it.
      }
      finddialog.setLocationRelativeTo(this);
      finddialog.setVisible(true);
    }

    // Whether searching forwards or backwards.
    boolean forward = finddialog.isForward();
    // Whether case sensitive or not.
    boolean matchCase = finddialog.isMatchCase();
    // The string to find.
    String stringToFind = finddialog.getStringToFind();

    // Only search if user didn't press cancel - which sets stringToFind
    // to "".
    if (!stringToFind.equals("")) {
      // Do the actual find for each pane that needs to be searched.
      for (int x = LEFTORIG; x <= RIGHT; x++) {
        if (finddialog.isPanetoBeSearched(x)) {
          // The pane we're working with.
          JTextArea jta = panes[x];

          // The text to search in.
          String text = jta.getText();

          // If not being case sensitive, make it all upper case
          // here to do so.
          if (!matchCase) {
            stringToFind = stringToFind.toUpperCase();
            text = text.toUpperCase();
          }

          // The starting position. If have just searched,
          // ensure start new search from *after* string just found.
          int start = jta.getCaretPosition();
          if (!prompt) {
            int diff = text.length() - start
                - stringToFind.length();
            if (diff < 0) {
              start = diff;
            }
            else {
              start += stringToFind.length();
            }
          }

          // The location of the next occurrence of stringToFind.
          int next = 0;

          // Search for the string.
          if (forward) {
            next = text.indexOf(stringToFind, start);
          }
          else {
            next = text.lastIndexOf(stringToFind, start);

            // Reached end of doc without finding anything, so
            // wrap around if necessary.
          }
          if (next == -1 && finddialog.isWrapAround()) {
            if (forward) {
              next = text.indexOf(stringToFind, 0);
            }
            else {
              next = text.lastIndexOf
                  (stringToFind, text.length());

              // Have reached end of document and not wrapping
              // (next == -1), say pattern not found.
            }
          }
          if (next == -1) {
            findFeedback[x].setText
                ("Pattern '" + stringToFind + "' not found.");

            // Otherwise have found it (though it may be the
            // one we started at), so move the focus to that point.
          }
          else {
            jta.setCaretPosition(next);
            findFeedback[x].setText("");
          }
        } // if searching in this pane.
      } // for... the actual searching
    } // if stringToFind != ""

  } // find

  /**
   * Detabify s, prepend number i and ensure the result has a certain
   * length.
   *
   * @author Ashley Ward
   * @version March 2002
   */
  private String numberDetabifySetLength(String s, int lineNo) {
    final int length = 80;
    StringBuffer r = new StringBuffer(length);

    // put lineNo at the start of r
    r.append( (new java.text.DecimalFormat("0000")).format( (long) lineNo));

    // Append detabified s to r, coping if s is null
    if (s == null) {
      s = "";
    }
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\t') {
        r.append("    ");
      }
      else {
        r.append(c);
      }
    }

    // Pad r out with spaces if necessary
    while (r.length() < length) {
      r.append(' ');

      // Truncate r to the correct length
    }
    r.setLength(length);

    return r.toString();
  }

  /**
   * Save parts of the two original files to a text file.  Each line of
   * the text file will contain two numbered lines from each original
   * file.  This is intended for later printing (eg a2ps --landscape
   * --columns=1 --rows=1 --chars-per-line=160 works quite well) to
   * provide readable evidence.
   *
   * @author Ashley Ward
   * @version March 2002
   */
  private void saveOriginalSectionsToASCIIFile() {
    //set default filename to save.
    String file1 = (new File(match.getFile1())).getName();
    String file2 = (new File(match.getFile2())).getName();

    //remove extension names
    int dotindex = file1.indexOf('.');
    file1 = file1.substring(0, dotindex);
    dotindex = file2.indexOf('.');
    file2 = file2.substring(0, dotindex);

    JTextField textField = new JTextField(40);
    textField.setText(file1 + "-" + file2 + ".mat");

    Object[] array = {
        "Enter filename for ASCII file.  It will be saved in the "
        + "match directory.",
        textField};

    //JOptionPane optpane = new JOptionPane();
    String filename;
    int choice = JOptionPane.showConfirmDialog
        (this, array, "Select filename", JOptionPane.OK_CANCEL_OPTION);
    //depending on user choice, determine whether to save file or not.
    if (choice == JOptionPane.OK_OPTION) {
      filename = textField.getText();
    }
    else {
      filename = null;

    }
    if (filename != null && !filename.equals("")) {
      File outName = new File(Settings.sourceDirectory + Settings
                              .fileSep +
                              Settings.sherlockSettings
                              .getMatchDirectory(),
                              filename);
      // Left original file
      File leftName = new File(Settings.sourceDirectory, match
                               .getFile1());
      int leftStart = match.getRun().getStartCoordinates()
          .getOrigLineNoInFile1();
      int leftEnd = match.getRun().getEndCoordinates()
          .getOrigLineNoInFile1();

      // Right original file
      File rightName = new File(Settings.sourceDirectory,
                                match.getFile2());
      int rightStart = match.getRun().getStartCoordinates()
          .getOrigLineNoInFile2();
      int rightEnd = match.getRun().getEndCoordinates()
          .getOrigLineNoInFile2();

      try {
        PrintWriter out =
            new PrintWriter(new BufferedWriter
                            (new FileWriter(outName)));

        out.println(getTitle());
        out.print(numberDetabifySetLength(fileDetails[LEFTORIG]
                                          .getText()
                                          + ".  Match begins line "
                                          + leftStart + ", ends line "
                                          + leftEnd,
                                          0));
        out.println(numberDetabifySetLength(fileDetails[RIGHTORIG]
                                            .getText()
                                            + ".  Match begins line "
                                            + rightStart
                                            + ", ends line "
                                            + rightEnd,
                                            0));

        int leftLineNo = 1, rightLineNo = 1;
        String l = "", r = "";

        BufferedReader lbr = new BufferedReader
            (new FileReader(leftName));
        BufferedReader rbr = new BufferedReader
            (new FileReader(rightName));

        // wind forward to leftStart and rightStart
        while (leftLineNo < leftStart) {
          l = lbr.readLine();
          leftLineNo++;
        }
        while (rightLineNo < rightStart) {
          r = rbr.readLine();
          rightLineNo++;
        }

        /* Read one line from left and right.  Number the lines.  Pad
           left to some length, and paste right to the end of it.
           Append the result to the output file. */

        l = lbr.readLine();
        r = rbr.readLine();
        while ( (l != null) || (r != null)) {
          if (l != null && l.startsWith("#line ")) {
            l = lbr.readLine();
            continue;
          }
          if (r != null && r.startsWith("#line ")) {
            r = rbr.readLine();
            continue;
          }

          if (leftLineNo > leftEnd) {
            l = null;
          }
          if (rightLineNo > rightEnd) {
            r = null;
          }
          if ( (leftLineNo > leftEnd) && (rightLineNo > rightEnd)) {
            break;
          }

          // Output numbered l and r, coping with null
          if (l == null) {
            l = "";
            leftLineNo = 9999;
          }
          if (r == null) {
            r = "";
            rightLineNo = 9999;
          }
          out.print(numberDetabifySetLength(l, leftLineNo));
          out.println(numberDetabifySetLength(r, rightLineNo));

          l = lbr.readLine();
          leftLineNo++;
          r = rbr.readLine();
          rightLineNo++;
        }

        lbr.close();
        rbr.close();
        out.flush();
        out.close();

      }
      catch (IOException f) {
// 		System.err.println(f.toString());
// 		f.printStackTrace();
        JOptionPane.showMessageDialog
            (this, "Cannot save file. Saving failed.",
             "Failed", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

  }

  /**
   * Print the current screen to the HTML file that is selected.
   */
  private void print() {

    String filename = JOptionPane.showInputDialog
        (this, "Enter the file to store the match in.\n.html will be "
         + "added to it.", "Select filename",
         JOptionPane.OK_CANCEL_OPTION);

    // If user presses ok and filename is not empty, save the file.
    if (filename != null && !filename.equals("")) {
      File frameDef = null;
      File sources[] = new File[4];

      try {
        frameDef = new File
            (Settings.sourceDirectory + Settings.fileSep +
             Settings.sherlockSettings.getMatchDirectory(),
             filename + ".html");

        printFrameDefinitionFile(frameDef);

        for (int x = 0; x < 4; x++) {
          sources[x] = new File
              (Settings.sourceDirectory + Settings.fileSep +
               Settings.sherlockSettings.getMatchDirectory(),
               filename + "-" + x + ".html");
          printSourceFile(sources[x], x);
        }
      }
      catch (IOException e) {
        // There has been a problem saving one of the files. Put
        // up an error message, and delete any files that have been
        // made.
        JOptionPane.showMessageDialog
            (this, "Error saving this match to HTML", "Printing error",
             JOptionPane.OK_OPTION);
        if (frameDef != null && frameDef.exists()) {
          frameDef.delete();
        }
        for (int x = 0; x < 4; x++) {
          if (sources[x] != null && sources[x].exists()) {
            sources[x].delete();
          }
        }
      }
    } // if user presses ok.

  } // print

  /**
   * Save the frame definition html file in the passed file.
   *
   * @param filethe file to save it in to
   */
  private void printFrameDefinitionFile(File file) throws IOException {

    // Get the filename less the ".html" to put in as the frame source
    // as sourcename + "-x.html"
    String sourcename = file.getName();
    sourcename = sourcename.substring(0, sourcename.length() - 5);

    PrintWriter out = new PrintWriter
        (new BufferedWriter(new FileWriter(file)));
    out.println("<html>");

    out.println("\t<frameset rows=50,50 border=\"1\">");

    out.println("\t\t<frameset cols=50,50 border=\"1\">");
    out.println("\t\t\t<frame name=\"leftOrig\" src=\"" + sourcename +
                "-" + LEFTORIG + ".html\">");
    out.println("\t\t\t<frame name=\"rightOrig\" src=\"" + sourcename +
                "-" + RIGHTORIG + ".html\">");
    out.println("\t\t</frameset>");

    out.println("\t\t<frameset cols=50,50 border=\"1\">");
    out.println("\t\t\t<frame name=\"leftTok\" src=\"" + sourcename +
                "-" + LEFT + ".html\">");
    out.println("\t\t\t<frame name=\"rightTok\" src=\"" + sourcename +
                "-" + RIGHT + ".html\">");
    out.println("\t\t</frameset>");

    out.println("\t</frameset>");

    out.println("</html>");

    out.flush();
    out.close();
  } // printFrameDefinitionFile

  /**
   * Save the contents of the passed pane in the passed html file.
   *
   * @param filethe file to save to
   * @param panethe pane to save the contents of
   */
  private void printSourceFile(File file, int pane) throws IOException {

    String code = panes[pane].getText();
    String editedCode = code.replace('<', '#');
    editedCode = editedCode.replace('>', '#');

    /*
      StringBuffer output = new StringBuffer(code);
      char lookFor = '<';
      String replaceWith = "&lt;";
      for (int y = 0; y < 2; y++) {
      if (y == 1) {
      lookFor = '>';
      replaceWith = "&gt;";
      }
      int codeIndex = code.indexOf(lookFor);
      System.out.print("Starting " + lookFor);
      while (codeIndex != -1) {
      System.out.print(codeIndex);
      output = output.replace(codeIndex, codeIndex, replaceWith);
      code = output.toString();
      codeIndex = code.indexOf(lookFor, codeIndex + 4);
      }
      System.out.print("Finished " + lookFor);
      }
      String editedCode = output.toString();
     */

    PrintWriter out = new PrintWriter
        (new BufferedWriter(new FileWriter(file)));
    out.println("<html>");
    out.println("\t<body>");
    out.println("\t\t<pre>");

    out.println("This match:");
    out.println("\t" + getTitle());
    out.println("This file:");
    out.println("\t" + fileDetails[pane].getText());
    out.println("=================================================");

    out.println(editedCode);

    out.println("\t\t</pre>");
    out.println("\t</body>");
    out.println("</html>");
    out.flush();
    out.close();
  } // printSourceFile

  /**
   * Set up the menus.
   */
  private void setUpMenus() {
    JMenuBar jmb = getJMenuBar();

    // File menu:
    JMenu fileMenu = jmb.getMenu(FILE_MENU);

    JMenuItem jmi = new JMenuItem("Find...", KeyEvent.VK_F);
    jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                              ActionEvent.CTRL_MASK));
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    jmi = new JMenuItem("Find again", KeyEvent.VK_A);
    jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    fileMenu.addSeparator();

    // Only add view exclude file if one has been chosen and so can be
    // viewed.
    if (!Settings.sherlockSettings.getExcludeFile().equals("")) {
      jmi = new JMenuItem("View exclude file", KeyEvent.VK_V);
      jmi.addActionListener(this);
      fileMenu.add(jmi);
      fileMenu.addSeparator();
    }

    jmi = new JMenuItem("Save original sections to ASCII file...",
                        KeyEvent.VK_S);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    jmi = new JMenuItem("Print...", KeyEvent.VK_P);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    fileMenu.addSeparator();

    jmi = new JMenuItem("Close", KeyEvent.VK_C);
    jmi.addActionListener(this);
    fileMenu.add(jmi);

    // Edit menu
    JMenu editMenu = jmb.getMenu(EDIT_MENU);
    editMenu.setEnabled(false);

    // Options menu:
    JMenu optionsMenu = jmb.getMenu(OPTIONS_MENU);

    showWholeSubFiles = new JCheckBoxMenuItem
        ("Show whole of submitted files",
         Settings.sherlockSettings.getShowWholeSub());
    showWholeSubFiles.setMnemonic(KeyEvent.VK_S);
    showWholeSubFiles.addActionListener(this);
    optionsMenu.add(showWholeSubFiles);

    showWholeTokFiles = new JCheckBoxMenuItem
        ("Show whole of " +
         Settings.fileTypes[match.getFileType()].getDescription() +
         " files", true); // Settings.viewWholeFiles
    showWholeTokFiles.setMnemonic(Settings.fileTypes[match.getFileType()]
                                  .getDescription().charAt(0));
    showWholeTokFiles.addActionListener(this);
    optionsMenu.add(showWholeTokFiles);

    optionsMenu.addSeparator();

    jmi = new JMenuItem("Viewing options...", KeyEvent.VK_V);
    jmi.addActionListener(this);
    optionsMenu.add(jmi);
  } // setUpMenus

  /**
   * Handle action events
   */
  public void actionPerformed(ActionEvent e) {
    JMenuItem m = (JMenuItem) e.getSource();
    String menu = m.getText();

    // File menu:
    if (menu.equals("Find...")) {
      find(true);

    }
    else if (menu.equals("Find again")) {
      find(false);

    }
    else if (menu.equals("View exclude file")) {
      TextWindow tw = new TextWindow
          (gui, "Exclude file: " +
           Settings.sherlockSettings.getExcludeFile());
      tw.setVisible(true);
    }

    else if (menu.equals("Save original sections to ASCII file...")) {
      saveOriginalSectionsToASCIIFile();

    }
    else if (menu.equals("Print...")) {
      print();

    }
    else if (menu.equals("Close")) {
      closeMe();

      // Options menu:
    }
    else if (m.equals(showWholeSubFiles)) {
      // Reload the code displaying as much code as now asked for.
      Settings.sherlockSettings.setShowWholeSub
          (showWholeSubFiles.isSelected());
      loadMatchedCode();
    }

    else if (m.equals(showWholeTokFiles)) {
      // Reload the code displaying as much code as now asked for.
      Settings.sherlockSettings.setShowWholeTok
          (showWholeTokFiles.isSelected());
      loadMatchedCode();
    }

    else if (menu.equals("Viewing options...")) {
      ViewingOptions vo = new ViewingOptions
          (this, showWholeTokFiles.getText());
      vo.setVisible(true);
      if (vo.okPressed()) {
        showWholeSubFiles.setSelected
            (Settings.sherlockSettings.getShowWholeSub());
        showWholeTokFiles.setSelected
            (Settings.sherlockSettings.getShowWholeTok());
        loadMatchedCode();
      }
    }

  } // actionPerformed

}
