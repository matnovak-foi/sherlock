package uk.ac.warwick.dcs.cobalt.sherlock;

import java.awt.*;
import java.awt.print.*;
import javax.swing.*;

/**
 * Class to generate the print out from Sherlock. This table is not shown,
 * it is needed because java.awt.print package can only print Components.
 * In Addition to the table, the original submissions are printed as well.
 * This is done by using JDK1.4's features, because otherwise a text file
 * will have to be wrapped into a Component, which increases the printing
 * job size dramatically!
 *
 * @author Weiliang Zhang
 * @version 30 Aug 2002
 */
public class PrintableTable
    extends JTable
    implements Printable {
  private JTable table;
  private JFrame frame;

  /**
   * Generate a printing version and send it to the printer specified by
   * user.
   */
  public PrintableTable(PrintableTableModel model) {
    table = new JTable(model);
    table.setAlignmentX(Component.CENTER_ALIGNMENT);
    table.doLayout();
    JScrollPane spane = new JScrollPane(table);
    frame = new JFrame("Summary");
    frame.getContentPane().add(spane);
    frame.pack();
    //frame.show();

    // for faster printing turn double buffering off
    RepaintManager.currentManager(frame).setDoubleBufferingEnabled(false);
  }

  /**
   * Print a JTable component.
   */
  public int print(Graphics g, PageFormat pageFormat,
                   int pageIndex) throws PrinterException {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(Color.black);
    int fontHeight = g2.getFontMetrics().getHeight();
    int fontDesent = g2.getFontMetrics().getDescent();

    //leave room for page number
    double pageHeight =
        pageFormat.getImageableHeight() - fontHeight;
    double pageWidth =
        pageFormat.getImageableWidth();
    double tableWidth = (double)
        table.getColumnModel(
        ).getTotalColumnWidth();
    double scale = 1;
    if (tableWidth >= pageWidth) {
      scale = pageWidth / tableWidth;
    }

    double headerHeightOnPage =
        table.getTableHeader(
        ).getHeight() * scale;
    double tableWidthOnPage = tableWidth * scale;

    double oneRowHeight = (table.getRowHeight() +
                           table.getRowMargin()) * scale;
    int numRowsOnAPage =
        (int) ( (pageHeight - headerHeightOnPage) /
               oneRowHeight);
    double pageHeightForTable = oneRowHeight *
        numRowsOnAPage;
    int totalNumPages =
        (int) Math.ceil( (
        (double) table.getRowCount()) /
                        numRowsOnAPage);
    if (pageIndex >= totalNumPages) {
      return NO_SUCH_PAGE;
    }

    g2.translate(pageFormat.getImageableX(),
                 pageFormat.getImageableY());
    //bottom center
    g2.drawString("Page: " + (pageIndex + 1),
                  (int) pageWidth / 2 - 35, (int) (pageHeight
        + fontHeight - fontDesent));

    g2.translate(0f, headerHeightOnPage);
    g2.translate(0f, -pageIndex * pageHeightForTable);

    //If this piece of the table is smaller
    //than the size available,
    //clip to the appropriate bounds.
    if (pageIndex + 1 == totalNumPages) {
      int lastRowPrinted =
          numRowsOnAPage * pageIndex;
      int numRowsLeft =
          table.getRowCount()
          - lastRowPrinted;
      g2.setClip(0,
                 (int) (pageHeightForTable * pageIndex),
                 (int) Math.ceil(tableWidthOnPage),
                 (int) Math.ceil(oneRowHeight *
                                 numRowsLeft));
    }
    //else clip to the entire area available.
    else {
      g2.setClip(0,
                 (int) (pageHeightForTable * pageIndex),
                 (int) Math.ceil(tableWidthOnPage),
                 (int) Math.ceil(pageHeightForTable));
    }

    g2.scale(scale, scale);
    table.paint(g2);
    g2.scale(1 / scale, 1 / scale);
    g2.translate(0f, pageIndex * pageHeightForTable);
    g2.translate(0f, -headerHeightOnPage);
    g2.setClip(0, 0,
               (int) Math.ceil(tableWidthOnPage),
               (int) Math.ceil(headerHeightOnPage));
    g2.scale(scale, scale);
    table.getTableHeader().paint(g2);
    //paint header at top

    return Printable.PAGE_EXISTS;
  }
}
