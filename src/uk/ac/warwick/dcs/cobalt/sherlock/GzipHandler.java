package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.zip.*;

import com.ice.tar.*;

/**
 * Decompresses files in .TAR.GZIP format. For the purpose of use in Sherlock,
 * the directory structure in the archive is not preserved for simplicity.
 * @author Weiliang Zhang
 */
public class GzipHandler {
  /*
   * Unzip ZIP file given into a directory name after the filename.
   */
  public static int gunzip(File gfile) throws IOException {
    //create sub-dir named after the zipfile.
    String dirname = gfile.getAbsolutePath();
    String tarname;

    //remove .gz in filename
    int dotindex = dirname.lastIndexOf('.');
    tarname = dirname.substring(0, dotindex);
    //remove .tar in filename
    dotindex = tarname.lastIndexOf('.');
    dirname = tarname.substring(0, dotindex);

    File dir = new File(dirname);
    File tarfile = new File(tarname);

    //make new directory, delete & make new if existed.
    if (!dir.exists()) {
      dir.mkdir();
    }
    else {
      if (!dir.isDirectory()) {
        dir.delete();
      }
      else {
        deleteDir(dir);
      }
      dir.mkdir();
    }

    //creating a pipelike structured input stream.
    //i.e. gz -> tar -> buffered reader
    GZIPInputStream gin = new GZIPInputStream
        (new FileInputStream(gfile));

    TarInputStream tarin = new TarInputStream(gin);

    TarEntry te = null;

    while ( (te = tarin.getNextEntry()) != null) {
      int c;
      File file;
      String name;
      int index;

      //remove directory information
      name = te.getName();
      index = name.lastIndexOf(Settings.fileSep);
      name = name.substring(index + 1, name.length());

      file = new File(dirname + Settings.fileSep + name);
      //ignore directory entries.
      if (!te.isDirectory()) {
        //if file exists, the new file will be renamed to
        //filename.extensionXX where XX is a number.
        int i = 1;
        while (file.exists()) {
          String tmp = name.concat(String.valueOf(i));
          file = new File
              (dir.getAbsolutePath(), tmp);
          i++;
        }
        file.createNewFile();

        BufferedWriter bw = new BufferedWriter
            (new FileWriter(file));
        BufferedReader br = new BufferedReader
            (new InputStreamReader(tarin));

        while ( (c = br.read()) != -1) {
          bw.write(c);

        }
        bw.close();
      }
    }
    gin.close();
    tarin.close();

    return 0;
  }

  /**
   * Delete directory.
   */
  private static void deleteDir(File dir) throws IOException {
    File[] files = dir.listFiles();
    //delete files in this directory
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        deleteDir(files[i]);
      }
      else {
        files[i].delete();
      }
    }

    //delete this directory
    dir.delete();
  }
}
