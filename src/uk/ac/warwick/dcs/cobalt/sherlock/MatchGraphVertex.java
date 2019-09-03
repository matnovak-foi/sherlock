package uk.ac.warwick.dcs.cobalt.sherlock;

/**
 * Node in Match Graph.
 *
 * @author Weiliang Zhang
 * @version 20 Sep 2002
 */
public class MatchGraphVertex {
  private String name;

  /**
   * Construct a vertex with given name.
   *
   * @param n name of the vertex.
   */
  public MatchGraphVertex(String n) {
    name = n;
  }

  public String getName() {
    return name;
  }

  /**
   * Whether given vertex has the same name as this vertex.
   */
  public boolean equals(MatchGraphVertex other) {
    return name.equals(other.getName());
  }

  /**
   * Hash code of vertex name.
   */
  public int hashCode() {
    return name.hashCode();
  }
}
