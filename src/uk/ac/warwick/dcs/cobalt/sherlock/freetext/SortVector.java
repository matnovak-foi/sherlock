/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.util.*;

/**
 * A Vector which attempts to sort its elements whenever a Comparable object
 * is added. Behaviour is not defined should there be objects in the Vector
 * which either do not implement Comparable or are not mutually-comparable.
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 * @author Daniel White
 * @version 4
 */

public class SortVector
    extends Vector {

  /**
   * Calls the empty constructor in java.util.Vector
   */
  public SortVector() {
    super();
  }

  /**
   * Adds the <code>Comparable</code> object in the normal way and then sorts
   * all elements in the Vector according to their natural ordering. Non-
   * mutually-comparable objects should not be added as they will cause this
   * method to fail.
   * @param c The Comparable object to be added.
   */
  public void add(Comparable c) {
    super.add(c);
    sortElements();
  }

  /**
   * Sorts all elements contained within the Vector. If any objects within
   * the Vector either do not implement <code>Comparable</code> or are not
   * mutually comparable then this method will fail, throwing a
   * <code>ClassCastException</code>.
   */
  public void sortElements() {
    Arrays.sort(elementData, 0, elementCount - 1);
  }
}
