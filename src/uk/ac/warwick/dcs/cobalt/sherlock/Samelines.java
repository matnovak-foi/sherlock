/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

import uk.ac.warwick.dcs.cobalt.sherlock.freetext.*;

/**
 * The actual comparison of the files.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Weiliang Zhang
 * @author Terri Mak
 * @version 19 Aug 2002
 */
public class Samelines
    extends SherlockProcess {

  /**
   * For serialisation purpose, the inner classes have been changed to public
   * classes within the package.
   */
  ////////////////////////////////////
  // Inner classes ///////////////////
  ////////////////////////////////////

  /**
   * It holds the line of input, and a vector of FileLineReferences
   * which in turn holds a record of the location of any occurrences of this
   * line.
   */
//     private class LineOfData implements Serializable {

// 	/**
// 	 * The string read in.
// 	 */
// 	private String data = "";

// 	/**
// 	 * List of occurrences of this line.
// 	 */
// 	private List occurrences = null;

// 	/**
// 	 * Keep track of the index of the last returned LineOfDataOccurrence
// 	 * from the vector.
// 	 */
// 	private int lastGivenIndex = -1;

// 	/**
// 	 * Empty constructor - initialises the occurrences vector.
// 	 */
// 	LineOfData() {
// 	    data = "";
// 	    occurrences = new ArrayList();
// 	    lastGivenIndex = -1;
// 	}

// 	/**
// 	 * Creates new LineOfData object.
// 	 *
// 	 * @param data the line of data just read in
// 	 */
// 	LineOfData( String data ) {
// 	    this.data = data;
// 	    occurrences = new ArrayList();
// 	    lastGivenIndex = -1;
// 	}

// 	/**
// 	 * Add an occurrence of this line of data to the vector recording that.
// 	 *
// 	 * @param lODO A LineOfDataOccurrence holding file and line number
// 	 * information
// 	 */
// 	void addOccurrence(LineOfDataOccurrence lODO) {
// 	    occurrences.add(lODO);
// 	}

// 	/**
// 	 * Returns the first stored occurrence of this line of data.
// 	 *
// 	 * @return A LineOfDataOccurrence object, or null if it doesn't exist
// 	 */
// 	LineOfDataOccurrence getFirstOccurrence() {
// 	    lastGivenIndex = 0;
// 	    return getLODO();
// 	}

// 	/**
// 	 * Returns the next stored occurrence of this line of data. This will
// 	 * return the first element if it is called before getFirstOccurrence()
// 	 *
// 	 * @return A LineOfDataOccurrence object, or null if it doesn't exist
// 	 */
// 	LineOfDataOccurrence getNextOccurrence() {
// 	    lastGivenIndex++;
// 	    return getLODO();
// 	}

// 	/**
// 	 * Used by the first and next get methods.
// 	 *
// 	 * @return A LineOfDataOccurrence object, or null one does not exist
// 	 */
// 	private LineOfDataOccurrence getLODO() {
// 	    try {
// 		return (LineOfDataOccurrence)occurrences.get(lastGivenIndex);
// 	    } catch (IndexOutOfBoundsException e) {
// 		return null;
// 	    }
// 	}

// 	/**
// 	 * Returns this LineOfData's data.
// 	 *
// 	 * @return this LineOfData's data
// 	 */
// 	String getData() {
// 	    return data;
// 	}

//     } // LineOfData

//     /**
//      * LineOfDataOccurrence keeps track of where a line appears: in which
//      * file, and what its position in that file is.
//      */
//     private class LineOfDataOccurrence implements Serializable {

// 	/**
// 	 * File this line of data was found in - FILEA, FILEB or FILEX.
// 	 */
// 	private int fileType = 0;

// 	/**
// 	 * Line number of the line of data.
// 	 */
// 	private int lineNo = 0;

// 	/**
// 	 * Original line number of the line of data.
// 	 */
// 	private int origLineNo = 0;

// 	/**
// 	 * Constructor for a LineOfDataOccurrence.
// 	 *
// 	 * @param fileType File this line of data was found in: FILEA, FILEB
// 	 * or FILEX
// 	 * @param lineNo Line number of the line of data
// 	 * @param origLineNo Original line number of the line of data
// 	 */
// 	LineOfDataOccurrence(int fileType, int lineNo, int origLineNo ) {
// 	    this.fileType = fileType;
// 	    this.lineNo = lineNo;
// 	    this.origLineNo = origLineNo;
// 	}

// 	/**
// 	 * Return the file type.
// 	 *
// 	 * @return the file type
// 	 */
// 	int getFileType() {
// 	    return fileType;
// 	}

// 	/**
// 	 * Return the line number.
// 	 *
// 	 * @return the line number
// 	 */
// 	int getLineNo() {
// 	    return lineNo;
// 	}

// 	/**
// 	 * Return the original line number.
// 	 *
// 	 * @return the original line number
// 	 */
// 	int getOrigLineNo() {
// 	    return origLineNo;
// 	}

//     } // LineOfDataOccurrence

  ////////////////////////////////////
  // Inner classes ///////////////////
  ////////////////////////////////////

  /**
   * The three files used during comparison.
   * files[0] = files[FILEX] is the exclude file.
   * files[1] (files[FILE1]) and files[2] (files[FILE2]) are the two files
   * being compared at any one time.
   */
  private File files[] = new File[3];

  /**
   * linesInFiles holds the number of lines in each of the three files being
   * compared. This information is used when calculating the simiarity
   * value of a match.
   */
  private int linesInFiles[] = {
      0, 0, 0};

  /**
   * Exclude file constant.
   */
  private final static int FILEX = 0;

  /**
   * First file constant.
   */
  private final static int FILE1 = 1;

  /**
   * Second file constant.
   */
  private final static int FILE2 = 2;

  /**
   * The list of all the lines of data read in from the files. This holds
   * LineOfData objects, and is cleared following each comparison of two
   * files.
   */
  private Map linesOfData;

  /**
   * The list of all lines of data read in from the exclude file. It is
   * put into linesOfData every time a new compare is initiated to avoid
   * reading the exclude file for many times.
   */
  private Map excludeMap;

  /**
   * The list of file pairs compared. Used to resume a saved session.
   */
  private Map comparedMap;

  /**
   * True if the current job is from a saved session, false otherwise.
   */
  private boolean fromSaved = false;

  /**
   * The list of all the runs. This holds Run objects, and is cleared
   * following each comparison of two files.
   */
  private List runs;

  /**
   * This flag can be set so that all lines beginning with a # are treated
   * as comments and so do not affect the line numbering involved.
   * Exactly how this works is still to be decided.
   */
  private boolean hashLinesAreComments = false;

  /**
   * Type of comparison: comparing Original files, tokenised, or what.
   */
  private int fileType = 0;

  /**
   * The class to pass exceptions to.
   */
  private SherlockProcessCallback parent;

  // *****************
  // Samelines options
  // *****************

  /**
   * The directory containing the files to be compared.
   */
  private File sourceDirectory;

  /**
   * The list of files in the source directory to be compared.
   */
  private String filesToCompare[];

  /**
   * The default minimum string that we will bother starting new Runs.
   */
  private int minStringLength = 0;

  /**
   * The minimum length of Run to bother printing.  This is tested for
   * in satisfies().
   */
  private int minRunLength = 0;

  /**
   * maxForwardJump and maxBackwardJump are the outer limits allowed for
   * line changes in either file since the last match.  Thus if one file
   * jumps forward by more than 3 lines, the match will not be considered
   * part of the Run.
   */
  private int maxForwardJump = 0;

  /**
   * maxForwardJump and maxBackwardJump are the outer limits allowed for line
   * changes in either file since the last match.  Thus if one file jumps
   * forward by more than 3 lines, the match will not be considered part of
   * the Run.
   */
  private int maxBackwardJump = 0;

  /**
   * The maximum difference in line increments allowed.
   */
  private int maxJumpDiff = 0;

  /**
   * Do we want to amalgamate nearby Runs?
   */
  private boolean amalgamate = true;

  /**
   * Do we want to concatenate nearby Runs?.
   */
  private boolean concatanate = true;

  /**
   * Strictness controls the amount of anomolies we allow.  Anomolies are
   * when a Run of lines doesn't go from the exact last line to the current
   * in both the previously read and the current file.  We only allow Runs
   * where runlength > strictness * anomolies.  This is tested for in
   * satisfies().
   */
  private int strictness = 0;

  /**
   * Constructor for new Samelines. It begins detection immediately.
   *
   * @param spc the parent class to pass any exceptions to
   */
  public Samelines(SherlockProcessCallback spc) {
    super();
    //Initialise KIND
    KIND = new Integer(SAMELINES);

    // Initialise parent variable.
    parent = spc;

    //initialise comparedMap to store compared file pares.
    comparedMap = new Hashtable();

    // Get the directory with the source files in.
    sourceDirectory = Settings.sourceDirectory;

    //obtain list of files from Settings.
    filesToCompare = Settings.getStringFileList();

    int numberOfFiles = 0;
    if (filesToCompare != null) {
      numberOfFiles = filesToCompare.length;

      // Work out which file types are to be used.
    }
    int numberOfFileTypes = 0;
    for (int fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
         fileType++) {
      if (Settings.fileTypes[fileType].isInUse()) {
        numberOfFileTypes++;

        // Work out the number of comparisons to be done.
        //i.e. stages * (number of comparison in each stage).
      }
    }
    stagesToDo = numberOfFileTypes * numberOfFiles * (numberOfFiles - 1) / 2;
  } // Samelines

  /**
   * Constructor for new Samelines. It begins detection immediately.
   *
   * @param spc the parent class to pass any exceptions to
   * @param excludeMap the Map of a exclude file specified in a saved
   * session.
   * @param comparedMap the Map of all file pairs compared in the saved
   * @param excludeMap the Map of exclude file in a saved session.
   * @param matches the number of matches found in a saved session.
   * session.
   */
  public Samelines(SherlockProcessCallback spc, Map comparedMap, Map excludeMap,
            int matches) {
    super();
    //Initialise KIND
    KIND = new Integer(SAMELINES);

    // Initialise parent variable.
    parent = spc;

    // Initialise excludeMap
    this.excludeMap = excludeMap;

    // Initialise comparedMap
    this.comparedMap = comparedMap;

    // indicate this is a saved job.
    if (this.comparedMap != null) {
      fromSaved = true;

    }
    matchesFound = matches;

    // Get the directory with the source files in.
    sourceDirectory = Settings.sourceDirectory;

    //obtain list of files from Settings.
    filesToCompare = Settings.getStringFileList();

    int numberOfFiles = 0;
    if (filesToCompare != null) {
      numberOfFiles = filesToCompare.length;

      // Work out which file types are to be used.
    }
    int numberOfFileTypes = 0;
    for (int fileType = 0; fileType < Settings.NUMBEROFFILETYPES;
         fileType++) {
      if (Settings.fileTypes[fileType].isInUse()) {
        numberOfFileTypes++;

        // Work out the number of comparisons to be done.
        //i.e. stages * (number of comparison in each stage).
      }
    }
    stagesToDo = numberOfFileTypes * numberOfFiles * (numberOfFiles - 1) / 2;
  } // Samelines

  /**
   * Return the hashtable of the exclude file.
   * @return null if there is no exclude file specified.
   */
  public Map getExcludeMap() {
    return excludeMap;
  }

  /**
   * Return the hashtable of the compared files.
   * @return null if no file has been compared.
   */
  public Map getProcessedFiles() {
    return comparedMap;
  }

  /**
   * Process the files by comparing them one to each other.
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
    Settings.message("Started detection at " + hour + ":" + minute);

    //only start comparison if files list is not empty
    if (filesToCompare != null) {
      // If necessary, create the directory to store the matches in.
      File md = new File(Settings.sourceDirectory,
                         Settings.sherlockSettings.getMatchDirectory());
      if (!md.exists()) {
        md.mkdirs();

        // Compare files using each filetype selected.
        // If we're killing this process, don't bother.
      }
      for (int x = Settings.ORI; x < Settings.NUMBEROFFILETYPES; x++) {
        if (Settings.fileTypes[x].isInUse() && !letDie) {
          /* if using the sentence parser, different settings are
             required. */
          if (x == Settings.SEN) {
            Sentence.SIM_THRESHOLD =
                Settings.fileTypes[x].getSimThreshold();
            Sentence.COMMON_THRESHOLD =
                Settings.fileTypes[x].getCommonThreshold();
            Comparison.MEM_INTENSIVE =
                Settings.fileTypes[x].getMemIntensive();
            Sentence.setCommon(Settings.fileTypes[x].getCommonWords());
            Comparison compare = new Comparison(filesToCompare,
                                                sourceDirectory,
                                                Settings.fileTypes[x], this);
            Settings.message
                ("Comparing " + Settings.fileTypes[x].getDescription()
                 + " files.");
            compare.begin();
          }
          else if (!getNatural()) {
            // Load the settings for this file type.
            minStringLength = Settings.fileTypes[x]
                .getMinStringLength();
            minRunLength = Settings.fileTypes[x].getMinRunLength();
            maxForwardJump = Settings.fileTypes[x].getMaxForwardJump();
            maxBackwardJump = Settings.fileTypes[x]
                .getMaxBackwardJump();
            maxJumpDiff = Settings.fileTypes[x].getMaxJumpDiff();
            amalgamate = Settings.fileTypes[x].getAmalgamate();
            concatanate = Settings.fileTypes[x].getConcatanate();
            strictness = Settings.fileTypes[x].getStrictness();

            // Used in the name of any match files.
            fileType = x;
            Settings.message
                ("Comparing " + Settings.fileTypes[x].getDescription()
                 + " files.");

            // Now do the comparison.

            prepareToCompareFiles();
          }
        }
      }
    }
    c = Calendar.getInstance();
    hour = c.get(Calendar.HOUR_OF_DAY);
    min = c.get(Calendar.MINUTE);
    if (min < 10) {
      minute = "0" + Integer.toString(min);
    }
    else {
      minute = Integer.toString(min);
    }
    Settings.message("Finished detection at " + hour + ":" + minute);
  } // processFiles

  /**
   * Called if the user cancels the process before it is finished. All files
   * and directories that have been created must be deleted.
   */
  public void deleteWorkDone() {
    // If match directory exists, empty it of files and then delete it.
    File dir = new File(sourceDirectory,
                        Settings.sherlockSettings.getMatchDirectory());
    if (dir.exists()) {
      File files[] = dir.listFiles();
      for (int x = 0; x < files.length; x++) {
        files[x].delete();
      }
      dir.delete();
    }
  } // deleteWorkDone

  /**
   * Takes the array of filenames, and systematically works through them,
   * comparing each one to another.
   */
  void prepareToCompareFiles() {

    // Initialise the hashtable that stores the lines read in.
    linesOfData = new Hashtable();

    // Initialise the vector that stores the Runs that are detected.
    runs = new ArrayList();

    // Offset in the files array where the filenames start - after any
    //options such as -r 3 etc.
    int arrayOffset = 0;

    // File that holds the exclude file.
    File filex = null;

    // File that holds the first file being compared.
    File file1 = null;

    // File that holds the second file being compared.
    File file2 = null;

    // It doesn't matter here if there is no exclude file
    // (Settings.excludeFile == "").
    // This is checked for during compare()
    filex = new File(Settings.sourceDirectory + Settings.fileSep +
                     Settings.fileTypes[fileType].getDirectory(),
                     Settings.sherlockSettings.getExcludeFile() + "." +
                     Settings.fileTypes[fileType].getExtension());
    files[FILEX] = filex;

    // Only load data if the file actually exists - might only happen
    // with the exclude file.
    // DRW - 06/04/2003 - It checks that the excludeMap is null. I don't
    // know why, as this stops it using the different forms of the exclude
    // file on the second or third parsed forms of the originals...
    if (filex.exists() /*&& excludeMap == null*/) {
      int lineNo = 0;
      int origLineNo = 0;
      String inputString = "";
      File fileToRead = filex;
      //Initialise the hashtable that stores the lines from exclude file.
      excludeMap = new Hashtable();

      try {
        BufferedReader readFromFile =
            new BufferedReader(new FileReader(fileToRead));

        inputString = readFromFile.readLine();

        // Loop through the file, inserting lines read into the
        // excludeMap hashtable.
        while (inputString != null) {
          lineNo++;
          origLineNo++;

          if (inputString.startsWith("#line") &&
              !hashLinesAreComments) {
            try {
              origLineNo = hashLineNumberAsInt(inputString);
            }
            catch (NumberFormatException e) {
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("File " + fileToRead.getName()
                    + " has a bad #line xxxx" +
                    " as follows:\n\t" + inputString, e));
            }
            lineNo--;
          }
          else {

            // Insert line into hash table.
            saveLineOfData
                (excludeMap, inputString, new LineOfDataOccurrence
                 (FILEX, lineNo, origLineNo));

            // Read a line from the file
          }
          inputString = readFromFile.readLine();
        }
        linesInFiles[FILEX] = lineNo;
        //fileToRead.setNumberOfLines(lineNo);

        // Put file back in the files array.
        /* ?!?!?!?!? [Ash] */
        /* don't know why the next line is here !!! */
        //files[FILEX] = fileToRead;

      }
      catch (IOException e) {
        pauseProcessing();
        parent.exceptionThrown
            (new SherlockProcessException
             ("Error processing " + fileToRead.getName(), e));
      }
    }

    // Loop through all the files, comparing each against each.
    for (int file2Loop = 0; file2Loop < filesToCompare.length;
         file2Loop++) {
      String file2name = filesToCompare[file2Loop];

      Settings.message("Comparing against " + file2name + "." +
                       Settings.fileTypes[fileType].getExtension());

      file2 = new File(sourceDirectory + Settings.fileSep
                       + Settings.fileTypes[fileType].getDirectory(),
                       file2name + "."
                       + Settings.fileTypes[fileType].getExtension());

      files[FILE2] = file2;

      String file1name;
      for (int file1Loop = file2Loop + 1;
           file1Loop < filesToCompare.length; file1Loop++) {
        // Set up the files being compared.
        file1name = filesToCompare[file1Loop];

        file1 = new File(sourceDirectory + Settings.fileSep
                         + Settings.fileTypes[fileType].getDirectory(),
                         file1name + "." +
                         Settings.fileTypes[fileType].getExtension());
        files[FILE1] = file1;

        // Pause the process if required as the user is deciding
        //whether or not to cancel it. If it is cancelled, return
        //from this method
        while (pause) {
          if (letDie) {
            return;
          }
        }

        String value = file1.getAbsolutePath() + " "
            + file2.getAbsolutePath();
        Integer key = new Integer(value.hashCode());

        //check whether this process is fired up with a saved session.
        if (!fromSaved) {
          // Do the actual compare now.
          compareFiles();
          comparedMap.put(key, value);
        }
        else {
          //check the comparedMap hashtable
          if (comparedMap.containsKey(key)
              && ( (String) comparedMap.get(key)).equals(value)) {
            stagesDone++;
            continue;
          }
          else {
            compareFiles();
            comparedMap.put(key, value);
          }
        }

        // Completed another stage!
        incStagesDone();
        //yield();
      } // file1Loop loop
    } // file2Loop loop
  } // prepareToCompareFiles

  /**
   * Compares the three files held in the files array: The exclude file
   * and two other data files.
   */
  private void compareFiles() {
    // Clear the structures used to compare files.
    runs.clear();
    linesOfData.clear();
    if (excludeMap != null) {
      linesOfData.putAll(excludeMap);

      // Read in each line from each file
    }
    for (int fileUnderScrutiny = FILE1; fileUnderScrutiny <= FILE2;
         fileUnderScrutiny++) {

      int lineNo = 0;
      int origLineNo = 0;
      String inputString = "";

      File fileToRead = files[fileUnderScrutiny];

      // Only load data if the file actually exists - might only happen
      // with the exclude file.
      if (!fileToRead.exists()) {
        continue;
      }

      try {
        BufferedReader readFromFile =
            new BufferedReader(new FileReader(fileToRead));

        inputString = readFromFile.readLine();

        // Loop through the file, inserting lines read into the
        // linesOfData hashtable.
        while (inputString != null) {
          lineNo++;
          if (fileType != Settings.COM) {
            origLineNo++;

            // Deal with any control characters in the string just
            //read in.
            //inputString = removeControlCharacters(inputString);

            // # line support -- indicates line number of original
            //file.
            //  We don't want to actually insert
            // #lines, or update the lineNo for them, so we have to do
            //  a lineNo-- to compensate for the ++
            // at the start of each loop iteration.
            //
            // The variable hashLinesAreComments is also checked here.
            // Proper use of this will have to be decided upon - it
            // will be used in hashLineNumberAsInt as well.
            //
            // if condition used to contain
            // hashLineNumberAsInt(inputString) != -1. This never
            // occurred as hashLineNumberAsInt can't return a negative
            // number

          }
          if (inputString.startsWith("#line") &&
              !hashLinesAreComments) {
            try {
              origLineNo = hashLineNumberAsInt(inputString);
            }
            catch (NumberFormatException e) {
              // This gets thrown by hashLineNumberAsInt
              // If it happens, there is a problem in one file
              // that will happen each time.
              pauseProcessing();
              parent.exceptionThrown
                  (new SherlockProcessException
                   ("File " + fileToRead.getName()
                    + " has a bad #line xxxx" +
                    " as follows:\n\t" + inputString, e));
            }
            lineNo--;
          }
          else {

            // Insert line into hash table.
            saveLineOfData
                (linesOfData, inputString, new LineOfDataOccurrence
                 (fileUnderScrutiny, lineNo, origLineNo));

            // Read a line from the file
          }
          inputString = readFromFile.readLine();
        }
        linesInFiles[fileUnderScrutiny] = lineNo;
        //fileToRead.setNumberOfLines(lineNo);

        // Put file back in the files array.
        /* ?!?!?!?!? [Ash] */
        //files[fileUnderScrutiny] = fileToRead;
        readFromFile.close();
      }
      catch (IOException e) {
        pauseProcessing();
        parent.exceptionThrown
            (new SherlockProcessException
             ("Error processing " + fileToRead.getName(), e));
      }
    } // for (int fileUnderScrutiny..

    // Have now read in all lines in all three files. Print out all matches
    // for the two submitted files.
    outputSuspectRuns();

  } // compareFiles

  /**
   * This method removes any control characters from the passed string and
   * returns the output
   *
   * @param input string to remove control characters from
   * @return the input string without any control characters
   */
  private String removeControlCharacters(String input) {
    return input;
  }

  /**
   * Add a record of this line of data to the linesOfData hash table.
   * If an occurrence of this data already exists in the hash table, add
   * another reference to it, and if necessary, update any runs that are
   * affected by this new data.
   *
   * @param data the String of data to be added
   * @param addingLineOfDataOccurrence the occurrence of the line of data to
   * add
   */
  private void saveLineOfData
      (Map table, String data,
       LineOfDataOccurrence addingLineOfDataOccurrence) {
    // Go through the hash table. If this line of data has been stored
    // already(from another file), add another occurrence of it to that
    // reference.
    // If this line of data has not previously been saved, do so now.

    int newHashCode = data.hashCode();

    LineOfData tempLineOfData = null;
    
    // This loop checks through the table hash table to see if
    // inputString has been stored already.
    for (int counter = 0;
         table.get(new Integer(newHashCode + counter)) != null;
         counter++) {

      Integer currentHashCode = new Integer(newHashCode + counter);

      tempLineOfData = (LineOfData) table.get(currentHashCode);

      // 1. Check if the data matches that just pulled from the hash
      // table.
      //
      // If the line of data just pulled from the hash table does NOT
      // equal inputString, try the next
      // entry in the hash table.
      //Settings.debugMessage("New data: " + data + "\nStored data: " +
      //		  tempLineOfData.getData());
      if (!data.equals(tempLineOfData.getData())) {
        continue;
      }

      // 2. Check whether the data is actually allowed to be copied.
      //
      // If the line of data we are attempting to add to the hash table
      // is not
      // from the exclude file, and it is already stored having come from
      // the exclude file, ignore this line of data as it was provided in
      // the project specification. All lines from the exclude file will
      // always be retrieved using getFirstOccurrence() as there will
      //only ever be the one occurrence stored. getFirstOccurrence()
      // will never return a null, as each LineOfData stored in the hash
      // table has at least one occurrence. Now return to compareFiles()
      // to continue to process
      // all the files
      LineOfDataOccurrence tempLineOfDataOccurrence =
          tempLineOfData.getFirstOccurrence();

      //   Settings.debugMessage("New file type: " +
      //		  addingLineOfDataOccurrence.getFileType() + "\n" +
      //		  "Stored file type: "
      //            + tempLineOfDataOccurrence.getFileType());
      if ( (addingLineOfDataOccurrence.getFileType() != FILEX) &&
          (tempLineOfDataOccurrence.getFileType() == FILEX)) {
        Settings.debugMessage("Part of excludeFile: " + data);
        return;
      }

      // 3. Create or update any Runs that are affected by this line of
      // data.
      //
      // If this data is from a different file from that stored, then
      // a Run is started/updated.
      // If this data has previously been found in this file, don't edit
      // any runs.
      while (tempLineOfDataOccurrence != null) {
        if (addingLineOfDataOccurrence.getFileType() !=
            tempLineOfDataOccurrence.getFileType()) {
          findRun(new RunCoordinates
                  (tempLineOfDataOccurrence.getLineNo(),
                   addingLineOfDataOccurrence.getLineNo(),
                   tempLineOfDataOccurrence.getOrigLineNo(),
                   addingLineOfDataOccurrence.getOrigLineNo()),
                  data.length());
        }
        tempLineOfDataOccurrence = tempLineOfData.getNextOccurrence();
      }

      // 4. Add this line of data to the existing recorded LineOfData.
      tempLineOfData.addOccurrence(addingLineOfDataOccurrence);
      table.put(currentHashCode, tempLineOfData);

      // Have now added the line of data, so move on.
      return;
    } // end of the for loop
    
    // If reach this part of saveLineOfData, then we are about to add a new
    // lineOfData entry to the table hash table. To have come beyond
    // the above for loop, either the hash table was empty, or the line of
    // data contained in data was not found to be already stored.

    // Create a new LineOfData object.
    tempLineOfData = new LineOfData(data);

    // Record this occurrence of it.
    tempLineOfData.addOccurrence(addingLineOfDataOccurrence);

    // Add to the hashtable
    table.put(new Integer(newHashCode), tempLineOfData);

  } // saveLineOfData

  /**
   * This method amends runs, the vector that holds all of the Runs for the
   * files currently being compared.
   * It can both update an existing Run, or add a new one.
   *
   * @param runsIndex the runs vector index of the Run to be updated;
   *  if this is -1, a new Run is created and added
   * @param matchCoordinates the new end coordinates of the Run;
   *  if this is a new Run, these are both the start and end coordinates.
   * @param anomoliesIncrease the amount to increase the anomolies value of
   * an existing Run. This is ignored for a new Run.
   */
  private void amendruns(int runsIndex, RunCoordinates matchCoordinates,
                         int anomoliesIncrease) {

    // Temporary Run used while updating runs.
    Run tempRun = null;

    // If the index of an exising run is passed, update that run in the
    // runs vector.
    if (runsIndex != -1) {
      tempRun = (Run) runs.get(runsIndex);
      tempRun.extend(matchCoordinates, 1, anomoliesIncrease);
      runs.set(runsIndex, tempRun);
    }
    // Otherwise add a new Run to runs.
    else {
      tempRun = new Run(matchCoordinates, matchCoordinates);
      runs.add(tempRun);
    }
  } // amendruns

  /**
   * We have now established that a line previously read in is the same as
   * one from the current file that is being examined. The coordinates of
   * this match
   * are now used to search through all existing runs, and if they meet the
   * requirements, this latest match will be added to an existing run.
   * If not, a new Run will be started and added to the runs vector.
   *
   * @param matchCoordinates the coordinates of the lines in two files
   *  which contain the identical data
   * @param dataLength the length of the line of data; needed as only data
   *  of at least a certain length is stored
   */
  private void findRun(RunCoordinates matchCoordinates, int stringLength) {

    // Holds each run when it is checked during the loop.
    Run tempRun = null;

    // Loop counter.
    int runsIndex = 0;

    // The fileXLineDifference values reflect the number of lines since
    // the last match recorded.
    int file1LineDifference = 0;
    int file2LineDifference = 0;

    // Becomes true if this match is added to a Run during the loop through
    // all of the runs. If it is false after that, a new Run is created.
    boolean added = false;

    // Loop through the Runs, checking if this new match can be added
    // to any existing Run.
    for (runsIndex = 0; runsIndex < runs.size(); runsIndex++) {

      tempRun = (Run) runs.get(runsIndex);

      // The fileXLineDifference values reflect the number of lines
      //since the
      // last match recorded. If these are within certain limits, and
      // are similar, we add the current line to the current Run in
      //tempRun.
      file1LineDifference = matchCoordinates.getLineNoInFile1() -
          tempRun.getEndCoordinates().getLineNoInFile1();
      file2LineDifference = matchCoordinates.getLineNoInFile2() -
          tempRun.getEndCoordinates().getLineNoInFile2();

      // No point in checking matches if we are duplicating a line.
      if ( (file1LineDifference == 0) || (file2LineDifference == 0)) {
        continue;
      }

      // We have two lines in two files that match.  The differences
      // in line numbers since the last match in the current Run
      // of the current files are diff1 and diff2.  If these are
      // both 1, then the previous line matched, too.  If these
      // are 2, then both files skipped one input line, etc.
      // The greater we make maxBackwardJump and maxForwardJump, the more
      // tolerant the program becomes to swapped round lines.
      // This ought to really be tuned for the target language
      // and whether the input is tokenised etc.  Too low values
      // for maxBackwardJump and maxForwardJump will make plagiarism
      // easy to
      // hide by swapping round groups of lines; too high values
      // will cause problems with spurious matches being produced
      // by skipping out large different sections of both file.
      // This problem will be especially prevalent in the tokenised
      // input form, where there will be lots of lines consisting
      // of things like (Pascal examples):
      //
      //         begin
      //         end ;
      //         <name> ( <integer> ) ;
      //         <name> ( <string> ) ;
      //         for <name> := <integer> to <integer> do
      //         <name> := <name> + <integer> ;

      // Before making maxBackwardJump a positive number:
      // if ( (maxBackwardJump <= file1LineDifference) &&
      //  (file1LineDifference <= maxForwardJump) &&
      //   (maxBackwardJump <= file2LineDifference) &&
      //  (file2LineDifference <= maxForwardJump) &&
      //   (Math.abs(file1LineDifference - file2LineDifference) <
      //maxJumpDiff)) {

      // As maxBackwardJump is now positive, make it negative for this
      // calculation as before.
      if ( ( (maxBackwardJump * -1) <= file1LineDifference) &&
          (file1LineDifference <= maxForwardJump) &&
          ( (maxBackwardJump * -1) <= file2LineDifference) &&
          (file2LineDifference <= maxForwardJump) &&
          (Math.abs(file1LineDifference - file2LineDifference) <
           maxJumpDiff)) {
        amendruns(runsIndex, matchCoordinates,
                  getAnomoly(file1LineDifference,
                             file2LineDifference));
        added = true;
      }

    } // for

    // If this an existing run has not been added to, and the data that is
    // the cause of this match meets the length required, then create a
    // new Run.
    if (!added && (stringLength >= minStringLength)) {
      amendruns( -1, matchCoordinates, 0);

    }
  } // findRun

  /**
   * Calculate how much to add to the anomoly of a Run. The two parameters
   * are the two differences in line numbers the last two matches.
   *
   * @param diff1 the difference between the two line numbers in the first
   * file
   * @param diff2 the difference between the two line numbers in the second
   * file
   * @return the value of this anomoly to be added to the Run's overall value
   */
  private int getAnomoly(int diff1, int diff2) {
    int thisAnomoly = 0;
    if (diff1 != 1 || diff2 != 1) {
      thisAnomoly++;
    }
    if (Math.abs(diff1 - diff2) > 3) {
      thisAnomoly++;
    }
    return thisAnomoly;
  } // getAnomoly

  /**
   * Checks whether a given Run satisfies the conditions for a Run to be
   * worth
   * printing.  It must be long enough (be comprised of enough lines) and not
   * have too many anomolies.
   *
   * @param testRun the Run to check
   * @return true if the run is worth printing
   */
  private boolean shouldPrintRun(Run testRun) {
    return (testRun.getRunning() >= minRunLength) &&
        (testRun.getRunning() >
         (int) (strictness * testRun.getAnomolies()));
  } // shouldPrintRun

  /**
   * This method tests whether two runs are close enough together to be
   * concatanated together. The start and end coordinates of two runs are
   * passed to this method, and along with maxForwardJump (the maximum
   * forward jump in
   * lines that is allowed in a run), this returns true if they are close
   * enough to be concatanated; false if they are not.
   *
   * @param startLine1 the starting line number (in first file) of the first
   *  run
   * @param endLine1 the end line number (in first file) of the second run
   * @param startLine2 the starting line number (in second file) of the
   * first run
   * @param endLine2 the end line number (in second file) of the second run
   */
  private boolean shouldConcatanateRuns(int startLine1, int endLine1,
      int startLine2, int endLine2) {
    boolean run1StartsCloseEnoughToEndOfRun2 =
      (startLine1 + maxForwardJump) >= (endLine1 - maxForwardJump);
    boolean spanOfRunsInFile1LessThanJump = (startLine1 - endLine1) <= (maxForwardJump * 3);
    return ( run1StartsCloseEnoughToEndOfRun2 &&
        ( spanOfRunsInFile1LessThanJump || ((startLine1 + maxForwardJump) <= (endLine1 - maxForwardJump)) ) )
            &&
            ( ( (startLine2 + maxForwardJump) > (endLine2 - maxForwardJump)) &&
                ( ( (startLine2 - endLine2) <= (maxForwardJump * 3)) ||
                    ( (startLine2 + maxForwardJump) <= (endLine2 - maxForwardJump))));
  } // shouldConcatanateRuns

  /**
   * Outputs all the Runs that match shouldPrintRun().
   * Print out any worthwhile Runs from the last file read in, then clear
   * the Run table, ready for next time.
   */
  private void outputSuspectRuns() {

    // Loop counters.
    int outerLoop = 0;
    int innerLoop = 0;

    // Temporary Runs used during processing prior to printing.
    Run outerRun = null;
    Run innerRun = null;

    // First amalgamate Runs, initially only look at the line1*'s
    for (outerLoop = 0; outerLoop < runs.size(); outerLoop++) {

      outerRun = (Run) runs.get(outerLoop);

      for (innerLoop = outerLoop + 1; innerLoop < runs.size();
           innerLoop++) {

        innerRun = (Run) runs.get(innerLoop);

        // Incorporate the comparison looking at line2s as well here.
        // For this part in the original code however, the running
        // value had to be >= HALF the minRunLength value, not a
        //quarter as for the line1s.
        if ( (outerRun.getRunning() >= minRunLength / 4) &&
            (innerRun.getRunning() >= minRunLength / 4)) {
          if (amalgamate) {
            // is outerRun's Run entirely within innerRun's, if so
            // cancel outerRun by setting its 'running' to 0, so
            //it doesn't get printed.
            if ( (
                // Comparing runs using the line numbers from the
                // first file
                // that comprises it.
                (outerRun.getStartCoordinates()
                 .getLineNoInFile1() >=
                 innerRun.getStartCoordinates()
                 .getLineNoInFile1()) &&
                (outerRun.getEndCoordinates()
                 .getLineNoInFile1() <=
                 innerRun.getEndCoordinates()
                 .getLineNoInFile1())) ||
                // Comparing runs using the line numbers from the
                //second file that comprises it.
                ( (outerRun.getStartCoordinates()
                   .getLineNoInFile2() >=
                   innerRun.getStartCoordinates()
                   .getLineNoInFile2()) &&
                 (outerRun.getEndCoordinates()
                  .getLineNoInFile2() <=
                  innerRun.getEndCoordinates()
                  .getLineNoInFile2()))) {
              outerRun.setRunning(0);
              runs.set(outerLoop, outerRun);
            }

            // ... and vice-versa
            if ( ( (innerRun.getStartCoordinates()
                    .getLineNoInFile1() >=
                    outerRun.getStartCoordinates()
                    .getLineNoInFile1()) &&
                  (innerRun.getEndCoordinates()
                   .getLineNoInFile1() <=
                   outerRun.getEndCoordinates()
                   .getLineNoInFile1())) ||
                ( (innerRun.getStartCoordinates()
                   .getLineNoInFile2() >=
                   outerRun.getStartCoordinates()
                   .getLineNoInFile2()) &&
                 (innerRun.getEndCoordinates()
                  .getLineNoInFile2() <=
                  outerRun.getEndCoordinates()
                  .getLineNoInFile2()))) {
              innerRun.setRunning(0);
              runs.set(innerLoop, innerRun);
            }
          } // if (amalgamate)

          // This bit doesn't work properly all the time
          // the concatenation is broken
          if (concatanate) {

            // If outerRun ends within (maxForwardJump + a few
            // lines) of
            // where innerRun starts, concatenate them (into
            // outerRun)
            // and delete innerRun.
            if (shouldConcatanateRuns
                (innerRun.getStartCoordinates().getLineNoInFile1(),
                 outerRun.getEndCoordinates().getLineNoInFile1(),
                 innerRun.getStartCoordinates().getLineNoInFile2(),
                 outerRun.getEndCoordinates().getLineNoInFile2())) {
              outerRun.extend(innerRun.getEndCoordinates(),
                              innerRun.getRunning(),
                              innerRun.getAnomolies());
              innerRun.setRunning(0);
              runs.set(outerLoop, outerRun);
              runs.set(innerLoop, innerRun);
            } // if (shouldConcatanateRuns...

            // ... and vice versa
            if (shouldConcatanateRuns
                (outerRun.getStartCoordinates().getLineNoInFile1(),
                 innerRun.getEndCoordinates().getLineNoInFile1(),
                 outerRun.getStartCoordinates().getLineNoInFile2(),
                 innerRun.getEndCoordinates().getLineNoInFile2())) {
              innerRun.extend(outerRun.getEndCoordinates(),
                              outerRun.getRunning(),
                              outerRun.getAnomolies());
              outerRun.setRunning(0);
              runs.set(outerLoop, outerRun);
              runs.set(innerLoop, innerRun);
            } // if (shouldConcatanateRuns...
          } // if (concatanate)
        } // if (outerRun.getRunning)
      } // for (innerLoop...)
    } // for (outerLoop...)

    // Now print them any which are close enough to warrant outputting.
    int counter = 0;
    Run tempRun = null;

    for (counter = 0; counter < runs.size(); counter++) {
      // A check number to ensure that any matches that have exactly the
      // same names are not overwritten - the id is used as part of the
      // filename the match is stored in.
      int serialID = 0;

      tempRun = (Run) runs.get(counter);

      // If this run satisfies the requirements to show it is worth
      // printing, do so.
      if (shouldPrintRun(tempRun)) {
        int length = linesInFiles[FILE1] + linesInFiles[FILE2];
        int similarity = Math.min
            (100, 100 * tempRun.getRunning() / (length / 2));

        String f1 = files[FILE1].getAbsolutePath();
        String f2 = files[FILE2].getAbsolutePath();

        // Ensure that store original file names - cut off the .xxx
        // extension.
        String f1name;
        String f2name;
        int dotindex = f1.lastIndexOf('.');
        int slashindex = f1.lastIndexOf(Settings.fileSep);
        f1name = f1.substring(slashindex + 1, dotindex);
        FileTypeProfile[] files = Settings.getFileTypes();

        // f1 and f2 should be stored in the Match as relative paths.
        // therefore, need to find the current directory name.
        String dir = "";
        for (int i = 0; i < files.length; i++) {
          if (f1.indexOf(files[i].getDirectory()) != -1) {
            dir = files[i].getDirectory();
            break;
          }
        }
        f1 = dir + f1.substring(slashindex);

        dotindex = f2.lastIndexOf('.');
        slashindex = f2.lastIndexOf(Settings.fileSep);
        f2name = f2.substring(slashindex + 1, dotindex);
        f2 = dir + f2.substring(slashindex);

        // Match to save.
        Match tempMatch = new Match(f1, f2, tempRun, fileType,
                                    similarity);

        // File to save it in.
        File fileToSave = new File(Settings.sourceDirectory
                                   + Settings.fileSep +
                                   Settings.sherlockSettings
                                   .getMatchDirectory(),
                                   f1name + "-" + f2name + "-" +
                                   Settings.fileTypes[fileType]
                                   .getExtension() +
                                   "-" + (serialID++) + "-"
                                   + similarity + "pc" + ".match");

        // increment matchesFound by 1 for each similarity pair of file
        matchesFound++;

        try {
          FileOutputStream fos = new FileOutputStream(fileToSave);
          ObjectOutputStream oos = new ObjectOutputStream(fos);
          oos.writeObject(tempMatch);
          oos.flush();
          oos.close();
          fos.close();
        }
        catch (IOException e) {
          pauseProcessing();
          parent.exceptionThrown(new SherlockProcessException
                                 ("Error saving match: \n"
                                  + tempMatch.toString() +
                                  "\nin file: " +
                                  fileToSave.getName(), e));
        }

      } // if should print run
    } // for (counter...)
  } // outputSuspectRuns

  /**
   * Simply returns the numeric part of "#line xxx" as an int xxx
   * This will have to change to take into account hashLinesAreComments.
   *
   * @param hashLineString String in form of "#line xxx"
   * @return xxx as an int
   */
  private int hashLineNumberAsInt(String hashLineString) throws
      NumberFormatException {
    return Integer.parseInt(hashLineString.substring(6));
  } // hashLineNumberAsInt

  /**
   * Tell the GUI or Sherlock the number of stages to do.
   *
   * @return the number of stages to do
   */
  public int getStagesToDo() {
    return stagesToDo;
  } // getStagesToDo

  public void incStagesDone() {
    stagesDone++;
  }

  public void setNatural(boolean natural) {
    super.setNatural(natural);
    if (natural) {
      stagesToDo = filesToCompare.length * (filesToCompare.length - 1) / 2;
    }
  }

  public void exceptionThrown(String message, Exception e) {
    parent.exceptionThrown(new SherlockProcessException(message, e));
  }

  /**
   * Tell the GUI or Sherlock how many stages have been done.
   *
   * @return the number of stages done
   */
  public int getStagesDone() {
    return stagesDone;
  } // getStagesDone
} // Samelines
