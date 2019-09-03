/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import uk.ac.warwick.dcs.cobalt.sherlock.*;

/**
 *
 * <P>Displays the results from the free-text detection facility in a table.
 * Allows users to quickly see which files are most likely to be plagiarised
 * so that the most likely examples can be examined.</P>
 *
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 *
 * @author Daniel White
 * @version 4
 */

public class GroupResults
    extends MyFrame {
  /**
   * Used by the filter, says how many similarities a sentence can have
   * before it is ignored.
   */
  public static int MAX_LINKS;
  /** How many similar documents to keep track of. This number of documents
       * will appear in the drop down box at the top of the Sentence results window.
   */
  public static int NUM_SIMILAR;
  /**
   * Whether the filter should attempt to group pairs that are very similar by
   * giving them the same score
   */
  public static boolean GROUP_PAIRS;
  // outputs some debugging info
  private final static boolean DEBUG = false;
  // outputs HTML tables of statistics, was included to aid the testing done
  // for the project report.
  private final static boolean TESTING = false;

  // stores all the pairs arrays, very important.
  private static SentencePair[][] pairs;
  private JTable results;
  private JLabel stats;
  // all the results files.
  private static File[] pairFiles;
  // the multipliers used by the filter for each individual document.
  private static double[] mults;
  // Each document has a list of documents it is most similar to.
  private static ValNamePair[][] mostSimilar;
  private ProgressMonitor progBar;
  private FilterTask task;

  private JDialog graph;

  private javax.swing.Timer timer;

  /**
   * Displays the group results for this set of data.
   * @param gui The class controlling the GUI of Sherlock as a whole.
   */
  public GroupResults(MyGUI gui) {
    super(gui, "Free-text Results Overview");
    MAX_LINKS = Settings.getFileTypes()[Settings.SEN].getMaxLinks();
    NUM_SIMILAR = Settings.getFileTypes()[Settings.SEN].getNumSimilar();
    GROUP_PAIRS = Settings.getFileTypes()[Settings.SEN].getGroupPairs();
    gui.addWindow(this);

    // The constructor for this object loads the pairs files.
    task = new FilterTask(true);
    pairs = new SentencePair[0][0];

    if (pairFiles == null || pairFiles.length == 0) {
      JOptionPane.showMessageDialog(this,
                                    "There are no results to show.",
                                    "Display Results",
                                    JOptionPane.INFORMATION_MESSAGE);
      closeMe();
      return;
    }

    results = new JTable();
    ResultsTableModel model = new ResultsTableModel();
    results.setRowSelectionAllowed(false);
    results.setColumnSelectionAllowed(false);
    results.setAutoCreateColumnsFromModel(true);
    results.setModel(model);
    results.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        else {
          openResultsViewer();
        }

      }
    });
    getContentPane().add(new JScrollPane(results));

    Box bottom = createBottomPanel();
    stats = new JLabel();
    updateStats(generateStatsTable());

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        int reply = saveChanges();
        if (reply != JOptionPane.CANCEL_OPTION) {
          closeMe();
        }
      }
    });
    getContentPane().add(bottom, BorderLayout.SOUTH);
    getContentPane().add(stats, BorderLayout.WEST);

    JMenuBar jmb = this.getJMenuBar();
    jmb.getMenu(FILE_MENU).setVisible(false);
    jmb.getMenu(EDIT_MENU).setVisible(false);
    jmb.getMenu(OPTIONS_MENU).setVisible(false);

    progBar = new ProgressMonitor(this, "Loading Results",
                                  "Reading Files", 0, task.lengthOfTask);
    progBar.setMillisToDecideToPopup(10);
    progBar.setMillisToPopup(10);

    timer = new javax.swing.Timer(75, new TimerListener());
    // Filter results and populate the table, displaying a progress bar if time
    // allows.
    task.start();
    timer.start();

  }

  /**
   * Timer used to update the progress bar during filtering or loading.
   * <p>Title: Sherlock 2003</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 1
   */
  class TimerListener
      implements ActionListener {
    /**
     * Called once every so often to update the progress bar's status
     * @param e An action event (not used).
     */
    public void actionPerformed(ActionEvent e) {
      if (task.done()) {
        progBar.close();
        timer.stop();
        pack();
        setVisible(true);
        if (graph == null) {
          initGraph();

        }
      }
      else {
        if (progBar.isCanceled()) {
          progBar.close();
          timer.stop();
          task.cancelled = true;
          displayError();
          return;
        }
        progBar.setNote(task.message);
        progBar.setProgress(task.currentStatus);
      }
    }
  }

  /**
   * Displays an error when the user stops filtering.
   */
  private void displayError() {
    if (!isVisible()) {
      return;
    }

    JOptionPane.showMessageDialog(this, "The data structure has only been " +
                                  "partially\nfiltered. Please let the " +
                                  "filter complete!", "ERROR",
                                  JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays the statistical graph.
   */
  private void initGraph() {
    if (graph == null) {
      graph = new JDialog(this, "Score Distributions", false);
      // display the graph to the right of the group results window.
      Point thisLoc = this.getLocationOnScreen();
      graph.setLocation( (int) thisLoc.getX() + getWidth(), (int) thisLoc.getY());

      graph.getContentPane().add(bGraph);
      graph.getContentPane().add(tableData, BorderLayout.SOUTH);
      graph.setResizable(false);
    }

    bGraph.setPreferredSize(bGraph.getPrefferedDimensions());
    bGraph.repaint();
    graph.pack();
    graph.setVisible(true);

  }

  /**
   * Controls the filtration of results.
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2003</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  class FilterTask
      extends Thread {
    /** Number of stages within the task */
    int lengthOfTask;
    /** Number of stages completed. */
    int currentStatus;
    /** Message that the ProgressMonitor should display */
    String message;
    /** Whether the task should load the pairs files */
    boolean load;
    /** Whether the user cancelled the task */
    boolean cancelled = false;

    /**
     * Constructor.
     * @param load Whether to load pairs files from disk.
     */
    FilterTask(boolean load) {
      this.load = load;

      // The match directory.
      File md = new File(Settings.getSourceDirectory(),
                         Settings.getSherlockSettings().getMatchDirectory());
      pairFiles = md.listFiles(new PairFilenameFilter());
      if (load) {
        lengthOfTask = pairFiles.length * 2;
        message = "Loading Files";
      }
      else {
        lengthOfTask = pairs.length;
      }
      // Need to set priority of the thread to normal
      // as it is created by the event-dispatching thread, which is high
      // priority. This ensures that the computer will have reasonable
      // performance if the user wants to do something else while Sherlock
      // is running
      setPriority(Thread.NORM_PRIORITY - 1);
    }

    /** Whether the task has completed all stages
     * @return true if all stages are complete.
     */
    public boolean done() {
      return currentStatus >= lengthOfTask;
    }

    /**
     * Starts filtering results.
     */
    public void run() {
      currentStatus = 0;
      if (load) {
        pairs = loadPairs(this);
        if (cancelled) {
          return;
        }
        // If this set of pairs has been saved after being looked at before.
        if (pairs != null && pairs.length > 0 && pairs[0] != null &&
            pairs[0].length > 0 && !pairs[0][0].isChanged()) {

          message = "Filtering Results";
          filterPairs(this, false);
          if (cancelled) {
            return;
          }

          message = "Populating Results Table";
          prepareTable(this);
          if (cancelled) {
            return;
          }

          currentStatus = lengthOfTask;
          return;
        }
      }
      unIgnoreEverything();
      if (cancelled) {
        return;
      }

      message = "Filtering Results";
      filterPairs(this, true);
      if (cancelled) {
        return;
      }
      message = "Populating Results Table";
      prepareTable(this);
    }
  }

  /**
   * Loads results into a table for the user to view.
   * @param task The task which called this method.
   */
  private void prepareTable(FilterTask task) {
    // Need to remove documents which contained no sentences worth comparing.
    int count = 0;
    for (int i = 0; i < pairs.length; i++) {
      if (pairs[i].length != 0) {
        count++;
      }
    }

    String[] colData = new String[3];

    colData[0] = "Filename";
    colData[1] = "Total Scores";
    colData[2] = "Relative Values";

    String[][] rowData = new String[count][colData.length];
    ValNamePair[] tempArray = new ValNamePair[count];
    // populate rowData array.
    count = 0;
    for (int i = 0; i < pairs.length; i++) {
      if (pairs[i].length != 0) {
        int score = 0;
        for (int j = 0; j < pairs[i].length; j++) {
          for (int k = 0; k < pairs[i][j].scores.size(); k++) {
            SentenceScore temp =
                (SentenceScore) pairs[i][j].scores.get(k);
            if (!temp.isIgnored()) {
              score += temp.score;
            }
          }
        }
        // TempNameScore stores the results so they can be easily sorted.
        tempArray[count] = new ValNamePair( (score * mults[i]),
                                           pairs[i][0].name);
        count++;
      }
      //task.currentStatus++;
    }

    // tempArray needs to be sorted
    Arrays.sort(tempArray);

    // Normalise Scores and convert to a format suitable for the table to
    // display.
    double topScore = 0;
    if (tempArray.length > 0) {
      topScore = tempArray[0].val;

    }
    for (int i = 0; i < tempArray.length; i++) {
      double tmp = (tempArray[i].val / topScore) * 100;
      rowData[i][0] = tempArray[i].name;
      rowData[i][1] = "" + (int) tempArray[i].val;
      rowData[i][2] = "" + (int) tmp + "%";
    }

    // create the table.

    ResultsTableModel model = (ResultsTableModel) results.getModel();
    model.setDataVector(rowData, colData);
    model.fireTableChanged(null);

    String statsTable = generateStatsTable();
    updateStats(statsTable);

    /**
         * included to export results in a format that can be used elsewhere, mostly
     * for the purposes of the project report. The code could be used in the
     * future as the basis of a function to export the table to a HTML file.
     */
    if (TESTING) {
      outputHTMLResults(rowData, colData, statsTable);
    }

  }

  /**
   * Updates the statistics table to the left of the results table.
   * @param statsTable A table in HTML format.
   */
  private void updateStats(String statsTable) {
    stats.setText("<HTML><BODY>" + statsTable + "</BODY></HTML>");
  }

  /**
   * Generates some stats to display on the left of the results table.
       * @return A HTML formatted table containing some interesting statistics about
   * the results.
   */
  private String generateStatsTable() {
    /*
     * Generate some stats
     */
    int totalDocs = pairs.length;
    int totalSentences = 0, totalPairs = 0, totalScore = 0, ignoredPairs = 0;
    int ignoredScore = 0;

    for (int i = 0; i < totalDocs; i++) {
      totalSentences += pairs[i].length;
      for (int j = 0; j < pairs[i].length; j++) {
        Vector scores = pairs[i][j].scores;
        totalPairs += scores.size();
        for (int k = 0; k < scores.size(); k++) {
          SentenceScore temp = (SentenceScore) scores.get(k);
          totalScore += temp.score;
          if (temp.isIgnored()) {
            ignoredPairs++;
            ignoredScore += temp.score;
          }
        }
      }
    }

    // For calculating std deviation, min and max values
    byte[] scores = new byte[totalPairs];
    byte[] ignoredScores = new byte[ignoredPairs];
    byte[] usefulScores = new byte[ (totalPairs - ignoredPairs)];

    // Div by 2, since all links and scores are repeated once.
    ignoredPairs /= 2;
    totalPairs /= 2;
    totalScore /= 2;
    ignoredScore /= 2;

    int stdIndex = 0, ignoreIndex = 0, usefulIndex = 0;
    for (int i = 0; i < totalDocs; i++) {
      for (int j = 0; j < pairs[i].length; j++) {
        Vector links = pairs[i][j].scores;
        for (int k = 0; k < links.size(); k++) {
          SentenceScore temp = (SentenceScore) links.get(k);
          scores[stdIndex++] = temp.score;
          if (temp.isIgnored()) {
            ignoredScores[ignoreIndex++] = temp.score;
          }
          else {
            usefulScores[usefulIndex++] = temp.score;
          }
        }
      }
    }

    updateGraph(scores, ignoredScores, usefulScores);

    // Calculate some stats.
    double avSentPerDoc = totalDocs != 0 ? (double) totalSentences / totalDocs :
        0;
    double avLinksPerSent = totalSentences != 0 ?
        (double) totalPairs / totalSentences : 0;
    double avIgnoredLinkScore = ignoredPairs != 0 ?
        (double) ignoredScore / ignoredPairs : 0;
    double percentageIgnoredPairs = 0, avLinkScore = 0, avUsefulLinkScore = 0;
    if (totalPairs != 0) {
      percentageIgnoredPairs = (double) ignoredPairs / totalPairs;
      avLinkScore = (double) totalScore / totalPairs;
      int usefulLinks = totalPairs - ignoredPairs;
      int usefulTotal = totalScore - ignoredScore;
      avUsefulLinkScore = (double) usefulTotal / usefulLinks;
    }

    int min = 200, max = 0;
    double sumOfSquaredDeviation = 0.0;
    for (int i = 0; i < scores.length; i++) {
      if (scores[i] < min) {
        min = scores[i];
      }
      if (scores[i] > max) {
        max = scores[i];
      }
      double deviation = scores[i] - avLinkScore;
      sumOfSquaredDeviation += (deviation * deviation);
    }
    double variance = scores.length > 1 ?
        sumOfSquaredDeviation / (scores.length - 1) : 0;
    double stdDeviation = Math.sqrt(variance);

    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(2);
    nf.setMinimumFractionDigits(2);
    NumberFormat pnf = NumberFormat.getPercentInstance();
    pnf.setMaximumFractionDigits(2);
    pnf.setMinimumFractionDigits(2);

    // Output the HTML.
    StringBuffer out = new StringBuffer();
    out.append(
        "<TABLE BORDER=1 CELLPADDING=2><TR><TD><B># of Docs</B></TD><TD>" +
        totalDocs + "</TD></TR>\n");
    out.append("\t<TR><TD><B># of Sentences</B></TD><TD>" +
               totalSentences + "</TD></TR>\n");
    out.append("\t<TR><TD><B># of Similarities</B></TD><TD>" +
               totalPairs + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Total Similarity Score</B></TD><TD>" +
               totalScore + "</TD></TR>\n");
    out.append("\t<TR><TD><B># of Ignored Similarities</B></TD><TD>" +
               ignoredPairs + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Total Ignored Score</B></TD><TD>" +
               ignoredScore + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Average Sentences per Doc</B></TD><TD>" +
               nf.format(avSentPerDoc) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Average Similarities per Sentence</B></TD><TD>" +
               nf.format(avLinksPerSent) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Average Similarity Score</B></TD><TD>" +
               nf.format(avLinkScore) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Percentage of Similarities Ignored</B></TD><TD>" +
               pnf.format(percentageIgnoredPairs) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Average Ignored Similarity Score</B></TD><TD>" +
               nf.format(avIgnoredLinkScore) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Average Useful Similarity Score</B></TD><TD>" +
               nf.format(avUsefulLinkScore) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Standard Deviation of all<BR>" +
               "Similarity Scores</B></TD><TD>" +
               nf.format(stdDeviation) + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Lowest Similarity Score</B></TD><TD>" +
               min + "</TD></TR>\n");
    out.append("\t<TR><TD><B>Highest Similarity Score</B></TD><TD>" +
               max + "</TD></TR>\n");
    out.append("</TABLE>\n");
    return out.toString();
  }

  private String boxGraphTable = "";
  private BoxPlotGraph bGraph = null;
  private JLabel tableData = null;
  private static final String[] labels = {
      "All Scores", "Ignored", "Useful"};

  /**
   * Updates the graph dialog. Updates the picture and then adds the table at
   * the bottom.
   * @param scores Every similarity score
   * @param ignoredScores Every ignored similarity score
   * @param usefulScores Every useful (non-ignored) similarity score.
   */
  private void updateGraph(byte[] scores, byte[] ignoredScores,
                           byte[] usefulScores) {
    byte[][] passedScores = new byte[3][];
    passedScores[0] = scores;
    passedScores[1] = ignoredScores;
    passedScores[2] = usefulScores;
    if (bGraph == null) {
      bGraph = new BoxPlotGraph(passedScores, labels, 0, 100, 4);
    }
    else {
      bGraph.calculateData(passedScores);

    }
    double[][] stats = bGraph.getStats();
    StringBuffer table = new StringBuffer();
    table.append("<TABLE BORDER=1 CELLPADDING=2><TR><TD></TD><TD>Minimum</TD>" +
                 "<TD>25th Percentile</TD><TD>Median</TD>" +
                 "<TD>75th Percentile</TD><TD>Maximum</TD></TR>\n");
    NumberFormat nf = NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(1);
    nf.setMinimumFractionDigits(0);
    for (int i = labels.length - 1; i >= 0; i--) {
      table.append("\t<TR><TD>" + labels[i] + "</TD>\n");
      table.append("\t\t<TD>" + nf.format(stats[i][bGraph.MIN]) + "</TD>\n");
      table.append("\t\t<TD>" + nf.format(stats[i][bGraph.TWENT_FIFTH]) +
                   "</TD>");
      table.append("\n\t\t<TD>" + nf.format(stats[i][bGraph.MEDIAN]) +
                   "</TD>\n");
      table.append("\t\t<TD>" + nf.format(stats[i][bGraph.SEVENT_FIFTH]) +
                   "</TD>");
      table.append("\n\t\t<TD>" + nf.format(stats[i][bGraph.MAX]) +
                   "</TD></TR>\n");
    }
    table.append("</TABLE>\n");
    // used for HTML output
    boxGraphTable = table.toString();

    if (tableData == null) {
      tableData = new JLabel("<HTML><BODY BGCOLOR=#FFFFFF>" + boxGraphTable +
                             "</BODY></HTML>");
    }
    else {
      tableData.setText("<HTML><BODY BGCOLOR=#FFFFFF>" + boxGraphTable +
                        "</BODY></HTML>");
    }
    tableData.validate();
    bGraph.validate();
    if (graph == null) {
      return;
    }
    initGraph();
  }

  /**
   * Used when I was writing the project reports to output some HTML tables,
   * which could then be imported into a spreadsheet.
   * @param rowData The data from the results table
   * @param colData The column headers from the results table.
   * @param statsTable A table of statistics.
   */
  private void outputHTMLResults(String[][] rowData, String[] colData,
                                 String statsTable) {
    FileTypeProfile sent = Settings.getFileTypes()[Settings.SEN];
    String filename = Settings.getSourceDirectory().getName() + "Sim" +
        sent.getSimThreshold() + "Com" + sent.getCommonThreshold() +
        "max" + sent.getMaxLinks() + "commonlength" +
        sent.getCommonWords().length + (GROUP_PAIRS ? "GROUP" : "") +
        ".html";

    File output = new File(Settings.getSourceDirectory(),
                           filename);
    StringBuffer out = new StringBuffer();

    out.append("<HTML><HEAD><TITLE>Results for " +
               Settings.getSourceDirectory() + "</TITLE></HEAD><BODY>\n");
    //output settings info
    out.append("<H3>Settings</H3>");
    out.append("<B>Similarity Threshold:</B> " + sent.getSimThreshold() +
               "<BR>\n");
    out.append("<B>Common Threshold:</B> " + sent.getCommonThreshold() +
               "<BR>\n");
    out.append("<B>Maximum Links:</B> " + sent.getMaxLinks() + "<BR>\n");
    out.append("<TABLE><TR><TD><B>Common Words</B></TD></TR>\n");
    String[] words = sent.getCommonWords();
    for (int i = 0; i < words.length; i++) {
      out.append("<TR><TD>" + words[i] + "</TD></TR>\n");
    }
    out.append("</TABLE>\n<HR>\n<H3>Stats</H3>\n");
    out.append(statsTable);
    out.append(boxGraphTable);

    out.append("<HR>\n<H3>Scores</H3>\n");

    out.append("<TABLE>\n\t<TR>\n");
    for (int i = 0; i < colData.length; i++) {
      out.append("\t\t<TD>" + colData[i] + "</TD>\n");
    }
    out.append("\t</TR>\n");

    for (int i = 0; i < rowData.length; i++) {
      out.append("\t<TR>\n");
      for (int j = 0; j < colData.length; j++) {
        out.append("\t\t<TD>" + rowData[i][j] + "</TD>\n");
      }
      out.append("\t</TR>\n");
    }

    out.append("</TABLE></BODY></HTML>");
    try {
      BufferedWriter buffOut = new BufferedWriter(
          new FileWriter(output));
      buffOut.write(out.toString());
      buffOut.flush();
      buffOut.close();
    }
    catch (IOException ioe) {
      System.err.println("IOException while writing results");
      ioe.printStackTrace();
    }
  }

  /**
   * Creates the panel displaying the controls allowing the user to refilter
   * the results with different parameters.
   * @return A Box containing all the necessary components.
   */
  private Box createBottomPanel() {
    JButton reFilter = new JButton("Re-Filter Results");
    final JCheckBox groupPairs = new JCheckBox("Attempt to group similar pairs");
    groupPairs.setSelected(
        Settings.getFileTypes()[Settings.SEN].getGroupPairs());
    final NumberField maxLinks = new NumberField(10);
    maxLinks.setToolTipText("<HTML>Any sentence linking to more than this<BR>" +
                            "number of sentences will be ignored</HTML>");
    maxLinks.setText(Settings.getFileTypes()[Settings.SEN].getMaxLinks() + "");
    JLabel lbl = new JLabel("Maximum number of similar sentences");

    Box box = Box.createHorizontalBox();
    box.add(lbl);
    box.add(maxLinks);
    box.add(Box.createHorizontalGlue());
    box.add(reFilter);

    reFilter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Settings.getFileTypes()[Settings.SEN].setMaxLinks(
            Integer.parseInt(maxLinks.getText()));
        Settings.getFileTypes()[Settings.SEN].setGroupPairs(
            groupPairs.isSelected());
        GROUP_PAIRS = groupPairs.isSelected();
        reFilterResults(Integer.parseInt(maxLinks.getText()));
      }
    });

    Box box2 = Box.createHorizontalBox();
    box2.add(groupPairs);
    box2.add(Box.createHorizontalGlue());

    Box box3 = Box.createVerticalBox();
    box3.add(box);
    box3.add(box2);

    return box3;
  }

  /**
   * Starts a FilterTask going with the specified parameter.
   * @param maxLinks The maximum number of sentences a document can link to
   * before it is ignored.
   */
  private void reFilterResults(int maxLinks) {
    MAX_LINKS = maxLinks;
    task = new FilterTask(false);
    progBar = new ProgressMonitor(this, "Re-Filtering Results",
                                  "Resetting results", 0, task.lengthOfTask);

    progBar.setMillisToDecideToPopup(10);
    timer = new javax.swing.Timer(75, new TimerListener());

    task.start();
    timer.start();
  }

  /**
   * The links for a specific document.
   * @param fileName The original file name of the document.
   * @return The set of results for the given document.
   */
  public static SentencePair[] getPairsForName(String fileName) {
    int row = getRowNumber(fileName);
    return row == -1 ? new SentencePair[0] : pairs[row];
  }

  /**
   * Searches the pairFiles array for the set of results for this document.
   * @param name The document's name.
   * @return The row number for that document within the pairsArray. -1 if the
   * document cannot be found.
   */
  private static int getRowNumber(String name) {
    for (int i = 0; i < pairFiles.length; i++) {
      if (pairFiles[i].getName().indexOf(name) != -1) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Called when a user double-clicks a cell in the table. Opens the results
   * viewer for that row of the table.
   */
  private void openResultsViewer() {
    int row = results.getSelectedRow();
    // get the filename and load the original text and serialised document.
    String name = (String) results.getModel().getValueAt(row, 0);

    // also need to identify the correct pairs for the document. For this we
    // search through the files array and find the one whose name matches, the
    // rows in the file array and the pairs array match.
    row = getRowNumber(name);

    File docFile = new File(Settings.getSourceDirectory() + File.separator +
                            Settings.getFileTypes()[Settings.SEN].getDirectory() +
                            File.separator + name + "." +
                            Settings.getFileTypes()[Settings.SEN].getExtension());
    File origFile = new File(Settings.getSourceDirectory() + File.separator +
                             Settings.getFileTypes()[Settings.ORI].getDirectory() +
                             File.separator + name + "." +
                             Settings.getFileTypes()[Settings.ORI].getExtension());
    Document doc = null;
    String[] origText = null;
    try {
      doc = readFileIntoDoc(docFile);
      doc.setPairsArray(pairs[row]);
      doc.setFileName(name);
      origText = SentenceResults.readOrigFile(origFile);
    }
    catch (FileNotFoundException fnfe) {
      JOptionPane.showMessageDialog(this, "File Not Found while reading data",
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
      SentenceResults.outputToErrorFile(fnfe);
      return;
    }
    catch (ClassNotFoundException cnfe) {
      // shouldn't happen
      SentenceResults.outputToErrorFile(cnfe);
      return;
    }
    catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, "IOException while reading data",
                                    "ERROR", JOptionPane.ERROR_MESSAGE);
      SentenceResults.outputToErrorFile(ioe);
      return;
    }
    String[] similarFiles = new String[mostSimilar[row].length];
    for (int i = 0; i < mostSimilar[row].length; i++) {
      similarFiles[i] = mostSimilar[row][i].name + " " +
          (int) mostSimilar[row][i].val + "%";
    }

    SentenceResults sr = new SentenceResults(this, doc, origText,
                                             similarFiles);
  }

  /**
   * Returns all the links between sentences for this dataset.
   * @param task The FilterTask which is controlling this method.
   * @return An array of SentencePair arrays.
   */
  private SentencePair[][] loadPairs(FilterTask task) {
    if (pairFiles == null || pairFiles.length == 0) {
      return new SentencePair[0][0];
    }
    SentencePair[][] temp = new SentencePair[pairFiles.length][];

    // load the pairs into the array.
    for (int i = 0; !task.cancelled && i < temp.length; i++) {
      try {
        temp[i] = readFileIntoPair(pairFiles[i]);
        task.currentStatus++;
      }
      catch (FileNotFoundException fnfe) {
        // shouldn't happen
        fnfe.printStackTrace();
      }
      catch (ClassNotFoundException cnfe) {
        // shouldn't happen
        cnfe.printStackTrace();
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(this, "IOException while reading results",
                                      "ERROR", JOptionPane.ERROR_MESSAGE);
        SentenceResults.outputToErrorFile(ioe);
      }
    }

    return temp;
  }

  /**
   * UnIgnores all similarities. Used to reset them before filtering begins.
   */
  private void unIgnoreEverything() {
    for (int i = 0; i < pairs.length; i++) {
      for (int j = 0; j < pairs[i].length; j++) {
        pairs[i][j].unIgnoreAllScores();
      }
    }
  }

  /**
   * <P>This method attempts to filter meaningless results. These results are
       * classed as being every sentence which is linked to a lot of other sentences
   * since these are likely to be common phrases needed within this piece of
   * work. Another way of filtering is assigning a multiplier to be applied to
   * the score for each sentence. This multiplier is calculated by finding the
   * document with the highest percentage of links (p) from this document and
       * multiplying the overall score by p/100. This is useful as most peer-to-peer
   * copying is done in groups that are very small, to avoid detection.
   * Documents which have low percentages are therefore likely to just be using
   * common phrases while a higher percentage indicates plagiarism.
   * <P>A new option added is to make the multiplier be 1+(p/100), which will
   * help catch people who have high scores without being similar to just one
   * other document. These people are likely to have plagiarised common sources
   * if they are not good at referencing their work.
   *
   * @param task The thread object that controls this method. Used to keep
   * track of progress for the ProgressMonitor.
   * @param filter Whether a full filter should be done, or just the score
   * multiplier.
   */
  private void filterPairs(FilterTask task, boolean filter) {
    // The multiplier for the individual score.
    mults = new double[pairs.length];
    // The most similar files to an individual file.
    mostSimilar = new ValNamePair[pairs.length][];
    int reciprocated = 0;
    int ignored = 0;
    Hashtable[] totals = new Hashtable[pairs.length];
    int[] runningTotal = new int[pairs.length];

    for (int i = 0; !task.cancelled && filter && i < pairs.length; i++) {
      for (int j = 0; !task.cancelled && j < pairs[i].length; j++) {
        /* when there are too many links from the sentence it indicates
         * that it is probably too common amongst this set of work to be
         * taken as a proper indication of plagiarism. */
        if (pairs[i][j].scores.size() > MAX_LINKS) {
          // ignore all scores in this sentence; it is too common according to
          // the settings.
          // for each score, must also disable the reciprocating score in
          // the file it links to.
          for (int k = 0; k < pairs[i][j].scores.size(); k++) {
            SentenceScore temp = (SentenceScore) pairs[i][j].scores.get(k);
            if (!temp.isIgnored()) {
              SentencePair[] tempPairs = getPairsForName(temp.name);

              if (tempPairs.length == 0 && DEBUG) {
                JOptionPane.showMessageDialog(this, "No pair for " + temp.name);

              }
              for (int x = 0; x < tempPairs.length; x++) {
                if (tempPairs[x].id == temp.id) {
                  tempPairs[x].setIgnored(pairs[i][j].name, pairs[i][j].id,
                                          true);

                  if (DEBUG) {
                    reciprocated++;

                  }
                }
              }
            }
          }
          if (DEBUG) {
            ignored += pairs[i][j].getNumNotIgnored();

          }
          pairs[i][j].ignoreAllScores();

        } //if(pairs[i][j].scores...
        if (DEBUG) {
          System.out.println("IGNORED:" + ignored + ", RECIPROCATED:" +
                             reciprocated);
        }
      } //for(int j...
      task.currentStatus++;
    } // for(int i...
    if (task.cancelled) {
      return;
    }

    for (int i = 0; i < pairs.length; i++) {
      totals[i] = new Hashtable();
      runningTotal[i] = 0;
      for (int j = 0; j < pairs[i].length; j++) {
        /* Now calculate some percentage for each document. The totals
         * hashtable is filled by totalling the scores for each document linked
         * to from this document. The highest total is then converted to a
         * percentage of the combined total and used as a multiplier for the
         * score */
        for (int k = 0; k < pairs[i][j].scores.size(); k++) {
          SentenceScore temp = (SentenceScore) pairs[i][j].scores.get(k);
          if (!temp.isIgnored()) {
            Integer totalScore = (Integer) totals[i].get(temp.name);
            if (totalScore == null) {
              totalScore = new Integer(0);

            }
            totalScore = new Integer(totalScore.intValue() + temp.score);
            totals[i].put(temp.name, totalScore);
            runningTotal[i] += temp.score;
          }
        }
      }
      // Can now calculate the multiplier
      // The highest matches are kept and used to display which documents are
      // most similar to this one.
      Enumeration keys = totals[i].keys();
      ValNamePair[] high = new ValNamePair[NUM_SIMILAR];
      for (int j = 0; j < NUM_SIMILAR; j++) {
        high[j] = new ValNamePair(0.0, "");

      }
      while (keys.hasMoreElements()) {
        String next = (String) keys.nextElement();
        Integer val = (Integer) totals[i].get(next);
        if (val.intValue() > high[NUM_SIMILAR - 1].val) {
          high[NUM_SIMILAR - 1].val = val.intValue();
          high[NUM_SIMILAR - 1].name = next;
          Arrays.sort(high);
        }

      }
      double multiplier;
      double max = high[0].val;
      if (runningTotal[i] != 0 && max != 0) {
        multiplier = max / (double) runningTotal[i];
      }
      else {
        multiplier = 1.0;

      }
      for (int j = 0; j < NUM_SIMILAR; j++) {
        high[j].val = (high[j].val / ( (double) runningTotal[i]) * 100);
      }

      mults[i] = multiplier + (GROUP_PAIRS ? 0.0 : 1.0);
      mostSimilar[i] = high;
      //task.currentStatus++;
    }
  }

  /**
   * Stores some plagiarism score with the name of the document it is the score
   * for. Used as a convenient way to sort results for the table.
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  private class ValNamePair
      implements Comparable {
    double val;
    String name;
    ValNamePair(double val, String name) {
      this.val = val;
      this.name = name;
    }

    public int compareTo(Object o) {
      ValNamePair compare = (ValNamePair) o;
      if (val < compare.val) {
        return 1;
      }
      if (val == compare.val) {
        return 0;
      }
      else {
        return -1;
      }
    }
  }

  /**
   * Asks the user if they would like to save any changes they have made to
   * the results.
   * @return The reply from the JOptionPane confirmation dialogue. Will be one
   * of JOptionPane.{YES_OPTION, NO_OPTION, CANCEL_OPTION}.
   */
  private int saveChanges() {
    if (!isChanged()) {
      return JOptionPane.NO_OPTION;
    }
    int reply = JOptionPane.showConfirmDialog(this, "Save changes to marking?" +
        "\nThis will save or revert all changes and close\nall results windows",
        "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
    if (reply == JOptionPane.CANCEL_OPTION) {
      return reply;
    }

    if (reply == JOptionPane.NO_OPTION) {
      return reply;
    }

    // reaching this point means the user wants to save the changes.
    saveChangedPairs();
    return reply;
  }

  /**
   * Looks through the 2d array of SentencePair objects and saves back the rows
   * that have a changed SentencePair within them. For example, if pairs[3] had
   * an object changed at pairs[3][7] then the whole of pairs[3] would need
   * to be saved back to disk in the correct file.
   */
  private void saveChangedPairs() {
    for (int i = 0; i < pairs.length; i++) {

      boolean shouldSave = false;
      for (int j = 0; j < pairs[i].length; j++) {
        if (pairs[i][j].isChanged()) {
          // remember to save this row.
          shouldSave = true;
          // set the changed flag to false so that it will register changes made
          // in future edits.
          pairs[i][j].setChanged(false);
        } //if(pairs[i][j].isChanged())
      } // for(int j= 0;...

      if (shouldSave) {
        File file = new File(Settings.getSourceDirectory() + File.separator
                             + Settings.getSherlockSettings().
                             getMatchDirectory(), pairs[i][0].name + "pairs." +
                             Settings.getFileTypes()[Settings.SEN].
                             getExtension());
        try {
          Comparison.writeObjToFileStatic(pairs[i], file);
        }
        catch (FileNotFoundException fnfe) {
          SentenceResults.outputToErrorFile(fnfe);
          JOptionPane.showMessageDialog(this, "File Not Found: " +
                                        file.getName(), "File Not Found",
                                        JOptionPane.ERROR_MESSAGE);
        }
        catch (IOException ex) {
          SentenceResults.outputToErrorFile(ex);
          JOptionPane.showMessageDialog(this, "Output Error: " +
                                        file.getName(), "File Not Found",
                                        JOptionPane.ERROR_MESSAGE);
        }
        shouldSave = false;
      } // if(shouldSave)...

    } // for(int i = 0;...
  }

  /**
   * If some set of pairs within this result set has been changed.
   * @return true if changes have been made to the results.
   */
  private boolean isChanged() {
    for (int i = 0; i < pairs.length; i++) {
      for (int j = 0; j < pairs[i].length; j++) {
        if (pairs[i][j].isChanged()) {
          return true;
        }
      }
    }
    return false;
  }

  private SentencePair[] readFileIntoPair(File file) throws
      FileNotFoundException,
      IOException, ClassNotFoundException {
    return (SentencePair[]) readFileIntoObject(file);
  }

  private Document readFileIntoDoc(File file) throws FileNotFoundException,
      IOException, ClassNotFoundException {
    return (Document) readFileIntoObject(file);
  }

  /**
   * Given a serialised file, this will return an Object constructed from that
   * file.
   * @param file The file to be read.
   * @return An Object, which can then be cast to the required class.
   * @throws FileNotFoundException If the file is not found.
   * @throws IOException If an IO error occurs.
   * @throws ClassNotFoundException If the class is not found.
   */
  private Object readFileIntoObject(File file) throws FileNotFoundException,
      IOException, ClassNotFoundException {
    FileInputStream in = new FileInputStream(file);
    ObjectInputStream docIn = new ObjectInputStream(in);
    Object ret = docIn.readObject();
    docIn.close();
    in.close();

    return ret;
  }

  /**
   * <P>This class is simply a filter that only allows files ending in
   * "pairs.sen"</P>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Ben Hart
   * @author Mike Joy
   * @author Daniel White
   * @version 4
   */
  private class PairFilenameFilter
      implements FilenameFilter {
    String extension = "pairs." +
        Settings.getFileTypes()[Settings.SEN].getExtension();
    PairFilenameFilter() {
    } // MatchFilenameFilter

    /**
     * File is accepted if it ends with the pairs.sen suffix.
     *
     * @param dir - the directory in which the file was found.
     * @param name - the name of the file.
     * @return true if the file ends in the correct extension. False otherwise.
     */
    public boolean accept(File dir, String name) {

      if (name.endsWith(extension)) {
        return true;
      }
      else {
        return false;
      }
    } // accept

  }

  /**
   *
   * <p>Makes sure that none of the cells in the table can be edited.</p>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   *
   * @author Daniel White
   * @version 4
   */
  private class ResultsTableModel
      extends DefaultTableModel {
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }
}
