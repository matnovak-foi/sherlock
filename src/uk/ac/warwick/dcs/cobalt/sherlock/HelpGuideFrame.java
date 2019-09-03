package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.net.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * A User guide frame for the system.
 *
 * @author Terri Mak
 * @version 4 Sept 2002
 */

public class HelpGuideFrame
    extends JFrame {

  private JEditorPane htmlPane;
  private static boolean DEBUG = false;
  private URL helpURL;

  /**
   * Construct a frame displaying the user guide in a split window
   * A tree directory is on the left for selecting different sessions.
   * And the corresponding session window is shown on the right.
   *
   */

  public HelpGuideFrame() {
    super("Sherlock Help Guide");

    //Create the nodes.
    DefaultMutableTreeNode top = new DefaultMutableTreeNode
        ("Sherlock Plagiarism Detection Tool");
    createNodes(top);

    //Create a tree that allows one selection at a time.
    final JTree tree = new JTree(top);
    tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);

    //Listen for when the selection changes.
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();

        if (node == null) {
          return;
        }

        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
          ItemInfo item = (ItemInfo) nodeInfo;
          displayURL(item.itemURL);
          if (DEBUG) {
            System.out.print(item.itemURL + ":  \n    ");
          }
        }
        else {
          //displayURL(helpURL);
        }
        if (DEBUG) {
          System.out.println(nodeInfo.toString());
        }
      }
    });

    //Create the scroll pane and add the tree to it.
    JScrollPane treeView = new JScrollPane(tree);

    //Create the HTML viewing pane.
    htmlPane = new JEditorPane();
    htmlPane.setEditable(false);
    JScrollPane htmlView = new JScrollPane(htmlPane);

    //Add the scroll panes to a split pane.
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(treeView);
    splitPane.setRightComponent(htmlView);

    Dimension minimumSize = new Dimension(100, 50);
    htmlView.setMinimumSize(minimumSize);
    treeView.setMinimumSize(minimumSize);
    splitPane.setDividerLocation(250);
    splitPane.setPreferredSize(new Dimension(700, 550));

    //Add the split pane to this frame.
    getContentPane().add(splitPane, BorderLayout.CENTER);

  }

  private class ItemInfo {
    public String itemName;
    public URL itemURL;

    public ItemInfo(String item, String filename) {
      itemName = item;
//             try {
      itemURL = getClass().getResource(filename);
//             } catch (java.net.MalformedURLException exc) {
//                 System.err.println("Attempted to create a ItemInfo "
//                                    + "with a bad URL: " + itemURL);
//                 itemURL = null;
//             }
    }

    public String toString() {
      return itemName;
    }
  }

  private void displayURL(URL url) {
    try {
      htmlPane.setPage(url);
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (this, "Attempted to read a bad URL: " + url,
           "File Not Found", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void createNodes(DefaultMutableTreeNode top) {
    DefaultMutableTreeNode category = null;
    DefaultMutableTreeNode item = null;
    String path = "/sherlockhelp/";//System.getProperty("user.dir")+File.separator+"sherlockhelp"+File.separator;
    //path = path.replace('\\', '/');
    //System.out.println(path);
    //About Sherlock
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("About Sherlock",
                                       path + "AboutSherlock.html"));
    top.add(item);

    category = new DefaultMutableTreeNode("Basic Procedures (in GUI)");
    top.add(category);

    //Stage One: Source Directory
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Stage One: Input Source Directory",
                                       path + "InputSourceDirectory.html"));
    category.add(item);

    //Stage Two: Detection
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Stage Two: Detection",
                                       path + "Detection.html"));
    category.add(item);

    //Stage Three: Examine Results
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Stage Three: Examine Results",
                                       path + "ExamineResult.html"));
    category.add(item);

    category = new DefaultMutableTreeNode("Natural Language");
    top.add(category);

    //About Natural Language Detection
    item = new DefaultMutableTreeNode(new ItemInfo
                                      (
        "About Natural Language Plagiarism Detection",
        path + "NaturalAbout.html"));
    category.add(item);

    //Preparation and Detection
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Preparation and Detection",
                                       path + "NaturalStageOne.html"));
    category.add(item);

    //What the Results Mean
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("What the Results Mean",
                                       path + "NaturalResults.html"));
    category.add(item);

    //What the Settings do
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("What the Settings do",
                                       path + "NaturalSettings.html"));
    category.add(item);

    category = new DefaultMutableTreeNode("More Functions");
    top.add(category);

    //Save session
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Save Session",
                                       path + "SaveSession.html"));
    category.add(item);

    //Load Saved Session
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Load Saved Session",
                                       path + "LoadSavedSession.html"));
    category.add(item);

    //General Setting
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("General Setting",
                                       path + "GeneralSetting.html"));
    category.add(item);

    //Detection setting parameter(matched sensitivity)
    item = new DefaultMutableTreeNode(new ItemInfo
                                      (
        "Detection Setting Parameter (matched sensitivity)",
        path + "DetectionSetting.html"));
    category.add(item);

    //Change file type name
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Change file type name",
                                       path + "ChangeFileTypeName.html"));
    category.add(item);

    //Excluded file
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Excluded file",
                                       path + "ExcludedFile.html"));
    category.add(item);

    //Examine stored matches
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Examine Stored Matches",
                                       path + "ExamineStoredMatches.html"));
    category.add(item);

    //Examine matched graph
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Examine Matched Graph",
                                       path + "ExamineMatchedGraph.html"));
    category.add(item);

    //View log (unexamined files)
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("View log (unexamined files)",
                                       path + "ViewLog.html"));
    category.add(item);

    //Print
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Print", path + "Print.html"));
    category.add(item);

    //Save Marking
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Save Marking",
                                       path + "SaveMarking.html"));
    category.add(item);

    //Load Marking
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Load Marking",
                                       path + "LoadMarking.html"));
    category.add(item);
    //Message Window
    item = new DefaultMutableTreeNode(new ItemInfo
                                      ("Message Window",
                                       path + "MessageWindow.html"));
    category.add(item);

  }

  /**
   * Displaying the user guide frame after user pressed the menubar Help.
   *
   */

  public void openWindow() {
    JFrame frame = new HelpGuideFrame();

    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        dispose();
      }
    });

    frame.pack();
    frame.setVisible(true);

  }

}
