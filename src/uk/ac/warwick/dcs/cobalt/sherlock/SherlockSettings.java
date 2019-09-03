/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * Holds the general settings
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 16 July 2000
 */
public class SherlockSettings
    implements Serializable {

  /**
   * The default Sherlock settings, ordering:
   *
   * boolean java
   * String matchDirectory
   * String excludeFile
   * boolean showWholeSub
   * int subLinesToShow
   * boolean showWholeTok
   * int tokLinesToShow
   */
  private final static String defaults[] = {
      "true", "match", "", "true", "3", "true", "3"};

  /**
   * The properties of this profile.
   */
  private Properties thisProfile;

  /**
   * The file that this profile is saved in.
   */
  private File propertiesFile;

  /**
   * Constructor for a SherlockSettings.
   */
  public SherlockSettings() {
    thisProfile = new Properties(createDefaults());
    propertiesFile = new File(Settings.sourceDirectory, "sherlock.ini");
    load();
  } // SherlockSettings

  /**
   * This creates the defaults for this SherlockSettings.
   *
   * @return a Properties object containing Sherlock's defaults
   */
  public Properties createDefaults() {
    Properties p = new Properties();
    p.setProperty("java", defaults[0]);
    p.setProperty("matchDirectory", defaults[1]);
    p.setProperty("excludeFile", defaults[2]);
    p.setProperty("showWholeSub", defaults[3]);
    p.setProperty("subLinesToShow", defaults[4]);
    p.setProperty("showWholeTok", defaults[5]);
    p.setProperty("tokLinesToShow", defaults[6]);
    return p;
  } // createDefaults

  /**
   * Save this SherlockSettings to a file.
   */
  public void store() {
    try {
      FileOutputStream fos = new FileOutputStream(propertiesFile);
      thisProfile.store(fos, "Sherlock Settings");
      fos.close();
      Settings.message("Properties file " + propertiesFile + " stored");
    }
    catch (java.io.IOException e) {
      Settings.message(e.toString() + "Error saving " +
                       propertiesFile.getAbsolutePath());
    }
  } // store

  /**
   * Load this SherlockSettings from a file.
   */
  public void load() {
    try {
      FileInputStream fis = new FileInputStream(propertiesFile);
      thisProfile.load(fis);
      fis.close();
    }
    catch (java.io.IOException e) {
      Settings.message(propertiesFile.getAbsolutePath() +
                       " does not exist: creating new one.");
    }
  } // load

  /**
   * Clear this SherlockSettings of its saved values, returning
   * to its default ones
   */
  public void clear() {
    thisProfile.clear();
  } // clear

  /**
   * Sets whether the language being detected over is Java.
   *
   * @param b true if it is Java, false if C++
   */
  public void setJava(boolean b) {
    thisProfile.setProperty("java", String.valueOf(b));
  } // setJava

  /**
   * Sets the match directory.
   *
   * @param s the new match directory
   */
  public void setMatchDirectory(String s) {
    thisProfile.setProperty("matchDirectory", s);
  } // setMatchDirectory

  /**
   * Sets the exclude file for this source directory.
   *
   * @param s the new exclude file
   */
  public void setExcludeFile(String s) {
    thisProfile.setProperty("excludeFile", s);
  } // setExcludeFile

  /**
   * Sets whether to show the whole of the submitted files.
   *
   * @param b if true, show the whole file
   */
  public void setShowWholeSub(boolean b) {
    thisProfile.setProperty("showWholeSub", String.valueOf(b));
  } // setShowWholeSub

  /**
   * Sets the number of lines before and after a suspicious section in
   * a submitted file to show.
   *
   * @param i the number of lines
   */
  public void setSubLinesToShow(int i) {
    thisProfile.setProperty("subLinesToShow", String.valueOf(i));
  } // setSubLinesToShow

  /**
   * Sets whether to show the whole of the tokenised files.
   *
   * @param b if true, show the whole file
   */
  public void setShowWholeTok(boolean b) {
    thisProfile.setProperty("showWholeTok", String.valueOf(b));
  } // setShowWholeTok

  /**
   * Sets the number of lines before and after a suspicious section in
   * a tokenised file to show.
   *
   * @param i the number of lines
   */
  public void setTokLinesToShow(int i) {
    thisProfile.setProperty("tokLinesToShow", String.valueOf(i));
  } // setSubLinesToShow

  /**
   * Returns true if the language is Java.
   *
   * @return true if the language is Java
   */
  public boolean isJava() {
    return Boolean.valueOf(thisProfile.getProperty("java")).booleanValue();
  } // getJava

  /**
   * Returns the match directory.
   *
   * @return the match directory
   */
  public String getMatchDirectory() {
    return thisProfile.getProperty("matchDirectory");
  } // getMatchDirectory

  /**
   * Returns the exclude file.
   *
   * @return the exclude file
   */
  public String getExcludeFile() {
    return thisProfile.getProperty("excludeFile");
  } // getExcludeFile

  /**
   * Returns true if to show whole of submitted file.
   *
   * @return true if to show whole of submitted file
   */
  public boolean getShowWholeSub() {
    return Boolean.valueOf(thisProfile.getProperty("showWholeSub")).
        booleanValue();
  } // getShowWholeSub

  /**
   * Returns number of lines before and after a suspicious section in
   * a submitted file to show.
   *
   * @return the number of lines
   */
  public int getSubLinesToShow() {
    return Integer.parseInt(thisProfile.getProperty("subLinesToShow"));
  } // getSubLinesToShow

  /**
   * Returns true if to show whole of tokenised file.
   *
   * @return true if to show whole of tokenised file
   */
  public boolean getShowWholeTok() {
    return Boolean.valueOf(thisProfile.getProperty("showWholeTok")).
        booleanValue();
  } // getShowWholeTok

  /**
   * Returns number of lines before and after a suspicious section in
   * a tokenised file to show.
   *
   * @return the number of lines
   */
  public int getTokLinesToShow() {
    return Integer.parseInt(thisProfile.getProperty("tokLinesToShow"));
  } // getTokLinesToShow

} // SherlockSettings
