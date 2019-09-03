package uk.ac.warwick.dcs.cobalt.sherlock;

import java.io.*;
import java.util.*;
import javax.swing.tree.*;


public class MatchTableDataStruct {
	/**
	 * Structure that contains the matches deserialised from files.
	 *
	 * @serial
	 */
	Match matches[] = null;

	/**
	 * Stores the marking.
	 */
	public static Marking marking;

	/**
	 * Table to obtain data.
	 */
	private MatchTableModelStruct model;

	/**
	 * Linked list holding all nodes except the root node.
	 */
	private LinkedList nodes;

	/**
	 * Number of matches considered suspicious.
	 */
	private int counter = 0;

	public MatchTableDataStruct(Marking mk) {
		matches = loadMatches();
		if (matches == null) {
			return;
		}

		marking = mk;
		if (marking == null) {
			marking = new Marking();

		}
		marking.setMatches(matches);
		marking.generate();


		//Create tree nodes, the linked list holds nodes which represent
		//a pair, these should be unique, each such node then contains
		//children which represent the actual matches.
		nodes = new LinkedList();
		ListIterator itr;
		//read in all match files and add them as nodes.
		for (int i = 0; i < matches.length; i++) {
			Match m = matches[i];
			//two possible names
			boolean added = false;
			String name1 = truncate(m.getFile1()) + " & "
			+ truncate(m.getFile2());
			String name2 = truncate(m.getFile2()) + " & "
			+ truncate(m.getFile1());

			//check whether this pair is know or not.
			itr = nodes.listIterator();
			while (itr.hasNext()) {
				MatchTreeNodeStruct n = (MatchTreeNodeStruct) itr.next();
				//if this is a match for a known pair, add it as a child.
				if (n.toString().equals(name1) || n.toString().equals(name2)) {
					int fileType = m.getFileType();
					RunCoordinates rcstart = m.getRun().getStartCoordinates();
					RunCoordinates rcend = m.getRun().getEndCoordinates();
					String lines1 = new String
					(rcstart.getOrigLineNoInFile1() + " - " +
							rcend.getOrigLineNoInFile1());
					String lines2 = new String
					(rcstart.getOrigLineNoInFile2() + " - " +
							rcend.getOrigLineNoInFile2());
					int percent = m.getSimilarity();
					if (n.toString().equals(name1)) {
						n.add(new MatchTreeNodeStruct
								(fileType, lines1, lines2, percent, i));
					}
					else {
						n.add(new MatchTreeNodeStruct
								(fileType, lines2, lines1, percent, i));
					}
					added = true;
					break;
				}
			}

			//if this is a match for a unknown pair, create a pair node for it,
			//and added as the child of this pair node.
			if (!added) {
				//a new pair is found, add it to the linked list.
				String name = truncate(m.getFile1()) + " & "
				+ truncate(m.getFile2());
				String file1 = truncate(m.getFile1());
				String file2 = truncate(m.getFile2());
				MatchTreeNodeStruct newnode = new MatchTreeNodeStruct(name, file1, file2);

				//add itself as the child.
				int fileType = m.getFileType();
				RunCoordinates rcstart = m.getRun().getStartCoordinates();
				RunCoordinates rcend = m.getRun().getEndCoordinates();
				String lines1 = new String
				(rcstart.getOrigLineNoInFile1() + " - " +
						rcend.getOrigLineNoInFile1());
				String lines2 = new String
				(rcstart.getOrigLineNoInFile2() + " - " +
						rcend.getOrigLineNoInFile2());
				int percent = m.getSimilarity();
				//add itself to the node representing a pair.
				newnode.add(new MatchTreeNodeStruct
						(fileType, lines1, lines2, percent, i));
				//add them to the list.
				nodes.add(newnode);
			}
		}

		//sort the children of every node.
		itr = nodes.listIterator();
		while (itr.hasNext()) {
			MatchTreeNodeStruct m = (MatchTreeNodeStruct) itr.next();

			Object[] tmp = m.getChildren().toArray();
			//cast objects to MatchTreeNodeStruct
			MatchTreeNodeStruct[] children = new MatchTreeNodeStruct[tmp.length];
			for (int i = 0; i < tmp.length; i++) {
				children[i] = (MatchTreeNodeStruct) tmp[i];

				//sort the children
			}
			Arrays.sort(children);
			//clear internal vector, create new ones.
			m.removeAllChildren();
			for (int i = 0; i < children.length; i++) {
				m.add(children[i]);
			}
		}

		//create root node
		MatchTreeNodeStruct root = new MatchTreeNodeStruct();
		//add all nodes to root to create complete tree.
		itr = nodes.listIterator();
		while (itr.hasNext()) {
			root.add( (MatchTreeNodeStruct) itr.next());

		}

		model = new MatchTableModelStruct(root);
		//load marking
		load();
	}
	/**
	 * Return the model
	 */
	public MatchTableModelStruct getModel() {
		return model;
	}

	public boolean hasMatch() {
		return (matches != null) && (matches.length > 0);
	}

	/**
	 * Return the number of marked suspicious matches.
	 */
	private int getCounter() {
		ListIterator itr = nodes.listIterator();
		int n = 0;
		while (itr.hasNext()) {
			Vector c = ( (MatchTreeNodeStruct) itr.next()).getChildren();
			for (int i = 0; i < c.size(); i++) {
				if ( ( (MatchTreeNodeStruct) c.get(i)).isSuspicious()
						.booleanValue()) {
					n++;
				}
			}
		}
		return n;
	}

	/**
	 * Save current marking to a file. Called to save when fram is closed.
	 * Marking in this frame is not saved in real-time, that is, marking
	 * is not updated until the from is closed.
	 */
	private void save() {
		//check every match node, if it's suspicious,
		//add it to marking, but first of all ,clear marking.
		//	if (!marking.isClean()) {
		marking.clear();
		for (int i = 0; i < nodes.size(); i++) {
			//check this pair node's children
			MatchTreeNodeStruct m = (MatchTreeNodeStruct) nodes.get(i);
			Vector matchNodes = m.getChildren();
			for (int j = (matchNodes.size() - 1); j >= 0; j--) {
				m = (MatchTreeNodeStruct) matchNodes.get(j);
				if (m.isSuspicious().booleanValue()) {
					marking.add(m.getIndex());
				}
			}
		}
	}

	/**
	 * Load from existing marking. Called when frame is constructed to
	 * assign correct values to match nodes or when marking is changed
	 * externally and the table needs to be updated.
	 */
	private void load() {
		//reset counter
		counter = 0;
		//reset all nodes
		ListIterator itr = nodes.listIterator();
		while (itr.hasNext()) {
			( (MatchTreeNodeStruct) itr.next())
			.setSuspicious(new Boolean(false));
		}

		//find out nodes which represents the suspicious matches
		//and marking them so that they're displayed correctly
		//in the table.
		counter = 0;
		ListIterator mitr = marking.getIndices().listIterator();
		while (mitr.hasNext()) {
			int index = ( (Integer) mitr.next()).intValue();
			ListIterator nodesItr = nodes.listIterator();
			String file1 = truncate(matches[index].getFile1());
			String file2 = truncate(matches[index].getFile2());
			//scan through all nodes.
			while (nodesItr.hasNext()) {
				//only scan through the children of the corresponding
				//PAIR NODE.
				MatchTreeNodeStruct m = (MatchTreeNodeStruct) nodesItr.next();
				if ( (m.getLines1().equals(file1) && m.getLines2()
						.equals(file2)) ||
						(m.getLines1().equals(file1) && m.getLines2()
								.equals(file2))) {
					Vector children = m.getChildren();
					//scan through all children
					for (int x = 0; x < children.size(); x++) {
						MatchTreeNodeStruct c = (MatchTreeNodeStruct)
						children.get(x);
						if (c.getIndex() == index) {
							c.setSuspicious(new Boolean(true));
							break;
						}
					}
					break;
				}
			}
		}

		//needs to call this as each call to change the nodes set this
		//flag to dirty.
		marking.setClean();
	}

	/**
	 * Update table to reflect any external changes.
	 */
	public void update() {
		load();
	}


	/**
	 * Extract original file name from preprocessed filenames. It also removes
	 * the directory information.
	 */
	private String truncate(String arg) {
		File file = new File(arg);
		String str = file.getName();
		int index = str.lastIndexOf(".");
		str = str.substring(0, index);
		return str;
	}

	/**
	 * Deserialises all matches stored in the match directory into the
	 * returned array, listed in order of similarity
	 *
	 * @return an array of Matches
	 */
	public static Match[] loadMatches() {

		// Get the match files, and create array for them as matches.
		File md = new File(Settings.sourceDirectory,
				Settings.sherlockSettings.getMatchDirectory());
		File matchFiles[] = md.listFiles(new MatchFilenameFilter());
		Match storedMatches[] = new Match[matchFiles.length];

		// If there are no matches, return a null value
		if (storedMatches.length == 0) {
			return null;
		}

		// Deserialise the matches, loading the match into the matches array
		// and details into the data array.
		for (int x = 0; x < storedMatches.length; x++) {
			try {
				FileInputStream fis = new FileInputStream(matchFiles[x]);
				ObjectInputStream ois = new ObjectInputStream(fis);

				// Add this match to the array.
				storedMatches[x] = (Match) ois.readObject();

				ois.close();
				fis.close();
			}
			catch (IOException e) {
				e.printStackTrace();
				// If have an exception then this file does not contain a
				// valid match; set the storedMatches[x] entry to null.
				storedMatches[x] = null;

				//write error log, skip this file and continue.
				String logname = new String(System.getProperty("user.home")
						+ Settings.fileSep
						+ "sherlock.log");
				Date day = new Date(System.currentTimeMillis());
				try {
					BufferedWriter out = new BufferedWriter
					(new FileWriter(logname, true));
					out.write(day + "-The following file does not contain a "
							+ "valid match:\n"
							+ matchFiles[x].getAbsolutePath()
							+ "\nFile skipped.");
					out.newLine();
					out.close();
				}
				catch (IOException e2) {
					System.err.println(day + "-The following file does not "
							+ "contain a valid match:\n"
							+ matchFiles[x].getAbsolutePath()
							+ "File skipped.");
				}
				continue;
			}
			catch (java.lang.ClassNotFoundException f) {
				// If have an exception then this file does not contain a
				// valid match; set the storedMatches[x] entry to null.
				storedMatches[x] = null;

				//write error log, skip this file and continue.
				String logname = new String(System.getProperty("user.home")
						+ Settings.fileSep
						+ "sherlock.log");
				Date day = new Date(System.currentTimeMillis());
				try {
					BufferedWriter out = new BufferedWriter
					(new FileWriter(logname, true));
					out.write(day + "-The following file does not contain a "
							+ "valid match:\n"
							+ matchFiles[x].getAbsolutePath()
							+ "\nFile skipped.");
					out.newLine();
					out.close();
				}
				catch (IOException e2) {
					System.err.println(day + "-The following file does not "
							+ "contain a valid match:\n"
							+ matchFiles[x].getAbsolutePath()
							+ "\nFile skipped.");
				}
				continue;
			}
		} // for

		// Return the matches.
		return storedMatches;
	} // loadMatches
}
