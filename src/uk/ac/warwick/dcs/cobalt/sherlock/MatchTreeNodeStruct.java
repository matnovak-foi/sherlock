package uk.ac.warwick.dcs.cobalt.sherlock;

import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class MatchTreeNodeStruct extends DefaultMutableTreeNode implements Comparable {
	/**
	 * Constant representing a root node.
	 */
	public static final int ROOT_NODE = 0;
	/**
	 * Constant representing a pair node which contain match nodes.
	 */
	public static final int PAIR_NODE = 1;
	/**
	 * Constant representing a match node.
	 */
	public static final int MATCH_NODE = 2;

	/**
	 * Indicate in which preprocessed version this match was found.
	 */
	private int matchType = -1;

	/**
	 * The type of this node.
	 */
	private int nodeType = -1;

	/**
	 * Line number range in file 1. For a PAIR NODE, this field is the file
	 * name.
	 */
	private String lines1;

	/**
	 * Line number range in file 2. For a PAIR NODE, this field is the file
	 * name.
	 */
	private String lines2;

	private int percent;
	private boolean isSuspicious = false;

	/**
	 * The actually index in the 'matches' array represented by the node
	 * in the MatchesScreen class. ROOT_NODE and PAIR_NODE do not have an
	 * valid index, only MATCH_NODE has valid index.
	 */
	private int index = -1;

	/**
	 * Construct a root node. Named by the source directory path.
	 */
	public MatchTreeNodeStruct() {
		super(Settings.sourceDirectory.getAbsolutePath());
		nodeType = ROOT_NODE;
		lines1 = "---";
		lines2 = "---";
		percent = -1;
		isSuspicious = false;
		index = -2;
	}

	/**
	 * Construct a pair node.
	 */
	public MatchTreeNodeStruct(String name, String file1, String file2) {
		super(name);
		nodeType = PAIR_NODE;
		lines1 = file1;
		lines2 = file2;
		percent = 0;
		isSuspicious = false;
		index = -1;
	}

	/**
	 * Construct a MATCH NODE.
	 *
	 * @param matchType the type of preprocessed file in which this match was
	 * found. This is one of type constant in Settings.java. Negative value
	 * of this variable indicates that this node is not a MATCH NODE.
	 * @param lines1 line number range for file 1.
	 * @param lines2 line number range for file 2.
	 * @param percent percentage of this match.
	 * @param index actual index of this match in the matches array.
	 */
	public MatchTreeNodeStruct(int matchType, String lines1, String lines2,
			int percent, int index) {
		super(Settings.fileTypes[matchType].getDescription());
		this.matchType = matchType;
		nodeType = MATCH_NODE;
		this.lines1 = lines1;
		this.lines2 = lines2;
		this.percent = percent;
		this.isSuspicious = false;
		// this.children = children;
		this.index = index;
	}

	/**
	 * Compares this node with node given.
	 */
	public int compareTo(Object node) {
		return Settings.fileTypes[matchType].getDescription()
		.compareTo(Settings.fileTypes[ ( (MatchTreeNodeStruct) node).getMatchType()]
		                               .getDescription());
	}

	/**
	 * Add given node as this node's child, also increment the precentage if
	 * this node is not the root node.
	 */
	public void add(MutableTreeNode node) {
		super.add(node);
		if (nodeType != ROOT_NODE) {
			percent += ( (MatchTreeNodeStruct) node).getPercent().intValue();
		}
	}

	/**
	 * Remove all children, also clear the percentage variable.
	 */
	public void removeAllChildren() {
		super.removeAllChildren();
		percent = 0;
	}

	/**
	 * All children of this node.
	 */
	public Vector getChildren() {
		return children;
	}

	/**
	 * Return the type of match represented by this node..
	 */
	public int getMatchType() {
		return matchType;
	}

	/**
	 * Line number pair in file 1.
	 */
	public String getLines1() {
		return lines1;
	}

	/**
	 * Line number pair in file 2.
	 */
	public String getLines2() {
		return lines2;
	}

	/**
	 * Percentage of this match.
	 */
	public Integer getPercent() {
		return new Integer(percent);
	}

	/**
	 * Type of this node.
	 */
	public int getNodeType() {
		return nodeType;
	}

	/**
	 * The actually index of this match node in the matches array.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Whether this match is considered to be suspicious.
	 *
	 * @return null if this node is the root node.
	 */
	public Boolean isSuspicious() {
		if (nodeType == ROOT_NODE) {
			return null;
		}
		else {
			//if all children of this node is set to true, set this node to
			//true. The reason why isSuspicious variable is not directly
			//returned is that when loading from a file, there is no indication
			//in the saved file that while mark a PAIR NODE is suspicious.
			int c = getChildCount();

			//if this node is a MATCH NODE, return isSuspicious directly.
			if (c == 0) {
				return new Boolean(isSuspicious);
			}
			//else compute isSuspicious and then return.
			isSuspicious = true;
			for (int i = 0; i < c; i++) {
				if (! ( (MatchTreeNodeStruct) getChildAt(i)).isSuspicious()
						.booleanValue()) {
					isSuspicious = false;
					break;
				}
			}
			return new Boolean(isSuspicious);
		}
	}

	/**
	 * True if this node is a leaf in the tree, false otherwise.
	 */
	public boolean isLeaf() {
		return (children == null) ? true : false;
	}

	/**
	 * Set this match to be suspicious.
	 */
	public void setSuspicious(Boolean value) {
		isSuspicious = value.booleanValue();
		MatchTableDataStruct.marking.setDirty();

		//set all children to this value.
		int c = getChildCount();
		for (int i = 0; i < c; i++) {
			( (MatchTreeNodeStruct) getChildAt(i)).setSuspicious(value);
		}
	}
}
