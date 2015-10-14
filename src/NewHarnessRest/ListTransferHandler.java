package NewHarnessRest;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

//For drag and drop list elements======================================
//Basic code is from oracal java document and made some modification.
class ListTransferHandler extends TransferHandler {
	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added.
	String tempString;

	/**
	 * We only support importing strings.
	 */
	public boolean canImport(TransferHandler.TransferSupport info) {
		// Check for String flavor
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		return true;
	}

	/**
	 * Bundle up the selected items in a single list for export. Each line is
	 * separated by a newline.
	 */
	protected Transferable createTransferable(JComponent c) {
		JList list = (JList) c;
		indices = list.getSelectedIndices();
		Object[] values = list.getSelectedValues();

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < values.length; i++) {
			Object val = values[i];
			buff.append(val == null ? "" : val.toString());
			if (i != values.length - 1) {
				buff.append("\n"); // TODO HERE \r\n
			}
		}

		return new StringSelection(buff.toString());
	}

	/**
	 * We support both copy and move actions.
	 */
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}

	/**
	 * Perform the actual import. This demo only supports drag and drop.
	 */
	public boolean importData(TransferHandler.TransferSupport info) {
		if (!info.isDrop()) {
			return false;
		}

		JList list = (JList) info.getComponent();
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		JList.DropLocation dl = (JList.DropLocation) info.getDropLocation();
		int index = dl.getIndex();
		boolean insert = dl.isInsert();

		// Get the string that is being dropped.
		Transferable t = info.getTransferable();
		String data;
		try {
			data = (String) t.getTransferData(DataFlavor.stringFlavor);

			tempString = data;
			// System.out.println("Data is "+tempString);
		} catch (Exception e) {
			return false;
		}

		// Wherever there is a newline in the incoming data,
		// break it into a separate item in the list.
		String[] values = data.split("\n");

		addIndex = index;
		addCount = values.length;
		

		// Perform the actual import.
		for (int i = 0; i < values.length; i++) {
			if (insert) {
				listModel.add(index++, values[i]);
			} else {
				// If the items go beyond the end of the current
				// list, add them in.
				if (index < listModel.getSize()) {
					listModel.set(index++, values[i]);
				} else {
					listModel.add(index++, values[i]);
				}
			}
		}
		return true;
	}

	/**
	 * Remove the items moved from the list.
	 */
	protected void exportDone(JComponent c, Transferable data, int action) {
		JList source = (JList) c;
		DefaultListModel listModel = (DefaultListModel) source.getModel();

		if (action == TransferHandler.MOVE) {
			System.out.println("Is Move!");
			for (int i = indices.length - 1; i >= 0; i--) {
				listModel.remove(source.getSelectedIndex());
				source.updateUI();
			}
		}
		indices = null;
		addCount = 0;
		addIndex = -1;
	}
}