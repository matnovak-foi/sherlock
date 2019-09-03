package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
//import java.awt.geom.Line2D.*;
//import java.awt.geom.Rectangle2D.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Panel containing match graphs.
 *
 * @author Weiliang Zhang
 * @version 20 Sep 2002
 */
public class MatchGraphPanel
    extends JPanel
    implements MouseMotionListener, MouseListener {
  /**
   * Zoom in/out.
   */
  private JSlider zoomSlider;

  /**
   * Percentage slider.
   */
  private JSlider percentSlider;

  /**
   * Status bar.
   */
  private JLabel statusBar;

  /**
   * Bounding rectangle representing vertices. It has the same indices as the
   * vertices linked list in MatchGraph class.
   */
  private CustomisedRectangle[] vertexModels;

  /**
   * Radius of a node.
   */
  private final float VERTEX_DIAMETER = 18.0f;

  /**
   * Radius of the virtual circle.
   */
  private final float DIAMETER = 400.0f;

  /**
   * Initial position of the bounding rectangle.
   */
  private float x = 40.0f;
  private float y = 40.0f;

  /**
   * Constant values to shift the centre of virtual circles for different
   * connected components.
   */
  private final float DELTA_X = 480.0f;
  private final float DELTA_Y = 480.0f;

  /**
   * Bounding rectangle of the virtual circle. Same indexing as variable CC.
   */
  private Rectangle2D VC[];

  /**
   * Lines representing edges.
   */
  private LinkedList lines;

  /**
   * Line widths. Wider lines are used for file boundries.
   */
  private static final BasicStroke S_NORMAL = new BasicStroke(1.0f);
  private static final BasicStroke S_LEVEL1 = new BasicStroke(2.0f);
  private static final BasicStroke S_LEVEL2 = new BasicStroke(4.0f);
  private static final BasicStroke S_LEVEL3 = new BasicStroke(6.0f);

  /**
   * Line widths. Wider lines are used for file boundries.
   */
  private static final Color C_LEVEL1 = Color.yellow;
  private static final Color C_LEVEL2 = Color.orange;
  private static final Color C_LEVEL3 = Color.red;

  /**
   * Colour when selected.
   */
  private static final Color C_SELECTED = Color.blue;

  /**
   * Normal colour.
   */
  private static final Color C_NORMAL = Color.black;

  /**
   * Linked list of linked list(s) storing the connected components.
   */
  private LinkedList CC = new LinkedList();

  /**
   * All matches. Used to load up matches.
   */
  private Match[] matches;

  /**
   * Parent GUI.
   */
  private MyGUI gui;

  /**
   * Parent container.
   */
  private MatchesScreen parent;

  /**
   * In addition to the original Rectangle2D class, add colour & stroke
   * support for each rectangle so that they can be painted accroding to
   * their respective colours.
   *
   * @author Weiliang Zhang
   * @version 20 Sep 2002
   */
  private class CustomisedRectangle
      extends Rectangle2D.Double {
    /**
     * Colour to draw this Rectangle.
     */
    private Color color;

    /**
     * Thickness of this line.
     */
    private Stroke stroke;

    /**
     * Name of this vertex.
     */
    private String name;

    /**
     * Construct a bounding rectangle representation a vertex. <p>
     * For performance issuses, the name of the vertex is also stored to
     * avoid searching through arrays.
     *
     * @param x x coordinate of the top left point of this rectangle.
     * @param y y coordinate of the top left point of this rectangle.
     * @param width width of this rectangle.
     * @param height height of this rectangle.
     * @param color colour of the bounding lines of this rectangle.
     * @param stroke thickness of the bounding lines of this rectangle.
     * @param name the name of this vertex.
     */
    public CustomisedRectangle(double x, double y, double width,
                               double height, Color color, Stroke stroke,
                               String name) {
      super(x, y, width, height);
      this.color = color;
      this.stroke = stroke;
      this.name = name;
    }

    public Color getColor() {
      return color;
    }

    public Stroke getStroke() {
      return stroke;
    }

    public String getName() {
      return name;
    }

    public void setColor(Color c) {
      color = c;
    }

    public void setStroke(Stroke s) {
      stroke = s;
    }
  }

  /**
   * In addition to the original Line2D class, add colour & stroke support
   * for each line so that they can be painted accroding to their respective
   * colours & thicknesses.
   *
   * @author Weiliang Zhang
   * @version 20 Sep 2002
   */
  private class CustomisedLine
      extends Line2D.Double {
    /**
     * Colour to draw this line.
     */
    private Color color;

    /**
     * Thickness of this line.
     */
    private Stroke stroke;

    /**
     * Weight of this edge.
     */
    private int weight;

    /**
     * Index of the first rectangle this line is connected to in the array.
     */
    private int r1index = -1;

    /**
     * Index of the second rectangle this line is connected to in the array.
     */
    private int r2index = -1;

    /**
     * Construct a graphical representation of a graph edge.<p>
     * For performance issus, edge weight, the indices of the rectangles
     * connected are stored to avoid searching through arrays or lists,
     * which, for a large data set, can be very time consuming.
     *
     * @param x1 x coordinate of the 1st point on this line.
     * @param y1 y coordinate of the 1st point on this line.
     * @param x2 x coordinate of the 2nd point on this line.
     * @param y2 y coordinate of the 2nd point on this line.
     * @param color colour of this edge.
     * @param stroke thickness of this edge.
     * @param weight weight of this edge in graph.
     * @param r1 index of the first vertex this edge is connected to.
     * @param r2 index of the second vertex this edge is connected to.
     */
    public CustomisedLine(double x1, double y1, double x2, double y2,
                          Color color, Stroke stroke, int weight,
                          int r1, int r2) {
      super(x1, y1, x2, y2);
      this.color = color;
      this.stroke = stroke;
      this.weight = weight;
      r1index = r1;
      r2index = r2;
    }

    public Color getColor() {
      return color;
    }

    public Stroke getStroke() {
      return stroke;
    }

    public int getWeight() {
      return weight;
    }

    public int getR1Index() {
      return r1index;
    }

    public int getR2Index() {
      return r2index;
    }

    public void setColor(Color c) {
      color = c;
    }

    public void setStroke(Stroke s) {
      stroke = s;
    }
  }

  /**
   * Disjoint set vertex. Used to find the connected components in a graph.
   *
   * @author Weiliang Zhang
   * @version 20 Sep 2002
   */
  private class DisjointSetVertex {
    /**
     * The index of this vertex in the graph's 'vertices' linked list.
     */
    private int index;

    /**
     * Parent of this node. Parent is null iff this node is a
     * representative.
     */
    private DisjointSetVertex parent = null;

    /**
     * Construct a set containing one node.
     */
    public DisjointSetVertex(int i) {
      index = i;
      parent = null;
    }

    /**
     * The index of this vertex in the graph's 'vertices' linked list.
     */
    public int getIndex() {
      return index;
    }

    /**
     * Find the representative of the set that this vertex belongs.
     */
    public DisjointSetVertex findSet() {
      DisjointSetVertex rep = this;
      while (rep.getParent() != null) {
        rep = rep.getParent();
      }
      return rep;
    }

    /**
     * Joint 2 sets. This set will be joined to the given set. I.E. the
     * representative in this set will be set to point to the
     * representative in the given set.
     */
    public void union(DisjointSetVertex v) {
      this.findSet().setParent(v.findSet());
    }

    public DisjointSetVertex getParent() {
      return parent;
    }

    public void setParent(DisjointSetVertex s) {
      parent = s;
    }
  }

  /**
   * Construct graphical view of the graph. The panel is splited into an
   * n row by 2 column grid. Each connected component is presented in a
   * separate slot in the the grid.
   *
   * @param g parent GUI
   * @param graph Match Graph for a given data set.
   * @param ms matches for a given data set.
   * @param parent parent container.
   */
  public MatchGraphPanel(MyGUI g, final MatchGraph graph, Match[] ms,
                         MatchesScreen parent) {
    setBackground(Color.white);
    gui = g;
    matches = ms;
    this.parent = parent;
    zoomSlider = new JSlider(1, 10, 1);
    percentSlider = new JSlider(0, 100, 1);

    statusBar = new JLabel();
    statusBar.setHorizontalAlignment(JLabel.RIGHT);
    statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    vertexModels = new CustomisedRectangle[graph.size()];
    lines = new LinkedList();

    addMouseMotionListener(this);
    addMouseListener(this);

    //process each Connected Component
    connectedComponent(graph);

    //set preferred size so that scrollbar will appear when necessary
    int scale = zoomSlider.getValue();
    setPreferredSize(new Dimension(1000 * scale,
                                   600 * (int)
                                   Math.ceil( ( (double) CC.size()) / 2.0)
                                   * scale));

    VC = new Rectangle2D[CC.size()];
    for (int i = 0; i < CC.size(); i++) {
      LinkedList nodes = (LinkedList) CC.get(i);

      //construct vertices & edges for this component.
      //construct all rectangles. Vertices are EVENTLY distributed
      //along a big virtual circle.
      final double ANGLE = 2.0d * Math.PI / nodes.size();
      // centre of VC
      double centreX;
      double centreY;
      if (i == 0) {
        centreX = x + DIAMETER / 2.0;
        centreY = y + DIAMETER / 2.0;
      }
      else if (i % 2 == 1) {
        centreX = x + DELTA_X + DIAMETER / 2.0;
        centreY = y + (double) (i / 2) * DELTA_Y + DIAMETER / 2.0;
      }
      else {
        centreX = x + DIAMETER / 2.0;
        centreY = y + (double) (i / 2) * DELTA_Y + DIAMETER / 2.0;
      }

      //virtual circle is created for use in mouse listener.
      VC[i] = new Rectangle2D.Double(centreX, centreY,
                                     DIAMETER, DIAMETER);

      for (double j = 0.0d; j < nodes.size(); j++) {
        //centre for each vertex
        double dx = (DIAMETER / 2) * Math.cos(j * ANGLE);
        double dy = (DIAMETER / 2) * Math.sin(j * ANGLE);

        //index is used to find the vertex in the graph's vertices
        //linked list.
        int index = ( (DisjointSetVertex) nodes.get( (int) j))
            .getIndex();
        String name = ( (MatchGraphVertex) graph.getVertices()
                       .get(index)).getName();
        vertexModels[index] = new CustomisedRectangle
            (centreX + dx, centreY - dy, VERTEX_DIAMETER,
             VERTEX_DIAMETER, C_NORMAL, S_NORMAL, name);
      }
    }

    //construct edges.
    Iterator itr = graph.getEdges().values().iterator();
    while (itr.hasNext()) {
      MatchGraphEdge edge = (MatchGraphEdge) itr.next();

      //can determine which lines should be draw.
      Color color = C_NORMAL;
      Stroke stroke = S_NORMAL;
      if (edge.getWeight() > 20) {
        color = C_LEVEL3;
        stroke = S_LEVEL3;
      }
      else if (edge.getWeight() > 10) {
        color = C_LEVEL2;
        stroke = S_LEVEL2;
      }
      else if (edge.getWeight() > 4) {
        color = C_LEVEL1;
        stroke = S_LEVEL1;
      }

      //find the rectangles for the 2 vertices.
      //if not find, skip.
      if (edge.getVertex1() < 0 || edge.getVertex2() < 0) {
        break;
      }

      Rectangle2D rect1 = vertexModels
          [edge.getVertex1()];
      Rectangle2D rect2 = vertexModels
          [edge.getVertex2()];

      //find the centres
      double x1 = rect1.getX() + rect1.getWidth() / 2.0;
      double y1 = rect1.getY() + rect1.getWidth() / 2.0;
      double x2 = rect2.getX() + rect2.getWidth() / 2.0;
      double y2 = rect2.getY() + rect2.getWidth() / 2.0;
      //construct edge & add to list.
      lines.add(new CustomisedLine
                (x1, y1, x2, y2, color, stroke, edge.getWeight(),
                 edge.getVertex1(), edge.getVertex2()));
    }
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    //turn on antialiasing.
    RenderingHints antialiasing = new RenderingHints
        (RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
    g2.addRenderingHints(antialiasing);

    int scale = zoomSlider.getValue();
    g2.scale(scale, scale);

    //set preferred size so that scrollbar will appear when necessary
//  	setPreferredSize(new Dimension(1000 * scale,
//  				       600 * (int)
// 				       Math.ceil(((double) CC.size()) / 2.0)
// 				       * scale));

    //draw edges.
    int reference = percentSlider.getValue();
    ListIterator itr = lines.listIterator();
    while (itr.hasNext()) {
      CustomisedLine line = (CustomisedLine) itr.next();
      //if line's weight is less than the slider value, don't draw it.
      if (line.getWeight() < reference) {
        continue;
      }
      //apply color & stroke.
      Color oriC = g2.getColor();
      Stroke oriS = g2.getStroke();
      g2.setColor(line.getColor());
      g2.setStroke(line.getStroke());

      g2.draw(line);
      //reset
      g2.setColor(oriC);
      g2.setStroke(oriS);
    }

    //draw vertices.
    for (int i = 0; i < vertexModels.length; i++) {
      int x = (int) vertexModels[i].getX();
      int y = (int) vertexModels[i].getY();
      int width = (int) vertexModels[i].getWidth();
      //apply color & stroke.
      Color oriC = g2.getColor();
      Stroke oriS = g2.getStroke();
      g2.setColor(vertexModels[i].getColor());
      g2.setStroke(vertexModels[i].getStroke());
      g2.drawOval(x, y, width, width);
      //reset
      g2.setColor(oriC);
      g2.setStroke(oriS);
    }

    g2.dispose();
    g.dispose();
  }

  public JSlider getZoomSlider() {
    return zoomSlider;
  }

  public JSlider getPercentSlider() {
    return percentSlider;
  }

  public JLabel getStatusBar() {
    return statusBar;
  }

  /**
   * Return the number of connected components. Used to resize the panel.
   */
  public int getNumberOfCC() {
    if (CC == null) {
      return 0;
    }
    else {
      return CC.size();
    }
  }

  /**
   * Find the connected components of a given graph and store the result
   * in private private variable CC.
   */
  private void connectedComponent(MatchGraph graph) {
    //vertexSets has the same indexing as the vertices linked list in
    //graph. Hence we can use the index stored in each edge to access it
    //directly.
    LinkedList vertexSets = new LinkedList();
    for (int i = 0; i < graph.getVertices().size(); i++) {
      vertexSets.add(new DisjointSetVertex(i));

    }
    Hashtable edges = graph.getEdges();
    Iterator itr = edges.values().iterator();
    while (itr.hasNext()) {
      MatchGraphEdge edge = (MatchGraphEdge) itr.next();
      DisjointSetVertex v1 =
          (DisjointSetVertex) vertexSets.get(edge.getVertex1());
      DisjointSetVertex v2 =
          (DisjointSetVertex) vertexSets.get(edge.getVertex2());
      if (v1.findSet() != v2.findSet()) {
        v1.union(v2);
      }
    }

    //find out how many disjoint sets there are.
    LinkedList reps = new LinkedList();
    itr = vertexSets.listIterator();
    while (itr.hasNext()) {
      if (reps.contains( ( (DisjointSetVertex) itr.next()).findSet())) {
        continue;
      }
      else {
        reps.add( ( (DisjointSetVertex) itr.next()).findSet());
      }
    }

    //place each vertex in their respective disjoint set in CC.
    //firstly, create linked list for each set.
    for (int i = reps.size(); i > 0; i--) {
      CC.add(new LinkedList());
      //place each vertex in the right place.
    }
    itr = vertexSets.listIterator();
    while (itr.hasNext()) {
      DisjointSetVertex v = (DisjointSetVertex) itr.next();
      ( (LinkedList) CC.get(reps.indexOf(v.findSet()))).add(v);
    }
  }

  // MouseMotionListener interface
  public void mouseDragged(MouseEvent e) {}

  /**
   * When mouse positioned on a vertex, set the status bar to show its info.
   * When mouse positioned on (closer enough to) an edge, set the status bar
   * to show the edge info.
   */
  public void mouseMoved(MouseEvent e) {
    //scale the coordinates to the originals
    int scale = zoomSlider.getValue();
    double x = ( (double) e.getX()) / ( (double) scale);
    double y = ( (double) e.getY()) / ( (double) scale);

    //check whether the mouse if over a vertex and which
    int ptr = 0;
    boolean found = false;
    for (int i = 0; i < vertexModels.length; i++) {
      if (vertexModels[i].contains(x, y)) {
        found = true;
        ptr = i;
        //highlight this vertex by setting thicker stroke
        vertexModels[i].setStroke(S_LEVEL3);
      }
      else {
        vertexModels[i].setStroke(S_NORMAL);
      }
    }

    //now ptr points to the vertex
    if (found) {
      //set status bar to indicate this vertex's name.
      statusBar.setText(vertexModels[ptr].getName());
      setToolTipText(vertexModels[ptr].getName());
      return;
    }

    //check whether the mouse is closer enough to an edge
    //if yes, display that edge's info.
    if (lines.size() < 1) {
      return;
    }
    else {
      findClosestLine(x, y);
    }
  }

  /**
   * Find the line that is closest to the current mouse position & set
   * the status bar if necessary.
   *
   * @param x x coordinate of the current mouse position.
   * @param y y coordinate of the current mouse position.
   *
   * @return The int value returned is the index in the lines linked list
   * of the line who is closest to the current mouse position and this
   * distance is within a certain range. If negative values are returned
   * as indices, then there is no line closer enough to the current
   * mouse position.
   */
  private int findClosestLine(double x, double y) {
    //The MAX range allowed for a line to be accepted as the closest line.
    final double MAX_DISTANCE = 0.2;

    double[] distances = new double[lines.size()];
    for (int i = 0; i < distances.length; i++) {
      CustomisedLine edge = (CustomisedLine) lines.get(i);
      Point2D p1 = edge.getP1();
      Point2D p2 = edge.getP2();

      //if mouse is outside the bounding virtual rectangle,
      //ignore this event. This is necessary because the
      //distances are calculated regardless where the lines
      //end, i.e. you can visualise what happens if the
      //edges do not end at vertices.
      //if an edge with weight less then the the percentage slider
      //value, ignore it.
      if ( ( ( (x < p1.getX() && p1.getX() < p2.getX()) ||
              (x > p2.getX() && p1.getX() < p2.getX()) ||
              (x < p2.getX() && p2.getX() < p1.getX()) ||
              (x > p1.getX() && p2.getX() < p1.getX())) &&
            ( (y < p1.getY() && p1.getY() < p2.getY()) ||
             (y > p2.getY() && p1.getY() < p2.getY()) ||
             (y < p2.getY() && p2.getY() < p1.getY()) ||
             (y > p1.getY() && p2.getY() < p1.getY()))) ||
          edge.getWeight() < percentSlider.getValue()) {
        distances[i] = java.lang.Double.MAX_VALUE;
      }
      else {
        //calculate the perpendicular distance from the
        //mouse event point to this edge.
        //first, find out the area of the triangle form by
        //this point, p1 & p2.
        double a = p1.distance(x, y);
        double b = p1.distance(p2);
        double c = p2.distance(x, y);
        double s = (a + b + c) / 2.0;
        double area = Math.sqrt( (s - a) * (s - b) * (s - c));
        distances[i] = 2.0 * area / b;
      }
    }

    //find out the closest edge, ie. min distance
    double min = java.lang.Double.MAX_VALUE;
    int index = -1;
    for (int i = 0; i < distances.length; i++) {
      if (distances[i] < min) {
        min = distances[i];
        index = i;
      }
    }
    //if this min distance is within a certain value,
    //display edge info in statusBar.
    if (min < MAX_DISTANCE) {
      CustomisedLine edge = (CustomisedLine)
          lines.get(index);

      statusBar.setText
          (vertexModels[edge.getR1Index()].getName() + " & "
           + vertexModels[edge.getR2Index()].getName() +
           ", " + edge.getWeight() + "%");

      return index;
    }
    else {
      return -1;
    }
  }

  //MouseListener Interface.
  public void mouseClicked(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {
    //scale the coordinates to the originals
    int scale = zoomSlider.getValue();
    double x = ( (double) e.getX()) / ( (double) scale);
    double y = ( (double) e.getY()) / ( (double) scale);

    int index = findClosestLine(x, y);
    //if no line is found, do nothing.
    if (index < 0) {
      return;
    }

    //if reaches here, a line is found.
    CustomisedLine edge = (CustomisedLine)
        lines.get(index);

    final String selectedfile1 = vertexModels[edge.getR1Index()]
        .getName();
    final String selectedfile2 = vertexModels[edge.getR2Index()]
        .getName();

    //find out all data for this pair.
    LinkedList list = new LinkedList();
    for (int i = 0; i < matches.length; i++) {
      String file1 = truncate(matches[i].getFile1());
      String file2 = truncate(matches[i].getFile2());

      if ( (selectedfile1.equals(file1) &&
            selectedfile2.equals(file2))
          || (selectedfile1.equals(file2) &&
              selectedfile2.equals(file1))) {
        list.add(matches[i]);
      }
    }
    final Match[] pm = new Match[list.size()];
    list.toArray(pm);

    JPopupMenu popup = new JPopupMenu();

    //Left mouse button click brings up popup menu which links to
    //compare match panel
    if (e.getButton() == MouseEvent.BUTTON1) {
      //add menu items, this has to be in a separate loop.
      for (int i = 0; i < pm.length; i++) {
        JMenuItem jmi = new JMenuItem
            (pm[i].getSimilarity() + "%, " +
             Settings.fileTypes[pm[i].getFileType()]
             .getDescription());
        // Set the Match's index to be the actionCommand
        jmi.setActionCommand(String.valueOf(i));

        jmi.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int matchToLoad = Integer.parseInt
                (e.getActionCommand());

            ComparePane cp = new ComparePane
                (gui, pm, pm[matchToLoad], parent);
          }
        });
        popup.add(jmi);
      }
      popup.show( (MatchGraphPanel) e.getSource(), e.getX(), e.getY());
    }
    //right mouse button click brings up popup menu which enables the user
    //to mark a particular match.
    else {
      //add menu items to mark the matches represented by this edge.
      for (int i = 0; i < pm.length; i++) {
        JMenuItem jmi;
        //if this match is already marked as suspicisou,
        //the menu item let user to unmark it.
        if (MatchesScreen.marking.isSuspicious(pm[i].output())) {
          jmi = new JMenuItem
              ("Mark " + pm[i].getSimilarity() + "%, " +
               Settings.fileTypes[pm[i].getFileType()]
               .getDescription() + " as INNOCENT");
        }
        else {
          jmi = new JMenuItem
              ("Mark " + pm[i].getSimilarity() + "%, " +
               Settings.fileTypes[pm[i].getFileType()]
               .getDescription() + " as SUSPICIOUS");

          // Set the Match's index to be the actionCommand
        }
        jmi.setActionCommand(String.valueOf(i));
        jmi.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int matchToLoad = Integer.parseInt
                (e.getActionCommand());

            //find this match's index in the matches array.
            String tag = pm[matchToLoad].output();
            for (int j = 0; j < matches.length; j++) {
              if (tag.equals(matches[j].output())) {
                if (MatchesScreen.marking
                    .isSuspicious(j)) {
                  //match already marked suspicious,
                  //unmark it.
                  MatchesScreen.marking.remove(j);
                }
                else {
                  //mark it suspicious.
                  MatchesScreen.marking.add(j);
                }
                break;
              }
            }
          }
        });
        popup.add(jmi);
      }
      popup.show( (MatchGraphPanel) e.getSource(), e.getX(), e.getY());
    }
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
