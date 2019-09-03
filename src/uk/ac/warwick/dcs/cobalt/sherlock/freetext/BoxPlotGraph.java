package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.text.*;
import java.util.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

/**
     * Configurable JPanel that will display box plots of inputted data series. Does
 * not resize yet.
 * <p>Title: Sherlock 2003</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 1
 */

public class BoxPlotGraph
    extends JPanel {
  public final static int MIN = 0, TWENT_FIFTH = 1, MEDIAN = 2,
      SEVENT_FIFTH = 3, MAX = 4;
  private double[][] stats;
  private int graphHeight, graphWidth, rowHeight, minX, maxX, divideX;
  private String[] labels;
  private int xAxisOffset = 0, componentHeight, componentWidth;
  private boolean firstDraw = true;

  /**
   * Constructor. Ensure that labels and the outer scores array have the same
   * length (ie. scores.length == labels.length), or an
   * <code>IllegalArgumentException</code> will be thrown.
   * @param scores Lists of values to derive a box plot from
   * @param labels Labels for the plots, in order they appear in the scores
   * array
   * @param minX The minimum value for the x axis. The input data should not
   * have any values lower than this.
   * @param maxX The maximum value for the x axis. The input data should not
   * have any values higher than this.
   * @param divideX How many divisions along the x axis. This value should
   * be one less than that which you want. There will always be a label at
   * the origin, so a value of one for <code>divideX</code> will mean there
   * is a label at the end of the axis too.
   */
  public BoxPlotGraph(byte[][] scores, String[] labels, int minX,
                      int maxX, int divideX) {
    if (labels.length != scores.length) {
      throw new IllegalArgumentException("Arrays are different lengths");
    }

    this.labels = labels;
    this.minX = minX;
    this.maxX = maxX;
    this.divideX = divideX;

    setBackground(Color.white);
    setForeground(Color.black);

    calculateData(scores);

    // causes painting to initialise itself
    BufferedImage image = new BufferedImage(600, 600,
                                            BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = GraphicsEnvironment.getLocalGraphicsEnvironment().
        createGraphics(image);
    paintComponent(g2d);

  }

  /**
   * Causes the object to recalculate new box plots using the new data. Scores
   * should be the same length as the original labels array. If you need
   * a different amount of series to be plotted, create a new BoxPlotGraph.
   * @param scores The new input data.
   */
  public void calculateData(byte[][] scores) {
    if (scores.length != labels.length) {
      throw new IllegalArgumentException("New scores array is wrong length");
    }

    stats = new double[scores.length][5];
    for (int i = 0; i < scores.length; i++) {
      // sort data.
      Arrays.sort(scores[i]);

      // min and max values will be at either end of the array
      if (scores[i].length > 0) {
        stats[i][MIN] = scores[i][0];
        stats[i][MAX] = scores[i][scores[i].length - 1];
      }
      else {
        stats[i][MIN] = 0;
        stats[i][MAX] = 0;
      }

      // get the percentile values.
      stats[i][TWENT_FIFTH] = computeStat(0.25 * scores[i].length, scores[i]);
      stats[i][MEDIAN] = computeStat(0.5 * scores[i].length, scores[i]);
      stats[i][SEVENT_FIFTH] = computeStat(0.75 * scores[i].length, scores[i]);
    }

  }

  /**
   * The sets of 5 points used to plot the box plots.
   * @return A 2d array, each row is the values needed for the box plots.
   */
  public double[][] getStats() {
    return stats;
  }

  /**
   * Given a double position in the array this will compute the average of
   * the two values either side of that position in the array.
   * @param position Should be between 0 and scores.length
   * @param scores A data series.
   * @return The average value of the indices in the array, surrounding the
   * given position.
   */
  private double computeStat(double position, byte[] scores) {
    if (scores.length == 0) {
      return 0;
    }

    int ceil = (int) Math.ceil(position);
    int floor = (int) Math.floor(position);
    assert(ceil == floor) || (ceil == floor + 1);

    return (scores[ceil] + scores[floor]) / 2.0;
  }

  // Bookmarks used when drawing the graphics.
  private Point2D.Double origin = null, topYAxis = null, endXAxis = null,
      yLabelPos[] = null, xLabelPos[] = null;
  private Line2D.Double xTick[] = null, yTick[] = null;
  private String xLabels[] = null;

  /**
   * Paints the graph onto the panel.
   * @param g The graphics context.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    //switch on anti-aliasing
    RenderingHints hints = g2.getRenderingHints();
    hints.put(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setRenderingHints(hints);

    if (firstDraw) {
      firstCallToPaint(g2);

      // 1 pixel-thick lines
    }
    g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_BUTT,
                                 BasicStroke.JOIN_ROUND));

    // y axis
    g2.draw(new Line2D.Double(topYAxis, origin));
    // x axis
    g2.draw(new Line2D.Double(origin, endXAxis));

    // ticks on x axis
    for (int i = 0; i < xTick.length; i++) {
      g2.draw(xTick[i]);

      // ticks on y axis
    }
    for (int i = 0; i < yTick.length; i++) {
      g2.draw(yTick[i]);

      // labels on x axis
    }
    for (int i = 0; i < xLabelPos.length; i++) {
      g2.drawString(xLabels[i], (float) xLabelPos[i].getX(),
                    (float) xLabelPos[i].getY());
    }

    //labels on the y axis
    for (int i = 0; i < yLabelPos.length; i++) {
      g2.drawString(labels[i], (float) yLabelPos[i].getX(),
                    (float) yLabelPos[i].getY());
    }

    //very thin lines for the vertical guidelines
    final float dash1[] = {
        10.0f};
    g2.setStroke(new BasicStroke(0.8f, BasicStroke.CAP_BUTT,
                                 BasicStroke.JOIN_ROUND, 1.0f, dash1, 1.0f));
    g2.setPaint(Color.lightGray);
    //vertical guidelines
    for (int i = 1; i < xTick.length; i++) {
      g2.draw(new Line2D.Double(xTick[i].getX1(), xTick[i].getY1(),
                                xTick[i].getX1(), topYAxis.getY()));
    }

    // 1.3 pixel-thick lines
    g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT,
                                 BasicStroke.JOIN_ROUND));
    g2.setPaint(Color.black);
    //box plots
    for (int i = 0; i < stats.length; i++) {
      drawBox(origin.getX(), origin.getY() - i * rowHeight, g2, i);
    }
  }

  /**
   * Draws a box plot at the given coordinates, using the indicated data.
   * @param xOff The offset on the x axis for the bottom left corner of the
   * box plot.
   * @param yOff The offset on the y axis for the bottom left corner of the
   * box plot.
   * @param g2 A Java2D graphics context.
   * @param index The index of the box plot values within the previously
   * calculated <code>stats</code> array.
   */
  private void drawBox(double xOff, double yOff, Graphics2D g2, int index) {
    double[] data = stats[index];
    double[] xPos = new double[data.length];

    double mid = yOff - rowHeight / 2, topY = yOff - rowHeight + 8;
    yOff -= 8;
    for (int i = 0; i < xPos.length; i++) {
      xPos[i] = calcXPos(data[i]) + xOff;
    }
    g2.setPaint(Color.black);

    //draw vertical minimum line
    g2.draw(new Line2D.Double(xPos[MIN], yOff, xPos[MIN], topY));
    //draw line from minimum to 25th percentile
    g2.draw(new Line2D.Double(xPos[MIN], mid, xPos[TWENT_FIFTH], mid));
    //draw box
    g2.setPaint(Color.blue);
    g2.fill(new Rectangle2D.Double(xPos[TWENT_FIFTH], topY + 5,
                                   xPos[SEVENT_FIFTH] - xPos[TWENT_FIFTH],
                                   yOff - topY - 10));
    g2.setPaint(Color.black);
    g2.draw(new Rectangle2D.Double(xPos[TWENT_FIFTH], topY + 5,
                                   xPos[SEVENT_FIFTH] - xPos[TWENT_FIFTH],
                                   yOff - topY - 10));
    //draw from box to max
    g2.draw(new Line2D.Double(xPos[SEVENT_FIFTH], mid, xPos[MAX], mid));
    //draw vertical max line
    g2.draw(new Line2D.Double(xPos[MAX], yOff, xPos[MAX], topY));
    //draw little circle for median
    g2.fill(new Ellipse2D.Double(xPos[MEDIAN] - 3, mid - 3, 6, 6));
  }

  /**
   * Calculates a value for the number of pixels along the x axis a value
   * will occur.
   * @param pos A value on the x axis.
   * @return The distnce along the x axis in pixels.
   */
  private double calcXPos(double pos) {
    return ( (pos - minX) / (maxX - minX)) * graphWidth;
  }

  /**
   * Initialises the fixed positions in the graph, to speed up drawing
   * on subsequent calls.
   * @param g2 The graphics context.
   */
  private void firstCallToPaint(Graphics2D g2) {
    // font metrics to calculate how much room the strings will take.
    FontMetrics metrics = g2.getFontMetrics();

    // calculate the longest label, so the x axis will not overlap.
    for (int i = 0; i < labels.length; i++) {
      int width = metrics.stringWidth(labels[i]);
      if (width > xAxisOffset) {
        xAxisOffset = width;
      }
    }
    xAxisOffset += 15;
    // make rows in the graph 3 times taller than the text.
    rowHeight = metrics.getHeight() * 3;

    // The length of the axes in pixels.
    graphHeight = rowHeight * labels.length;
    graphWidth = 300;

    int distBetweenXTicks = graphWidth / divideX;

    // The area that will enclose the entire graph.
    componentHeight = 20 + graphHeight + metrics.getAscent();
    componentWidth = graphWidth + xAxisOffset + 10;

    // Useful points.
    origin = new Point2D.Double(xAxisOffset, 5 + graphHeight);
    topYAxis = new Point2D.Double(xAxisOffset, 5);
    endXAxis = new Point2D.Double(xAxisOffset + graphWidth,
                                  5 + graphHeight);

    // Useful objects for the x axis.
    xTick = new Line2D.Double[divideX + 1];
    xLabelPos = new Point2D.Double[divideX + 1];
    // also calculates its own labels for the x axis.
    xLabels = new String[divideX + 1];
    NumberFormat nf = NumberFormat.getNumberInstance();
    double xTickValIncrease = (maxX - minX) / (double) divideX;
    double y = origin.getY() + 10 + metrics.getAscent();
    for (int i = 0; i < xTick.length; i++) {
      xTick[i] = new Line2D.Double(origin.getX() + (i * distBetweenXTicks),
                                   origin.getY(),
                                   origin.getX() + (i * distBetweenXTicks),
                                   origin.getY() + 5);
      xLabels[i] = "" + (minX + (i * xTickValIncrease));
      double x = origin.getX() + (i * distBetweenXTicks) -
          metrics.stringWidth(xLabels[i]) / 2;
      xLabelPos[i] = new Point2D.Double(x, y);
    }

    // Useful objects for the y axis.
    yTick = new Line2D.Double[labels.length + 1];
    yLabelPos = new Point2D.Double[labels.length];
    for (int i = 0; i < yTick.length; i++) {
      yTick[i] = new Line2D.Double(origin.getX(),
                                   origin.getY() - i * rowHeight,
                                   origin.getX() - 5,
                                   origin.getY() - i * rowHeight);
    }

    int lblOffset = (rowHeight / 2) - (metrics.getAscent() / 2);
    for (int i = 0; i < yLabelPos.length; i++) {
      yLabelPos[i] =
          new Point2D.Double(xAxisOffset - 10 -
                             metrics.stringWidth(labels[i]),
                             origin.getY() - (i * rowHeight + lblOffset));
    }
    firstDraw = false;
  }

  /**
   * This can probably be changed, I use this when setting the object's
   * preferred size from other objects because setting the preferred size
   * did not work from within the object when I tried it before.
   * @return The panel's preferred size. It's only size really because it does
   * not scale.
   */
  public Dimension getPrefferedDimensions() {
    return new Dimension(componentWidth, componentHeight);
  }
}