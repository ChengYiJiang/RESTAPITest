package NewHarnessRest;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class MyFileTable extends JTable{
	
	private static final long serialVersionUID = 1L;
	private int myRow = -1, myCol = -1;
	TableCellEditor myEditor;
	
	public MyFileTable(DefaultTableModel model) {
		// TODO Auto-generated constructor stub
		super(model);
	}
	

	public void setComboCell(int r, int c, TableCellEditor ce) {
		this.myRow = r;
		this.myCol = c;
		this.myEditor = ce;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {		
		if (this.getValueAt(row, 0).equals("Method") && column == myCol)		
			return myEditor;
		return super.getCellEditor(row, column);
	}


	
}
