/*
 * Copyright (c) 1999-2002 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * Filter out all NON-TEXT files.
 */
public class TextFileFilter
    implements FileFilter {
  //Maximum number of lines to read to test the file.
  private static final int MAX_LINE = 3;

  /**
   * Determine whether a file is acceptable or not.
   * @param file The file to test.
   * @return True if file is a text file, false otherwise.
   */
  public boolean accept(File file) {
    if (file.getName().endsWith(".java") || file.getName().endsWith(".cs") || file.getName().endsWith(".php") || file.getName().endsWith(".css") || file.getName().endsWith(".html") || file.getName().endsWith(".js") || file.getName().endsWith(".txt")) {
      return true;
    }
    //make sure the file is not a directory
    if (file.isDirectory()) {
      return false;
    }
    
    //ne .match fajlove
    if(file.getName().endsWith("."+Settings.getSherlockSettings().getMatchDirectory()))
        return false;

    //boolean isTextFile = true;
    //System.out.println("text filter: " + file.getAbsolutePath());
    BufferedReader in;
    String tmpLine;
    byte[] line;
    byte b;

    try {
      in = new BufferedReader(new FileReader(file));
      //read at most 3 lines, and test each character.
      for (int i = 0; i < MAX_LINE; i++) {
        tmpLine = in.readLine();
        if (tmpLine != null) {
          line = tmpLine.getBytes();
          for (int j = 0; j < line.length; j++) {
            b = line[j];
            //if MSB is set to 1, file is binary.
            if ( (b & 0x80) == 0x80) {
              in.close();
              System.out.println("Binary File "+file.getName());
              return false;
            }
          }
        }
        else {
          in.close();
          return true;
        }
      }
      in.close();
    }
    catch (IOException e) {
      //System.out.println("Exception in TextFileFilter: " + e);
      //write error log, skip this file and continue.
      Date day = new Date(System.currentTimeMillis());
      try {
        BufferedWriter out = new BufferedWriter
            (new FileWriter(Settings.logFile.getAbsolutePath(), true));
        out.write(day + "-TextFileFilter failed on file:"
                  + file.getAbsolutePath()
                  + " File skipped.");
        out.newLine();
        out.close();
      }
      catch (IOException e2) {
        //if failed to write to log, write to stderr
        System.err.println
            (day + "-Cannot write to log file. "
             + "TextFileFilter failed on file:"
             + file.getAbsolutePath()
             + " File skipped.");
      }
      return false;
    }

    //check whether file is one type of the pre-processed file types.
    //these files are not needed.
    //boolean isAcceptable = true;
    for (int i = 0; i < Settings.NUMBEROFFILETYPES; i++) {
      if (file.getName().endsWith
          (Settings.fileTypes[i].getExtension())) {
// 		isAcceptable = false;
// 		break;
        return false;
      }
    }

    //also filter out ".ini" and directories file created by Sherlock
    return!file.getName().endsWith(".ini");
  }
}
