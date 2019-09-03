/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

/**
 * The GUI extension of MyFrame.
 * The GUI that extends this class is the control window for its program.
 * This class has the code needed to control which windows are visible or not
 * on every window's Window menu. The openWindows vector from MyFrame here
 * is the control vector of windows that is passed to all child windows
 * when refreshing their Window menus.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class MyGUI
    extends MyFrame {

  /**
   * Creates a new MyGUI with the passed title.
   */
  public MyGUI(String title) {
    super(null, title);
  }

  /**
   * Add a window to the MyGUI's record of open windows, and to
   * all open windows' Window menu.
   *
   * @param newWindow the new window
   */
  public void addWindow(MyFrame newWindow) {
    openWindows.add(newWindow);
    reloadWindowMenus();
  }

  /**
   * Remove a window from the Window menu.
   *
   * @param windowToRemove the window to remove
   */
  public void removeWindow(MyFrame windowToRemove) {
    openWindows.remove(windowToRemove);
    reloadWindowMenus();
  }

  /**
   * Reload every window's Window menu.
   */
  private void reloadWindowMenus() {
    for (int x = 0; x < openWindows.size(); x++) {
      ( (MyFrame) openWindows.get(x)).setWindowMenu(openWindows);
    }
  }
}
