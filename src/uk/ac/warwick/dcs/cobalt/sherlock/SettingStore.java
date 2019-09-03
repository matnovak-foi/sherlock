/**
 * Create a copy of the current sherlock settings. Used to be saved into a
 * file.
 *
 * @author Weiliang Zhang
 * @version 20 Aug 2002
 */

package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;

public class SettingStore
    implements Serializable {
  //source directory setting.
  protected File sourceDirectory;

  //Sherlock Settings
  protected SherlockSettings sherlockSettings;

  /**
   * The array that holds the properties. It is initialised by the GUI
   * or command line module.
   */
  protected FileTypeProfile fileTypes[];

  /**
   * The string that is the file separator on this file system.
   */
  protected String fileSep;

  /**
   * Flag to say where messages go.
   */
  protected boolean runningGUI;

  /**
   * Flag ot say whether to print debug messages or not.
   */
  protected boolean debug = false;

  /**
   * List of files that needs to be processed.
   */
  protected File[] fileList = null;

  /**
   * Map of exlcude file.
   */
  protected Map excludeMap = null;

  /**
   * Number of matches found in Samelines.
   */
  protected int matches = 0;

  /**
   * Construct an object containing the current Sherlock Settings.
   */
  public SettingStore() {
    //get settings from Settings class
    sourceDirectory = Settings.sourceDirectory;
    sherlockSettings = Settings.sherlockSettings;
    fileTypes = Settings.fileTypes;
    fileSep = Settings.fileSep;
    runningGUI = Settings.runningGUI;
    debug = Settings.debug;
    fileList = Settings.fileList;
  }

  /**
   * Restore settings from this object.
   */
  public void restore() {
    Settings.sourceDirectory = sourceDirectory;
    Settings.sherlockSettings = sherlockSettings;
    Settings.fileTypes = fileTypes;
    Settings.fileSep = fileSep;
    Settings.runningGUI = runningGUI;
    Settings.debug = debug;
    Settings.fileList = fileList;
  }
}
