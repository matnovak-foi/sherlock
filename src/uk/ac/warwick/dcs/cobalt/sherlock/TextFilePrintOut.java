package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import javax.print.*;
import javax.print.attribute.*;

import javax.swing.*;

/**
 * For printing text files. This class automatically formats the code
 * to force 80 characters per line. Lines longer than 80 chars will be broken
 * into multiple lines.
 *
 * @author Weiliang Zhang
 * @version 26 Sep 2002
 */
public class TextFilePrintOut
    implements MultiDoc {
  private File current;
  private File next;
  private final DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

  /**
   * Text file formatted print out.
   *
   *@param file1 current print document
   *@param file2 next print document, returned by next() method.
   */
  public TextFilePrintOut(File file1, File file2) {
    current = file1;
    next = file2;
  }

  /**
   * Return the first source code file in this pair.
   */
  public Doc getDoc() {
    try {
      //process file, insert line number tags.
      BufferedReader br = new BufferedReader
          (new FileReader(current));
      File temp = File.createTempFile("sherprint", "tmp");
      BufferedWriter bw = new BufferedWriter
          (new FileWriter(temp));
      StringTokenizer st;
      String line;
      int lineNo = 0;
      int width = 0;
      bw.write("File: " + current.getName());
      bw.newLine();
      bw.newLine();
      while ( (line = br.readLine()) != null) {
        lineNo++;
        String tag = new String(lineNo + ":\t");
        width += tag.length();
        bw.write(tag);
        st = new StringTokenizer(line, " ");
        String token;
        while (st.hasMoreTokens()) {
          token = st.nextToken() + " ";
          width += token.length() + 1;
          if (width > 80) {
            bw.newLine();
            bw.write("\t");
            width = token.length() + 1;
          }
          bw.write(token);
        }
        bw.newLine();
        width = 0;
      }

      br.close();
      bw.flush();
      bw.close();

      FileInputStream fin = new FileInputStream(temp);
      DocAttributeSet das = new HashDocAttributeSet();
      Doc doc = new SimpleDoc(fin, flavor, das);
      return doc;
    }
    catch (IOException e) {
      JOptionPane.showMessageDialog
          (null, "Cannot find source file: "
           + current.getAbsolutePath(),
           "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
  }

  /**
   * Return the next source code file in this pair. Null if this object is
   * the second file, to indicate the end of the whole sherlock print out.
   */
  public MultiDoc next() {
    if (next == null) {
      //reached the end of the whole print job.
      return null;
    }
    else {
      return new TextFilePrintOut(next, null);
    }
  }
}
