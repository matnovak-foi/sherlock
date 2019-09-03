package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * Accepts all files in either ZIP format.
 * @author Weiliang Zhang
 */

public class ZipFilenameFilter
    implements FilenameFilter {
  /*
   * Return true if and only if file ends with one of the extension names:
   * zip, ZIP.
   */
  public boolean accept(File dir, String name) {
    File f = new File(dir, name);
    TextFileFilter tff = new TextFileFilter();

    return (!tff.accept(f) && !f.isDirectory()
            && (name.endsWith(".zip") || name.endsWith("ZIP")));
  }
}
