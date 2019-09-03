/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class FindDialog
    extends JDialog {

  /**
   * Combo box holding the recent strings that have been searched for.
   *
   * @serial
   *
   * @author Ben Hart
   * @author Mike Joy
   * @version 12 July 2000
   */
  private JTextField toFind = new JTextField(28);

  /**
   * The JCheckBoxes that appear on the Find dialog.
   *
   * @serial
   */
  private JCheckBox panesToSearch[] = new JCheckBox[4];

  /**
   * If selected, find must wrap around the document when it reaches
   * either end during the find.
   *
   * @serial
   */
  private JCheckBox wrapAround = new JCheckBox("Wrap around", true);

  /**
   * If selected, search is case sensitive.
   *
   * @serial
   */
  private JCheckBox matchCase = new JCheckBox("Match case", true);

  /**
   * Set to true if the user selects Next, false if Previous.
   *
   * @serial
   */
  private boolean forward = true;

  /**
   * A new FindDialog to show.
   *
   * @param cp the ComparePane that is the owner of this dialog
   * @param fileTypeDescription the file type descripton
   * @param filename1 the first filename
   * @param filename2 the second filename
   */
  FindDialog(ComparePane cp, String fileTypeDescription, String filename1,
             String filename2) {
    super(cp, "Find", true);
    setUpScreen(fileTypeDescription, filename1, filename2);
    setDefaultCloseOperation(HIDE_ON_CLOSE);
    pack();
    setResizable(false);
  } // FindDialog

  /**
   * Create the screen, using the passed details.
   *
   * @param fileTypeDescription the file type descripton
   * @param filename1 the first filename
   * @param filename2 the second filename
   */
  private void setUpScreen(String fileTypeDescription, String filename1,
                           String filename2) {

    // The main panel with the centre and button panels on.
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // The centre panel with the find and pane panels on.
    JPanel centrePanel = new JPanel(new BorderLayout());

    // The find panel.
    JPanel findPanel = new JPanel(new GridLayout(2, 1));
    findPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));

    JLabel jl = new JLabel("Find what:");
    findPanel.add(jl);
    toFind.setEditable(true);
    findPanel.add(toFind);
    centrePanel.add(findPanel, BorderLayout.NORTH);

    // The panel with the pane selections and wrap around on.
    JPanel panePanel = new JPanel(new GridBagLayout());

    // The GridBagConstraints used.
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    constraints.gridx = 1;
    constraints.gridy = 1;
    jl = new JLabel("Files to search:");
    panePanel.add(jl, constraints);

    constraints.gridy++;
    jl = new JLabel("Submitted:");
    panePanel.add(jl, constraints);

    constraints.gridx++;
    panesToSearch[0] = new JCheckBox(filename1, true);
    panePanel.add(panesToSearch[0], constraints);
    constraints.gridx++;
    panesToSearch[1] = new JCheckBox(filename2, true);
    panePanel.add(panesToSearch[1], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    jl = new JLabel(fileTypeDescription + ":");
    panePanel.add(jl, constraints);

    constraints.gridx++;
    panesToSearch[2] = new JCheckBox(filename1, true);
    panePanel.add(panesToSearch[2], constraints);

    constraints.gridx++;
    panesToSearch[3] = new JCheckBox(filename2, true);
    panePanel.add(panesToSearch[3], constraints);

    constraints.gridx = 1;
    constraints.gridy++;
    panePanel.add(wrapAround, constraints);

    constraints.gridx++;
    panePanel.add(matchCase, constraints);

    centrePanel.add(panePanel, BorderLayout.CENTER);
    mainPanel.add(centrePanel, BorderLayout.CENTER);

    // The panel with the buttons on.
    JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
    JButton jb = new JButton("Next");
    jb.setMnemonic(KeyEvent.VK_N);
    jb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Set the forward flag to true.
        forward = true;
        setVisible(false);
      }
    });
    buttonPanel.add(jb);

    jb = new JButton("Previous");
    jb.setMnemonic(KeyEvent.VK_V);
    jb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Set the forward flag to false.
        forward = false;
        setVisible(false);
      }
    });
    buttonPanel.add(jb);

    jb = new JButton("Cancel");
    jb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        toFind.setText("");
        setVisible(false);
      }
    });
    buttonPanel.add(jb);
    mainPanel.add(buttonPanel, BorderLayout.EAST);

    getContentPane().add(mainPanel);
  } // setUpScreen

  /**
   * Sets the string to be searched for.
   *
   * @param newStringToFind to find - the new string to be searched for
   */
  void setStringToFind(String newStringToFind) {
    toFind.setText(newStringToFind);
  } // setStringToFind

  /**
   * Returns the string to be searched for.
   *
   * @return the string to be searched for
   */
  String getStringToFind() {
    return toFind.getText();
  } // getStringToFind

  /**
   * Returns whether the passed pane is to be searched.
   *
   * @param pane the pane in question
   * @return true if it is to be searched, false if not
   */
  boolean isPanetoBeSearched(int pane) {
    return panesToSearch[pane].isSelected();
  } // isPanetoBeSearched

  /**
   * Return whether the find is to wrap around the documents
   * in the panes when either end is reached.
   *
   * @return true if wrap around is selected, false if not
   */
  boolean isWrapAround() {
    return wrapAround.isSelected();
  } // isWrapAround

  /**
   * Returns whether the search is to be case sensitive or not.
   *
   * @return true if the search is to be case sensitive, false if not
   */
  boolean isMatchCase() {
    return matchCase.isSelected();
  } // isMatchCase

  /**
   * Returns whether to search forward from the current point in
   * the pane being searched.
   *
   * @return true if to search forward, false if to search backwards
   */
  boolean isForward() {
    return forward;
  } // isForward

}
