/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.util.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * An enhanced JFrame to use as a base frame for all the non-dialogs in
 * Sherlock. This class includes the menu bar that they all have - handling
 * the Help menu and ensuring this frame is selected on the Window menu.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @author Terri Mak
 * @version 2nd Sep 2002
 */

public class MyFrame
    extends JFrame {

  /**
   * Constants to access the different menus.
   */
  protected final static int FILE_MENU = 0, EDIT_MENU = 1, OPTIONS_MENU = 2;
  protected final static int WINDOW_MENU = 3, HELP_MENU = 4;

  /**
   * A vector containing all the currently open windows.
   *
   * @serial
   */
  List openWindows = new ArrayList();

  /**
   * This window's index on the Window menu.
   *
   * @serial
   */
  private int windowIndex;

  /**
   * The GUI that's running.
   *
   * @serial
   */
  MyGUI gui;

  HelpGuideFrame helpguide = new HelpGuideFrame();

  /**
   * Create a new MyFrame with the specified title and parent GUI.
   *
   * @param gui - the parent GUI.
   * @param title - the window's title.
   */
  public MyFrame(MyGUI gui, String title) {
    this(title);
    this.gui = gui;
  } // MyFrame

  /**
   * Create a new MyFrame with the specified title without specifying a
   * parent GUI - called by the GUI!
   *
   * @param title the window's title
   */
  MyFrame(String title) {
    super(title);

    // File menu:
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    // Options menu:
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);

    // Options menu:
    JMenu optionsMenu = new JMenu("Options");
    optionsMenu.setMnemonic(KeyEvent.VK_O);

    // Window menu:
    JMenu windowMenu = new JMenu("Window");
    windowMenu.setMnemonic(KeyEvent.VK_W);

    // Help menu:
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);

    JMenuItem jmi = new JMenuItem("Help topics...", KeyEvent.VK_H);
    jmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        helpguide.openWindow();

      }
    });
    helpMenu.add(jmi);

    helpMenu.addSeparator();

    jmi = new JMenuItem("About Sherlock 2003");
    jmi.setMnemonic(KeyEvent.VK_A);
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog
            ( (MyFrame) openWindows.get(windowIndex),
             "Sherlock 2003\n" +
             "Version " + Settings.SHERLOCKVERSION + "\n" +
             "Benjamin Hart, Mike Joy, William Smith, Robert Pitt,"
             + "\nAshley Ward, Weiliang Zhang, Terri Mak\n" +
             "and Daniel White",
             "About Sherlock 2003",
             JOptionPane.INFORMATION_MESSAGE);
      }
    });
    helpMenu.add(jmi);

    // Menu bar itself:
    JMenuBar mb = new JMenuBar();
    mb.add(fileMenu);
    mb.add(editMenu);
    mb.add(optionsMenu);
    mb.add(windowMenu);
    mb.add(helpMenu);
    setJMenuBar(mb);

    // Add the window listener that checks this window on
    // the Window menu when it gets the focus.
    addWindowListener(new WindowAdapter() {
      public void windowActivated(WindowEvent e) {
        JMenuBar jmb = getJMenuBar();
        JMenu jm = jmb.getMenu(MyFrame.WINDOW_MENU);
        JMenuItem jmi = jm.getItem(windowIndex);
        if (jmi != null) {
          jmi.setSelected(true);
        }
      }
    });

  } // setUpMenus

  /**
   * Sets the contents of the Window menu.
   *
   * @param windows a List of MyFrames to go on the Window menu
   */
  void setWindowMenu(List windows) {
    openWindows = windows;

    JMenu windowMenu = getJMenuBar().getMenu(MyFrame.WINDOW_MENU);
    windowMenu.removeAll();
    ButtonGroup group = new ButtonGroup();

    // Add all windows in the passed vector to this window's Window menu
    // and button group.
    for (int x = 0; x < openWindows.size(); x++) {
      String title = ( (MyFrame) openWindows.get(x)).getTitle();
      // If adding this window to the menu, ensure that the windowIndex
      // variable is correct.
      if (getTitle().equals(title)) {
        windowIndex = x;
      }
      JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(title);
      jrbmi.setActionCommand(String.valueOf(x));
      jrbmi.addActionListener(new ActionListener() {
        // When the user selects a window on the menu, give
        //it the focus.
        public void actionPerformed(ActionEvent e) {
          JRadioButtonMenuItem chosenWindow =
              (JRadioButtonMenuItem) e.getSource();
          int index = Integer.parseInt
              (chosenWindow.getActionCommand());
          ( (MyFrame) openWindows.get(index)).requestFocus();
        } // actionPerformed
      }
      );
      windowMenu.add(jrbmi);
      group.add(jrbmi);
    }

  } // setWindowMenu

  /**
   * Close this frame, notifying the gui.
   */
  public void closeMe() {
    gui.removeWindow(this);
    dispose();
  } // closeMe

  public MyGUI getGUI() {
    return gui;
  }
}
