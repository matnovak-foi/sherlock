/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */
package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;

/**
 The command line class for running Sherlock.

 @author Ben Hart
 @author Mike Joy
 @version 12 July 2000
 */
public class Sherlock
        implements SherlockProcessCallback {

    /**
     The process that is to be run: TokeniseFiles or Samelines.
     */
    private SherlockProcess process = null;

    /**
     Load up a new instance of Sherlock!
     */
    public static void main(String args[]) {
        Sherlock s = new Sherlock(args);
    } // main

    /**
     Run Sherlock from the command line.

     @param args[] the command line arguments
     */
    public Sherlock(String args[]) {

        // Tell Settings where to send debug messages.
        Settings.runningGUI = false;

        // Flags used to run different parts of Sherlock if changed to true.
        boolean preProcess = false, detect = false, view = false;

        // If there are no arguments, display how-to-use instructions.
        if (args.length == 0) {
            showCommandLineParameters();
            return;
        } // Otherwise check they are valid. If not, exit with error and
        // display how-to-use instructions.
        else {
            for (int x = 0; x < args.length; x++) {
                // First argument must be the source directory.
                if (x == 0) {
                    // Check source directory is a directory.
                    File sd = new File(args[x]);
                    if (!sd.isDirectory()) {
                        Settings.message("Directory containing source files is invalid");
                        showCommandLineParameters();
                        return;
                    } // If ok, put these details into Settings.
                    else {
                        Settings.sourceDirectory = new File(args[x]);
                    }
                } else if (args[x].equals("-p")) {
                    preProcess = true;
                } else if (args[x].equals("-d")) {
                    detect = true;
                } else if (args[x].equals("-v")) {
                    view = true;
                }
            }
        } // End of checking arguments are valid.

        // If no valid arguments aside from the directory have been given, tell the user.
        if (!preProcess && !detect && !view) {
            Settings.message(
                    "No valid options given - Sherlock 2000 has nothing to do!");
            showCommandLineParameters();
            return;
        }

        // Load settings with the default detection options.
        Settings.init();

        // Run Sherlock however is needed.
        runSherlock(preProcess, detect, view);

        // Save the settings used.
        for (int x = 0; x < Settings.NUMBEROFFILETYPES; x++) {
            Settings.fileTypes[x].store();
        }
    } // Sherlock

    /**
     Actually run the parts of sherlock that the user has requested.

     @param preProcess if true, run source files through the tokenisers
     @param detect if true, run pre-processed files through Samelines
     @param view if true, output any matches found
     */
    public void runSherlock(boolean preProcess, boolean detect, boolean view) {

        // Tokenise files
        if (preProcess) {
            process = new TokeniseFiles(this);
            process.start();
        }
        if (process != null) {
            try {
                process.join();
            } catch (InterruptedException e) {
                Settings.message("Error tokenising files:\n" + e.getMessage());
                return;
            }
        }
        // Detect copying!
        if (detect) {
            // Wait for tokenising to finish if necessary.

            process = new Samelines(this);
            process.start();
        }
        if (process != null) {
            try {
                process.join();
            } catch (InterruptedException e) {
                Settings.message("Error tokenising files:\n" + e.getMessage());
                return;
            }
        }
        // Display matches found.
        if (view) {
            // Wait for tokenising or detecting to finish if necessary.

            Match matches[] = MatchesScreen.loadMatches();
            for (int x = 0; x < matches.length; x++) {
                System.out.println(matches[x].toString());
            }
        }

    } // runSherlock

    /**
     Handle an exception that has occurred during a SherlockProcess's
     processing.

     @param e the SherlockProcessException thrown
     */
    public void exceptionThrown(SherlockProcessException spe) {

        // Remove everything that's been done, and let the user know.
        process.deleteWorkDone();
        process.letProcessDie();

        String msg = spe.getMessage() + "\n" + spe.getOriginalException().toString();

        Settings.message("Error:\n" + msg);

        return;

    } // exceptionThrown

    /**
     Displays the options that can be passed to Sherlock from the command line.
     */
    private void showCommandLineParameters() {
        System.out.println("Command line parameters are: ");
        System.out.println(
                "java Sherlock.Sherlock [drive:][path]directoryname [-p] [-d] [-v]\n");
        System.out.println("\t[drive:][path]directoryname\n"
                + "\t  "
                + "\tSpecifies drive and directory containing the source files to be used.\n");
        System.out.println("\t-p"
                + "\tPre-process files in this source directory.\n");
        System.out.println("\t-d" + "\tDetect over pre-processed files.\n");
        System.out.println("\t-v"
                + "\tView matches found following detection over these files.\n");
    } // showCommandLineParameters

}
