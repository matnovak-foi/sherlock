/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import uk.ac.warwick.dcs.cobalt.sherlock.freetext.*;

/**
 * Runs the files in the source directory through the selected tokenisers.
 *
 * Multiple static parser instances are created in this class, i.e. NoWhite,
 * NoComment, Comment and cplusplus, however, they tend to share the same
 * SimpleCharStream.java class which is generated by Javacc automatically.
 * This class is set to be static by default, in which case the parsers
 * couldn't reinitialise themselves without reinitialise this class.
 * Reinitialisation of this class is not possible as it doesn't have any
 * such methods.
 * One way to solve this problem instead of using non-static parsers is to
 * simply remove ALL 'static' declaration in the SimpleCharStream.java file
 * after it is generated by Javacc and set the 'statiFlag' variable to false.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @author Terri Mak
 * @author Daniel White
 * @version 8 Apr 2003
 */
public class TokeniseFiles
    extends SherlockProcess {

  /**
   * Holds the files from the source directory to tokenise.
   */
  private File filesToProcess[];

  /**
   * Holds the files which have been processed. It is used when
   * saving the current work.
   */
  private Map processedFiles;

  /**
   * Ture if current job is from a saved session, false otherwise.
   */
  private boolean fromSaved = false;

  /**
   * The class to pass exceptions to.
   */
  private SherlockProcessCallback parent;

  private static Normaliser normaliser = new Normaliser(System.in);
  private static NoWhite nowhite = new NoWhite(System.in);
  private static NoComment nocomment = new NoComment(System.in);
  private static Comment comment = new Comment(System.in);
  private static Java java = new Java(System.in);
  private static cplusplus cpp = new cplusplus(System.in);
  private static SentenceParser sentence = new SentenceParser(System.in);
  private static NaturalParser natural = new NaturalParser(System.in);

  /**
   * Runs the files selected through the tokenisers specified.
   *
   * @param spc the parent class to pass any exceptions to
   */
  public TokeniseFiles(SherlockProcessCallback spc) {
    //Initialise KIND
    KIND = new Integer(TOKENISE);

    // Initialise parent variable.
    parent = spc;

    // Establish array of the files to be run through the tokenisers.
    filesToProcess = Settings.getFileList();

    // Calculate the number of stages - the number of files to be
    //processed.
    // If there was an I/O error, null will be returned
    if (filesToProcess == null) {
      stagesToDo = 0;
    }
    else {
      stagesToDo = filesToProcess.length;
      processedFiles = new Hashtable();
    }

  } // TokeniseFiles

  /**
   * Runs the files selected through the tokenisers specified.
   *
   * @param spc the parent class to pass any exceptions to
   * @param list the list of files to be processed. Used to recover from
   * a previously saved session.
   */
  public TokeniseFiles(SherlockProcessCallback spc, Map processed) {
    // Initialise KIND
    KIND = new Integer(TOKENISE);

    // Initialise parent variable.
    parent = spc;

    processedFiles = processed;

    if (processedFiles != null) {
      fromSaved = true;

    }
    filesToProcess = Settings.getFileList();
    // If there was an I/O error, null will be returned
    if (filesToProcess == null) {
      stagesToDo = 0;
    }
    else {
      stagesToDo = filesToProcess.length;
    }
  }

  /**
   * Return the list of files which are not yet processed.
   */
  public Map getProcessedFiles() {
    return processedFiles;
  }

  /**
   * Return hashtable for the exclude file. However, during tokenising, the
   * hastable is not created yet, therefore this method returns null for
   * TokeniseFiles objects. The reason why it much be implemented is becasue
   * the super class uses this method for Samelines objects.
   */
  public Map getExcludeMap() {
    return null;
  }

  /**
   * Process the source files through the relevant tokenisers.
   */
  public void run() {
    Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int min = c.get(Calendar.MINUTE);
    String minute;
    if (min < 10) {
      minute = "0" + Integer.toString(min);
    }
    else {
      minute = Integer.toString(min);
    }
    Settings.message("Started TokeniseFiles at " + hour + ":" + minute);

    //only start processing if the file list is non-empty.
    if (filesToProcess != null) {
      for (int fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
           fileType++) {
        if (Settings.fileTypes[fileType].isInUse()) {
          File f = new File
              (Settings.sourceDirectory.getAbsolutePath() +
               Settings.fileSep +
               Settings.fileTypes[fileType].getDirectory());
          if (!f.exists()) {
            f.mkdirs();
          }
        }
      }

      if (processedFiles == null) {
        processedFiles = new Hashtable();

        // Run each file through the tokenisers.
      }
      for (int x = 0; x < filesToProcess.length; x++) {

        // If the user has chosen to cancel the process, get out
        // of this loop and let the thread die.
        if (letDie) {
          break;
        }

        // Process the file.
        Settings.message("Processing " + filesToProcess[x]
                         .getAbsolutePath());
        String value = filesToProcess[x].getAbsolutePath();
        Integer key = new Integer(value.hashCode());
        if (processedFiles.containsKey(key)
            && ( (String) processedFiles.get(key)).equals(value)) {
          stagesDone++;
          continue;
        }
        else {
          runTokenisers(filesToProcess[x]);
          processedFiles.put(key, value);
          //yield();
        }
        stagesDone++;
      }
    }

    // Now have finished.
    c = Calendar.getInstance();
    hour = c.get(Calendar.HOUR_OF_DAY);
    min = c.get(Calendar.MINUTE);
    if (min < 10) {
      minute = "0" + Integer.toString(min);
    }
    else {
      minute = Integer.toString(min);
    }
    Settings.message("Finished TokeniseFiles at " + hour + ":" + minute);

  } // processFiles

  /**
   * Called if the user cancels the process before it is finished. All files
   * and directories that have been created must be deleted.
   */
  public void deleteWorkDone() {

    // Check through and delete any directories that have been created.
    for (int fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
         fileType++) {
      if (Settings.fileTypes[fileType].isInUse()) {
        File dir = new File(Settings.sourceDirectory.getAbsolutePath()
                            + Settings.fileSep +
                            Settings.fileTypes[fileType]
                            .getDirectory());

        // If the directory has been created, first delete any files
        //it contains.
        if (dir.exists()) {
          File files[] = dir.listFiles();
          for (int x = 0; x < files.length; x++) {
            files[x].delete();
          }
          dir.delete();
        }
      }
    }
  } // deleteWorkDone

  /**
   * Runs the passed file through all the necessary tokenisers.
   *
   * @param file the file to tokenise and copy
   */
  private void runTokenisers(File file) {

    // The original file to be tokenised from the source directory.
    File origFile = file;

    // Need to keep tabs of the no comments file to use again for no
    // comments or whitespace.
    File noComments = null;

    // Need to keep tabs of the no comment & normalised/nowhite file to
    // use again for tokenisation.
    File normalised = null;
    File noWhite = null;
    File toTokeniser = null;

    // The input stream that is passed to each tokeniser, accessing
    // the file to be tokenised.
    InputStream inputStream = null;

    // Send origFile through the selected tokenisers.
    for (int fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
         fileType++) {

      // Pause the process if required as the user is deciding whether
      //or not to cancel it. If it is cancelled, return from this method
      while (pause) {
        if (letDie) {
          return;
        }
      }

      if (Settings.fileTypes[fileType].isInUse()) {
        // Create the file to save the results of the tokeniser in.
        File outputFile = new File
            (Settings.sourceDirectory.getAbsolutePath() +
             Settings.fileSep +
             Settings.fileTypes[fileType].getDirectory(),
             origFile.getName() + "." +
             Settings.fileTypes[fileType].getExtension());

        Settings.debugMessage("Creating " + outputFile.getName());

        switch (fileType) {
          case (Settings.ORI): {
            try {
              copyFile(origFile, outputFile);
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error making original copy of "
                    + origFile.getName(),
                    e));
            }
            break;
          }

          case (Settings.NOR): {
            try {
              inputStream = new FileInputStream(origFile);
              normaliser.ReInit(inputStream, outputFile);
              normaliser.Input();
              inputStream.close();
              normalised = outputFile;
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating normalised version of " +
                    origFile.getName(), e));
            }
            break;
          }

          case (Settings.NOW): {
            try {
              inputStream = new FileInputStream(origFile);
              nowhite.ReInit(inputStream, outputFile);
              nowhite.Input();
              inputStream.close();
              noWhite = outputFile;
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating no white version of " +
                    origFile.getName(), e));
            }
            break;
          }

          case (Settings.NOC): {
            try {
              inputStream = new FileInputStream(origFile);
              nocomment.ReInit(inputStream, outputFile);
              nocomment.Input();
              inputStream.close();
              // Save reference to this file.
              noComments = outputFile;
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating no comment version of " +
                    origFile.getName(), e));
            }
            break;
          }
          //this case is assumped not to be activated with the next
          //case in a same session.
          case (Settings.NCN): {
            File temp = null;
            try {
              // If a no comments file was not made, make one now.
              if (noComments == null) {
                temp = File.createTempFile("temp", "noc");
                inputStream = new FileInputStream(origFile);
                nocomment.ReInit(inputStream, temp);
                nocomment.Input();
                inputStream.close();
                inputStream = new FileInputStream(temp);
              }
              else {
                inputStream = new FileInputStream(noComments);

                // Run the Normaliser tokeniser on the temporary or
                //permanent NoComment output.
              }
              normaliser.ReInit(inputStream, outputFile);
              normaliser.Input();
              inputStream.close();
              toTokeniser = outputFile;
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating no comment normalised version of"
                    + " " + origFile.getName(), e));
            }

            // Delete temporary no comments file.
            if (temp != null) {
              temp.delete();
            }
            break;
          }

          case (Settings.NCW): {
            File temp = null;
            try {
              // If a no comments file was not made, make one now.
              if (noComments == null) {
                temp = File.createTempFile("temp", "noc");
                inputStream = new FileInputStream(origFile);
                nocomment.ReInit(inputStream, temp);
                nocomment.Input();
                inputStream.close();
                inputStream = new FileInputStream(temp);
              }
              else {
                inputStream = new FileInputStream(noComments);

                // Run the Normaliser tokeniser on the temporary or
                //permanent NoComment output.
              }
              nowhite.ReInit(inputStream, outputFile);
              nowhite.Input();
              inputStream.close();
              toTokeniser = outputFile;
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating no comment no white version of"
                    + " " + origFile.getName(), e));
            }

            // Delete temporary no comments file.
            if (temp != null) {
              temp.delete();
            }
            break;
          }

          case (Settings.COM): {
            try {
              inputStream = new FileInputStream(origFile);
              comment.ReInit(inputStream, outputFile);
              comment.Input();
              inputStream.close();
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating comment only version of " +
                    origFile.getName(), e));
            }
            break;
          }

          case (Settings.SEN): {
            try {
              inputStream = new FileInputStream(origFile);
              Sentence.setCommon(Settings.fileTypes[Settings.SEN].
                                 getCommonWords());
              if (!getNatural()) {
                //sentence.ReInit(inputStream,outputFile,origFile.getName());
                sentence = new SentenceParser(inputStream);
                sentence.setOutput(outputFile, origFile.getName());
                sentence.Input();
              }
              else {
                natural = new NaturalParser(inputStream);
                natural.setOutput(outputFile, origFile.getName());
                // natural.ReInit(inputStream, outputFile, origFile.getName());
                natural.Input();
              }
              inputStream.close();
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown(
                  new SherlockProcessException(
                  "Error creating sentence-parsed version of " +
                  origFile.getName(), e));
            }
            break;
          }
          //process the no comment & normailsed/no white version.
          case (Settings.TOK): {
            try {
              File temp = null;
              
              if (true){//toTokeniser == null) {
                  //Settings.message("");
                if(true)///Settings.fileTypes[Settings.ORI].isInUse())
                {
                    //Settings.message("BBBBBBB");
                    //ADDED TO PERFORM TOKENISED VERSION ON ORIGINAL FILE THAT IS GIVEN
                    inputStream = new FileInputStream(origFile);
                }else if (Settings.fileTypes[Settings.NOR].isInUse()&&false) {
                    //if normalser is selected.
                  if (normalised != null) {
                    temp = File.createTempFile("temp", "ncn");
                    inputStream = new FileInputStream
                        (normalised);
                    nocomment.ReInit(inputStream, temp);
                    nocomment.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                  }
                  else if (noComments != null) {
                    temp = File.createTempFile("temp", "ncn");
                    inputStream = new FileInputStream
                        (noComments);
                    normaliser.ReInit(inputStream, temp);
                    normaliser.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                  }
                  else {
                    temp = File.createTempFile("temp", "noc");
                    inputStream = new FileInputStream
                        (origFile);
                    nocomment.ReInit(inputStream, temp);
                    nocomment.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                    File temp2 = File.createTempFile
                        ("temp2", "ncn");
                    normaliser.ReInit(inputStream, temp2);
                    normaliser.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp2);
                  }
                }
                //if no white is selected
                else if(false){
                  if (noWhite != null) {
                    temp = File.createTempFile("temp", "ncw");
                    inputStream = new FileInputStream
                        (noWhite);
                    nocomment.ReInit(inputStream, temp);
                    nocomment.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                  }
                  else if (noComments != null) {
                    temp = File.createTempFile("temp", "ncw");
                    inputStream = new FileInputStream
                        (noComments);
                    nowhite.ReInit(inputStream, temp);
                    nowhite.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                  }
                  else {
                    temp = File.createTempFile("temp", "noc");
                    inputStream = new FileInputStream
                        (origFile);
                    nocomment.ReInit(inputStream, temp);
                    nocomment.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp);
                    File temp2 = File.createTempFile
                        ("temp2", "ncw");
                    nowhite.ReInit(inputStream, temp2);
                    nowhite.Input();
                    inputStream.close();
                    inputStream = new FileInputStream(temp2);
                  }
                }
              }
              else {
                inputStream = new FileInputStream(toTokeniser);
                Settings.message("CCCCCC");
              }
              if (Settings.sherlockSettings.isJava()) {
                java.ReInit(inputStream, outputFile);
                java.Input();
                //problem with closing so it is added
                java.save.flush();
                java.save.close();
              }
              else {
                cpp.ReInit(inputStream, outputFile);
                cpp.Input();
                //problem with closing so it is added
                cpp.save.flush();
                cpp.save.close();
              }
              
            }
            catch (Exception e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error creating tokenised version of " +
                    origFile.getName(), e));
            }finally{
                try {
 //                   parent.exceptionThrown(new SherlockProcessException("IOSTREAM closing"));
                    if(inputStream != null)
                        inputStream.close();
//                    parent.exceptionThrown(new SherlockProcessException("IOSTREAM closed"));
                } catch (IOException ex) {
                    parent.exceptionThrown
                  (new SherlockProcessException
                   ("Error closing tokenised version of " +
                    origFile.getName(), ex));
                }
            }
            break;
          }
        } // switch
        
      } // if isInUse
    } // for
  } // runTokenisers

  /**
   * This method does a byte by byte copy of a file to another.
   *
   * @todo surely we can use some kind of buffering here?
   *
   * @param from the file to copy from
   * @param to the file to copy it to
   */
  private void copyFile(File from, File to) throws IOException,
      FileNotFoundException {
    FileInputStream inFile = new FileInputStream(from);
    FileOutputStream outFile = new FileOutputStream(to);
    BufferedInputStream input = new BufferedInputStream(inFile);
    BufferedOutputStream output = new BufferedOutputStream(outFile);
    int thisByte;
    // Works for nil length files!
    thisByte = input.read();
    while (thisByte != -1) {
      output.write(thisByte);
      thisByte = input.read();
    }
    input.close();
    output.flush();
    output.close();
  } // copy

  /**
   * Tell the GUI or Sherlock the number of stages to do.
   *
   * @return the number of stages to do
   */
  public int getStagesToDo() {
    return stagesToDo;
  } // getStagesToDo

  /**
   * Tell the GUI or Sherlock how many stages have been done.
   *
   * @return the number of stages done
   */
  public int getStagesDone() {
    return stagesDone;
  } // getStagesDone
}
