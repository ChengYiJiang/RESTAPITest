package NewHarnessRest;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class MyPropertiesTable extends JTable{
	TableCellEditor myEditor;
	private int myRow = -1, myCol = -1;
	List<String> modelList = new ArrayList<String>();
	String[] codeArray = {"200", "400", "401", "404", "406", "422", "500"};
	ArrayList<String> codeList = new ArrayList<String>();
	String[] messageArray = {"OK", "Bad Request", "Not Authorized", "Not Found", "Not Acceptable", "Unprocessable Entity", "Internal Server Error"};
	
	DefaultCellEditor dceCode = new DefaultCellEditor(new JAutoCompleteComboBox(codeArray));
	DefaultCellEditor dceMessage = new DefaultCellEditor(new JAutoCompleteComboBox(messageArray));
	
	public MyPropertiesTable(DefaultTableModel model) {
		// TODO Auto-generated constructor stub
		super(model);	
		for (int i=0; i<codeArray.length; i++){
			codeList.add(codeArray[i]);			
		}
		
	}
	
	public void updateMessageComboBox(String s){
		if (codeList.contains(s)){
			int i = 0;
			for (; i<codeList.size(); i++){
				if (codeList.get(i).equals(s))
					break;
			}
			modelList.clear();
			modelList.add(messageArray[i]);
			Object[] modelArray = modelList.toArray();
			JAutoCompleteComboBox comboBox2 = new JAutoCompleteComboBox( modelArray );
			dceMessage = new DefaultCellEditor( comboBox2 );
		}else{
			dceMessage = new DefaultCellEditor(new JAutoCompleteComboBox(messageArray));
		}
	}
	
	public void setComboCell(int r, int c, TableCellEditor ce) {
		this.myRow = r;
		this.myCol = c;
		this.myEditor = ce;
	}
	
	@Override
	public TableCellEditor getCellEditor(int row, int column) {		
		int modelColumn = convertColumnIndexToModel( column );
       // if (modelColumn == 1 && this.getValueAt(row, 0).equals("responseCode"))  
            //return dceCode;
       // else if (modelColumn == 1 && this.getValueAt(row, 0).equals("responseMessage"))
        	//return dceMessage;
        
        return super.getCellEditor(row, column);
	}
}
