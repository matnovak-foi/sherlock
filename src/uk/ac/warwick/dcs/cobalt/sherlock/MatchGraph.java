package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * Graph representation of all matches for a given data set..
 *
 * @author Weiliang Zhang
 * @version 20 Sep 2002
 */
public class MatchGraph {
  /**
   * A hashtable containing of all edges in this graph.
   */
  private Hashtable edges;

  /**
   * All vertices in this graph.
   */
  private LinkedList vertices;

  /**
   * Construct a graph for a set of matches.
   *
   * @param matches matches for a given data set.
   */
  public MatchGraph(Match[] matches) {
    vertices = new LinkedList();
    edges = new Hashtable();

    //construct all vertices & edges.
    for (int i = 0; i < matches.length; i++) {
      MatchGraphVertex n1 = new MatchGraphVertex
          (truncate(matches[i].getFile1()));
      MatchGraphVertex n2 = new MatchGraphVertex
          (truncate(matches[i].getFile2()));

      //check n1
      boolean found = false;
      int n1index = -1;
      int n2index = -2;
      for (int j = 0; j < vertices.size(); j++) {
        if (n1.equals( (MatchGraphVertex) vertices.get(j))) {
          found = true;
          n1index = j;
          break;
        }
      }

      if (!found) {
        vertices.add(n1);
        n1index = vertices.indexOf(n1);
      }

      //check n2
      found = false;
      for (int j = 0; j < vertices.size(); j++) {
        if (n2.equals( (MatchGraphVertex) vertices.get(j))) {
          found = true;
          n2index = j;
          break;
        }
      }
      if (!found) {
        vertices.add(n2);
        n2index = vertices.indexOf(n2);
      }
      //System.out.println(n1.getName()+n2.getName());
      int percent = matches[i].getSimilarity();
      //System.out.println(n1.getName()+n2.getName()+percent);
      MatchGraphEdge edge = new MatchGraphEdge
          (n1index, n2index, percent, i);

      //this addition ensures that the order of n1 and n2 does not matter
      int code = n1.hashCode() + n2.hashCode();
      int j = 0;
      boolean added = false;
      //if a collision occurs, either they are edges between same
      //vertices, or they are different edges with same hashcode.
      //in the former case, increase the existing edge's weight;
      //in the latter case, add a new edge to the set in sequential
      //order.
      for (; edges.get(new Integer(code + j)) != null; j++) {
        MatchGraphEdge e = (MatchGraphEdge) edges.get
            (new Integer(code + j));

        //detects collision
        if (edge.equals(e)) {
          //modify the weigth
          e.setWeight((e.getWeight() + edge.getWeight())/2);
          //add this new pair of match's index info
          e.add(i);
          added = true;
          break;
        }
      }
      //if new edge found.
      if (!added) {
        edges.put(new Integer(code + j), edge);
      }
    }
  }

  /**
   * Return the weight of 2 given edge.
   *
   * @return positive values if an edge exists between the 2 vertices,
   * zero if no edges exist.
   */
  public int edge(MatchGraphVertex v1, MatchGraphVertex v2) {
    int code = v1.hashCode() + v2.hashCode();
    int j = 0;
    int n1 = indexOf(v1);
    int n2 = indexOf(v2);
    for (; edges.get(new Integer(code + j)) != null; j++) {
      MatchGraphEdge edge = (MatchGraphEdge) edges.get
          (new Integer(code + j));
      //construct a psoude-edge just for the ease of comparison.
      //weight & index are dummy values.
      if (edge.equals(new MatchGraphEdge(n1, n2, 0, 0))) {
        return edge.getWeight();
      }
    }

    //when program reaches here, edge does not exist.
    return 0;
  }

  /**
   * Return the edges set.
   */
  public Hashtable getEdges() {
    return edges;
  }

  /**
   * Return the vertices in this graph.
   */
  public LinkedList getVertices() {
    return vertices;
  }

  /**
   * Return the index of a given Vertex.
   *
   * @return positive integer indices, negative if not found.
   */
  public int indexOf(MatchGraphVertex v) {
    for (int i = 0; i < vertices.size(); i++) {
      if (v.getName().equals
          ( ( (MatchGraphVertex) vertices.get(i)).getName())) {
        return i;
      }
    }

    //reaches here if not found.
    return -1;
  }

  /**
   * Number of vertices.
   */
  public int size() {
    return vertices.size();
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
