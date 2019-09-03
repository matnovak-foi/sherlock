/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The option window used by ComparePane to change the viewing options.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 16 July 2000
 */
class ViewingOptions
    extends JDialog
    implements ActionListener {

  /**
   * True if OK was pressed to close the dialog.
   *
   * @serial
   */
  private boolean okpressed = false;

  /**
   * Show all of the submitted files?
   *
   * @serial
   */
  private JCheckBox jcbSub = new JCheckBox("Show whole of submitted files");

  /**
   * Show all of the tokenised files?
   *
   * @serial
   */
  private JCheckBox jcbTok = new JCheckBox("");

  /**
       * Number of lines to show either side of suspicious code in the submitted file.
   *
   * @serial
   */
  private JTextField jtfSub = new JTextField(2);

  /**
       * Number of lines to show either side of suspicious code in the tokenised file.
   *
   * @serial
   */
  private JTextField jtfTok = new JTextField(2);

  /**
   * Create a new option window.
   *
   * @param parent the parent frame
   * @param fileTypeDescription the file type description to display next
   *  to the tokenised checkbox
   */
  ViewingOptions(MyFrame parent, String fileTypeDescription) {
    super(parent, "Viewing options", true);

    JPanel jp = new JPanel(new GridLayout(4, 2));

    // Submitted file options.
    jcbSub.setSelected(Settings.sherlockSettings.getShowWholeSub());
    jp.add(jcbSub);
    jp.add(new JLabel(""));
    jp.add(new JLabel(
        "Number of lines to display either side of suspicious code:"));
    jtfSub.setText(String.valueOf(Settings.sherlockSettings.getSubLinesToShow()));
    jp.add(jtfSub);

    // Tokenised file options.
    jcbTok.setText(fileTypeDescription);
    jcbTok.setSelected(Settings.sherlockSettings.getShowWholeTok());
    jp.add(jcbTok);
    jp.add(new JLabel(""));
    jp.add(new JLabel(
        "Number of lines to display either side of suspicious code:"));
    jtfTok.setText(String.valueOf(Settings.sherlockSettings.getTokLinesToShow()));
    jp.add(jtfTok);

    // Panel holding the buttons at the bottom of the screen.
    JPanel buttonPanel = new JPanel();
    JButton jb = new JButton("OK");
    jb.addActionListener(this);
    buttonPanel.add(jb);
    jb = new JButton("Cancel");
    jb.addActionListener(this);
    buttonPanel.add(jb);
    jb = new JButton("Help");
    jb.addActionListener(this);
    buttonPanel.add(jb);

    getContentPane().add(jp, BorderLayout.CENTER);
    getContentPane().add(buttonPanel, BorderLayout.SOUTH);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.pack();
  } // ViewingOptions

  /**
   * Return whether the user pressed cancel or not.
   *
   * @return true if this dialog was cancelled, false if ok was pressed.
   */
  boolean okPressed() {
    return okpressed;
  } // okPressed

  /**
   * Handle action events.
   */
  public void actionPerformed(ActionEvent e) {
    JButton button = (JButton) e.getSource();

    // OK
    //// if (button.getText() == "OK") {
    if (button.getText().equals("OK")) {
      try {
        int toSave = Integer.valueOf(jtfSub.getText()).intValue();
        Settings.sherlockSettings.setSubLinesToShow(toSave);
        toSave = Integer.valueOf(jtfTok.getText()).intValue();
        Settings.sherlockSettings.setTokLinesToShow(toSave);
      }
      catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this,
            "Numeric characters allowed only in the text fields.", "Error",
                                      JOptionPane.WARNING_MESSAGE);
        return;
      }
      Settings.sherlockSettings.setShowWholeSub(jcbSub.isSelected());
      Settings.sherlockSettings.setShowWholeTok(jcbTok.isSelected());
      okpressed = true;
      this.setVisible(false);
    }

    // Cancel
    //// else if (button.getText() == "Cancel")
    else if (button.getText().equals("Cancel")) {
      this.dispose();
    }

    // Help
    //// else if (button.getText() == "Help") {
    else if (button.getText().equals("Help")) {
    }
  } // actionPerformed

} // ViewingOptions
