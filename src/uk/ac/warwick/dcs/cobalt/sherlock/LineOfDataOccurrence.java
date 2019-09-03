/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * LineOfDataOccurrence keeps track of where a line appears: in
 * which file, and what its position in that file is.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
class LineOfDataOccurrence
    implements Serializable {

  /**
   * File this line of data was found in - FILEA, FILEB or FILEX.
   */
  private int fileType = 0;

  /**
   * Line number of the line of data.
   */
  private int lineNo = 0;

  /**
   * Original line number of the line of data.
   */
  private int origLineNo = 0;

  /**
   * Constructor for a LineOfDataOccurrence.
   *
   * @param fileType File this line of data was found in: FILEA, FILEB
   * or FILEX
   * @param lineNo Line number of the line of data
   * @param origLineNo Original line number of the line of data
   */
  LineOfDataOccurrence(int fileType, int lineNo, int origLineNo) {
    this.fileType = fileType;
    this.lineNo = lineNo;
    this.origLineNo = origLineNo;
  }

  /**
   * Return the file type.
   *
   * @return the file type
   */
  int getFileType() {
    return fileType;
  }

  /**
   * Return the line number.
   *
   * @return the line number
   */
  int getLineNo() {
    return lineNo;
  }

  /**
   * Return the original line number.
   *
   * @return the original line number
   */
  int getOrigLineNo() {
    return origLineNo;
  }

} // LineOfDataOccurrence
