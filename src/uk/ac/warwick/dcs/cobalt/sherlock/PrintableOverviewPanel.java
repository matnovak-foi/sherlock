package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Printable version of the navigator, contains overviews for all
 * preprocessed files.
 *
 * @author Weiliang Zhang
 * @version 26 Sep 2002
 */
public class PrintableOverviewPanel
    extends JPanel {
  private LinkedList[] sectionlists = new LinkedList[8];
  private LinkedList[] linelists = new LinkedList[8];
  private Point2D[] points = new Point2D[16];
  private int[] lines = new int[2];

  File[] files = new File[2];

  /**
   * CONSTANTS FOR RECTANGLE DIMENSIONS, used together with respective
   * ratios for fit into a panel.
   */
  private final int FILEWIDTH = 100;
  private final int LINEWIDTH = 80;

  private LinkedList sectionlist;
  private LinkedList linelist;

  /**
   * Line widths. Wider lines are used for file boundries.
   */
  private static final BasicStroke normal = new BasicStroke(4.0f);
  private static final BasicStroke wide = new BasicStroke(8.0f);

  /**
   * Construct panel.
   *
   * @param matches all matches for this pair of file.
   * @param file1 filename of the first file
   * @param file2 filename of the second file
   */
  public PrintableOverviewPanel(LinkedList[] matches,
                                String file1, String file2) {
    setBackground(Color.white);
    //setup coordinates.
    //printing is assume to be done on a piece of 1024*768 size paper,
    //containing n sub pages, where n is the number of preprocessed
    //versions.
    points[0] = new Point2D.Float(10.0f, 10.0f);
    points[1] = new Point2D.Float(130.0f, 10.0f);
    points[2] = new Point2D.Float(274.0f, 10.0f);
    points[3] = new Point2D.Float(394.0f, 10.0f);
    points[4] = new Point2D.Float(530.0f, 10.0f);
    points[5] = new Point2D.Float(650.0f, 10.0f);
    points[6] = new Point2D.Float(786.0f, 10.0f);
    points[7] = new Point2D.Float(906.0f, 10.0f);
    points[8] = new Point2D.Float(10.0f, 1078.0f);
    points[9] = new Point2D.Float(130.0f, 1078.0f);
    points[10] = new Point2D.Float(274.0f, 1078.0f);
    points[11] = new Point2D.Float(394.0f, 1078.0f);
    points[12] = new Point2D.Float(530.0f, 1078.0f);
    points[13] = new Point2D.Float(650.0f, 1078.0f);
    points[14] = new Point2D.Float(786.0f, 1078.0f);
    points[15] = new Point2D.Float(906.0f, 1078.0f);

    files[0] = new File(file1);
    files[1] = new File(file2);

    //count number of lines in original files.
    String tmp = null;
    for (int i = 0; i < files.length; i++) {
      try {
        BufferedReader br = new BufferedReader
            (new FileReader(files[i]));
        lines[i] = 0;
        //count the number of lines in file.
        while ( (tmp = br.readLine()) != null) {
          lines[i]++;
        }
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog
            (this, "Cannot read file: " + files[i].getAbsolutePath(),
             "Failed to read from file", JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    //for each type of preprocessed files.
    for (int i = 0; i < 8; i++) {
      sectionlists[i] = new LinkedList();
      linelists[i] = new LinkedList();

      //construct matched sections
      ListIterator itr = matches[i].listIterator();
      while (itr.hasNext()) {
        Match m = (Match) itr.next();
        int length1 = m.getRun().getEndCoordinates()
            .getOrigLineNoInFile1()
            - m.getRun().getStartCoordinates()
            .getOrigLineNoInFile1();
        int length2 = m.getRun().getEndCoordinates()
            .getOrigLineNoInFile2()
            - m.getRun().getStartCoordinates()
            .getOrigLineNoInFile2();

        //add sections & lines to linked list
        sectionlists[i].add(new Rectangle2D.Float
                            ( (int) points[2 * i].getX() + 10,
                             (int) points[2 * i].getY() + m.getRun()
                             .getStartCoordinates()
                             .getOrigLineNoInFile1(), LINEWIDTH, length1));
        sectionlists[i].add(new Rectangle2D.Float
                            ( (int) points[2 * i + 1].getX() + 10,
                             (int) points[2 * i + 1].getY() + m.getRun()
                             .getStartCoordinates()
                             .getOrigLineNoInFile2(), LINEWIDTH, length2));
        linelists[i].add(new Line2D.Float
                         ( (int) points[2 * i].getX() + FILEWIDTH - 10,
                          (int) points[2 * i].getY() + m.getRun()
                          .getStartCoordinates()
                          .getOrigLineNoInFile1() + length1 / 2,
                          (int) points[2 * i + 1].getX() + 10,
                          (int) points[2 * i + 1].getY() + m.getRun()
                          .getStartCoordinates()
                          .getOrigLineNoInFile2() + length2 / 2));
      }
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    //scale to file on current panel.
    double yscale = 1;
    if (lines[0] > lines[1]) {
      yscale = (double) getHeight() / 2 / ( (double) lines[0] / 0.75);
    }
    else {
      yscale = (double) getHeight() / 2 / ( (double) lines[1] / 0.75);
    }
    double xscale = (double) getWidth() / 1024.0d;

    g2.scale(xscale, yscale);

    //turn on antialiasing.
    RenderingHints antialiasing = new RenderingHints
        (RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
    g2.addRenderingHints(antialiasing);

    for (int i = 0; i < 8; i++) {
      //draw string labels
      g2.setStroke(normal);
      g2.drawString(files[0].getName(), (int) points[i].getX(),
                    (int) points[i].getY() - g2.getFontMetrics()
                    .getHeight());
      g2.drawString(files[1].getName(), (int) points[i].getX() + 36,
                    (int) points[i].getY() - g2.getFontMetrics()
                    .getHeight());

      //draw file boundries
      g2.setStroke(wide);
      Rectangle2D rect = new Rectangle2D.Float
          ( (int) points[2 * i].getX(), (int) points[2 * i].getY(),
           FILEWIDTH, lines[0]);
      g2.draw(rect);
      g2.setColor(Color.lightGray);
      g2.fill(rect);
      g2.setColor(Color.black);

      rect = new Rectangle2D.Float
          ( (int) points[2 * i + 1].getX(),
           (int) points[2 * i + 1].getY(),
           FILEWIDTH, lines[1]);
      g2.draw(rect);
      g2.setColor(Color.lightGray);
      g2.fill(rect);
      g2.setColor(Color.black);

      //draw sections
      g2.setStroke(normal);
      ListIterator itr = sectionlists[i].listIterator();
      while (itr.hasNext()) {
        Rectangle2D r = (Rectangle2D) itr.next();
        //	    System.out.println(r.toString());
        g2.draw(r);
        g2.setColor(Color.yellow);
        g2.fill(r);
        g2.setColor(Color.black);
      }

      //draw lines to link sections
      itr = linelists[i].listIterator();
      while (itr.hasNext()) {
        g2.draw( (Line2D) itr.next());
      }
    }

    g2.dispose();
    g.dispose();
  }
}
