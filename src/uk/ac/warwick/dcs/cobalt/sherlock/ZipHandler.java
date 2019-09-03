/*
 * Handles ZIP files.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Decompresses files in ZIP format.
 * @author Weiliang Zhang
 * @version 22 July 2002
 */
public class ZipHandler {
  /*
   * Unzip ZIP file given into a directory name after the filename.
   */
  public static int unzip(ZipFile zfile) throws IOException {
    //create sub-dir named after the zipfile.
    String dirname = zfile.getName();
    //System.out.println(dirname);
    int dotindex = dirname.lastIndexOf('.');
    dirname = dirname.substring(0, dotindex);

    File dir = new File(dirname);
    //System.out.println(dir);

    File f;
    InputStream instream;
    BufferedReader br;
    BufferedWriter bw;
    Enumeration emu;
    ZipEntry ze;

    if (!dir.exists()) {
      dir.mkdir();
    }
    else {
      if (!dir.isDirectory()) {
        dir.delete();
      }
      else {
        deleteDir(dir);
      }
      dir.mkdir();
    }

    emu = zfile.entries();
    //System.out.println("finish opening zip file");

    String name;
    //for each file in zip
    while (emu.hasMoreElements()) {
      ze = (ZipEntry) emu.nextElement();
      //System.out.println("dir real path:" + dir.getAbsolutePath());
      //System.out.println("ze name: "+ze.getName());

      //build directory-structure-less filename
      name = ze.getName();
      int slashindex = name.lastIndexOf(Settings.fileSep);
      if (slashindex >= 0 && slashindex < name.length()) {
        name = name.substring(slashindex + 1, name.length());

        //if name is an empty string, then this entry is a directory,
        //skip it.
      }
      if (name.length() == 0) {
        continue;
      }

      f = new File(dir.getAbsolutePath(), name);
      //System.out.println("f real path:" + f.getAbsolutePath());
      //if file exists, the new file will be renamed to
      //filename.extensionXX where XX is a number.
      int i = 1;
      while (f.exists()) {
        String tmp = name.concat(String.valueOf(i));
        f = new File
            (dir.getAbsolutePath(), tmp);
        i++;
      }

      instream = zfile.getInputStream(ze);
      br = new BufferedReader(new InputStreamReader(instream));
      bw = new BufferedWriter(new FileWriter(f));

      //System.out.println("finished creating streams");

      //for each line in such file
      int c;
      while ( (c = br.read()) > -1) {
        bw.write(c);

        //System.out.println("finished writing file.");

      }
      br.close();
      bw.close();
      //System.out.println("closing streams");
    }

    return 0;
  }

  /**
   * Delete directory.
   */
  private static void deleteDir(File dir) throws IOException {
    File[] files = dir.listFiles();
    //delete files in this directory
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDir(files[i]);
      }
      else {
        files[i].delete();
      }
    }

    //delete this directory
    dir.delete();
  }
}
