/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * This class either shows the message window, or the exclude file.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
class TextWindow
    extends MyFrame {

  /**
   * The message window text area. It is static so that messages can be passed
   * to it using Settings.message(String). Also used by the ProgressWindow.
   */
  static JTextArea messages = new JTextArea();

  /**
   * Constructor for a TextWindow.
   *
   * @param gui - the MyGUI that's running
   * @param title - the title of the window; this also determines whether the
   *  messages are shown, or the exclude file
   */
  TextWindow(MyGUI gui, String title) {
    super(gui, title);

    // Update all windows' Window menus.
    gui.addWindow(this);

    // Scrollpane used to view the messages or exclude file.
    JScrollPane jsp = new JScrollPane();

    // Show the messages window.
    if (title.equals("Messages")) {
      messages.setEditable(false);
      jsp.setViewportView(messages);
    }

    // Show the file that has been passed.
    else {
      // If the file is valid, load it up.
      File file = new File(Settings.sourceDirectory,
                           Settings.sherlockSettings.getExcludeFile());
      JTextArea jta = new JTextArea();
      try {
        jta.read(new BufferedReader(new FileReader(file)), file);
        jsp.setViewportView(jta);
      }
      catch (IOException e) {
        // If there are any problems, show error message and close the window.
        JOptionPane.showMessageDialog(this, "Error reading the exclude file,\n" +
                                      file.getAbsolutePath() + "\n" +
                                      e.toString(),
                                      "View exclude file error",
                                      JOptionPane.OK_OPTION);
        gui.removeWindow(this);
        dispose();
      }
    }

    getContentPane().add(jsp);

    JMenu fileMenu = getJMenuBar().getMenu(FILE_MENU);
    JMenuItem jmi = new JMenuItem("Close");
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeMe();
      }
    });
    fileMenu.add(jmi);
    getJMenuBar().getMenu(OPTIONS_MENU).setEnabled(false);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeMe();
      }
    });

    setSize(300, 200);
  } // TextWindow

} // TextWindow
