/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * This class is simply a filter that only allows files ending in .match
 * It is needed by MatchesScreen.loadMatches() as that is a static method,
 * so putting the filter in this separate file saves having every class that
 * calls loadMatches() having to implement FilenameFilter and passing itself.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class MatchFilenameFilter
    implements FilenameFilter {

  public MatchFilenameFilter() {
  } // MatchFilenameFilter

  /**
   * File is accepted if it ends with .match
   *
   * @param dir - the directory in which the file was found.
   * @param name - the name of the file.
   */
  public boolean accept(File dir, String name) {
    if (name.endsWith(".match")) {
      return true;
    }
    else {
      return false;
    }
  } // accept

}
