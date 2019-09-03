package uk.ac.warwick.dcs.cobalt.sherlock;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Construct graphs for all matches found. Vertices are distributed around a
 * virtual circle.
 *
 * @author Weiliang Zhang
 * @version 21 Sep 2002
 */
public class MatchGraphFrame
    extends MatchesScreen {
  /**
   * Zoom slider. It should be obtained from MatchGraphPanel.
   */
  private JSlider zoomSlider;

  /**
   * Percentage slider. It should be obtained from MatchGraphPanel.
   */
  private JSlider percentSlider;

  /**
   * The panel that graphs would be painted on.
   */
  private MatchGraphPanel pane;

  /**
   * JScrollPane containing the graph pane.
   */
  private JScrollPane spane;

  public MatchGraphFrame(MyGUI gui, Marking m) {
    super(gui, "Match Graph");
    Arrays.sort(matches);

    if (matches == null || matches.length == 0) {
      return;
    }

    marking = m;
    marking.setMatches(matches);
    marking.generate();

    setSize(1000, 600);

    setUpMenus();

    MatchGraph graph = new MatchGraph(matches);
    pane = new MatchGraphPanel(gui, graph, matches, this);
    spane = new JScrollPane(pane);
    //use SIMPEL_SCROLL_MODE, otherwise the pane is crapped up when
    //scrolled. This is bceause sometimes the paint algorithm
    //doesn't work correctly when you use custom painting.
    spane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
    getContentPane().add(spane, BorderLayout.CENTER);

    statusBar = pane.getStatusBar();
    getContentPane().add(statusBar, BorderLayout.SOUTH);
    statusBar.setText("Ready");

    //setup zoom slider
    final JLabel zoom = new JLabel("Zoom: *1");
    zoomSlider = pane.getZoomSlider();
    zoomSlider.setToolTipText("Zoom: *" + zoomSlider.getValue());
    zoomSlider.addChangeListener(new ChangeListener() {
      //repaint graph when zoomed.
      public void stateChanged(ChangeEvent e) {
        if (e.getSource() == zoomSlider) {
          int scale = zoomSlider.getValue();

          //set new panel preferred size for scroll bar to work
          pane.setPreferredSize
              (new Dimension(1000 * scale,
                             600 * (int)
                             Math.ceil
                             ( ( (double) pane.getNumberOfCC())
                              / 2.0) * scale));

          //set new tool tip text.
          zoomSlider.setToolTipText
              ("Zoom: *" + zoomSlider.getValue());
          zoom.setText("Zoom: *" + zoomSlider.getValue());

          repaint();
        }
      }
    });

    //setup percentage slider
    percentSlider = pane.getPercentSlider();
    final JLabel percent = new JLabel("Percentage above (including) " +
                                      percentSlider.getValue()
                                      + "% will be shown:");
    percentSlider.setToolTipText
        (percentSlider.getValue() + "%");
    percentSlider.addChangeListener(new ChangeListener() {
      //reset label & repaint
      public void stateChanged(ChangeEvent e) {
        percent.setText("Percentage above (including) "
                        + percentSlider.getValue()
                        + "% will be shown:");
        percentSlider.setToolTipText
            (percentSlider.getValue() + "%");
        pane.repaint();
      }
    });

    JPanel sliderPanel = new JPanel();
    sliderPanel.add(zoom, BorderLayout.WEST);
    sliderPanel.add(zoomSlider, BorderLayout.WEST);
    sliderPanel.add(percent, BorderLayout.EAST);
    sliderPanel.add(percentSlider, BorderLayout.EAST);
    getContentPane().add(sliderPanel, BorderLayout.NORTH);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
// 		    pane.save();
        closeMe();
      }
    });
    setVisible(true);
  }

  /**
   * Set up the menus for this DisplayMatches.
   */
  private void setUpMenus() {
    JMenuBar jmb = getJMenuBar();

    // Remove the options menu.
    JMenu jm = jmb.getMenu(OPTIONS_MENU);
    jm.setEnabled(false);

    // Add Close to the File menu.
    JMenu fileMenu = jmb.getMenu(FILE_MENU);

    JMenuItem jmi = new JMenuItem("Close", KeyEvent.VK_O);
    jmi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        closeMe();
      }
    });
    fileMenu.add(jmi);

    JMenu editMenu = jmb.getMenu(EDIT_MENU);
    editMenu.setEnabled(false);
  } // setUpMenus
}
