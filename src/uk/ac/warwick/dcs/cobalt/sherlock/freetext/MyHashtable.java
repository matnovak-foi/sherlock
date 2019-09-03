/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.util.*;

/**
 *
 * <P>A simple extension to the java.util.Hashtable object which ensures that
 * when Integer objects are key and String objects are the value then no values
 * are overwritten. The put method now searches for the first empty space after
 * the required one. This must be remembered when calling get since get may
 * not return the object you require if it is has been placed somewhere
 * else.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 *
 * @author Daniel White
 * @version 4
 */

public class MyHashtable
    extends Hashtable {
  /**
   * Constructor. Simply calls the default hashtable constructor.
   */
  public MyHashtable() {
    super();
  }

  /**
   * Constructor. Simply calls the correct hashtable constructor.
   * @param initialCapacity The required initial capacity of the hashtable.
   */
  public MyHashtable(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Additional put method. Ensures that a chaining algorithm is used so that
   * no values are overwritten in the table. Both the key and the value must be
   * non-null.
   *
   * @param key The hashcode for the value
   * @param value The value you wish to store in the table
   * @return returns null if the value was not present in the table. Otherwise,
   * returns the value iff it is already present in the table.
   */
  public synchronized Object put(Integer key, String value) {
    if (key == null || value == null) {
      throw new NullPointerException();
    }
    String prev = get(key);
    while (prev != null) {
      // if the value is present, no need to store it again.
      if (prev.equals(value)) {
        return value;
      }
      // try the next address.
      key = new Integer(key.intValue() + 1);
      prev = get(key);
    }
    // use the normal method to place the pair in the table since we know there
    // is no value at this address.
    return put( (Object) key, (Object) value);
  }

  /**
   * Returns the string stored with the given key. You must never use this
   * class if you are using Integer keys to store something other than String
   * objects.
   *
   * @param key The index of the required string.
   * @return The String stored at this index.
   */
  public synchronized String get(Integer key) {
    return (String) get( (Object) key);
  }
}