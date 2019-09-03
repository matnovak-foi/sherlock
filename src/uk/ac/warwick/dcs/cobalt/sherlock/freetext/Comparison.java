/*
 * Copyright (c) 1999-2003 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock.freetext;

import java.io.*;
import java.util.*;

import uk.ac.warwick.dcs.cobalt.sherlock.*;

/**
 * <P>Class which carries out the business of comparing document objects to
 * one another in order to detect plagiarism. This class is called by the
 * Samelines class in Sherlock during a detection run and implements a
 * different detection algorithm tailored to natural language plagiarism
 * detection.</p>
 * <p>Title: Sherlock 2000</p>
 * <p>Description: Plagiarism Detection Software</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: University of Warwick</p>
 *
 * @author Daniel White
 * @version 4
 */

public class Comparison {
  // The list of all original file names.
  private String[] filesToCompare;
  // references to documents used in the comparison.
  private Document doc1 = null, doc2 = null, docExclude = null;
  // useful File objects.
  private File sourceDirectory, excludeFile;
  // The settings for comparing document objects.
  private FileTypeProfile sentProfile;
  // The Samelines process which is controlling all the different comparisons.
  private Samelines samelines;
  // The name of the exclude file, empty if there isn't an exclude file.
  private String excludeName = "";
  // 2D array of SentencePair objects. The first coordinated is the document,
  // while the second is the sentence within that document.
  private SentencePair sentencePairs[][];

  public static boolean MEM_INTENSIVE = true;

  /**
   * The constructor.
   * @param filesToCompare List of filenames in the comparison.
   * @param sourceDirectory The source directory, where all files and settings
   * are stored.
   * @param sentProfile The settings for this comparison mode.
   * @param samelines The parent process.
   */
  public Comparison(String[] filesToCompare, File sourceDirectory,
                    FileTypeProfile sentProfile, Samelines samelines) {
    this.filesToCompare = filesToCompare;
    sentencePairs = new SentencePair[filesToCompare.length][];
    this.sourceDirectory = sourceDirectory;
    this.sentProfile = sentProfile;
    this.samelines = samelines;
    excludeFile = new File(sourceDirectory + File.separator +
                           sentProfile.getDirectory(),
                           Settings.getSherlockSettings().getExcludeFile() +
                           "." +
                           sentProfile.getExtension());
    if (excludeFile.exists()) {
      docExclude = readFileIntoDoc(excludeFile);
      excludeName = Settings.getSherlockSettings().getExcludeFile();
    }
  }

  /**
   * Called to begin the comparison. When this method returns, the comparison
   * will be over and all relevant files will have been saved. Communicates
       * with the progress window to say what progress is made during the running of
   * the method.
   */
  public void begin() {
    // if there is an exclude file.
    Document[] docs = null;
    if (excludeName.length() > 0) {
      boolean arrayAltered = false;
      //remove the exclude file from the list of files to compare.
      for (int i = 0; i < filesToCompare.length; i++) {
        if (filesToCompare[i].equals(excludeName)) {
          String[] newArray = new String[filesToCompare.length - 1];
          System.arraycopy(filesToCompare, 0, newArray, 0, i);
          System.out.println("filesToCompare.length: " + filesToCompare.length +
                             "\ni: " + i + "\nnewArray.length: " +
                             newArray.length);
          if (i + 1 != filesToCompare.length) {
            System.arraycopy(filesToCompare, i + 1, newArray, i,
                             filesToCompare.length - (i + 1));
          }
          filesToCompare = newArray;
          arrayAltered = true;
          sentencePairs = new SentencePair[filesToCompare.length][];
          //samelines.yield();
          break;
        }
      } // for(... i<filesToCompare.length; ...)

      docs = new Document[filesToCompare.length];
      if (MEM_INTENSIVE) {
        for (int i = 0; i < docs.length; i++) {
          File temp = new File(sourceDirectory + File.separator
                               + sentProfile.getDirectory(),
                               filesToCompare[i] + "."
                               + sentProfile.getExtension());
          docs[i] = readFileIntoDoc(temp);
          //samelines.yield();
        }
      }

      if (arrayAltered) {
        // remove excluded sentences from the document objects.
        Settings.message("Removing excluded sentences from all documents.");
        for (int i = 0; i < filesToCompare.length; i++) {

          File temp = new File(sourceDirectory + File.separator +
                               sentProfile.getDirectory(), filesToCompare[i] +
                               "." + sentProfile.getExtension());

          Document doc = MEM_INTENSIVE ? docs[i] : readFileIntoDoc(temp);
          if (doc != null) {
            boolean changed = removeExcluded(doc);
            if (changed) {
              writeObjToFile(doc, temp);
            }
          }
          samelines.incStagesDone();
        }
      } // if(arrayAltered)
    } // if(excludeName.length() >0)
    else if (MEM_INTENSIVE) {
      docs = new Document[filesToCompare.length];
      if (MEM_INTENSIVE) {
        for (int i = 0; i < docs.length; i++) {
          File temp = new File(sourceDirectory + File.separator
                               + sentProfile.getDirectory(),
                               filesToCompare[i] + "."
                               + sentProfile.getExtension());
          docs[i] = readFileIntoDoc(temp);
          //samelines.yield();
        }
      }
    }

    // start comparing
    File file1, file2;
    for (int i = 0; i < filesToCompare.length; i++) {
      file1 = new File(sourceDirectory + File.separator
                       + sentProfile.getDirectory(), filesToCompare[i] + "."
                       + sentProfile.getExtension());
      if (file1.equals(excludeFile)) {
        continue;
      }

      doc1 = MEM_INTENSIVE ? docs[i] : readFileIntoDoc(file1);
      Settings.message("Comparing against " + filesToCompare[i] + "." +
                       sentProfile.getExtension());
      // doc1 being null means there was a problem reading it
      if (doc1 == null) {
        checkPause();
        continue;
      }

      if (i == 0) {
        // on the first pass, no document has anything other than the empty
        // pairs array so use that to populate the master array.
        sentencePairs[i] = doc1.getPairsArray();
      }
      else if (!MEM_INTENSIVE) {
        // use the master pairs array since that may contain links to documents
        // already compared.
        doc1.setPairsArray(sentencePairs[i]);
      }

      for (int j = i + 1; j < filesToCompare.length; j++) {

        file2 = new File(sourceDirectory + File.separator
                         + sentProfile.getDirectory(), filesToCompare[j] + "."
                         + sentProfile.getExtension());
        if (file2.equals(excludeFile)) {
          continue;
        }

        doc2 = MEM_INTENSIVE ? docs[j] : readFileIntoDoc(file2);
        if (doc2 == null) {
          checkPause();
          continue;
        }

        if (i == 0) {
          // on the first pass, no document has anything other than the empty
          // pairs array so use that to populate the master array.
          sentencePairs[j] = doc2.getPairsArray();
        }
        else if (!MEM_INTENSIVE) {
          doc2.setPairsArray(sentencePairs[j]);
        }

        // do the actual comparison.
        doc1.compareToDocument(doc2);
        samelines.incStagesDone();
        //samelines.yield();

      } // for j

      // sentencePairs[i] will not be changed again so write it to the match
      // directory.

      // Set the flag
      if (sentencePairs[i].length > 0) {
        sentencePairs[i][0].setChanged(true);

      }
      if (!writePairsToFile(sentencePairs[i], doc1.getFileName())) {
        samelines.pauseProcessing();
        checkPause();
      }
      // remove the reference to that document's pairs so that the memory can be
      // reclaimed. On large sets the memory used could become quite large if
      // this step were not taken.
      sentencePairs[i] = null;
      if (MEM_INTENSIVE) {
        docs[i] = null;
      }
    } // for i

  }

  /**
   * Read the given file as if it were a serialised document object.
   * @param file The serialised document file.
   * @return A Document object containing the previously serialised object.
   */
  private Document readFileIntoDoc(File file) {
    try {
      return readFileIntoDocStatic(file);
    }
    catch (FileNotFoundException fnfe) {
      samelines.pauseProcessing();
      samelines.exceptionThrown("File not found: " + file.toString(), fnfe);
    }
    catch (IOException ioe) {
      samelines.pauseProcessing();
      samelines.exceptionThrown("Problem with IO on " + file.toString(), ioe);
    }
    catch (ClassNotFoundException cnfe) {
      samelines.pauseProcessing();
      samelines.exceptionThrown("Class not found in " + file.toString(), cnfe);
    }
    return null;
  }

  /**
   * Static method to read a serialised Document object into an object.
   * @param file The file containing the serialised document.
   * @return A Document object.
   * @throws FileNotFoundException If the file is not found.
   * @throws IOException If some other IO error occured.
   * @throws ClassNotFoundException If the Document class is not found,
   * shouldn't happen.
   */
  static Document readFileIntoDocStatic(File file) throws FileNotFoundException,
      IOException, ClassNotFoundException {
    FileInputStream in = new FileInputStream(file);
    ObjectInputStream docIn = new ObjectInputStream(in);
    Document ret = (Document) docIn.readObject();
    docIn.close();
    in.close();
    return ret;
  }

  /**
   * Write the given object to the given file.
   * @param doc The object that needs to be serialised.
   * @param file The file to be written to.
   * @return true if the operation were successful, false if not.
   */
  private boolean writeObjToFile(Object doc, File file) {
    try {
      return writeObjToFileStatic(doc, file);
    }
    catch (FileNotFoundException fnfe) {
      samelines.pauseProcessing();
      samelines.exceptionThrown("File not found while writing: " +
                                file.toString(), fnfe);
    }
    catch (IOException ioe) {
      samelines.pauseProcessing();
      samelines.exceptionThrown("Problem with IO on " + file.toString(), ioe);
    }

    return false;
  }

  /**
   * Static method to write an object to a file in serialised form.
   * @param doc The object to be written.
   * @param file The file to be written to.
   * @return true if the operation were successful, false if not.
   * @throws FileNotFoundException If the output file was not found.
       * @throws IOException Some other IO error occured, such as a non-serialisable
   * object.
   */
  static boolean writeObjToFileStatic(Object doc, File file) throws
      FileNotFoundException, IOException {
    FileOutputStream out = new FileOutputStream(file);
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(doc);
    objOut.flush();
    objOut.close();
    out.close();
    return true;
  }

  /**
   * Write the array of SentencePair objects for a document with the given name
   * to the match directory.
   * @param pairs The pairs for the document.
   * @param name The original file name of the document.
   * @return true if successful, false if not.
   */
  private boolean writePairsToFile(SentencePair[] pairs, String name) {
    File file = new File(sourceDirectory + File.separator
                         + Settings.getSherlockSettings().getMatchDirectory(),
                         name + "pairs."
                         + sentProfile.getExtension());
    return writeObjToFile(pairs, file);
  }

  /**
   * Remove any sentences in the given document which exactly match the
   * sentences in the exclude file.
   * @param doc The document to be examined.
   * @return true if sentences were removed.
   */
  private boolean removeExcluded(Document doc) {
    if (docExclude == null) {
      return false;
    }
    Vector sentences = doc.getSentences();
    Vector exclSent = docExclude.getSentences();
    boolean changed = false;
    for (int i = 0; i < sentences.size(); i++) {
      Sentence temp = (Sentence) sentences.get(i);
      for (int j = 0; j < exclSent.size(); j++) {
        Sentence exclude = (Sentence) exclSent.get(j);
        if (temp.equals(exclude)) {
          temp.setInExcludeFile(true);
          changed = true;
          break;
        }
      } // for j
    } // for i
    return changed;
  }

  /**
   * Used when processing has been paused.
   */
  private void checkPause() {
    while (samelines.getPause()) {
      if (samelines.getLetDie()) {
        return;
      }
    }
  }
}