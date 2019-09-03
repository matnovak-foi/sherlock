package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import javax.print.*;
import javax.print.attribute.*;

/**
 * Construct print out evidence for a pair of suspicious submissions.<p>
 * This is a series of documents, their structure is like a linked list.
 * Please refer to MultiDoc's API doc for details. This structure makes it
 * easy to insert a new page into the print out, no matter what form this
 * new page is in.
 *<p>
 * Note: Some of the sections of the print out are commented out because
 * existing bugs in JDK caused them to be printed incorrectly/to generate
 * runtime exceptions. You can find detailed comments in the code where these
 * code are commented out. If you'd like to put them back to use, the order
 * of the sections in the print job needs to be customised.
 *
 * @author Weiliang Zhang
 * @version 26 Sep 2002
 */
public class PrintOut
    implements MultiDoc {
  private PrintableTableModel model;

  public PrintOut(PrintableTableModel m) {
    model = m;
  }

  public Doc getDoc() {
    // return the printable summary table.
    PrintableTable table = new PrintableTable(model);
    DocFlavor f = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
    DocAttributeSet das = new HashDocAttributeSet();
    Doc summary = new SimpleDoc(table, f, das);
    return summary;
  }

  public Attribute getAttribute() {
    return null;
  }

  public MultiDoc next() {
    //The line below is used to print out all matched sections for this
    //pair of files in landscape mode. However, javax.print package
    //seems to have some bug which alwyas forced text print jobs to be
    //printed in portrait mode. This line can be uncommented when this bug
    //is fixed.
    return (new MatchSectionPrintOut(model.getMatches()));

    /*File file1 = new File(Settings.sourceDirectory
           + Settings.fileSep
           + Settings.fileTypes[Settings.ORI]
           .getDirectory(),
           model.getColumnName(0) + "." +
           Settings.fileTypes[Settings.ORI]
           .getExtension());
      File file2 = new File(Settings.sourceDirectory
           + Settings.fileSep
           + Settings.fileTypes[Settings.ORI]
           .getDirectory(),
           model.getColumnName(1) + "." +
           Settings.fileTypes[Settings.ORI]
           .getExtension());
      //The line below is to print out an graphical view of the matched
      //sections for this match. It has the same content as the navigator
      //in the CompanePane class. However, jdk 1.4.0_1 for Linux has a bug
      //which caused image buffer overflows when printing this document.
      //This feature works fine in Windows with the same version of JDK.
      //This code can be used when this bug is removed.
// 	return new PrintableOverviewPanel(model.getMatches(), file1.getName(),
// 					  file2.getName());
      //print source code files.
      return (new TextFilePrintOut(file1, file2));*/
  }

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
}
