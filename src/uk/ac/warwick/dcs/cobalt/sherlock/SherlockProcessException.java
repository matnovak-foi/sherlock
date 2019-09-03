/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

/**
 * Exception thrown by a SherlockProcess. As well as any text added by the
 * SherlockProcess, this exception also holds the original exception
 * thrown by the JavaVM.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 13 July 2000
 */
public class SherlockProcessException
    extends Exception {

  /**
   * The original exception thrown.
   *
   * @serial
   */
  private Exception originalException;

  /**
   * Constructs an SherlockProcessException with no detail message.
   */
  public SherlockProcessException() {
    super();
    originalException = null;
  } // SherlockProcessException

  /**
   * Constructs an SherlockProcessException with the specified detail message.
   *
   * @param s the detail message
   */
  public SherlockProcessException(String s) {
    super(s);
    originalException = new Exception(s);
  }

  /**
   * Constructs an SherlockProcessException with the specified detail message
   * and original exception.
   *
   * @param s the detail message
   * @param e the original exception
   */
  public SherlockProcessException(String s, Exception e) {
    super(s);
    originalException = e;
  }

  /**
   * Return the original exception.
   *
   * @return the original exception, null if not set
   */
  public Exception getOriginalException() {
    return originalException;
  }

} // SherlockProcessException
