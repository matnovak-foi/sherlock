/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 * Holds global settings used when running Sherlock; these should not need to
 * be changed unless a substantial modification to the algorithm is
 * implemented.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @author Ashley Ward
 * @author Weiliang Zhang
 * @version 6 Sep 2002
 */
public class Settings {

	/**
	 * Version number of Sherlock.  Should help to keep track of exactly what
	 * code someone is using.  Increment this every time the code is given to
	 * someone and the code has been changed.
	 */
	public final static int SHERLOCKVERSION = 5;

	/**
	 * Number of file types.
	 */
	public final static int NUMBEROFFILETYPES = 9;

	/**
	 * Numeric value for the original.
	 */
	public final static int ORI = 0;

	/**
	 * Numeric value for the normalised.
	 */
	public final static int NOR = 1;

	/**
	 * Numeric value for the nowhile.
	 */
	public final static int NOW = 2;

	/**
	 * Numeric value for the nocomment.
	 */
	public final static int NOC = 3;

	/**
	 * Numeric value for the nocomment and normalised.
	 */
	public final static int NCN = 4;

	/**
	 * Numerica value for the no comment no white.
	 */
	public final static int NCW = 5;

	/**
	 * Numeric value for the comment.
	 */
	public final static int COM = 6;

	/**
	 * Numeric value for the sentence-based free-text parser.
	 */
	public final static int SEN = 7;

	/**
	 * Numeric value for the tokenised files.
	 */
	public final static int TOK = 8;

	/**
	 * The array that holds the properties. It is initialised by the GUI
	 * or command line module.
	 */
	static FileTypeProfile fileTypes[] =
		new FileTypeProfile[NUMBEROFFILETYPES];

	/**
	 * Holds the settings for Sherlock.
	 */
	static SherlockSettings sherlockSettings = null;

	/**
	 * The directory that holds the source code to be compared.
	 */
	static java.io.File sourceDirectory = null;

	/**
	 * The string that is the file separator on this file system.
	 */
	static String fileSep = "";

	/**
	 * Flag to say where messages go.
	 */
	static boolean runningGUI = true;

	/**
	 * Flag ot say whether to print debug messages or not.
	 */
	static boolean debug = false;

	/**
	 * List of files that needs to be processed.
	 */
	static File[] fileList = null;

	/**
	 * Log file.
	 */
	static File logFile = new File(System.getProperty("user.home"),
	"sherlock.log");

	/**
	 * Return the list of files to be processed. This list does not give path
	 * information at all because it is used just in SameLines.java and
	 * the path data is automatically generated in that class.
	 */
	public static String[] getStringFileList() {
		String[] list = new String[fileList.length];
		for (int i = 0; i < fileList.length; i++) {
			list[i] = fileList[i].getName();
		}
		return list;
	}

	/**
	 * Return the list of files to be processed.
	 */
	public static File[] getFileList() {
		return fileList;
	}

	/**
	 * Debug message - only print if we are debugging.
	 *
	 * @param debugMessage the debug message to be printed
	 */
	public static void debugMessage(String debugMsg) {
		if (debug) {
			message(debugMsg);
		}
	} // debugMessage

	public static int filterSherlockDirs(File[] dirs) {
		int count = dirs.length;
		for (int i = 0; i < dirs.length; i++) {
			for (int j = 0; j < fileTypes.length; j++) {
				if (dirs[i].getName().equals(fileTypes[j].getDirectory())) {
					dirs[i] = null;
					count--;
					break;
				}
			}
		}
		return count;
	}

	/**
	 * Take a message and print it where appropriate.
	 *
	 * @param msg the message to be printed
	 */
	public static void message(String msg) {
		if (runningGUI) {
			TextWindow.messages.append(msg + "\n");
			TextWindow.messages.setCaretPosition
			(TextWindow.messages.getDocument().getLength());
		}
		else {
			System.out.println(msg);
		}
	} // message

	/**
	 * This loads Sherlock's settings, the fileTypes[] array with the
	 * file type profiles, and gets the file separator.
	 */
	public static void init() {
		sherlockSettings = new SherlockSettings();
		for (int x = 0; x < NUMBEROFFILETYPES; x++) {
			fileTypes[x] = new FileTypeProfile(x);
		}
		fileSep = System.getProperty("file.separator");
	} // init

	public static FileTypeProfile[] getFileTypes() {
		return fileTypes;
	}

	public static File getSourceDirectory() {
		return sourceDirectory;
	}

	public static SherlockSettings getSherlockSettings() {
		return sherlockSettings;
	}

	/**
	 * @return the fileSep
	 */
	public static String getFileSep() {
		return fileSep;
	}

	/**
	 * @param fileSep the fileSep to set
	 */
	public static void setFileSep(String fileSep) {
		Settings.fileSep = fileSep;
	}

	/**
	 * @return the runningGUI
	 */
	public static boolean isRunningGUI() {
		return runningGUI;
	}

	/**
	 * @param runningGUI the runningGUI to set
	 */
	public static void setRunningGUI(boolean runningGUI) {
		Settings.runningGUI = runningGUI;
	}

	/**
	 * @param fileList the fileList to set
	 */
	public static void setFileList(File[] fileList) {
		Settings.fileList = fileList;
	}

	/**
	 * @param fileTypes the fileTypes to set
	 */
	public static void setFileTypes(FileTypeProfile[] fileTypes) {
		Settings.fileTypes = fileTypes;
	}

	/**
	 * @param sherlockSettings the sherlockSettings to set
	 */
	public static void setSherlockSettings(SherlockSettings sherlockSettings) {
		Settings.sherlockSettings = sherlockSettings;
	}

	/**
	 * @param sourceDirectory the sourceDirectory to set
	 */
	public static void setSourceDirectory(java.io.File sourceDirectory) {
		Settings.sourceDirectory = sourceDirectory;
	}

	/**
	 * @return the logFile
	 */
	public static File getLogFile() {
		return logFile;
	}

	/**
	 * @param logFile the logFile to set
	 */
	public static void setLogFile(File logFile) {
		Settings.logFile = logFile;
	}

	
} // Settings
