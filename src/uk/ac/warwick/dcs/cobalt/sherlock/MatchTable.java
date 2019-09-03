package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

/**
 * Display all detected matchese in a JTable in the form of a JTree.
 *
 * @author Weiliang Zhang
 * @version 21 Sep 2002
 */
public class MatchTable
    extends MatchesScreen
    implements MouseListener {
  /**
   * Table to obtain data.
   */
  private MatchTableModel model;

  /**
   * The Tree Table inside.
   */
  private JTreeTable treeTable;

  /**
   * Linked list holding all nodes except the root node.
   */
  private LinkedList nodes;

  /**
   * To transfer table data to other programs.
   */
  private Clipboard sysClipboard;

  /**
   * Number of matches considered suspicious.
   */
  private int counter = 0;

  public MatchTable(MyGUI gui, Marking mk) {
    super(gui, "Display Matches");

    Comparator comp = new Comparator() {
      public int compare(Object o1, Object o2) {
        Match m1 = (Match) o1;
        Match m2 = (Match) o2;
        if (m1.getSimilarity() < m2.getSimilarity()) {
          return 1;
        }
        if (m1.getSimilarity() > m2.getSimilarity()) {
          return -1;
        }
        return 0;
      }
    };

    Arrays.sort(matches, comp);

    if (matches == null) {
      return;
    }

    marking = mk;
    if (marking == null) {
      marking = new Marking();

    }
    marking.setMatches(matches);
    marking.generate();

    //set up status bar
    statusBar = new JLabel("Loading matches...");
    statusBar.setHorizontalAlignment(JLabel.RIGHT);
    statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    getContentPane().add(statusBar, BorderLayout.SOUTH);

    setSize(1000, 600);
    setVisible(true);

    //Create tree nodes, the linked list holds nodes which represent
    //a pair, these should be unique, each such node then contains
    //children which represent the actual matches.
    nodes = new LinkedList();
    ListIterator itr;
    //read in all match files and add them as nodes.
    for (int i = 0; i < matches.length; i++) {
      Match m = matches[i];
      //two possible names
      boolean added = false;
      String name1 = truncate(m.getFile1()) + " & "
          + truncate(m.getFile2());
      String name2 = truncate(m.getFile2()) + " & "
          + truncate(m.getFile1());

      //check whether this pair is know or not.
      itr = nodes.listIterator();
      while (itr.hasNext()) {
        MatchTreeNode n = (MatchTreeNode) itr.next();
        //if this is a match for a known pair, add it as a child.
        if (n.toString().equals(name1) || n.toString().equals(name2)) {
          int fileType = m.getFileType();
          RunCoordinates rcstart = m.getRun().getStartCoordinates();
          RunCoordinates rcend = m.getRun().getEndCoordinates();
          String lines1 = new String
              (rcstart.getOrigLineNoInFile1() + " - " +
               rcend.getOrigLineNoInFile1());
          String lines2 = new String
              (rcstart.getOrigLineNoInFile2() + " - " +
               rcend.getOrigLineNoInFile2());
          int percent = m.getSimilarity();
          if (n.toString().equals(name1)) {
            n.add(new MatchTreeNode
                  (fileType, lines1, lines2, percent, i));
          }
          else {
            n.add(new MatchTreeNode
                  (fileType, lines2, lines1, percent, i));
          }
          added = true;
          break;
        }
      }

      //if this is a match for a unknown pair, create a pair node for it,
      //and added as the child of this pair node.
      if (!added) {
        //a new pair is found, add it to the linked list.
        String name = truncate(m.getFile1()) + " & "
            + truncate(m.getFile2());
        String file1 = truncate(m.getFile1());
        String file2 = truncate(m.getFile2());
        MatchTreeNode newnode = new MatchTreeNode(name, file1, file2);

        //add itself as the child.
        int fileType = m.getFileType();
        RunCoordinates rcstart = m.getRun().getStartCoordinates();
        RunCoordinates rcend = m.getRun().getEndCoordinates();
        String lines1 = new String
            (rcstart.getOrigLineNoInFile1() + " - " +
             rcend.getOrigLineNoInFile1());
        String lines2 = new String
            (rcstart.getOrigLineNoInFile2() + " - " +
             rcend.getOrigLineNoInFile2());
        int percent = m.getSimilarity();
        //add itself to the node representing a pair.
        newnode.add(new MatchTreeNode
                    (fileType, lines1, lines2, percent, i));
        //add them to the list.
        nodes.add(newnode);
      }
    }

    //sort the children of every node.
    itr = nodes.listIterator();
    while (itr.hasNext()) {
      MatchTreeNode m = (MatchTreeNode) itr.next();

      Object[] tmp = m.getChildren().toArray();
      //cast objects to MatchTreeNode
      MatchTreeNode[] children = new MatchTreeNode[tmp.length];
      for (int i = 0; i < tmp.length; i++) {
        children[i] = (MatchTreeNode) tmp[i];

        //sort the children
      }
      Arrays.sort(children);
      //clear internal vector, create new ones.
      m.removeAllChildren();
      for (int i = 0; i < children.length; i++) {
        m.add(children[i]);
      }
    }

    //create root node
    MatchTreeNode root = new MatchTreeNode();
    //add all nodes to root to create complete tree.
    itr = nodes.listIterator();
    while (itr.hasNext()) {
      root.add( (MatchTreeNode) itr.next());

    }
    model = new MatchTableModel(root);
    treeTable = new JTreeTable(model);
    treeTable.getTree().getModel().addTreeModelListener
        (new TreeModelListener() {
      public void treeNodesChanged(TreeModelEvent e) {
        counter = getCounter();
        statusBar.setText("Total number of matches: "
                          + matches.length + ", " +
                          counter + " considered suspicious.");
      }

      public void treeNodesInserted(TreeModelEvent e) {}

      public void treeNodesRemoved(TreeModelEvent e) {}

      public void treeStructureChanged(TreeModelEvent e) {}
    });
// 	treeTable.setDefaultRenderer
// 	    (String.class, new MatchTableStringRenderer());
    treeTable.setDefaultRenderer
        (Integer.class, new MatchTableIntegerRenderer());
    treeTable.setDefaultRenderer
        (Integer.class, new MatchTableBooleanRenderer());
    treeTable.addMouseListener(this);

    setUpMenus();

    statusBar.setText("Total number of matches: " + matches.length);
    JScrollPane spane = new JScrollPane(treeTable);
    spane.getViewport().setBackground(Color.white);
    getContentPane().add(spane, BorderLayout.CENTER);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        save();
        closeMe();
      }
    });

    //load marking
    load();
  }

  /**
   * The renderer used for string in the TreeTable. The only thing it does,
   * is to format a null String as '---'.
   */
  static class MatchTableStringRenderer
      extends DefaultTableCellRenderer {
    public MatchTableStringRenderer() {
      super();
    }

    public void setValue(Object value) {
      setText( (value.equals("")) ? "---" : value.toString());
    }
  }

  static class MatchTableIntegerRenderer
      extends DefaultTableCellRenderer {
    public MatchTableIntegerRenderer() {
      super();
    }

    //problem: does not render negative vaules to ---!!!!
    public void setValue(Object value) {
      setText( ( ( (Integer) value).intValue() < 0) ? "---"
              : value.toString());
    }
  }

  static class MatchTableBooleanRenderer
      extends DefaultTableCellRenderer {
    public MatchTableBooleanRenderer() {
      super();
    }

    public void setValue(Object value) {
      setText( (value == null) ? "---" : value.toString());
    }
  }

  private class PrintActionListener
      implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //if no selection is made, prompt user.
      if (treeTable.getTree() == null ||
          treeTable.getTree().getSelectionPath() == null) {
        JOptionPane.showMessageDialog
            (null, "Please select the pair you wish to print.",
             "No pair selected.", JOptionPane.WARNING_MESSAGE);
        return;
      }

      MatchTreeNode selected = (MatchTreeNode) treeTable.getTree()
          .getSelectionPath().getLastPathComponent();

      int realIndex = selected.getIndex();
      Match[] pm;

      if (realIndex >= 0) {
        //selected row is a MATCH NODE
        String selectfile1 =
            truncate(matches[realIndex].getFile1());
        String selectfile2 =
            truncate(matches[realIndex].getFile2());

        //find out all data for this pair.
        Vector peers = ( (MatchTreeNode) selected.getParent())
            .getChildren();
        pm = new Match[peers.size()];
        for (int i = 0; i < pm.length; i++) {
          pm[i] = matches[ ( (MatchTreeNode) peers.get(i))
              .getIndex()];
        }
      }
      else if (realIndex == -1) {
        //node selected is a PAIR_NODE
        Vector peers = selected.getChildren();
        pm = new Match[peers.size()];
        for (int i = 0; i < pm.length; i++) {
          pm[i] = matches[ ( (MatchTreeNode) peers.get(i))
              .getIndex()];
        }
      }
      else {
        //node selected is the ROOT_NODE, do nothing.
        JOptionPane.showMessageDialog
            (null, "Please select the pair you wish to print.",
             "Cannot print Root Node.", JOptionPane.WARNING_MESSAGE);
        return;
      }

      // Now sort the array of matches into descending order,
      // according to file type in which the match was found.
      for (int i = 0; i < pm.length; i++) {
        for (int j = i + 1; j < pm.length; j++) {
          if (Settings.fileTypes[pm[i].getFileType()].
              getDescription().compareTo
              (Settings.fileTypes[pm[j].getFileType()].
               getDescription()) < 0) {
            Match temp = pm[i];
            pm[i] = pm[j];
            pm[j] = temp;
          }
        }
      }

      //construct print out.
      PrintOut docs = new PrintOut
          (new PrintableTableModel(pm));

      //actual printing.
      DocFlavor[] flavors = new DocFlavor[2];
      flavors[0] = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
      flavors[1] = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
      DocAttributeSet das = new HashDocAttributeSet();
      PrintRequestAttributeSet pras =
          new HashPrintRequestAttributeSet();

      PrintService printer =
          PrintServiceLookup.lookupDefaultPrintService();
      PrintService[] printers =
          PrintServiceLookup.lookupPrintServices
          (
          /*flavors[0]*/null, das);

      // 		if (printer == null)
      // 		    System.out.println("no default printer");
      // 		else System.out.println("default printer exists.");
      // 		if (printers == null)
      // 		    System.out.println("no multidoc printers");
      // 		else System.out.println("multidoc printers exits"
      // 					+ printers.length);
      if (printer == null || printers == null
          || printers.length == 0) {
        JOptionPane.showMessageDialog
            (MatchTable.this, "No printers found.",
             "Printing cannot proceed", JOptionPane.ERROR_MESSAGE);
        return;
      }

      statusBar.setText("Printing...");
      PrintService service = ServiceUI.printDialog
          (null, 100, 100, printers, null, flavors[0], pras);

      if (service != null) {
        try {
          //print everything in docs
          statusBar.setText("Printing...");
          MultiDoc tmp = docs;
          while (tmp != null) {
            DocPrintJob job = service.createPrintJob();
            job.addPrintJobListener
                (new PrintJobAdapter() {

              public void printJobFailed
                  (PrintJobEvent e) {
                statusBar.setText("Printing Failed.");
                return;
              }
            });

            //add the doc attribute for current doc to pras.
            pras = new HashPrintRequestAttributeSet();
            if (tmp.getDoc() != null) {
              AttributeSet as =
                  tmp.getDoc().getAttributes();
              if (as != null) {
                pras.addAll(as);
              }
            }
            pras.add(OrientationRequested.LANDSCAPE);
            job.print(tmp.getDoc(), pras);
            tmp = tmp.next();
          }
          statusBar.setText("Printing Complete.");
        }
        catch (PrintException exception) {
          statusBar.setText("Printing failed.");
          JOptionPane.showMessageDialog
              (null, "Printing failed.", "Error",
               JOptionPane.ERROR_MESSAGE);
          exception.printStackTrace();
          return;
        }
        catch (IOException exception) {
          //multiDoc object is damaged.
          statusBar.setText("Printing failed.");
          return;
        }
      }
      else {
        return;
      }
    }
  }

  /**
   * Return the number of marked suspicious matches.
   */
  private int getCounter() {
    ListIterator itr = nodes.listIterator();
    int n = 0;
    while (itr.hasNext()) {
      Vector c = ( (MatchTreeNode) itr.next()).getChildren();
      for (int i = 0; i < c.size(); i++) {
        if ( ( (MatchTreeNode) c.get(i)).isSuspicious()
            .booleanValue()) {
          n++;
        }
      }
    }
    return n;
  }

  /**
   * Save current marking to a file. Called to save when fram is closed.
   * Marking in this frame is not saved in real-time, that is, marking
   * is not updated until the from is closed.
   */
  private void save() {
    //check every match node, if it's suspicious,
    //add it to marking, but first of all ,clear marking.
    //	if (!marking.isClean()) {
    marking.clear();
    for (int i = 0; i < nodes.size(); i++) {
      //check this pair node's children
      MatchTreeNode m = (MatchTreeNode) nodes.get(i);
      Vector matchNodes = m.getChildren();
      for (int j = (matchNodes.size() - 1); j >= 0; j--) {
        m = (MatchTreeNode) matchNodes.get(j);
        if (m.isSuspicious().booleanValue()) {
          marking.add(m.getIndex());
        }
      }
    }
  }

  /**
   * Load from existing marking. Called when frame is constructed to
   * assign correct values to match nodes or when marking is changed
   * externally and the table needs to be updated.
   */
  private void load() {
    //reset counter
    counter = 0;
    //reset all nodes
    ListIterator itr = nodes.listIterator();
    while (itr.hasNext()) {
      ( (MatchTreeNode) itr.next())
          .setSuspicious(new Boolean(false));
    }

    //find out nodes which represents the suspicious matches
    //and marking them so that they're displayed correctly
    //in the table.
    counter = 0;
    ListIterator mitr = marking.getIndices().listIterator();
    while (mitr.hasNext()) {
      int index = ( (Integer) mitr.next()).intValue();
      ListIterator nodesItr = nodes.listIterator();
      String file1 = truncate(matches[index].getFile1());
      String file2 = truncate(matches[index].getFile2());
      //scan through all nodes.
      while (nodesItr.hasNext()) {
        //only scan through the children of the corresponding
        //PAIR NODE.
        MatchTreeNode m = (MatchTreeNode) nodesItr.next();
        if ( (m.getLines1().equals(file1) && m.getLines2()
              .equals(file2)) ||
            (m.getLines1().equals(file1) && m.getLines2()
             .equals(file2))) {
          Vector children = m.getChildren();
          //scan through all children
          for (int x = 0; x < children.size(); x++) {
            MatchTreeNode c = (MatchTreeNode)
                children.get(x);
            if (c.getIndex() == index) {
              c.setSuspicious(new Boolean(true));
              break;
            }
          }
          break;
        }
      }
    }

    counter = getCounter();
    statusBar.setText("Total number of matches: "
                      + matches.length
                      + ", " + counter
                      + " considered suspicious.");

    //update table.
    ( (AbstractTableModel) treeTable.getModel())
        .fireTableDataChanged();

    //needs to call this as each call to change the nodes set this
    //flag to dirty.
    marking.setClean();
  }

  /**
   * Update table to reflect any external changes.
   */
  public void update() {
    load();
  }

  /**
   * Set up the menus for this DisplayMatches.
   */
  private void setUpMenus() {
    JMenuBar jmb = getJMenuBar();

    // Remove the options menu.
    JMenu jm = jmb.getMenu(OPTIONS_MENU);
    jm.setEnabled(false);

    // Add Close to the File menu.
    JMenu fileMenu = jmb.getMenu(FILE_MENU);

    //Produce a summary of the selected pair
    //commented out for installation in Finland.
    JMenuItem jmi = new JMenuItem("Print out selected pair",
                                  KeyEvent.VK_P);
    jmi.addActionListener(new PrintActionListener());
    fileMenu.add(jmi);

    //this menu item is a replacement for the match section print out,
    //which is working correctly due to a bug in java. It lets you save
    //these sections in a text file, which can be printed later with
    //other printing tools, such as 'a2ps'.
    //Note: This text file is 160 characters wide, you need to specify the
    //line width explicitly in 'a2ps' to print it correctly.
    jmi = new JMenuItem("Save matched sections to file", KeyEvent.VK_M);
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //if no selection is made, prompt user.
        if (treeTable.getTree() == null ||
            treeTable.getTree().getSelectionPath() == null) {
          JOptionPane.showMessageDialog
              (null, "Please select the pair you wish to print.",
               "No pair selected.", JOptionPane.WARNING_MESSAGE);
          return;
        }

        //examinate selected node & find out peer matches.
        MatchTreeNode selected = (MatchTreeNode) treeTable
            .getTree().getSelectionPath().getLastPathComponent();

        int realIndex = selected.getIndex();
        Match[] pm;

        if (realIndex >= 0) {
          //selected row is a MATCH NODE
          String selectfile1 =
              truncate(matches[realIndex].getFile1());
          String selectfile2 =
              truncate(matches[realIndex].getFile2());

          //find out all data for this pair.
          Vector peers = ( (MatchTreeNode) selected.getParent())
              .getChildren();
          pm = new Match[peers.size()];
          for (int i = 0; i < pm.length; i++) {
            pm[i] = matches[ ( (MatchTreeNode) peers.get(i))
                .getIndex()];
          }
        }
        else if (realIndex == -1) {
          //node selected is a PAIR_NODE
          Vector peers = selected.getChildren();
          pm = new Match[peers.size()];
          for (int i = 0; i < pm.length; i++) {
            pm[i] = matches[ ( (MatchTreeNode) peers.get(i))
                .getIndex()];
          }
        }
        else {
          //node selected is the ROOT_NODE, do nothing.
          JOptionPane.showMessageDialog
              (null, "Please select the pair you wish to save.",
               "Cannot print Root Node.",
               JOptionPane.WARNING_MESSAGE);
          return;
        }

        // Now sort the array of matches into descending order,
        // according to file type in which the match was found.
        for (int i = 0; i < pm.length; i++) {
          for (int j = i + 1; j < pm.length; j++) {
            if (Settings.fileTypes[pm[i].getFileType()].
                getDescription().compareTo
                (Settings.fileTypes[pm[j].getFileType()].
                 getDescription()) < 0) {
              Match temp = pm[i];
              pm[i] = pm[j];
              pm[j] = temp;
            }
          }
        }

        boolean choosing = true;
        while (choosing) {
          JFileChooser jfc = new JFileChooser();
          jfc.setDialogTitle("Save matched sections");
          jfc.setFileSelectionMode
              (JFileChooser.FILES_AND_DIRECTORIES);
          int choice = jfc.showSaveDialog(MatchTable.this);

          if (choice == JFileChooser.APPROVE_OPTION) {
            File outfile = jfc.getSelectedFile();
            if (!outfile.exists()) {
              choosing = false;
              new MatchSectionPrintOut(outfile, pm);
              statusBar.setText("File saved");
            }
            else if (outfile.isDirectory()) {
              JOptionPane.showMessageDialog
                  (MatchTable.this, outfile.getName() +
                   " is a directory, try again please.",
                   "Cannot write to directories",
                   JOptionPane.ERROR_MESSAGE);
              continue;
            }
            //confirm overwrite operation if file exists.
            else {
              int overwrite =
                  JOptionPane.showConfirmDialog
                  (MatchTable.this,
                   "File selected already exists, " +
                   "are you sure to overwrite?",
                   "Overwrite existing file?",
                   JOptionPane.YES_NO_OPTION);
              if (overwrite == JOptionPane.YES_OPTION) {
                outfile.delete();
                choosing = false;
                new MatchSectionPrintOut(outfile, pm);
                statusBar.setText("File saved");
              }
            }
          }
          else {
            break;
          }
        }
      }
    });
    fileMenu.add(jmi);

    fileMenu.addSeparator();

    jmi = new JMenuItem("Close", KeyEvent.VK_O);
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        save();
        closeMe();
      }
    });
    fileMenu.add(jmi);

    JMenu editMenu = jmb.getMenu(EDIT_MENU);
    //add copy function
    //jmi = new JMenuItem("Copy Selected Rows...", KeyEvent.VK_C);
    //jmi.setAccelerator(KeyStroke.getKeyStroke
     //                  (KeyEvent.VK_C, KeyEvent.CTRL_MASK));
    // this has no action listener associated so it might as well be disabled
    //jmi.setEnabled(true);

    //need rework to be fit into the new treeTable.
// 	//transfer selected rows to system clipboard
// 	jmi.addActionListener(new ActionListener() {
// 		public void actionPerformed(ActionEvent e) {
// 		    //System.out.println("Copy catched!");
// 		    sysClipboard = Toolkit.getDefaultToolkit()
// 			.getSystemClipboard();

// 		    int[] rows = treeTable.getSelectedRows();
// 		    StringBuffer transfer = new StringBuffer();
// 		    //construct content to copy.
// 		    //System.out.println("Rows selected: ");
// 		    for (int i = 0; i < rows.length; i++) {
// 			System.out.println(i);
// 			for (int j = 0; j < treeTable.getColumnCount(); j++) {
// 			    transfer.append(model.getValueAt(i, j));
// 			    if (j + 1 < treeTable.getColumnCount())
// 				transfer.append(" ");
// 			}
// 			transfer.append("\n");
// 		    }

// 		    System.out.println(transfer);
// 		    StringSelection selection = new StringSelection
// 			(transfer.toString());
// 		    sysClipboard.setContents(selection, null);
// 		}
// 	    });
   // editMenu.add(jmi);
  } // setUpMenus

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
   * If the user double-clicks on a match, display it in a ComparePane.
   *
   * @param e the MouseEvent
   */
  public void mouseReleased(MouseEvent e) {
// 	// If has just double-clicked on an entry, show it in a ComparePane
// 	else if (e.getSource().equals(treeTable) && e.getClickCount() == 2) {
    if (e.getSource().equals(treeTable) && e.getClickCount() == 2) {
      MatchTreeNode selected = (MatchTreeNode) treeTable.getTree()
          .getSelectionPath().getLastPathComponent();

      int realIndex = selected.getIndex();
      if (realIndex >= 0) {
        //find out all peers of this node to fire up NavigatePane.
        Vector peers = ( (MatchTreeNode) selected.getParent())
            .getChildren();
        Match[] pm = new Match[peers.size()];
        for (int i = 0; i < pm.length; i++) {
          pm[i] = matches[ ( (MatchTreeNode) peers.get(i)).getIndex()];

          //node selected represends a MATCH_NODE.
          //fire up ComparePane.
        }
        ComparePane cp = new ComparePane
            (gui, pm, matches[realIndex], this);

      }
      else {
        return;
      }
    }
  } // mouseReleased

  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}

/**
 * Table model for the MatchTable.
 *
 * @author Weiliang Zhang
 * @version 21 Sep 2002
 */
class MatchTableModel
    extends DynamicTreeTableModel {
  private static final String[] columnNames = {
      "TYPE", "FILE 1", "FILE 2", "%", "SUSPICIOUS"};

  private static final Class[] classes = {
      TreeTableModel.class, String.class, String.class, Integer.class,
      Boolean.class};

  private static final String[] methodNames = {
      "toString", "getLines1", "getLines2", "getPercent", "isSuspicious",
      "isLeaf"};

  /**
   * Empty strings used to match methodNames, otherwise exception
   * will be thrown in DynamicTreeTableModel.
   */
  private static final String[] setterMethodNames = {
      "", "", "", "", "setSuspicious"};

  /**
   * @param root the name of the source directory.
   */
  public MatchTableModel(MatchTreeNode root) {
    super(root, columnNames, methodNames, setterMethodNames, classes);
  }

  public boolean isCellEditable(Object node, int column) {
    //only allowed to edit first column (to expend the tree)
    //and SUPICIOUS column.
    if (column == 0) {
      return true;
    }
    else if (column == 4) {
      return (root != node);
    }
    else {
      return false;
    }
  }
}

/**
 * Tree node representing a match.<p>
 * There are three kinds of forms which a node can take:
 * <ul>
 *   <li> 1. Root node, named after the source directory, other fields will
 *           be either empty strings or default value if it's a primitive type.
 *   <li> 2. Node representing a pair, named in the form of "filename1 &
 *           filename2", other strings fields are empty, the presentage is the
 *           sum of percentages of all matches for this pair.
 *   <li> 3. Node representing a match, named after the pre-processed version
 *           that this match was found in, followed by the line number pairs,
 *           percentage & a boolean indicating whether this match is considered
 *            to be suspicious (set by the user).
 * </ul>
 *
 * @author Weiliang Zhang
 * @version 21 Sep 2002
 */
class MatchTreeNode
    extends DefaultMutableTreeNode
    implements Comparable {
  /**
   * Constant representing a root node.
   */
  public static final int ROOT_NODE = 0;
  /**
   * Constant representing a pair node which contain match nodes.
   */
  public static final int PAIR_NODE = 1;
  /**
   * Constant representing a match node.
   */
  public static final int MATCH_NODE = 2;

  /**
   * Indicate in which preprocessed version this match was found.
   */
  private int matchType = -1;

  /**
   * The type of this node.
   */
  private int nodeType = -1;

  /**
   * Line number range in file 1. For a PAIR NODE, this field is the file
   * name.
   */
  private String lines1;

  /**
   * Line number range in file 2. For a PAIR NODE, this field is the file
   * name.
   */
  private String lines2;

  private int percent;
  private boolean isSuspicious = false;

  /**
   * The actually index in the 'matches' array represented by the node
   * in the MatchesScreen class. ROOT_NODE and PAIR_NODE do not have an
   * valid index, only MATCH_NODE has valid index.
   */
  private int index = -1;

  /**
   * Construct a root node. Named by the source directory path.
   */
  public MatchTreeNode() {
    super(Settings.sourceDirectory.getAbsolutePath());
    nodeType = ROOT_NODE;
    lines1 = "---";
    lines2 = "---";
    percent = -1;
    isSuspicious = false;
    index = -2;
  }

  /**
   * Construct a pair node.
   */
  public MatchTreeNode(String name, String file1, String file2) {
    super(name);
    nodeType = PAIR_NODE;
    lines1 = file1;
    lines2 = file2;
    percent = 0;
    isSuspicious = false;
    index = -1;
  }

  /**
   * Construct a MATCH NODE.
   *
   * @param matchType the type of preprocessed file in which this match was
   * found. This is one of type constant in Settings.java. Negative value
   * of this variable indicates that this node is not a MATCH NODE.
   * @param lines1 line number range for file 1.
   * @param lines2 line number range for file 2.
   * @param percent percentage of this match.
   * @param index actual index of this match in the matches array.
   */
  public MatchTreeNode(int matchType, String lines1, String lines2,
                       int percent, int index) {
    super(Settings.fileTypes[matchType].getDescription());
    this.matchType = matchType;
    nodeType = MATCH_NODE;
    this.lines1 = lines1;
    this.lines2 = lines2;
    this.percent = percent;
    this.isSuspicious = false;
   // this.children = children;
    this.index = index;
  }

  /**
   * Compares this node with node given.
   */
  public int compareTo(Object node) {
    return Settings.fileTypes[matchType].getDescription()
        .compareTo(Settings.fileTypes[ ( (MatchTreeNode) node).getMatchType()]
                   .getDescription());
  }

  /**
   * Add given node as this node's child, also increment the precentage if
   * this node is not the root node.
   */
  public void add(MutableTreeNode node) {
    super.add(node);
    if (nodeType != ROOT_NODE) {
      percent += ( (MatchTreeNode) node).getPercent().intValue();
    }
  }

  /**
   * Remove all children, also clear the percentage variable.
   */
  public void removeAllChildren() {
    super.removeAllChildren();
    percent = 0;
  }

  /**
   * All children of this node.
   */
  public Vector getChildren() {
    return children;
  }

  /**
   * Return the type of match represented by this node..
   */
  public int getMatchType() {
    return matchType;
  }

  /**
   * Line number pair in file 1.
   */
  public String getLines1() {
    return lines1;
  }

  /**
   * Line number pair in file 2.
   */
  public String getLines2() {
    return lines2;
  }

  /**
   * Percentage of this match.
   */
  public Integer getPercent() {
    return new Integer(percent);
  }

  /**
   * Type of this node.
   */
  public int getNodeType() {
    return nodeType;
  }

  /**
   * The actually index of this match node in the matches array.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Whether this match is considered to be suspicious.
   *
   * @return null if this node is the root node.
   */
  public Boolean isSuspicious() {
    if (nodeType == ROOT_NODE) {
      return null;
    }
    else {
      //if all children of this node is set to true, set this node to
      //true. The reason why isSuspicious variable is not directly
      //returned is that when loading from a file, there is no indication
      //in the saved file that while mark a PAIR NODE is suspicious.
      int c = getChildCount();

      //if this node is a MATCH NODE, return isSuspicious directly.
      if (c == 0) {
        return new Boolean(isSuspicious);
      }
      //else compute isSuspicious and then return.
      isSuspicious = true;
      for (int i = 0; i < c; i++) {
        if (! ( (MatchTreeNode) getChildAt(i)).isSuspicious()
            .booleanValue()) {
          isSuspicious = false;
          break;
        }
      }
      return new Boolean(isSuspicious);
    }
  }

  /**
   * True if this node is a leaf in the tree, false otherwise.
   */
  public boolean isLeaf() {
    return (children == null) ? true : false;
  }

  /**
   * Set this match to be suspicious.
   */
  public void setSuspicious(Boolean value) {
    isSuspicious = value.booleanValue();
    MatchTable.marking.setDirty();

    //set all children to this value.
    int c = getChildCount();
    for (int i = 0; i < c; i++) {
      ( (MatchTreeNode) getChildAt(i)).setSuspicious(value);
    }
  }
}
