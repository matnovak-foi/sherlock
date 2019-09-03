/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import uk.ac.warwick.dcs.cobalt.sherlock.*;

/**
 * <p>Visualization window. Displays results from the comparison in a user-
 * friendly form that is easier to understand and helps the user determine
 * if the submission should be treated as plagiarism.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */

public class SentenceResults
    extends JDialog {
  private static SimpleAttributeSet attrs[] = MyStyledDocument.getAttributes();
  private JSplitPane split;
  private MyTextPane mainEdit;
  private JTabbedPane compareDocPane;
  private Frame owner;

  private MyStyledDocument mainDoc;

  private String[] similarFiles;

  private boolean changed = false;

  /**
   * Displays the window.
   * @param owner The owner of this dialog.
   * @param doc The document object which will be displayed on the left.
   * @param origText The original file the document object was created from.
       * @param similarFiles The list of the top 5 files this document is similar to
   * Each line in the array should represent a line in the original file.
   */
  public SentenceResults(Frame owner, Document doc, String[] origText,
                         String[] similarFiles) {
    super(owner, "Free-Text Results for " + doc.getFileName(), true);
    this.similarFiles = similarFiles;
    this.owner = owner;

    //gui.addWindow(this);

    mainDoc = new MyStyledDocument(doc, origText);
    mainEdit = new MyTextPane(mainDoc, doc.getFileName());

    mainEdit.setEditable(false);
    mainEdit.setCaretPosition(0);
    mainEdit.setMargin(new Insets(5, 5, 5, 5));

    JScrollPane scroller = new JScrollPane(mainEdit);

    compareDocPane = new JTabbedPane(JTabbedPane.TOP,
                                     JTabbedPane.WRAP_TAB_LAYOUT);
    compareDocPane.setPreferredSize(new Dimension(350, 550));
    split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scroller,
                           compareDocPane);
    split.setResizeWeight(0.5);
    getContentPane().add(split, BorderLayout.CENTER);

    mainEdit.addMouseListener(new MyMouseListener(mainEdit, compareDocPane));

    JPanel tools = new JPanel();
    Box box = Box.createHorizontalBox();
    box.add(Box.createGlue());

    JLabel lbl = new JLabel("Most Similar Files:");
    box.add(lbl);

    final JComboBox cmbSimilar = new JComboBox(similarFiles);
    box.add(cmbSimilar);

    JButton btnGo = new JButton("1-on-1 comparison");
    btnGo.setToolTipText("Compare the two files, ignoring all other " +
                         "similarities");
    btnGo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String text = cmbSimilar.getSelectedItem().toString();
        text = text.substring(0, text.lastIndexOf(" "));
        openOneOnOne(text);
      }
    });
    box.add(btnGo);
    tools.add(box, BorderLayout.EAST);

    getContentPane().add(tools, BorderLayout.NORTH);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        //closeMe();
      }
    });
    pack();
    setVisible(true);

  }

  private void openOneOnOne(String name) {
    int tab = 0;
    // Check if document already loaded.
    if ( (tab = compareDocPane.indexOfTab(name)) != -1) {
      JScrollPane scroller = (JScrollPane) compareDocPane.getComponentAt(tab);
      MyStyledDocument right =
          ( (MyTextPane) scroller.getViewport().getView()).getStyledDoc();
      OneOnOneViewer newView = new OneOnOneViewer(this, mainDoc, right);
      if (newView.isChanged()) {
        reload();
      }
      return;
    }

    // Document not open. Read it in.
    Document newDoc;
    String[] newText;
    try {
      newDoc = readDoc(name);
      newText = readOriginal(name);
    }
    catch (FileNotFoundException fnfe) {
      JOptionPane.showMessageDialog(this, "File Not Found: " +
                                    name, "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
      outputToErrorFile(fnfe);
      return;
    }
    catch (ClassNotFoundException cnfe) {
      JOptionPane.showMessageDialog(this, "Class Not Found in File: " +
                                    name, "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
      outputToErrorFile(cnfe);
      return;
    }
    catch (IOException ioe) {
      JOptionPane.showMessageDialog(this, "IO Exception from " +
                                    name, "ERROR",
                                    JOptionPane.ERROR_MESSAGE);
      outputToErrorFile(ioe);
      return;
    }

    OneOnOneViewer newView = new OneOnOneViewer(this, mainDoc, newDoc, newText);
    if (newView.isChanged()) {
      reload();

    }
  }

  private void reload() {
    dispose();
    new SentenceResults(owner, mainDoc.getDocument(), mainDoc.getOriginalText(),
                        similarFiles);
  }

  static Document readDoc(String name) throws FileNotFoundException,
      IOException, ClassNotFoundException {
    FileTypeProfile profile = (Settings.getFileTypes())[Settings.SEN];
    String ext = profile.getExtension();
    File sourceDir = Settings.getSourceDirectory();
    String subDir = profile.getDirectory();

    Document retDoc = Comparison.readFileIntoDocStatic(new File(sourceDir +
        File.separator + subDir, name + "." + ext));
    retDoc.setFileName(name);
    return retDoc;
  }

  static String[] readOriginal(String name) throws FileNotFoundException,
      IOException {
    FileTypeProfile profile = (Settings.getFileTypes())[Settings.ORI];
    String origExt = profile.getExtension();
    String origSubDir = profile.getDirectory();
    File sourceDir = Settings.getSourceDirectory();
    String[] retText = readOrigFile(new File(sourceDir + File.separator +
                                             origSubDir, name + "." + origExt));
    return retText;
  }

  /**
   * Outputs an exception to the log file.
   * @param e The exception that has been thrown.
   */
  static void outputToErrorFile(Exception e) {
    String msg = e.getMessage();
    Date day = new Date(System.currentTimeMillis());
    try {
      String file = new String
          (System.getProperty("user.home") + File.separator
           + "sherlock.log");
      BufferedWriter out = new BufferedWriter
          (new FileWriter(file, true));
      out.write(day + "-" + msg);
      out.newLine();
      out.close();
    }
    catch (IOException ioe) {
      System.err.println("Cannot write to log file.\n" + msg);
    }
  }

  /**
   * Reads the original file from disk into a string array.
   * @param file The file to be read.
   * @return A String array with each entry containing one line from the
   * original file.
   * @throws FileNotFoundException If the file could not be found.
   * @throws IOException If some other IO error occured.
   */
  static String[] readOrigFile(File file) throws FileNotFoundException,
      IOException {
    BufferedReader read = new BufferedReader(new FileReader(file));
    Vector lines = new Vector();
    String line = read.readLine();
    while (line != null) {
      lines.add(line + "\n");
      line = read.readLine();
    }
    String[] retArray = new String[lines.size()];
    return (String[]) lines.toArray(retArray);
  }

  /**
   * <p>MouseListener for the pane on the left of the window. Deals with
   * popup menus when user clicks on a suspicous sentence.</p>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  class MyMouseListener
      extends MouseAdapter {
    // the component being listened to.
    private MyTextPane textPane;
    // the popup menu to be displayed.
    private JPopupMenu popup;
    // The formatted form of the file.
    private MyStyledDocument doc;
    // The tabbed pane which will display files which are similar to the main
    // one.
    private JTabbedPane tabPane;

    /**
     * Creates the MouseListener.
     * @param textPane The text pane the object will be listening to.
     * @param tabPane The tabbed pane where new documents can be opened.
     */
    public MyMouseListener(MyTextPane textPane, JTabbedPane tabPane) {
      this.textPane = textPane;
      popup = textPane.getPopup();
      doc = textPane.getStyledDoc();
      this.tabPane = tabPane;
    }

    /**
     * Deals with displaying a popup menu which links to suspicous sentences
         * in other documents. Popup will only display if the sentence is suspicous.
     * @param e The Event caused by the mouse click.
     */
    public void mouseClicked(MouseEvent e) {
      int loc = textPane.viewToModel(e.getPoint());
      textPane.setCaretPosition(loc);
      AttributeSet attrs = textPane.getCharacterAttributes();
      int style = doc.getLineStyle(attrs);
      if (style == MyStyledDocument.DEFAULT_STYLE) {
        return;
      }

      // if we get here, will need to display a popup menu.
      Sentence sentence = doc.getSentence(loc);
      if (sentence == null) {
        return;
      }
      if (style == MyStyledDocument.HIGHLIGHTED ||
          style == MyStyledDocument.HIGHLIGHT_IGNORE) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          prepareLeftPopup(sentence);
        }
        else {
          prepareRightPopup(sentence);
        }
        popup.show(textPane, e.getX(), e.getY());
      }
    }

    /**
     * Prepares the popup menu caused by a right button click.
     * @param sentence The sentence which the mouse was clicked over.
     */
    private void prepareLeftPopup(Sentence sentence) {
      // clear all old menu items.
      popup.removeAll();
      SentencePair pairs = sentence.getSentencePairs();

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
          temp.addActionListener(new LeftPopupActionListener(doc.getDocument(),
              scoresArray[i].name, scoresArray[i].id, tabPane));
          popup.add(temp);
        }
      }
    }

    private void prepareRightPopup(final Sentence sentence) {
      // clear all old menu items.
      popup.removeAll();
      SentencePair pairs = sentence.getSentencePairs();

      final SentenceScore[] scoresArray =
          new SentenceScore[pairs.scores.size()];
      pairs.scores.toArray(scoresArray);

      // sorting causes the most suspicous sentences to be placed highest.
      Arrays.sort(scoresArray);

      // add the two header items
      JMenuItem temp = new JMenuItem("Ignore all related sentences");
      temp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          changeAllScoresIgnore(sentence, scoresArray, true);
        }
      });
      if (pairs.isIgnored()) {
        temp.setEnabled(false);
      }
      popup.add(temp);

      temp = new JMenuItem("Un-ignore all related sentences");
      temp.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          changeAllScoresIgnore(sentence, scoresArray, false);
        }
      });
      if (!pairs.someSentenceIgnored()) {
        temp.setEnabled(false);
      }
      popup.add(temp);

      popup.addSeparator();

      for (int i = 0; i < scoresArray.length; i++) {
        String ignored = !scoresArray[i].isIgnored() ? "Ignore " : "Un-ignore ";
        temp = new JMenuItem(ignored + scoresArray[i].name + " sentence " +
                             scoresArray[i].id);
        temp.addActionListener(new RightPopupActionListener(sentence,
            scoresArray[i], textPane));
        popup.add(temp);

      }
    }

    private void changeAllScoresIgnore(Sentence sentence,
                                       SentenceScore[] scores,
                                       boolean val) {
      SentencePair pairs = sentence.getSentencePairs();
      // change ignored flags.
      if (val) {
        pairs.ignoreAllScores();
      }
      else {
        pairs.unIgnoreAllScores();

      }
      MyStyledDocument styled = textPane.getStyledDoc();

      // change formatting to either ignored style or suspicous style
      alterSentenceFormat(textPane, sentence, val, true, false);

      // Go through the SentenceScore objects and change all the values in the
      // linked file to false as well.

      for (int i = 0; i < scores.length; i++) {
        SentencePair linkedTo =
            GroupResults.getPairsForName(scores[i].name)[scores[i].id];
        linkedTo.setIgnored(sentence.getFileName(), sentence.getId(), val);

        // now we may need to alter formatting if the linked document is being
        // displayed
        int tab = -1;
        if ( (tab = tabPane.indexOfTab(scores[i].name)) != -1) {
          JScrollPane scroller = (JScrollPane) tabPane.getComponentAt(tab);
          MyTextPane tempPane = (MyTextPane) scroller.getViewport().getView();
          styled = tempPane.getStyledDoc();
          Document tempDoc = styled.getDocument();
          Sentence tempSent =
              (Sentence) tempDoc.getSentences().get(scores[i].id);
          boolean permanent = (tempPane.lastPermanent == null);
          if (!permanent) {
            permanent =
                tempPane.lastPermanent.equals(styled.findLocater(tempSent));
          }

          alterSentenceFormat(tempPane, tempSent, linkedTo.isIgnored(),
                              false, permanent);
        }

      }
      changed = true;

    }

  }

  class RightPopupActionListener
      implements ActionListener {
    private SentenceScore score;
    private Sentence sentence;
    private MyTextPane textPane;
    private SentenceLocation locater;

    /**
     * Makes an ActionListener which will switch the link in this sentence and
     * the linked to sentence to its opposite value, so either from ignored
     * to unignored or vice-versa.
     * @param sentence The sentence this belongs to.
     * @param score The link to switch.
     * @param textPane The text pane this object is associated with.
     */
    public RightPopupActionListener(Sentence sentence,
                                    SentenceScore score,
                                    MyTextPane textPane) {
      this.score = score;
      this.sentence = sentence;
      this.textPane = textPane;
      locater = textPane.getStyledDoc().findLocater(sentence);
    }

    public void actionPerformed(ActionEvent e) {
      boolean val = !score.isIgnored();
      SentencePair pairs = sentence.getSentencePairs();
      pairs.setIgnored(score, val);

      // If all pairs are ignored
      if (pairs.isIgnored()) {
        alterSentenceFormat(textPane, sentence, val, true, false);
      }

      /* Need to change the state of the linked-to sentence as well. This
           * will require a change in formatting if the linked-to sentence is open*/
      SentencePair linkedToPair =
          GroupResults.getPairsForName(score.name)[score.id];
      linkedToPair.setIgnored(sentence.getFileName(), sentence.getId(), val);
      pairs.setChanged(true);

      int tab = -1;
      // If this document is open.
      if ( (tab = compareDocPane.indexOfTab(score.name)) != -1) {
        JScrollPane scroller = (JScrollPane) compareDocPane.getComponentAt(tab);
        MyTextPane tempPane = (MyTextPane) scroller.getViewport().getView();
        MyStyledDocument styled = tempPane.getStyledDoc();
        Document tempDoc = styled.getDocument();
        Sentence tempSent =
            (Sentence) tempDoc.getSentences().get(score.id);
        boolean permanent = (tempPane.lastPermanent == null);
        if (!permanent) {
          permanent =
              tempPane.lastPermanent.equals(styled.findLocater(tempSent));
        }

        alterSentenceFormat(tempPane, tempSent, linkedToPair.isIgnored(),
                            false, permanent);
        linkedToPair.setChanged(true);

      }
      changed = true;
    }
  }

  /**
   * Changes the formatting of a sentence within a document to the specified
   * value of ignored and the specified highlighted value. A true value of
   * ignore means the sentence will be changed to an ignored style, the reverse
   * also applies. A true value of highlighted means the sentence will start
   * in the highlighted version of that style, or as if the mouse were hovering
   * over it.
   * @param text The text pane to change.
   * @param sentence The sentence within the document.
   * @param ignore Whether to format the sentence as ignored.
   * @param highlight Whether to format the sentence as if the mouse were
   * hovering over it.
   * @param permanent Whether the change in format will be permanent. Only one
   * sentence in the display may have a format which does not change when the
   * mouse moves over it. This is used to denote the last sentence which the
   * user has selected to look at by clicking a link.
   */
  private void alterSentenceFormat(MyTextPane text, Sentence sentence,
                                   boolean ignore, boolean highlight,
                                   boolean permanent) {
    SimpleAttributeSet after, before;
    MyStyledDocument styled = text.getStyledDoc();
    if (ignore && !highlight) {
      after = MyStyledDocument.getAttribute(MyStyledDocument.HIGHLIGHT_IGNORE);
      before = MyStyledDocument.getAttribute(MyStyledDocument.IGNORED);
    }
    else if (ignore && highlight) {
      after = MyStyledDocument.getAttribute(MyStyledDocument.IGNORED);
      before = MyStyledDocument.getAttribute(MyStyledDocument.HIGHLIGHT_IGNORE);
    }
    else if (!ignore && highlight) {
      after = MyStyledDocument.getAttribute(MyStyledDocument.SUSPICOUS);
      before = MyStyledDocument.getAttribute(MyStyledDocument.HIGHLIGHTED);
    }
    else { //must be (!ignore && highlight)
      after = MyStyledDocument.getAttribute(MyStyledDocument.HIGHLIGHTED);
      before = MyStyledDocument.getAttribute(MyStyledDocument.SUSPICOUS);
    }
    text.alterFormat(styled.findLocater(sentence), after, permanent, before);
  }

  /**
   * <p>Causes the tabbed pane to display the file the user wishes to look at.
   * Also scrolls the pane to the line where the suspicous sentence starts.</p>
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * <p>Company: University of Warwick</p>
   * @author Daniel White
   * @version 4
   */
  class LeftPopupActionListener
      implements ActionListener {
    private Document origDoc;
    private String filename;
    private JTabbedPane parent;
    private MyTextPane textPane = null;
    private JScrollPane scroller = null;
    private int id;

    /**
         * Creates a listener for one of the popup menu links, on the left hand side
     * of the window.
     * @param origDoc The original document object that is being examined.
     * @param filename The name of the file this sentence links to.
     * @param id The id of the sentence this menu item links to.
     * @param parent The parent component, so that the listener can display
     * message dialogs.
     */
    LeftPopupActionListener(Document origDoc, String filename,
                            int id, JTabbedPane parent) {
      this.origDoc = origDoc;
      this.filename = filename;
      this.parent = parent;
      this.id = id;
      int tab = 0;
      if ( (tab = parent.indexOfTab(filename)) != -1) {
        scroller = (JScrollPane) parent.getComponentAt(tab);
        textPane = (MyTextPane) scroller.getViewport().getView();
      }
    }

    /**
     * When this is called, the listener will add a tab to the right hand pane
     * which will display the linked-to document.
     * @param e The ActionEvent object.
     */
    public void actionPerformed(ActionEvent e) {
      if (scroller != null) {
        parent.setSelectedComponent(scroller);
        scrollToSentence();
        return;
      }

      // reaching this point means the tab does not exist.
      MyStyledDocument styledDoc = prepareDocument();
      textPane = new MyTextPane(styledDoc, filename);
      textPane.setEditable(false);
      textPane.setCaretPosition(0);
      textPane.setMargin(new Insets(5, 5, 5, 5));
      textPane.addMouseListener(new RightHandListener(textPane, filename));
      scroller = new JScrollPane(textPane);
      parent.addTab(filename, scroller);
      parent.setSelectedComponent(scroller);

      /**
       * Bit of a hack but it sometimes freezes without this being here.
       * Don't have any idea why, but I think it's something to do with
       * the validate() method. If I call this myself it always freezes at this
       * point!
       */
      Thread t = new Thread() {
        public void run() {
          try {
            sleep(100);
          }
          catch (InterruptedException e) {}
          scrollToSentence();
        }
      };
      t.start();
    }

    /**
     * Scrolls the viewport to the desired sentence.
     */
    private void scrollToSentence() {
      Document doc = textPane.getStyledDoc().getDocument();
      Sentence sentence = (Sentence) doc.getSentences().get(id);
      MyStyledDocument styled = textPane.getStyledDoc();
      SentenceLocation locater = styled.findLocater(sentence);
      Rectangle start, end;
      try {
        start = textPane.modelToView(locater.startLoc);
        end = textPane.modelToView(locater.endLoc);
        if (start == null || end == null) {
          return;
        }
      }
      catch (BadLocationException ex) {
        return;
      }
      int height = scroller.getViewport().getViewSize().height;
      int divider = 2;
      int rectHeight = Math.abs(start.y - end.y) + height / divider;

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
                           MyStyledDocument.getAttribute(MyStyledDocument.HIGHLIGHTED),
                           true,
                           MyStyledDocument.getAttribute(MyStyledDocument.SUSPICOUS));
    }

    /**
         * Takes the original document that is being examined by the SentenceResults
     * window and prepares a styled document which highlights the sentences
     * which are similar to sentences in the original document.
     * @return The MyStyledDocument that will be used on the right hand side of
     * the window.
     */
    MyStyledDocument prepareDocument() {

      Document retDoc = null;
      String[] retText = null;
      SentencePair[] storedPairs = null;

      try {
        retDoc = readDoc(filename);
        retText = readOriginal(filename);
        storedPairs = GroupResults.getPairsForName(filename);
        retDoc.setFileName(filename);
      }
      catch (FileNotFoundException fnfe) {
        JOptionPane.showMessageDialog(parent, "File Not Found: " +
                                      filename, "ERROR",
                                      JOptionPane.ERROR_MESSAGE);
        outputToErrorFile(fnfe);
        return null;
      }
      catch (ClassNotFoundException cnfe) {
        JOptionPane.showMessageDialog(parent, "Class Not Found in File: " +
                                      filename, "ERROR",
                                      JOptionPane.ERROR_MESSAGE);
        outputToErrorFile(cnfe);
        return null;
      }
      catch (IOException ioe) {
        JOptionPane.showMessageDialog(parent, "IO Exception from " +
                                      filename, "ERROR",
                                      JOptionPane.ERROR_MESSAGE);
        outputToErrorFile(ioe);
        return null;
      }

      // now we have done the input, need to create appropriate sentence pairs
      SentencePair[] origPairs = origDoc.getPairsArray();

      SentencePair[] retPairs = new SentencePair[retDoc.getSentences().size()];
      for (int i = 0; i < retPairs.length; i++) {
        Sentence temp = (Sentence) retDoc.getSentences().get(i);
        retPairs[i] = new SentencePair(temp);
      }

      String origName = origDoc.getFileName();
      // for each stored SentencePair
      for (int i = 0; i < storedPairs.length; i++) {
        // for each of the stored scores in that pair
        for (int j = 0; j < storedPairs[i].scores.size(); j++) {
          SentenceScore temp = (SentenceScore) storedPairs[i].scores.get(j);
          // if that score links to the main document
          if (temp.name.equals(origName)) {
            // add it to the pairs array for the displayed object.
            retPairs[i].addSimilarity(temp);
          }
        }
      }
      /*
             // for each of the sentence pairs from the original object
             for(int i = 0; i<origPairs.length; i++){
        // and for each of the scores within these pairs
        for(int j = 0; j<origPairs[i].scores.size(); j++){
          SentenceScore temp = (SentenceScore)origPairs[i].scores.get(j);
          // if that score links to the document we wish to create
          if(temp.name.equals(filename)){
            // add a reciprocating link in the new set of pairs.
            Sentence tempSent = (Sentence)origDoc.getSentences().get(i);
            retPairs[temp.id].addSimilarity(tempSent, temp.score,
                temp.isIgnored());
          }
        }
             }*/

      // These sentence pairs all relate the new document to the original one,
      // and not any others.
      retDoc.setPairsArray(retPairs);
      return new MyStyledDocument(retDoc, retText);
    }
  }

  class RightHandListener
      extends MouseAdapter {
    private MyTextPane pane;
    public RightHandListener(MyTextPane pane, final String name) {
      this.pane = pane;
      JPopupMenu popup = pane.getPopup();
      popup.removeAll();
      JMenuItem item = new JMenuItem("Compare One-on-One");
      item.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          openOneOnOne(name);
        }
      });
      popup.add(item);
    }

    public void mouseClicked(MouseEvent e) {
      pane.getPopup().show(pane, e.getX(), e.getY());
    }
  }

}

/**
 * <p>A custom JTextPane for displaying documents in a comprehendable
 * form.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */
class MyTextPane
    extends JTextPane
    implements MouseMotionListener {
  // hovering over a link.
  private final Cursor hand = new Cursor(Cursor.HAND_CURSOR);
  // not hovering over a link.
  private final Cursor arrow = new Cursor(Cursor.DEFAULT_CURSOR);
  // the popup menu for this component.
  private JPopupMenu popup;
  //private JSplitPane split;

  private SimpleAttributeSet[] attrs = MyStyledDocument.getAttributes();
  /**
   * Creates the component.
   * @param doc The formatted document being displayed.
   * @param label The label for the popup menu.
   */
  public MyTextPane(MyStyledDocument doc, String label) {
    super(doc);
    popup = new JPopupMenu(label);
    popup.setInvoker(this);
    setToolTipText(null);
    addMouseMotionListener(this);
    setPreferredSize(new Dimension(350, 550));
  }

  /**
   * Set the popup menu to a different object.
   * @param popup The new popup menu.
   */
  public void setPopup(JPopupMenu popup) {
    this.popup = popup;
  }

  /**
   * Returns this component's popup menu.
   * @return The popup menu.
   */
  public JPopupMenu getPopup() {
    return popup;
  }

  /**
   * Returns the formatted document for this component.
   * @return A MyStyledDocument containing a formatted version of the original
   * file.
   */
  public MyStyledDocument getStyledDoc() {
    return (MyStyledDocument)this.getDocument();
  }

  /**
   * If the mouse pointer is over a suspicous sentence then the tool tip will
   * show the sentence's id number and the cursor will change to a hand,
   * letting the user know that action can be taken by clicking the mouse.
   * @param e The event object generated.
   */
  public void mouseMoved(MouseEvent e) {
    int loc = viewToModel(e.getPoint());
    setCaretPosition(loc);
    AttributeSet attrSet = getCharacterAttributes();
    int style = getStyledDoc().getLineStyle(attrSet);
    if (style == MyStyledDocument.DEFAULT_STYLE) {
      setCursor(arrow);
      setToolTipText(null);
      requestFocusInWindow();

      alterFormat(lastUsed, lastUsedSet);
      lastUsed = null;
      lastUsedSet = null;

    }
    else if (style == MyStyledDocument.SUSPICOUS ||
             style == MyStyledDocument.IGNORED) {
      setCursor(hand);
      MyStyledDocument styled = getStyledDoc();
      Sentence sent = styled.getSentence(loc);
      setToolTipText("Sentence " + sent.getId());
      requestFocusInWindow();

      SentenceLocation locater = styled.findLocater(loc);

      if (style == MyStyledDocument.SUSPICOUS) {
        alterFormat(locater, attrs[MyStyledDocument.HIGHLIGHTED], false,
                    attrs[MyStyledDocument.SUSPICOUS]);
      }
      else if (style == MyStyledDocument.IGNORED) {
        alterFormat(locater, attrs[MyStyledDocument.HIGHLIGHT_IGNORE], false,
                    attrs[MyStyledDocument.IGNORED]);

      }
    }
    else if (style == MyStyledDocument.HIGHLIGHTED ||
             style == MyStyledDocument.HIGHLIGHT_IGNORE) {
      setCursor(hand);
      MyStyledDocument styled = getStyledDoc();
      SentenceLocation locater = styled.findLocater(loc);

      Sentence sent = locater.sentence;
      setToolTipText("Sentence " + sent.getId());
      //if(style == MyStyledDocument.HIGHLIGHTED){
      requestFocusInWindow();

      if (locater.equals(lastPermanent) && lastUsed != lastPermanent) {
        alterFormat(lastUsed, lastUsedSet);
        lastUsed = lastPermanent;
      }
      //}
    }
  }

  SentenceLocation lastUsed = null, lastPermanent = null;
  SimpleAttributeSet lastUsedSet = null, lastPermanentSet = null;
  void alterFormat(SentenceLocation location, SimpleAttributeSet sas) {
    if (location == null || sas == null) {
      return;
    }
    this.getStyledDoc().setCharacterAttributes(location.startLoc,
                                               location.endLoc -
                                               location.startLoc, sas, true);
  }

  public void alterFormat(SentenceLocation location, SimpleAttributeSet sas,
                          boolean permanent, SimpleAttributeSet last) {
    if (permanent && lastPermanent != null) {
      alterFormat(lastPermanent, lastPermanentSet);
    }
    else if (!permanent && lastUsed != null &&
             !lastUsed.equals(lastPermanent)) {
      alterFormat(lastUsed, lastUsedSet);
    }

    alterFormat(location, sas);
    if (permanent) {
      lastPermanent = location;
      lastPermanentSet = last;
    }
    else {
      lastUsed = location;
      lastUsedSet = last;
    }
  }

  /**
   * Does nothing.
   * @param e The event object generated.
   */
  public void mouseDragged(MouseEvent e) {}

}
