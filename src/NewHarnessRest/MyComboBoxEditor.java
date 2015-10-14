package NewHarnessRest;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

public class MyComboBoxEditor extends DefaultCellEditor{
	private static final long serialVersionUID = 1L;
	JAutoCompleteComboBox cmb;

	public MyComboBoxEditor(Object[] items) {
		
		super(new JAutoCompleteComboBox(items));
		
	}
}
