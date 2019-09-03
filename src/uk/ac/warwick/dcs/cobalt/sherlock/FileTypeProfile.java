/*
 * Copyright (c) 1999-2000 The University of Warwick. All Rights Reserved.
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

/**
 * Holds the detection settings (the profile) of a file type - original,
 * no comments etc.
 *
 * @author Ben Hart
 * @author Mike Joy
 * @version 12 July 2000
 */
public class FileTypeProfile
    implements Serializable {

  private static final char sep = '*';
  /**
   * The default profile settings for each file type's FileTypeProfile. Each array item is itself an
   * array, with the ordering as follows:
   *
   *   String description
   *   String directory
   *   String extension
   *   String tokeniserName
   *   boolean inUse
   *   int minStringLength
   *   int minRunLength
   *   int maxForwardJump
   *   int maxBackwardJump
   *   int maxJumpDiff
   *   boolean amalgamate
   *   boolean concatanate
   *   int strictness
   *
   * The only difference to this rule is the sentence parser which has the same
   * settings up until minStringLength. The settings are the replaced with:
   *
   *   int simThreshold
   *   int commonThreshold
   *   String commonWords
   *   int maxLinks
   *   int numSimilar
   *
   * simThreshold is the similarity score above which a sentence is stored as
   * being similar to another. commonThreshold is how many words sentences can
   * have in common before it is stored as being similar. If either of these
   * conditions are met the sentence similarity will be stored.
   * commonWords is a String seperated by the "*" character, containing the
       * list of words which should be ignored by the program as they are too common.
   * When comparison is over, if a sentence has a number of links greater than
   * maxLinks then it will be ignored. The numSimilar property calculates that
       * number of files which are most likely to be plagiarised from the file being
   * viewed.
   *
   * The typing is shown only for information - all are stored here as strings.
   */
  public final static String defaults[][] = {
      {
      "Original", "original", "ori", "", "true", "8", "6", "3", "1", "3",
      "true", "true", "2"}
      , { // ORI
      "Normalised (For Java/C/C++ syntax families)", "normalised", "nor",
      "Normalised", "false", "8", "6", "3",
      "1", "3", "true", "true", "2"}
      , { // NOR
      "No whitespace (For other syntax families)", "nowhite", "now", "NoWhite",
      "false", "8", "6", "3",
      "1", "3", "true", "true", "2"}
      , { // NOR
      "No comments", "nocomments", "noc", "NoComments", "false", "8", "6", "3",
      "1", "3", "true", "true", "2"}
      , { // NOC
      "No comments & normalised", "nocomnor", "ncn", "NoCommentsNormalised",
      "true", "8", "6", "3", "1", "3", "true", "true", "2"}
      , { // NCN
      "No comments & no white", "nocomwhi", "ncw", "NoCommentsNoWhite",
      "false", "8", "6", "3", "1", "3", "true", "true", "2"}
      , { // NCW
      "Comments only(samelines)", "comments", "cmt", "Comments", "false", "3",
      "6",
      "3", "1", "3", "true", "false", "2"}
      , { // COM
      "Comments only(sentence)", "comsent", "sen", "Sentence", "true", "80",
      "8",
      "the" + sep + "an" + sep + "and" + sep + "a" + sep + "as" + sep + "or" +
      sep + "of" + sep + "to",
      "6", "5", "true", "true"}
      , { // SEN
      "Tokenised", "tokenised", "tok", "Token", "true", "8", "6", "3", "1", "3",
      "true", "true", "2"} // TOK

  };

  /**
   * The properties of this profile.
   */
  private Properties thisProfile;

  /**
   * The file that this profile is saved in.
   */
  private File propertiesFile;

  /**
   * Constructor for a FileTypeProfile.
   *
   * @param fileType the file type to create
   */
  public FileTypeProfile(int fileType) {
    thisProfile = new Properties(createDefaults(fileType));
    //System.out.println(Settings.sourceDirectory);
    propertiesFile = new File(Settings.sourceDirectory,
                              thisProfile.getProperty("extension") + ".ini");
    load();
  } // FileTypeProfile

  /**
       * This creates the defaults for this FileTypeProfile, retrieving the data from
   * Settings.details[][]
   *
   * @param fileType the file type
   * @return a Properties object containing fileType's defaults
   */
  private Properties createDefaults(int fileType) {
    String ftDefaults[] = defaults[fileType];
    Properties p = new Properties();
    p.setProperty("description", ftDefaults[0]);
    p.setProperty("directory", ftDefaults[1]);
    p.setProperty("extension", ftDefaults[2]);
    p.setProperty("tokeniserName", ftDefaults[3]);
    p.setProperty("inUse", ftDefaults[4]);
    if (fileType != Settings.SEN) {
      p.setProperty("minStringLength", ftDefaults[5]);
      p.setProperty("minRunLength", ftDefaults[6]);
      p.setProperty("maxForwardJump", ftDefaults[7]);
      p.setProperty("maxBackwardJump", ftDefaults[8]);
      p.setProperty("maxJumpDiff", ftDefaults[9]);
      p.setProperty("amalgamate", ftDefaults[10]);
      p.setProperty("concatanate", ftDefaults[11]);
      p.setProperty("strictness", ftDefaults[12]);
    }
    else {
      p.setProperty("simThreshold", ftDefaults[5]);
      p.setProperty("commonThreshold", ftDefaults[6]);
      p.setProperty("commonWords", ftDefaults[7]);
      p.setProperty("maxLinks", ftDefaults[8]);
      p.setProperty("numSimilar", ftDefaults[9]);
      p.setProperty("memIntensive", ftDefaults[10]);
      p.setProperty("groupPairs", ftDefaults[11]);
    }
    return p;
  } // createDefaults

  /**
   * Save this FileTypeProfile to a file.
   */
  public void store() {
    try {
        System.out.println("PROFILE FILE: "+propertiesFile);
      FileOutputStream fos = new FileOutputStream(propertiesFile);
      thisProfile.store(fos, getDescription());
      fos.close();
    }
    catch (java.io.IOException e) {
      Settings.message(e.toString() + "Error saving " +
                       propertiesFile.getAbsolutePath());
    }
  } // store

  /**
   * Load this FileTypeProfile from a file.
   */
  private void load() {
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
       * Clear this FileTypeProfile of its saved values, returning to its default ones
   */
  public void clear() {
    thisProfile.clear();
  } // clear

  /**
   * Sets the description of this file type.
   *
   * @param s the new description
   */
  public void setDescription(String s) {
    thisProfile.setProperty("description", s);
  }

  /**
   * Sets the default directory of this file type.
   *
   * @param s the new default directory
   */
  public void setDirectory(String s) {
    thisProfile.setProperty("directory", s);
  }

  /**
   * Sets the extension of this file type.
   *
   * @param s the new extension
   */
  public void setExtension(String s) {
    thisProfile.setProperty("extension", s);
  }

  /**
   * Sets the tokeniser name of this file type.
   *
   * @param s the new tokener name
   */
  public void setTokeniserName(String s) {
    thisProfile.setProperty("tokeniserName", s);
  }

  /**
   * Sets whether this file type is in use.
   *
   * @param b true if it is, false otherwise
   */
  public void setInUse(boolean b) {
    thisProfile.setProperty("inUse", String.valueOf(b));
  }

  /**
   * Sets the minimum string length allowed for this file type.
   *
   * @param i the new minimum string length
   */
  public void setMinStringLength(int i) {
    thisProfile.setProperty("minStringLength", String.valueOf(i));
  }

  /**
   * Sets the minimum run length allowed for this file type.
   *
   * @param i the new minimum run length
   */
  public void setMinRunLength(int i) {
    thisProfile.setProperty("minRunLength", String.valueOf(i));
  }

  /**
   * Sets the maximum forward jump allowed in runs for this file type.
   *
   * @param i the new maximum forward jump
   */
  public void setMaxForwardJump(int i) {
    thisProfile.setProperty("maxForwardJump", String.valueOf(i));
  }

  /**
   * Sets the maximum backward jump allowed in runs for this file type.
   *
   * @param i the new maximum backward jump
   */
  public void setMaxBackwardJump(int i) {
    thisProfile.setProperty("maxBackwardJump", String.valueOf(i));
  }

  /**
   * Sets the maximum jump difference allowed in runs for this file type.
   *
   * @param i the new maximum jump difference
   */
  public void setMaxJumpDiff(int i) {
    thisProfile.setProperty("maxJumpDiff", String.valueOf(i));
  }

  /**
   * Sets whether to amalgamate nearby runs in this file type.
   *
   * @param b true if to amalgamate, false otherwise
   */
  public void setAmalgamate(boolean b) {
    thisProfile.setProperty("amalgamate", String.valueOf(b));
  }

  /**
   * Sets whether to concatanate nearby runs in this file type.
   *
   * @param b true if to concatanate, false otherwise
   */
  public void setConcatanate(boolean b) {
    thisProfile.setProperty("concatanate", String.valueOf(b));
  }

  /**
   * Sets the strictness allowed in runs for this file type.
   *
   * @param i the new strictness
   */
  public void setStrictness(int i) {
    thisProfile.setProperty("strictness", String.valueOf(i));
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return thisProfile.getProperty("description");
  }

  /**
   * Returns the default directory.
   *
   * @return the default directory
   */
  public String getDirectory() {
    return thisProfile.getProperty("directory");
  }

  /**
   * Returns the extension.
   *
   * @return the extension
   */
  public String getExtension() {
    return thisProfile.getProperty("extension");
  }

  /**
   * Returns the tokeniser name.
   *
   * @return the tokeniser name
   */
  public String getTokeniserName() {
    return thisProfile.getProperty("tokeniserName");
  }

  /**
   * Returns whether this file type is in use.
   *
   * @return true it is in use, false otherwise
   */
  public boolean isInUse() {
    return Boolean.valueOf(thisProfile.getProperty("inUse")).booleanValue();
  }

  /**
   * Returns the minimum string length.
   *
   * @return the minimum string length
   */
  public int getMinStringLength() {
    return Integer.parseInt(thisProfile.getProperty("minStringLength"));
  }

  /**
   * Returns the minimum run length.
   *
   * @return the minimum run length
   */
  public int getMinRunLength() {
    return Integer.parseInt(thisProfile.getProperty("minRunLength"));
  }

  /**
   * Returns the maximum forward jump.
   *
   * @return the maximum forward jump
   */
  public int getMaxForwardJump() {
    return Integer.parseInt(thisProfile.getProperty("maxForwardJump"));
  }

  /**
   * Returns the maximum backward jump.
   *
   * @return the maximum backward jump
   */
  public int getMaxBackwardJump() {
    return Integer.parseInt(thisProfile.getProperty("maxBackwardJump"));
  }

  /**
   * Returns the maximum jump difference.
   *
   * @return the maximum jump difference
   */
  public int getMaxJumpDiff() {
    return Integer.parseInt(thisProfile.getProperty("maxJumpDiff"));
  }

  /**
   * Returns the amalgamate.
   *
   * @return the amalgamate
   */
  public boolean getAmalgamate() {
    return Boolean.valueOf(thisProfile.getProperty("amalgamate")).booleanValue();
  }

  /**
   * Returns the concatanate.
   *
   * @return the concatanate
   */
  public boolean getConcatanate() {
    return Boolean.valueOf(thisProfile.getProperty("concatanate")).booleanValue();
  }

  /**
   * Returns the strictness.
   *
   * @return the strictness
   */
  public int getStrictness() {
    return Integer.parseInt(thisProfile.getProperty("strictness"));
  }

  public int getSimThreshold() {
    return Integer.parseInt(thisProfile.getProperty("simThreshold"));
  }

  public void setSimThreshold(int i) {
    thisProfile.setProperty("simThreshold", String.valueOf(i));
  }

  public int getCommonThreshold() {
    return Integer.parseInt(thisProfile.getProperty("commonThreshold"));
  }

  public boolean getGroupPairs() {
    return Boolean.valueOf(thisProfile.getProperty("groupPairs")).
        booleanValue();
  }

  public void setGroupPairs(boolean val) {
    thisProfile.setProperty("groupPairs", String.valueOf(val));
  }

  public void setCommonThreshold(int i) {
    thisProfile.setProperty("commonThreshold", String.valueOf(i));
  }

  public void setMaxLinks(int i) {
    thisProfile.setProperty("maxLinks", String.valueOf(i));
  }

  public int getMaxLinks() {
    return Integer.parseInt(thisProfile.getProperty("maxLinks"));
  }

  public void setNumSimilar(int i) {
    thisProfile.setProperty("numSimilar", String.valueOf(i));
  }

  public int getNumSimilar() {
    return Integer.parseInt(thisProfile.getProperty("numSimilar"));
  }

  public void setMemIntensive(boolean val) {
    thisProfile.setProperty("memIntensive", String.valueOf(val));
  }

  public boolean getMemIntensive() {
    return Boolean.valueOf(thisProfile.getProperty("memIntensive")).
        booleanValue();
  }

  public String[] getCommonWords() {
    String temp = thisProfile.getProperty("commonWords");
    if (temp == null) {
      return new String[0];
    }

    StringTokenizer tok = new StringTokenizer(temp, "" + sep);
    String[] retArray = new String[tok.countTokens()];

    for (int i = 0; i < retArray.length; i++) {
      retArray[i] = tok.nextToken();
    }
    return retArray;
  }

  public void setCommonWords(String[] common) {
    StringBuffer temp = new StringBuffer();
    for (int i = 0; i < common.length; i++) {
      if (i + 1 < common.length) {
        temp.append(common[i]);
        temp.append(sep);
      }
      else {
        temp.append(common[i]);
      }
    }
    thisProfile.setProperty("commonWords", temp.toString());
  }
}
