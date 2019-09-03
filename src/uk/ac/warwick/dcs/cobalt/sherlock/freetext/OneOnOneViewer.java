package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * <p>A viewer for examining a pair of files without consideration of the
 * similarities they may have to other documents.</p>
 * <p>Title: Sherlock 2003</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */

public class OneOnOneViewer
    extends JDialog {
  private MyTextPane left, right;
  private HTMLOutAction htmlExport = new HTMLOutAction(this);
  private boolean changed = false;

  private JSplitPane split;

  /**
   * Constructor.
   * @param owner The owner of this viewer
   * @param leftStyled The document to display on the left.
   * @param rightStyled The document to display on the right.
   */
  public OneOnOneViewer(Dialog owner, MyStyledDocument leftStyled,
                        MyStyledDocument rightStyled) {
    this(owner, leftStyled, rightStyled.getDocument(),
         rightStyled.getOriginalText());
  }

  /**
   * Constructor.
   * @param owner The owner of this viewer.
   * @param leftStyled The Document to display on the left.
   * @param rightDoc The parsed Document which will be displayed on the right.
   * @param rightOrig The original text for the RHS document.
   */
  public OneOnOneViewer(Dialog owner, MyStyledDocument leftStyled,
                        Document rightDoc, String[] rightOrig) {
    super(owner, "", true);

    Document leftDoc = leftStyled.getDocument();
    String[] leftOrig = leftStyled.getOriginalText();

    MyStyledDocument newLeft =
        prepareDocument(leftDoc, leftOrig, rightDoc.getFileName());
    MyStyledDocument newRight =
        prepareDocument(rightDoc, rightOrig, leftDoc.getFileName());

    left = new MyTextPane(newLeft, leftDoc.getFileName());
    left.setEditable(false);
    left.setCaretPosition(0);
    left.setMargin(new Insets(5, 5, 5, 5));
    left.addMouseListener(new MyMouseListener(left, true));

    right = new MyTextPane(newRight, rightDoc.getFileName());
    right.setEditable(false);
    right.setCaretPosition(0);
    right.setMargin(new Insets(5, 5, 5, 5));
    right.addMouseListener(new MyMouseListener(right, false));

    setTitle("Comparing " + left.getStyledDoc().getDocument().getFileName() +
             " to " + right.getStyledDoc().getDocument().getFileName());
    // gui.addWindow(this);

    prepareScreen();

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        notifyChanges();
        //closeMe();
      }
    });

    split.setResizeWeight(0.5);
    pack();
    setVisible(true);

  }

  /**
   * Notifies the original pairs array if any changes were made.
   */
  private void notifyChanges() {
    SentencePair[] origPairs = GroupResults.getPairsForName(
        left.getStyledDoc().getDocument().getFileName());
    SentencePair[] changedPairs =
        left.getStyledDoc().getDocument().getPairsArray();
    for (int i = 0; i < changedPairs.length; i++) {
      if (changedPairs[i].isChanged()) {
        changed = true;
        origPairs[i].setChanged(true);
      }
    }
    origPairs = GroupResults.getPairsForName(
        right.getStyledDoc().getDocument().getFileName());
    changedPairs =
        right.getStyledDoc().getDocument().getPairsArray();
    for (int i = 0; i < changedPairs.length; i++) {
      if (changedPairs[i].isChanged()) {
        changed = true;
        origPairs[i].setChanged(true);
      }
    }
  }

  /**
   * Constructs a MyStyledDocument which will only display links between the
   * two desired document.
   * @param doc The parsed document object.
   * @param origText The original text of the document.
   * @param compareToName The file it is being compared to.
   * @return The desired MyStyledDocument.
   */
  private MyStyledDocument prepareDocument(Document doc, String[] origText,
                                           String compareToName) {
    SentencePair[] origPairs = GroupResults.getPairsForName(doc.getFileName());
    SentencePair[] displayedPairs = new SentencePair[origPairs.length];

    // now have a new sentencepair array to put a subset of the original into.
    for (int i = 0; i < origPairs.length; i++) {
      displayedPairs[i] = new SentencePair( (Sentence) doc.getSentences().get(i));
      Vector scores = origPairs[i].scores;
      for (int j = 0; j < scores.size(); j++) {
        // only display scores which link to the given name.
        SentenceScore temp = (SentenceScore) scores.get(j);
        if (temp.name.equals(compareToName)) {
          displayedPairs[i].addSimilarity( (SentenceScore) temp.clone());
        }
      } //for(int j=...
    } //for(int i=...

    Document newDoc = (Document) doc.clone();
    filterPairs(displayedPairs);
    newDoc.setPairsArray(displayedPairs);

    return new MyStyledDocument(newDoc, origText);
  }

  /**
   * Ignore any pairs that were ignored by the original filtering.
   * All objects in the array should be cloned, therefore it doesn't matter
   * if the filtering changes as it won't affect the master copy...
   * @param pairs A pairs array for one of the documents.
   */
  private void filterPairs(SentencePair[] pairs) {
    int maxLinks = GroupResults.MAX_LINKS;
    for (int i = 0; i < pairs.length; i++) {
      // Display all links, unless there are a lot between the two documents.
      /*if(pairs[i].scores.size() > maxLinks){
        pairs[i].ignoreAllScores();
             } else {
        pairs[i].unIgnoreAllScores();
             }*/

      // This filter just ignores anything not picked up by the original filter.
      if (pairs[i].isIgnored()) {
        pairs[i].scores = new Vector();
        pairs[i].unIgnoreAllScores();
      }
    }
  }

  /**
   * Lays out the window.
   */
  private void prepareScreen() {
    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false,
                           new JScrollPane(left), new JScrollPane(right));
    getContentPane().add(split);

    JPanel top = new JPanel();
    Box box = Box.createHorizontalBox();
    JButton html = new JButton(htmlExport);
    box.add(html);
    box.add(Box.createHorizontalGlue());
    top.add(box);

    getContentPane().add(top, BorderLayout.NORTH);
  }

  /**
   * <p>Called by the HTML export button to write the results to a HTML file.
   * <p>Title: Sherlock 2003</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  private class HTMLOutAction
      extends AbstractAction {
    final String HTML_START = "<HTML>";
    final String HTML_END = "</HTML>";
    final String HEAD_START = "<HEAD><TITLE>";
    final String HEAD_END = "</TITLE></HEAD>";
    final String BODY_START = "<BODY BACKGROUND=\"#FFFFFF\">";
    final String BODY_END = "</BODY>";
    final String BR = "<BR>";
    final String A_END = "</A>";
    final String SUSPICOUS_FONT = "<FONT COLOR=\"#FF0000\">";
    final String IGNORED_FONT = "<FONT COLOR=\"#888888\">";
    final String FONT_END = "</FONT>";
    final String FRAMESET_END = "</FRAMESET>";
    final String TOP_LEFT = "topLeft";
    final String TOP_RIGHT = "topRight";
    final String BOT_LEFT = "botLeft";
    final String BOT_RIGHT = "botRight";

    private Dialog owner;

    /**
     * Constructor for the action.
     * @param owner The dialog the action is working inside.
     */
    HTMLOutAction(Dialog owner) {
      this.owner = owner;
      putValue(Action.NAME, "Export To HTML");
      //putValue(Action.SMALL_ICON, null);
      putValue(Action.SHORT_DESCRIPTION, "<HTML>Export to a HTML format.<br>" +
               "This can be viewed easily on other computers.");
      setEnabled(true);
    }

    /**
     * Called when the user wishes to export results to a HTML file.
     * @param e The ActionEvent (not used).
     */
    public void actionPerformed(ActionEvent e) {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setDialogTitle("Choose a directory to save results");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      int reply = chooser.showSaveDialog(owner);
      if (reply != JFileChooser.APPROVE_OPTION) {
        return;
      }

      final File dir = chooser.getSelectedFile();
      if (! (dir.canWrite() && dir.canRead())) {
        JOptionPane.showMessageDialog(owner, "You do not have permission" +
                                      "to access\nthis directory", "ERROR",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }

      Runnable r = new Runnable() {
        public void run() {
          doConversion(dir);
        }
      };

      Thread t = new Thread(r);
      t.start();
    }

    /**
     * Prepares the HTML files for output to the given directory.
     * @param dir The directory to write the files to.
     */
    private void doConversion(File dir) {

      String[] HTMLFiles = new String[5];
      HTMLFiles[0] = createFrameSet();
      HTMLFiles[1] = createTop(true);
      HTMLFiles[2] = createTop(false);
      HTMLFiles[3] = createBottom(true);
      HTMLFiles[4] = createBottom(false);

      File[] files = new File[5];
      files[0] = new File(dir, "index.html");
      files[1] = new File(dir, TOP_LEFT + ".html");
      files[2] = new File(dir, TOP_RIGHT + ".html");
      files[3] = new File(dir, left.getStyledDoc().getDocument().
                          getFileName() + ".html");
      files[4] = new File(dir, right.getStyledDoc().getDocument().
                          getFileName() + ".html");

      for (int i = 0; i < files.length; i++) {
        if (checkFile(files[i]) == JOptionPane.NO_OPTION) {
          return;
        }
      }
      boolean success = true;
      for (int i = 0; i < files.length; i++) {
        try {
          writeString(HTMLFiles[i], files[i]);
        }
        catch (IOException ex) {
          SentenceResults.outputToErrorFile(ex);
          success = false;
        }
      }

      String message = success ? "Files successfully written!" :
          "There were problems writing the files,\n" +
          "please see the log file for details";
      JOptionPane.showMessageDialog(owner, message, "Finished",
                                    JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Asks the use if they wish to over-write files when they exist already.
     * @param file The file to check.
     * @return The reply from the dialog. Will always return yes if the file
     * does not exist.
     */
    private int checkFile(File file) {
      if (file.exists()) {
        int ans = JOptionPane.showConfirmDialog(owner, "The file " +
                                                file.getName() +
            " exists!\nDo you want to overwrite it?",
            "Overwrite File?",
            JOptionPane.YES_NO_OPTION);
        return ans;
      }
      return JOptionPane.YES_OPTION;
    }

    /**
     * Creates the frame set for the results.
     * @return A string containing all the HTML for the frame set.
     */
    private String createFrameSet() {
      StringBuffer file = new StringBuffer();
      file.append(HTML_START);
      file.append(makeTitle(getTitle()) + "\n");
      file.append(makeFramesetCols("50%,50%") + "\n");
      file.append(makeFramesetRows("100,*") + "\n");
      file.append(makeFrame(TOP_LEFT + ".html", TOP_LEFT) + "\n");
      file.append(makeFrame(left.getStyledDoc().getDocument().
                            getFileName() + ".html", BOT_LEFT) + "\n");
      file.append(FRAMESET_END);
      file.append(makeFramesetRows("100,*") + "\n");
      file.append(makeFrame(TOP_RIGHT + ".html", TOP_RIGHT) + "\n");
      file.append(makeFrame(right.getStyledDoc().getDocument().
                            getFileName() + ".html", BOT_RIGHT) + "\n");
      file.append(FRAMESET_END);
      file.append(FRAMESET_END + "\n");
      file.append(HTML_END);

      return file.toString();
    }

    /**
     * Creates the top HTML pane.
     * @param leftPane true if the file is on the left.
     * @return The HTML.
     */
    private String createTop(boolean leftPane) {
      MyTextPane textPane = leftPane ? left : right;
      StringBuffer file = new StringBuffer();
      file.append(HTML_START + "\n");
      file.append(BODY_START + "\n");
      file.append("<H3>" + textPane.getStyledDoc().getDocument().getFileName() +
                  "</H3>");
      file.append(BODY_END + "\n");
      file.append(HTML_END + "\n");
      return file.toString();
    }

    /**
     * Creates the bottom row of pages. This will display the actual text.
     * @param leftPane Whether the desired file is on the left side of the
     * window.
     * @return The HTML for the page.
     */
    private String createBottom(boolean leftPane) {
      StringBuffer file = new StringBuffer();
      file.append(HTML_START + "\n");
      file.append(BODY_START + "\n");

      // Now do document.
      MyTextPane textPane = leftPane ? left : right;
      //String document = textPane.getText();
      Vector sentences = textPane.getStyledDoc().getDocument().getSentences();
      String[] origText = textPane.getStyledDoc().getOriginalText();
      int currentPos = 0;

      int coords[] = {
          0, 0, 0, 0};
      for (int i = 0; i < sentences.size(); i++) {
        Sentence sentence = (Sentence) sentences.get(i);
        // if this sentence needs different formatting
        if (sentence.getSentencePairs().isIgnored() ||
            sentence.getSentencePairs().scores.size() > 0) {
          // insert up to the sentence's start.
          coords[Sentence.END_LINE] =
              sentence.getCoords()[Sentence.START_LINE];
          coords[Sentence.END_COL] =
              sentence.getCoords()[Sentence.START_COL];
          String temp = constructString(origText, coords);
          file.append(makeNormal(temp));

          System.arraycopy(sentence.getCoords(), 0, coords, 0, coords.length);

          // insert up to the end of this sentence
          if (sentence.getSentencePairs().isIgnored()) {
            file.append(makeIgnored(constructString(origText, coords)));
          }
          else {
            String formatted = makeSuspicous(constructString(origText, coords));
            SentenceScore score =
                (SentenceScore) sentence.getSentencePairs().scores.get(0);
            String target = leftPane ? BOT_RIGHT : BOT_LEFT;
            file.append(makeAnchor(makeName(sentence.getId())));
            file.append(makeLink(score.name + ".html", makeName(score.id),
                                 target, formatted));
          }

          coords[Sentence.START_LINE] = coords[Sentence.END_LINE];
          coords[Sentence.START_COL] = coords[Sentence.END_COL];
        }
        // if this is the last sentence, insert the rest of the text.
        if (i == sentences.size() - 1) {
          coords[Sentence.END_LINE] = origText.length - 1;
          coords[Sentence.END_COL] = origText[origText.length - 1].length();

          file.append(makeNormal(constructString(origText, coords)));
        }
      }

      file.append(BODY_END + "\n");
      file.append(HTML_END + "\n");
      return file.toString();
    }

    /**
     * Returns the string between the given coordinates.
     * @param file The array of lines from the original file.
     * @param coords The coordinates for the begin and end of the desired
     * string.
     * @return The desired string.
     */
    private String constructString(String[] file, int[] coords) {
      StringBuffer out = new StringBuffer();
      int currentCol = coords[Sentence.START_COL];

      for (int i = coords[Sentence.START_LINE]; i < coords[Sentence.END_LINE];
           i++) {
        out.append(file[i].substring(currentCol));
        currentCol = 0;
      }
      if (coords[Sentence.END_COL] > file[coords[Sentence.END_LINE]].length()) {
        coords[Sentence.END_COL] = file[coords[Sentence.END_LINE]].length();

      }
      out.append(file[coords[Sentence.END_LINE]].substring(currentCol,
          coords[Sentence.END_COL]));
      return out.toString();
    }

    /**
     * Applies HTML formatting to a normal line of text.
     * @param text The line of text
     * @return The original string with new line characters replaced by
     * HTML line breaks
     */
    private String makeNormal(String text) {

      text = text.replaceAll("\n", "<BR>\n");

      return text;
    }

    private String makeFramesetCols(String colDef) {
      return "<FRAMESET cols=\"" + colDef + "\">";
    }

    private String makeFramesetRows(String rowDef) {
      return "<FRAMESET rows=\"" + rowDef + "\">";
    }

    private String makeFrame(String src, String name) {
      return "<FRAME src=\"" + src + "\" name=\"" + name + "\">";
    }

    private String makeTitle(String title) {
      return HEAD_START + title + HEAD_END;
    }

    private String makeAnchor(String name) {
      return "<A NAME=\"" + name + "\"><!-- -->" + A_END;
    }

    private String makeLink(String file, String anchor, String target,
                            String text) {
      return "<A HREF=\"" + file + (anchor.length() > 0 ? "#" + anchor : "") +
          "\" TARGET=\"" +
          target + "\">" + text + A_END;
    }

    private String makeName(int sentId) {
      return "Sentence" + sentId;
    }

    private String makeSuspicous(String text) {
      return SUSPICOUS_FONT + makeNormal(text) + FONT_END;
    }

    private String makeIgnored(String text) {
      return IGNORED_FONT + makeNormal(text) + FONT_END;
    }
  }

  /**
   * <p>A mouse listener for the text panes.</p>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  private class MyMouseListener
      extends MouseAdapter {
    MyTextPane parent;
    boolean left;

    /**
     * Constructor.
     * @param parent The pane to listen to.
     * @param left true iff this is the left-hand-side pane.
     */
    MyMouseListener(MyTextPane parent, boolean left) {
      this.parent = parent;
      this.left = left;
    }

    /**
     * Called when a user clicks the mouse. Displays the menus if required,
     * otherwise just scrolls the opposite window.
     * @param e The MouseEvent.
     */
    public void mouseClicked(MouseEvent e) {
      int loc = parent.viewToModel(e.getPoint());
      parent.setCaretPosition(loc);
      AttributeSet attrs = parent.getCharacterAttributes();
      int style = parent.getStyledDoc().getLineStyle(attrs);
      if (style == MyStyledDocument.DEFAULT_STYLE) {
        return;
      }

      Sentence sentence = parent.getStyledDoc().getSentence(loc);
      if (sentence == null) {
        return;
      }
      if (style == MyStyledDocument.HIGHLIGHTED ||
          style == MyStyledDocument.HIGHLIGHT_IGNORE) {
        if (e.getButton() == e.BUTTON1) {
          leftClick(sentence, e.getX(), e.getY());
        }
        else {
          //prepareLeftPopup(sentence);
        }
      }
    }

    void leftClick(Sentence sentence, int x, int y) {
      SentencePair pairs = sentence.getSentencePairs();
      if (pairs.scores.size() == 1) {
        SentenceScore score = (SentenceScore) pairs.scores.get(0);
        scrollToSentence(score.id, left);
        return;
      }
      else {
        parent.getPopup().removeAll();
        SentenceScore[] scoresArray =
            new SentenceScore[pairs.scores.size()];
        pairs.scores.toArray(scoresArray);

        // sorting causes the most suspicous sentences to be placed highest.
        Arrays.sort(scoresArray);

        // add the scores to the popup.
        for (int i = 0; i < scoresArray.length; i++) {
          if (!scoresArray[i].isIgnored()) {
            JMenuItem temp = new JMenuItem(scoresArray[i].name +
                                           " sentence " + scoresArray[i].id);
            temp.addActionListener(new LeftActionListener(scoresArray[i].id,
                left));
            //temp.addActionListener(new LeftPopupActionListener(doc.getDocument(),
            //    scoresArray[i].name, scoresArray[i].id, tabPane));
            parent.getPopup().add(temp);
          }
        }

        parent.getPopup().show(parent, x, y);
      }
    } // leftClick()
  }

  /**
   * <p>An action listener for left-click menus.</p>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  private class LeftActionListener
      implements ActionListener {
    int id;
    boolean left;
    LeftActionListener(int id, boolean left) {
      this.id = id;
      this.left = left;
    }

    public void actionPerformed(ActionEvent e) {
      scrollToSentence(id, left);
    }
  }

  /**
   * Scrolls the opposite pane to the requested sentence.
   * @param id The id of the requested sentence
   * @param leftPane Which pane is calling this method?
   */
  private void scrollToSentence(int id, boolean leftPane) {
    MyTextPane textPane = leftPane ? right : left;

    Document doc = textPane.getStyledDoc().getDocument();
    Sentence sentence = (Sentence) doc.getSentences().get(id);
    MyStyledDocument styled = textPane.getStyledDoc();
    SentenceLocation locater = styled.findLocater(sentence);
    Rectangle start, end;
    try {
      start = textPane.modelToView(locater.startLoc);
      end = textPane.modelToView(locater.endLoc);
    }
    catch (BadLocationException ex) {
      return;
    }

    JScrollPane scroller = !leftPane ? (JScrollPane) split.getLeftComponent() :
        (JScrollPane) split.getRightComponent();

    int height = scroller.getViewport().getViewSize().height;
    int divider = 2;
    int rectHeight = Math.abs(start.y - end.y) + height / divider;

    /**
     * HACK! Dunno if this is the correct way or not, but it works. Scrolls
     * the window to the location of the sentence.
     */
    while (rectHeight > height) {
      rectHeight = Math.abs(start.y - end.y) + height / ++divider;

    }
    Rectangle r = new Rectangle(Math.min(start.x, end.x),
                                Math.min(start.y, end.y),
                                Math.abs(start.x - end.x),
                                height);

    if (!scroller.getViewport().getViewRect().contains(r)) {
      scroller.getViewport().setViewPosition(new Point(0, 0));
      scroller.getViewport().scrollRectToVisible(r);
    }

    textPane.alterFormat(styled.findLocater(sentence),
                         styled.getAttribute(MyStyledDocument.HIGHLIGHTED),
                         true,
                         styled.getAttribute(MyStyledDocument.SUSPICOUS));
  }

  /**
   * Whether the data has been changed.
   * @return true if it has.
   */
  public boolean isChanged() {
    return changed;
  }

  /**
   * Write the string to the given file. Will overwrite whatever was there.
   * @param str The string to store in the file.
   * @param file The output file.
   * @throws IOException If an IO error occured.
   */
  public static void writeString(String str, File file) throws IOException {
    BufferedWriter out = new BufferedWriter(new FileWriter(file));
    out.write(str);
    out.flush();
    out.close();
  }
}