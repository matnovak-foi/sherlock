package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import javax.swing.*;

/**
 * Extract the matched sections from a given pair and produce a text file
 * containing these matched pairs.
 *
 * @author Weiliang Zhang
 * @version 26 Sep 2002
 */
public class MatchSectionPrintOut
    implements MultiDoc {
  /**
   * Original source code files.
   */
  private RandomAccessFile[] origFiles = new RandomAccessFile[2];

  /**
   * Constant to access the first file of the files array.
   */
  private final static int FILE1 = 0;

  /**
   * Constant to access the first file of the files array.
   */
  private final static int FILE2 = 1;

  /**
   * The line numbers of a match section in the original files.
   */
  private int origStart[] = new int[2];
  private int origEnd[] = new int[2];

  /**
   * Writer to output and ASCII file for printing.
   */
  private BufferedWriter bw;

  /**
   * Width of line.
   */
  private static final int WIDTH = 80;

  /**
   * Length of a TAB char.
   */
  private static final int TABLENGTH = 8;

  /**
   * Temp file containing the final output.
   */
  private File printOut = null;

  /**
   * All matches for this pair.
   */
  private Match[] matches;

  /**
   * Construct this doc.
   *
   * @param ms all matches found for a pair of submissions.
   */
  public MatchSectionPrintOut(Match[] ms) {
    matches = ms;
    if (matches == null || matches.length == 0) {
      return;
    }

    construct();
  }

  /**
   * This constructor is used to save the temp file printOut. It is used
   * to let the user to print out this section with some other printing
   * utilities, such as 'a2ps', because the current jdk fails to print it.
   * Therefore THIS METHOD CAN BE REMOVED WHEN THE BUG IS FIXED.
   *
   * @param fileToSave filename of the output file
   * @param ms all matches found for a pair of submissions.
   */
  public MatchSectionPrintOut(File fileToSave, Match[] ms) {
    printOut = fileToSave;
    matches = ms;
    if (matches == null || matches.length == 0) {
      return;
    }

    construct();
  }

  /**
   * Actual construction of this object.
   */
  private void construct() {
    try {
      //setup original files
      origFiles[FILE1] = new RandomAccessFile
          (new File(Settings.sourceDirectory + Settings.fileSep
                    + Settings.fileTypes[Settings.ORI].getDirectory(),
                    truncate(matches[0].getFile1()) + "." +
                    Settings.fileTypes[Settings.ORI].getExtension()),
           "r");
      origFiles[FILE2] = new RandomAccessFile
          (new File(Settings.sourceDirectory + Settings.fileSep
                    + Settings.fileTypes[Settings.ORI].getDirectory(),
                    truncate(matches[0].getFile2()) + "." +
                    Settings.fileTypes[Settings.ORI].getExtension()),
           "r");
      //if printOut != null, then it points to a file that the user
      //wishes to save this section in. This is assigned in the other
      //constructor.
      if (printOut == null) {
        printOut = File.createTempFile("sherlock", "print");
      }
      bw = new BufferedWriter(new FileWriter(printOut, true));
      //setup title.
      bw.write("File 1: " + truncate(matches[0].getFile1()));
      bw.write("\t\t\t\t\t\t\t\t");
      bw.write("File 2: " + truncate(matches[0].getFile2()));
      bw.newLine();
      bw.newLine();
    }
    catch (FileNotFoundException e) {
      JOptionPane.showMessageDialog
          (null, "Source code file Not found, skipped.",
           "Error", JOptionPane.ERROR_MESSAGE);
      printOut = null;
      return;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (null, "Cannot generated temp file, skipped",
           "Error", JOptionPane.ERROR_MESSAGE);
      printOut = null;
      return;
    }

    //read in each match & then process it.
    for (int i = 0; i < matches.length; i++) {
      try {
        //which type of the preprocess
        int type = matches[i].getFileType();

        //output file type & similarity
        bw.write(Settings.fileTypes[type].getDescription());
        bw.write(", " + matches[i].getSimilarity() + "%");
        bw.newLine();
        //get the sections
        RunCoordinates rcstart = matches[i].getRun()
            .getStartCoordinates();
        RunCoordinates rcend = matches[i].getRun()
            .getEndCoordinates();

        origStart[FILE1] = rcstart.getOrigLineNoInFile1();
        origEnd[FILE1] = rcend.getOrigLineNoInFile1();
        origStart[FILE2] = rcstart.getOrigLineNoInFile2();
        origEnd[FILE2] = rcend.getOrigLineNoInFile2();

        int[] lineNo = new int[2];
        RandomAccessFile[] tmpFiles = new RandomAccessFile[2];
        File[] temp = new File[2];
        for (int z = FILE1; z <= FILE2; z++) {
          //locate the matched sections in each file.
          String a = "";
          for (lineNo[z] = 1; lineNo[z] < origStart[z]; lineNo[z]++) {
            a = origFiles[z].readLine();

          }
          temp[z] = File.createTempFile("sherprint", ".tmp");
          temp[z].deleteOnExit();
          BufferedWriter tmpWriter = new BufferedWriter
              (new FileWriter(temp[z]));
          StringTokenizer st;
          String line;
          int width = TABLENGTH;
          ;

          while (lineNo[z] <= origEnd[z] &&
                 (line = origFiles[z].readLine()) != null) {
            String tag = new String(lineNo[z] + ":\t");
            width += tag.length();
            tmpWriter.write(tag);
            st = new StringTokenizer(line, " ");
            String token;
            while (st.hasMoreTokens()) {
              token = st.nextToken() + " ";
              width += token.length();
              if (width > WIDTH) {
                tmpWriter.newLine();
                tmpWriter.write("\t");
                width = TABLENGTH + token.length();
              }
              tmpWriter.write(token);
            }
            tmpWriter.newLine();
            width = TABLENGTH;
            lineNo[z]++;
          }

          tmpWriter.close();
          //set up reader to use later.
          tmpFiles[z] = new RandomAccessFile(temp[z], "r");
        }

        //mix the 2 temp files into one.
        String[] lines = new String[2];
        long[] fptr = new long[2];
        while (true) {
          //current position, in case roll back is necessary.
          for (int j = FILE1; j <= FILE2; j++) {
            fptr[j] = tmpFiles[j].getFilePointer();
            lines[j] = tmpFiles[j].readLine();
          }

          //if EOF is reached
          if (lines[FILE2] == null && lines[FILE1] != null) {
            bw.write(lines[FILE1]);
            bw.newLine();
            //finish file 1
            while ( (lines[FILE1] = tmpFiles[FILE1].readLine())
                   != null) {
              bw.write(lines[FILE1]);
              bw.newLine();
            }
            break;
          }
          else if (lines[FILE1] == null && lines[FILE2] != null) {
            bw.write("\t\t\t\t\t\t\t\t\t\t" + lines[FILE2]);
            bw.newLine();
            //finish file 2
            while ( (lines[FILE2] = tmpFiles[FILE2].readLine())
                   != null) {
              bw.write("\t\t\t\t\t\t\t\t\t\t" + lines[FILE2]);
              bw.newLine();
            }
            break;
          }
          else if (lines[FILE1] == null && lines[FILE2] == null) {
            break;
          }

          //if line from file 1 is a broken line, output this line,
          //but do not output line from file 2.
          if (lines[FILE1].indexOf("\t") == 0 &&
              lines[FILE2].indexOf("\t") != 0) {
            bw.write(lines[FILE1]);
            bw.newLine();
            //roll back file 2.
            tmpFiles[FILE2].seek(fptr[FILE2]);
          }
          else if (lines[FILE2].indexOf("\t") == 0 &&
                   lines[FILE1].indexOf("\t") != 0) {
            bw.write("\t\t\t\t\t\t\t\t" + lines[FILE2]);
            bw.newLine();
            //roll back file 1.
            tmpFiles[FILE1].seek(fptr[FILE1]);
          }
          //both lines are of same type.
          else {
            String output = lines[FILE1];

            int counter = 0;
            for (int n = 0; n < output.length(); n++) {
              if (output.charAt(n) == '\t') {
                counter++;

                //find the length of line number tag
              }
            }
            int len = output.indexOf(":") + 1;

            //find out the actual length of the line from
            //file 1. This equals the fake length - number of
            //tab chars + tab length * number of tab chars
            // - length of line number tag.
            int realLen = output.length() - counter +
                TABLENGTH * counter - len;
            int tabs = (int) Math.ceil
                ( (double) (WIDTH - realLen) /
                 ( (double) TABLENGTH));

            while (tabs > 0) {
              output += "\t";
              tabs--;
            }
            output += lines[FILE2];

            bw.write(output);
            bw.newLine();
          }
        }

        //to separate different sections.
        bw.newLine();
        bw.newLine();
        bw.newLine();
        bw.newLine();

        tmpFiles[FILE1].close();
        tmpFiles[FILE2].close();
        temp[FILE1].delete();
        temp[FILE2].delete();

        //reset original file.
        origFiles[FILE1].seek(0);
        origFiles[FILE2].seek(0);
      }
      catch (IOException e) {
        JOptionPane.showMessageDialog
            (null, "Cannot read file needed for : " + matches[i]
             + " Skipped.", "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }
    try {
      bw.close();
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (null, "Failed to wirte to temp file. Printing skipped.",
           "Error", JOptionPane.ERROR_MESSAGE);
      printOut = null;
      return;
    }
  }

  /**
   * Extract original file name from preprocessed filenames. It also removes
   * the directory information.
   */
  private String truncate(String arg) {
    File file = new File(arg);
    String str = file.getName();
    int index = str.lastIndexOf(".");
    str = str.substring(0, index);
    return str;
  }

  /**
   * Return this doc for printing.
   */
  public Doc getDoc() {
    try {
      if (printOut == null) {
        JOptionPane.showMessageDialog
            (null, "Failed to read to temp file. Section skipped.",
             "Error", JOptionPane.ERROR_MESSAGE);
        return null;
      }
      DocFlavor flavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
      FileInputStream fin = new FileInputStream(printOut);
      DocAttributeSet das = new HashDocAttributeSet();
      das.add(OrientationRequested.LANDSCAPE);
      Doc doc = new SimpleDoc(fin, flavor, das);
      return doc;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (null, "Failed to read to temp file. Section skipped.",
           "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  /**
   * Return the next section in the whole print out.
   */
  public MultiDoc next() {
// 	//return original source code listings.
// 	File file1 = new File(Settings.sourceDirectory + Settings.fileSep +
// 			      Settings.fileTypes[Settings.ORI].getDirectory(),
// 			      truncate(matches[0].getFile1()) +
// 			      Settings.fileTypes[Settings.ORI].getExtension());
// 	File file2 = new File(Settings.sourceDirectory + Settings.fileSep +
// 			      Settings.fileTypes[Settings.ORI].getDirectory(),
// 			      truncate(matches[0].getFile2()) +
// 			      Settings.fileTypes[Settings.ORI].getExtension());
// 	return (new TextFilePrintOut(file1, file2));

    return null;
  }
}
