/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

/**
 * This interface is implemented by the ProgressWindow and Sherlock, the
 * command line module, to receive exceptions that are thrown by either of
 * the SherlockProcesses, TokeniseFiles or Samelines. Exceptions must be
 * handled in this manner due to the fact that a SherlockProcess is a thread,
 * so they can not simply be thrown in the usual manner. The controlling class
 * can then decide how to handle the situation - it will probably kill
 * the thread. Just prior to calling exceptionThrown, the SherlockProcess
 * will pause itself to wait for further guidance.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public interface SherlockProcessCallback {

  /**
   * Alert the controlling class that an exception has occurred during
   *  a SherlockProcess's processing.
   *
   * @param e the SherlockProcessException thrown
   */
  void exceptionThrown(SherlockProcessException spe);

}
