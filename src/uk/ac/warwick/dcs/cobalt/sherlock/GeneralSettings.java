/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The General Settings tab, where the user can select the language to tested.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 16 July 2000
 */
class GeneralSettings
    extends JDialog
    implements ActionListener {

  /**
   * Java button.
   *
   * @serial
   */
  private JRadioButton java;

  /**
   * Creates a modal GeneralSettings dialog.
   *
   * @param gui the MyGUI owner of this dialog
   */
  GeneralSettings(MyGUI gui) {
    super(gui, "General Settings", true);

    JPanel jp = new JPanel(new GridLayout(0, 1));
    JLabel jl = new JLabel("Select language of the files to be tested:");
    jp.add(jl);
    ButtonGroup bg = new ButtonGroup();
    java = new JRadioButton("Java", true);
    bg.add(java);
    jp.add(java);
    JRadioButton cplusplus = new JRadioButton("C++", false);
    bg.add(cplusplus);
    jp.add(cplusplus);

    // Panel holding the buttons at the bottom of the screen.
    JPanel buttonPanel = new JPanel();
    JButton jb = new JButton("OK");
    jb.addActionListener(this);
    buttonPanel.add(jb);
    jb = new JButton("Cancel");
    jb.addActionListener(this);
    buttonPanel.add(jb);

    getContentPane().add(jp, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setResizable(false);
    pack();
  } // GeneralSettings

  /**
   * Handle action events.
   */
  public void actionPerformed(ActionEvent e) {
    JButton button = (JButton) e.getSource();

    // OK
    //// if (button.getText() == "OK") {
    if (button.getText().equals("OK")) {
      Settings.sherlockSettings.setJava(java.isSelected());
      this.dispose();
    }

    // Cancel
    //// else if (button.getText() == "Cancel")
    else if (button.getText().equals("Cancel")) {
      this.dispose();
    }

  } // actionPerformed

}
