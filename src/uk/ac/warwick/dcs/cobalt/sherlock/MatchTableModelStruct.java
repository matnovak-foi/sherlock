package uk.ac.warwick.dcs.cobalt.sherlock;

public class MatchTableModelStruct extends DynamicTreeTableModel {
	private static final String[] columnNames = {
		"TYPE", "FILE 1", "FILE 2", "%", "SUSPICIOUS"};

	private static final Class[] classes = {
		TreeTableModel.class, String.class, String.class, Integer.class,
		Boolean.class};

	private static final String[] methodNames = {
		"toString", "getLines1", "getLines2", "getPercent", "isSuspicious",
	"isLeaf"};

	/**
	 * Empty strings used to match methodNames, otherwise exception
	 * will be thrown in DynamicTreeTableModel.
	 */
	private static final String[] setterMethodNames = {
		"", "", "", "", "setSuspicious"};

	/**
	 * @param root the name of the source directory.
	 */
	public MatchTableModelStruct(MatchTreeNodeStruct root) {
		super(root, columnNames, methodNames, setterMethodNames, classes);
	}

	public boolean isCellEditable(Object node, int column) {
		//only allowed to edit first column (to expend the tree)
		//and SUPICIOUS column.
		if (column == 0) {
			return true;
		}
		else if (column == 4) {
			return (root != node);
		}
		else {
			return false;
		}
	}
}
