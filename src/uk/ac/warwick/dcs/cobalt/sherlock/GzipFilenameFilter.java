package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * Accept filenames in Gzip format.
 * @author Weiliang Zhang
 */

public class GzipFilenameFilter
    implements FilenameFilter {
  public boolean accept(File dir, String name) {
    File f = new File(dir, name);
    TextFileFilter tff = new TextFileFilter();

    return (!tff.accept(f) && !f.isDirectory()
            && (name.endsWith(".gz") || name.endsWith(".GZ")
                || name.endsWith("tgz") || name.endsWith("TGZ")));
  }
}
