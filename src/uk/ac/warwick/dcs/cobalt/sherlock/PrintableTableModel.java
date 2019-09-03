package uk.ac.warwick.dcs.cobalt.sherlock;

//import java.awt.*;
import java.io.*;

import javax.swing.table.*;

/**
 * TableModel for the summary table for one pair.
 *
 * @author Weiliang Zhang
 * @version 29 Aug 2002
 */
public class PrintableTableModel
    extends AbstractTableModel {

  private Match[] matches = null;
  private String[] columnNames = {
      "", "", "File Type", "%"};

  /**
   * @param matches - the list of matches to print, should NOT be NULL!
   */
  public PrintableTableModel(Match[] matches) {
    this.matches = matches;
    File file1 = new File(matches[0].getFile1());
    File file2 = new File(matches[0].getFile2());
    columnNames[0] = truncate(file1.getName());
    columnNames[1] = truncate(file2.getName());
  }

  /**
   * Extract original file name from preprocessed filenames.
   */
  private String truncate(String arg) {
    String str = arg;
    int index = str.lastIndexOf(".");
    str = str.substring(0, index);
    return str;
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public int getRowCount() {
    return matches.length;
  }

  public Object getValueAt(int row, int col) {
    Match m = matches[row];
    switch (col) {
      case 3:
        return new Integer(m.getSimilarity());
      case 2:
        return Settings.fileTypes[m.getFileType()].
            getDescription();
      case 0: {
        RunCoordinates rcstart = m.getRun().getStartCoordinates();
        RunCoordinates rcend = m.getRun().getEndCoordinates();
        return (rcstart.getOrigLineNoInFile1() + "-" +
                rcend.getOrigLineNoInFile1());
      }
      case 1: {
        RunCoordinates rcstart = m.getRun().getStartCoordinates();
        RunCoordinates rcend = m.getRun().getEndCoordinates();
        return (rcstart.getOrigLineNoInFile2() + "-" +
                rcend.getOrigLineNoInFile2());
      }
      default:
        return new Object();
    }
// 	return new Object();
    // Never gets here, but must have a return for javac
  }

  public String getColumnName(int column) {
    return columnNames[column];
  }

  public Class getColumnClass(int col) {
    return getValueAt(0, col).getClass();
  }

  /**
   * Return the match array.
   */
  public Match[] getMatches() {
    return matches;
  }
}
