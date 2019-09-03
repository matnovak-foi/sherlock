package uk.ac.warwick.dcs.cobalt.sherlock;

import java.util.*;

/**
 * Edge in Match Graph.
 *
 * @author Weiliang Zhang
 * @version 20 Sep 2002
 */
public class MatchGraphEdge {
  /**
   * Index of vertex 1 in a vertices list/array.
   */
  private int v1;

  /**
   * Index of vertex 1 in a vertices list/array.
   */
  private int v2;

  /**
   * Weight of this edge.
   */
  private int weight = 0;

  /**
   * Linked list holding the indices of the matches represented by this
   * edge. These indices come from the matches array from the MatchScreen
   * class.
   */
  private LinkedList indices;

  /**
   * Construct an edge.
   *
   * @param v1 index of vertex 1 in a vertices list/array
   * @param v2 index of vertex 2 in a vertices list/array
   * @param w weight of this edge
   * @param index index of this match in the matches array.
   */
  public MatchGraphEdge(int v1, int v2, int w, int index) {
    indices = new LinkedList();
    indices.add(new Integer(index));
    this.v1 = v1;
    this.v2 = v2;
    weight = w;
  }

  /**
   * Add a match index to this edge, i.e. this match to is between the
   * same 2 fils linked by this edge.
   */
  public void add(int index) {
    indices.add(new Integer(index));
  }

  /**
   * Get the indices of the matches represented by this edge. Indexing
   * is the same as the matches array in MatchesScreen.
   */
  public LinkedList getMatchIndices() {
    return indices;
  }

  /**
   * Set the weight of this edge.
   */
  public void setWeight(int w) {
    weight = w;
  }

  /**
   * The weight of this edge.
   */
  public int getWeight() {
    return weight;
  }

  /**
   * Return the index of vertex 1 in the vertices linked list.
   */
  public int getVertex1() {
    return v1;
  }

  /**
   * Return the index of vertex 2 in the vertices linked list.
   */
  public int getVertex2() {
    return v2;
  }

  /**
   * @return true if the edges are between the same vertices.
   */
  public boolean equals(MatchGraphEdge other) {
    return (v1 == other.getVertex1() && v2 == other.getVertex2())
        || (v2 == other.getVertex1() && v1 == other.getVertex2());
  }
}
