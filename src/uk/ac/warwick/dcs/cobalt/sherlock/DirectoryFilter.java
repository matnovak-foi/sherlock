package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * A FileFilter which will return a list of sub-directories of the currenty
 * directory.
 *
 * @author Weiliang Zhang
 */
public class DirectoryFilter
    implements FileFilter {
  public boolean accept(File name) {
    return name.isDirectory();
  }
}
