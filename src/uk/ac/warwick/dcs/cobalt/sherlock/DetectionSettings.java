/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * This screen is where the user selects which file types
 * to use during processing, and all the settings for each one.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @version 16 Aug 2002
 */
class DetectionSettings
    extends JDialog
    implements ActionListener, ItemListener {

  /////////////////////////////////
  // Inner classes for tab objects.
  /////////////////////////////////

  /**
   * This inner class simply holds all the data-containing objects
   * on the File Types To Use tab.
   */
  private class FTToUseComponents {
    JToggleButton inUseToggleButtons[] = new
        JToggleButton[Settings.NUMBEROFFILETYPES];
    JTextField directories[] = new JTextField[Settings.NUMBEROFFILETYPES];
    JButton directoryButtons[] = new JButton[Settings.NUMBEROFFILETYPES];
    JTextField matchDirectory;
    JTextField excludeFile;
    String oldExcludeFile = null;
    JButton efButton;
    JButton viewEFButton;
    JButton rad;

    FTToUseComponents() {
      for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
        inUseToggleButtons[x] = new JCheckBox();
        directories[x] = new JTextField(20);
        directoryButtons[x] = new JButton("...");
      }

      matchDirectory = new JTextField(20);
      excludeFile = new JTextField(20);
      excludeFile.setEditable(false);
      efButton = new JButton("...");
      viewEFButton = new JButton("View exclude file");
      rad = new JButton("Restore all defaults");
    } // FTToUseComponents()
  } // FTToUseComponents

  /**
   * This inner class simply holds all the data-containing objects
   * on each File Type tab.
   */
  private class FileTypeComponents {
    NumberField settingValue[] = new NumberField[6];
    JCheckBox amal;
    JCheckBox conc;
    JButton rd;

    /**
     * Constructor to create the objects with the default settings.
     */
    FileTypeComponents() {
      for (int x = 0; x < 6; x++) {
        settingValue[x] = new NumberField(10);
      }
      amal = new JCheckBox("Amalgamate nearby runs");
      conc = new JCheckBox("Concatanate nearby runs");
      rd = new JButton("Restore defaults");
    } // FileTypeComponents()
  } // FileTypeComponents

  /**
   * An extension of the FileTypeComponents class, providing extra
   * components only needed by the free-text detection settings.
   * <p>Title: Sherlock 2000</p>
   * <p>Description: Plagiarism Detection Software</p>
   * <p>Copyright: Copyright (c) 2002</p>
   * @author Daniel White
   * @version 4
   */
  private class SentenceComponents
      extends FileTypeComponents {
    JPanel listPanel;
    JList wordList;
    JButton add, remove;
    JCheckBox mem = new JCheckBox("Memory Intensive");
    SentenceComponents(String[] commonWords) {
      super();

      mem.setToolTipText("<HTML><BODY>Uses 20-40% more memory but<BR>" +
                         "increases detection speed by roughly 30%</BODY>" +
                         "</HTML>");
      listPanel = new JPanel(new GridBagLayout());
      DefaultListModel model = new DefaultListModel();
      for (int i = 0; i < commonWords.length; i++) {
        model.addElement(commonWords[i]);

      }

      wordList = new JList(model);
      wordList.setToolTipText("Words which are common enough to be ignored" +
                              " by Sherlock");
      wordList.setVisibleRowCount(8);

      add = new JButton("Add word...");

      remove = new JButton("Remove word(s)");
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 1.0;
      c.weighty = 1.0;
      c.fill = c.BOTH;
      c.gridheight = 2;

      listPanel.setBorder(new TitledBorder(new EtchedBorder(),
                                           "Common Words"));
      listPanel.add(new JScrollPane(wordList), c);

      c.gridx = 2;
      c.gridheight = 1;
      c.weighty = 0;
      c.weightx = 0;
      c.fill = c.NONE;

      listPanel.add(add, c);
      c.gridy = 2;
      listPanel.add(remove, c);
    }
  }

//     /**
//      * Private class defining the behaviour of radio buttons for
//      * Normaliser & NoWhite.
//      */
//     private class RadioListener implements ActionListener {
// 	public void actionPerformed(ActionEvent e) {
// 	    fttuComponents.directories[Settings.NOR].setEditable
// 		(!fttuComponents.directories[Settings.NOR]
// 		 .isEditable());
// 	    jtp.setEnabledAt(Settings.NOR +1,
// 			     !jtp.isEnabledAt(Settings.NOR+1));
// 	    fttuComponents.directories[Settings.NOW].setEditable
// 		(!fttuComponents.directories[Settings.NOW]
// 		 .isEditable());
// 	    jtp.setEnabledAt(Settings.NOW+1,
// 			     !jtp.isEnabledAt(Settings.NOW+1));
// 	}
//     }

  ////////////////////////////////
  // End of inner classes.
  ////////////////////////////////

  /**
   * The objects on the File Types To Use tab which need to
   * be accessible for their data.
   *
   * @serial
   */
  private FTToUseComponents fttuComponents = null;

  /**
   * The objects on each File Types tab which need to be
   * accessible for their data.
   *
   * @serial
   */
  private FileTypeComponents ftComponents[] =
      new FileTypeComponents[Settings.NUMBEROFFILETYPES];

  /**
   * The tabbed pane that holds the options.
   *
   * @serial
   */
  private JTabbedPane jtp = null;

  /**
   * Holds whether the user pressed OK to close this dialog. Only changed
   * to true when OK is pressed -
   * closing the window leaves it as false.
   *
   * @serial
   */
  private boolean okpressed = false;

  /**
   * The GUI that's running.
   *
   * @serial
   */
  private GUI gui;

  /**
   * If this is true, then the user is coming in to run Samelines having
   * already run the tokenisers, and so shouldn't be able
   * to change which file types are used, or their output directories.
   *
   * @serial
   */
  private boolean detecting = false;

  /**
   * Creates a modal DetectionSettings dialog.
   *
   * @param gui a GUI owner of this dialog
   * @param detecting if true, then the user CAN'T choose the output
   * directories for each file type
   */
  DetectionSettings(GUI gui, boolean detecting) {
    super(gui, "Detection Settings", true);
    this.gui = gui;
    this.detecting = detecting;

    loadTabs();

    setDefaultCloseOperation(HIDE_ON_CLOSE);
    //setResizable(false);
    pack();
  } // DetectionSettings

  /**
   * Return whether the user pressed cancel or not.
   *
   * @return true if this dialog was cancelled, false if ok was pressed
   */
  boolean okPressed() {
    return okpressed;
  } // okPressed

  /**
   * Load the tabs to be displayed.
   */
  private void loadTabs() {

    // Set up the tabs, one for each of the file types to be used.
    jtp = new JTabbedPane();
    JPanel panelToAdd = createFileTypesToUseTab();
    jtp.addTab("File Types To Use", panelToAdd);

    for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
      panelToAdd = createFileTypeTab(x);
      jtp.addTab(Settings.fileTypes[x].getDescription(), panelToAdd);
      // If this file type is not in use, disable the tab.
      if (!Settings.fileTypes[x].isInUse()) {
        jtp.setEnabledAt(x + 1, false);
      }
    }

    // Panel holding the buttons at the bottom of the screen.
    JPanel buttonPanel = new JPanel();
    JButton jb = new JButton("OK");
    jb.setMnemonic('O');
    jb.addActionListener(this);
    buttonPanel.add(jb);
    jb = new JButton("Cancel");
    jb.setMnemonic('C');
    jb.addActionListener(this);
    buttonPanel.add(jb);

    getContentPane().add(jtp, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);
  } // loadTabs

  /**
   * Handle action events.
   * @param e The action event.
   */
  public void actionPerformed(ActionEvent e) {
    String path = "";
    AbstractButton button = (AbstractButton) e.getSource();

    // OK
    //// if (button.getText() == "OK") {
    if (button.getText().equals("OK")) {
      saveSettings();
      okpressed = true;
      this.setVisible(false);
    }

    // Cancel
    //// else if (button.getText() == "Cancel") {
    else if (button.getText().equals("Cancel")) {
      // If user selected a new exlude file, need to restore the
      //original one.
      if (fttuComponents.oldExcludeFile != null) {
        Settings.sherlockSettings.setExcludeFile
            (fttuComponents.oldExcludeFile);
      }
      this.setVisible(false);
    }

    // View exclude file
    // else if (button.getText() == "View exclude file") {
    else if (button.getText().equals("View exclude file")) {
      TextWindow tw = new TextWindow
          (gui, "Exclude file: " +
           Settings.sherlockSettings.getExcludeFile());
      tw.setVisible(true);
    }

    // Need to choose exclude file.
    else if (button.getText().equals("...")) {
      String newExcludeFile = gui.chooseExcludeFile(this);

      // If user didn't press cancel show selected exclude file.
      if (newExcludeFile != null) {
        // Because view exclude file references
        //Settings.sherlockSettings, must
        // set this new exclude file choice there. However,
        // need to save the old choice in case the user presses cancel.
        fttuComponents.oldExcludeFile = fttuComponents.excludeFile.
            getText();
        fttuComponents.excludeFile.setText(newExcludeFile);
        Settings.sherlockSettings.setExcludeFile(newExcludeFile);

        // If user has selected to have no exclude file, make sure
        // they can't view it - otherwise let them.
        if (newExcludeFile.equals("No exclude file")) {
          fttuComponents.viewEFButton.setEnabled(false);
        }
        else {
          fttuComponents.viewEFButton.setEnabled(true);
        }
      }
    }

    // Restore one file type's default values.
    else if (button.getText() == ("Restore defaults")) {
      int fileType = 0;
      for (fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
           fileType++) {
        if (button.equals(ftComponents[fileType].rd)) {
          break;
        }
      }
      restoreDefaultValues(fileType);
    }

    // Restore every file type's default values.
    else if (button.getText() == ("Restore all defaults")) {
      restoreDefaultValues(Settings.NUMBEROFFILETYPES);
    }
  } // actionPerformed

  /**
   * Restore the default values for one or all of the file types.
   *
   * @param fileType the type of file to restore the defaults for. If
   * this is Settings.NUMBEROFFILETYPES then all defaults will be restored.
   */
  private void restoreDefaultValues(int fileType) {

    String title = "Restore all defaults";
    String message = "Restore default values for ALL file types? "
        + "This will lose any changes that have been made.";
    if (fileType < Settings.NUMBEROFFILETYPES) {
      message = "Restore default values for " +
          Settings.fileTypes[fileType].getDescription() + " files?" +
          " This will lose any changes that have been made.";
      title = "Restore defaults";
    }

    int status = JOptionPane.showConfirmDialog
        (this, message, title, JOptionPane.OK_CANCEL_OPTION);

    // If the user confirms the action, restore the defaults as required.
    if (status == JOptionPane.OK_OPTION) {
      int start = 0;
      int stop = Settings.NUMBEROFFILETYPES;
      // See if just resetting one, as opposed to all file types.
      if (fileType < Settings.NUMBEROFFILETYPES) {
        start = fileType;
        stop = fileType + 1;
      }
      for (int x = start; x < stop; x++) {
        Settings.fileTypes[x].clear();

        // Reload this dialog with the default values.
      }
      getContentPane().removeAll();
      loadTabs();
      this.pack();
    }

  } // restoreDefaultValues

  /**
   * Handle item events thrown by the checkboxes.
   * @param e The ItemEvent
   */
  public void itemStateChanged(ItemEvent e) {
    JCheckBox jcb = (JCheckBox) e.getItem();
    for (int x = 0; x < fttuComponents.inUseToggleButtons.length; x++) {
      if (jcb.equals(fttuComponents.inUseToggleButtons[x])) {
        fttuComponents.directories[x].setEditable
            (!fttuComponents.directories[x].isEditable());
        jtp.setEnabledAt(x + 1, !jtp.isEnabledAt(x + 1));
        break;
      }
    } // for
  } // itemStateChanged

  /**
   * Save the settings from the dialog to the file type profile objects.
   */
  private void saveSettings() {

    Settings.sherlockSettings.setMatchDirectory
        (fttuComponents.matchDirectory.getText());
    if (fttuComponents.excludeFile.getText().equals("No exclude file")) {
      Settings.sherlockSettings.setExcludeFile("");
    }
    else {
      Settings.sherlockSettings.setExcludeFile
          (fttuComponents.excludeFile.getText());

    }
    for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
      Settings.fileTypes[x].setInUse
          (fttuComponents.inUseToggleButtons[x].isSelected());
      if (x != Settings.SEN) {
        Settings.fileTypes[x].setMinStringLength
            (Integer.parseInt(ftComponents[x].settingValue[0].getText()));
        Settings.fileTypes[x].setMinRunLength
            (Integer.parseInt(ftComponents[x].settingValue[1].getText()));
        Settings.fileTypes[x].setMaxForwardJump
            (Integer.parseInt(ftComponents[x].settingValue[2].getText()));
        Settings.fileTypes[x].setMaxBackwardJump
            (Integer.parseInt(ftComponents[x].settingValue[3].getText()));
        Settings.fileTypes[x].setMaxJumpDiff
            (Integer.parseInt(ftComponents[x].settingValue[4].getText()));
        Settings.fileTypes[x].setStrictness
            (Integer.parseInt(ftComponents[x].settingValue[5].getText()));
        Settings.fileTypes[x].setAmalgamate
            (ftComponents[x].amal.isSelected());
        Settings.fileTypes[x].setConcatanate
            (ftComponents[x].conc.isSelected());
      }
      else {
        SentenceComponents sent = (SentenceComponents) ftComponents[x];
        Settings.fileTypes[x].setSimThreshold(
            Integer.parseInt(sent.settingValue[0].getText()));
        Settings.fileTypes[x].setCommonThreshold(
            Integer.parseInt(sent.settingValue[1].getText()));
        Settings.fileTypes[x].setMemIntensive(sent.mem.isSelected());

        Object[] objects = ( (DefaultListModel) sent.wordList.getModel()).
            toArray();
        String[] strings = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
          strings[i] = (String) objects[i];

        }
        Settings.fileTypes[x].setCommonWords(strings);
      }
    }

  } // saveSettings

  /**
   * This returns a JPanel with the options regarding which file types are
   * used to detect plagiarism. This incorporates choosing which directory
   * they are stored in.
   *
   * @return this tab's contents on a JPanel
   */
  private JPanel createFileTypesToUseTab() {
    // Main panel
    JPanel fileTypesTab = new JPanel(new BorderLayout());
    fileTypesTab.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Panel to hold the file type selections and output directories.
    JPanel directoriesPanel = new JPanel(new GridBagLayout());
    directoriesPanel.setBorder(BorderFactory.createTitledBorder
                               ("Select file types to use:"));

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.gridy = 0;

    // Components to use.
    fttuComponents = new FTToUseComponents();

    JLabel jl;

    // Create the checkboxes and output directory textfields for
    // each type. If the user is detecting now,  disable these objects
    // as to change them will need tokenising to be redone.
    for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
      constraints.gridx = 1;
      constraints.gridy++;
      FileTypeProfile profile = Settings.fileTypes[x];
      fttuComponents.inUseToggleButtons[x].setText
          (profile.getDescription());
      fttuComponents.inUseToggleButtons[x].setSelected
          (profile.isInUse());
      fttuComponents.inUseToggleButtons[x].addItemListener(this);
      if (detecting) {
        fttuComponents.inUseToggleButtons[x].setEnabled(false);
      }
      directoriesPanel.add(fttuComponents.inUseToggleButtons[x],
                           constraints);

      constraints.gridy++;
      jl = new JLabel("Output directory: ");
      directoriesPanel.add(jl, constraints);
      constraints.gridx = 2;
      fttuComponents.directories[x].setText(profile.getDirectory());
      if (!fttuComponents.inUseToggleButtons[x].isSelected()
          || detecting) {
        fttuComponents.directories[x].setEditable(false);
      }
      directoriesPanel.add(fttuComponents.directories[x], constraints);
    } // for

    constraints.gridx = 1;
    constraints.gridy++;
    jl = new JLabel("Any matches found will be stored");
    directoriesPanel.add(jl, constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    jl = new JLabel("in the Match directory:");
    directoriesPanel.add(jl, constraints);
    constraints.gridx = 2;
    fttuComponents.matchDirectory.setText(Settings.sherlockSettings.
                                          getMatchDirectory());
    directoriesPanel.add(fttuComponents.matchDirectory, constraints);

    fileTypesTab.add(directoriesPanel, BorderLayout.NORTH);

    // Exclude file tab.
    JPanel excludeFilePanel = new JPanel(new GridBagLayout());
    excludeFilePanel.setBorder
        (BorderFactory.createTitledBorder
         ("Select exclude file to use (in source directory):"));

    constraints.gridx = 1;
    constraints.gridy = 1;
    fttuComponents.excludeFile.setText(Settings.sherlockSettings.
                                       getExcludeFile());
    // If no exclude file selected, don't let the user look at it.
    if (fttuComponents.excludeFile.getText().equals("")) {
      fttuComponents.excludeFile.setText("No exclude file");
      fttuComponents.viewEFButton.setEnabled(false);
    }
    excludeFilePanel.add(fttuComponents.excludeFile, constraints);

    constraints.gridx = 2;
    fttuComponents.efButton.addActionListener(this);
    excludeFilePanel.add(fttuComponents.efButton, constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    fttuComponents.viewEFButton.addActionListener(this);
    excludeFilePanel.add(fttuComponents.viewEFButton, constraints);

    fileTypesTab.add(excludeFilePanel, BorderLayout.CENTER);

    // Put the restore all defaults button on the main panel too!
    fttuComponents.rad.addActionListener(this);
    fileTypesTab.add(fttuComponents.rad, BorderLayout.SOUTH);

    return fileTypesTab;
  } // createFileTypesTab

  /**
   * This returns a new JPanel with all the text fields and labels in
   * the right place
   *
   * @param fileType the file type
   * @return a JPanel with no data in it
   */
  private JPanel createFileTypeTab(int fileType) {
    FileTypeProfile profile = Settings.fileTypes[fileType];
    if (fileType == Settings.SEN) {
      return createSentenceTab(profile, fileType);
    }
    FileTypeComponents comp = new FileTypeComponents();

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    JPanel options = new JPanel(gbl);

    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    constraints.gridx = 1;
    constraints.gridy = 1;
    JLabel l = new JLabel("Minimum string length to store:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[0].setText(String.valueOf
                                 (profile.getMinStringLength()));
    options.add(comp.settingValue[0], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    l = new JLabel("Minimum run length to store:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[1].setText(String.valueOf
                                 (profile.getMinRunLength()));
    options.add(comp.settingValue[1], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    l = new JLabel("Maximum forward jump:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[2].setText(String.valueOf
                                 (profile.getMaxForwardJump()));
    options.add(comp.settingValue[2], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    l = new JLabel("Maximum backward jump:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[3].setText(String.valueOf
                                 (profile.getMaxBackwardJump()));
    options.add(comp.settingValue[3], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    l = new JLabel("Maximum jump difference:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[4].setText(String.valueOf(profile.getMaxJumpDiff()));
    options.add(comp.settingValue[4], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    l = new JLabel("Strictness:");
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[5].setText(String.valueOf(profile.getStrictness()));
    options.add(comp.settingValue[5], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    comp.amal.setSelected(profile.getAmalgamate());
    comp.amal.addItemListener(this);
    options.add(comp.amal, constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    comp.conc.setSelected(profile.getConcatanate());
    comp.conc.addItemListener(this);
    options.add(comp.conc, constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    comp.rd.addActionListener(this);
    options.add(comp.rd, constraints);

    // Save this set of components.
    ftComponents[fileType] = comp;

    JPanel toReturn = new JPanel();
    toReturn.add(options);
    return toReturn;
  } // createFileTypeTab

  private JPanel createSentenceTab(FileTypeProfile profile, int fileType) {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    JPanel options = new JPanel(gbl);

    String tooltip = "Setting either of these values higher reduces the " +
        "number of sentences the parser marks and vice-versa";

    String[] commonWords = profile.getCommonWords();
    final SentenceComponents comp = new SentenceComponents(commonWords);

    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    //constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    constraints.gridx = 1;
    constraints.gridy = 1;

    JLabel l = new JLabel("Similarity Threshold");
    l.setToolTipText(tooltip);
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[0] = new NumberField(10, 2);
    comp.settingValue[0].setText(String.valueOf
                                 (profile.getSimThreshold()));
    comp.settingValue[0].setToolTipText(tooltip);
    options.add(comp.settingValue[0], constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;

    l = new JLabel("Common Threshold");
    l.setToolTipText(tooltip);
    options.add(l, constraints);
    constraints.gridx = 2;
    comp.settingValue[1].setText(String.valueOf
                                 (profile.getCommonThreshold()));
    comp.settingValue[1].setToolTipText(tooltip);
    options.add(comp.settingValue[1], constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.gridwidth = 2;

    comp.mem.setSelected(profile.getMemIntensive());
    options.add(comp.mem, constraints);

    constraints.gridx = 1;
    constraints.gridy = 4;
    constraints.gridwidth = 2;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = constraints.BOTH;

    options.add(comp.listPanel, constraints);
    comp.add.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String temp = JOptionPane.showInputDialog(comp.listPanel, "Which word?",
                                                  "Common words",
                                                  JOptionPane.QUESTION_MESSAGE);
        if (temp == null || temp.equals("") || temp.equals(" ")) {
          return;
        }
        if (temp.indexOf('*') != -1 || temp.indexOf('/') != -1) {
          JOptionPane.showMessageDialog(comp.listPanel, "Characters \'*\'" +
                                        " and \'/\' are not allowed!",
                                        "Illegal Word!",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }
        ( (DefaultListModel) comp.wordList.getModel()).addElement(temp);
      }
    });

    comp.remove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selected = comp.wordList.getSelectedValues();
        if (selected == null || selected.length == 0) {
          return;
        }

        for (int i = 0; i < selected.length; i++) {
          ( (DefaultListModel) comp.wordList.getModel()).
              removeElement(selected[i]);
        }
      }
    });

    constraints.gridx = 1;
    constraints.gridy++;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.fill = constraints.NONE;
    comp.rd.addActionListener(this);
    options.add(comp.rd, constraints);

    // Save this set of components.
    ftComponents[fileType] = comp;

    JPanel toReturn = new JPanel(new BorderLayout());
    //constraints = new GridBagConstraints(1,1,1,1,1.0,0,GridBagConstraints.CENTER,
    //    GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0);
    toReturn.add(options, BorderLayout.CENTER);
    return toReturn;
  }

}
