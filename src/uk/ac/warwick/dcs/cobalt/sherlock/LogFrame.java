package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.event.*;
import javax.swing.*;

/**
 * Windows displaying the log file.
 *
 * @author Weiliang Zhang
 * @version 14 Aug 2002
 */
public class LogFrame
    extends MyFrame {
  /**
   * Construct a frame displaying the log file.
   *
   * @param gui MyGUI object that is currently running.
   * @param file The log file.
   */
  public LogFrame(MyGUI gui, File file) {
    super(gui, "View Log File");
    setSize(600, 300);

    JMenu fileMenu = getJMenuBar().getMenu(FILE_MENU);
    JMenuItem jmi = new JMenuItem("Close");
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeMe();
      }
    });
    fileMenu.add(jmi);
    getJMenuBar().getMenu(OPTIONS_MENU).setEnabled(false);
    getJMenuBar().getMenu(WINDOW_MENU).setEnabled(false);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        closeMe();
      }
    });

    //Read from log file.
    String text = "";
    try {
      BufferedReader in = new BufferedReader
          (new FileReader(file));
      String tmp;
      while ( (tmp = in.readLine()) != null) {
        text += tmp + "\n";
      }
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (this, "Cannot read from file: " + file,
           "Failed to open Log file.", JOptionPane.ERROR_MESSAGE);
      Date day = new Date(System.currentTimeMillis());
      try {
        BufferedWriter out = new BufferedWriter
            (new FileWriter(file.getAbsolutePath(), true));
        out.write(day + "-Cannont read from file: " + file);
        out.newLine();
        out.close();
      }
      catch (IOException e2) {
        //if failed to write to log file, write to stderr
        System.err.println(day + "-Cannot read from file " + file);
      }
    }

    JTextArea msg = new JTextArea(text);
    msg.setEditable(false);
    JScrollPane jsp = new JScrollPane();
    jsp.setViewportView(msg);

    getContentPane().add(jsp);
  }
}
