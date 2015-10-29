package NewHarnessRest;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

import org.json.*;

public class RestRequestView extends JPanel{

	/**
	 * @param args
	 */
	
	private String url = "";
	private MyFileTable jtable;
	private JSONObject jo;
	private DefaultTableModel model;
	Properties prop = new Properties();
	private String [] headings = {"Param", "Value"};
	private JFileChooser chooser = new JFileChooser(".");
	//buttons
	private JLabel jlabel = new JLabel();
	public JButton saveStepButton = new JButton("Save as");	
	public JButton newStepButton = new JButton("New Step");
	public JButton clearButton = new JButton("Delete");
	private JButton newParamButton = new JButton("Add");
	public JButton savetoButton = new JButton("Save");
	public JButton dataButton = new JButton("Data");
	//////////////////////////////////////////////////
	private String loadFilePath = "";
	private FileInputStream in;
	private String saveFilePath = "";
	private FileWriter fw;
	public JSONObject rawData = new JSONObject();
	
	
	public JSONObject getRawJSONData(){
		return rawData;
	}
	
	public void setRawJSONData(JSONObject a){
		rawData = a;
	}
	
	public String getSaveFilePath(){
		return saveFilePath;
	}
	
	public void setSaveFilepath(String s){
		saveFilePath = s;
	}
	
	public RestRequestView(JSONObject config){		
		init(config);
	}
	
	public RestRequestView() {
		init(null);
	}

	public void addNews(Object[] data){
		model.addRow(data);
	}
	
	JButton getSaveButton(){
		return saveStepButton;
	}	
	
	
	public JSONObject getParams(){
		HashMap<String, String> hm = new HashMap<String, String>();
		for (int i =0; i < model.getRowCount(); i++){
			hm.put(model.getValueAt(i, 0).toString(), model.getValueAt(i,1).toString());
		}		
		JSONObject result = new JSONObject(hm);	
		try {			
			result.put("payload", rawData);
		} catch (JSONException e) {			
			e.printStackTrace();
		}
		return result;		
	}
	
	public void updateData(String path){
		File file = null;
		while(model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);
		if (!path.equals("")) {
			saveFilePath = path;			
			file = new File(path);
		    FileInputStream in;
		    String str = "";
			try {
				in = new FileInputStream(file);
				int size = in.available();
				byte[] buffer = new byte[size];
				in.read(buffer);
				in.close();
				str = new String(buffer);
			} catch (FileNotFoundException e) {				
				e.printStackTrace();
			} catch (IOException e) {				
				e.printStackTrace();
			}
			//System.out.println(str);
			try {
				jo = new JSONObject(str);
			} catch (JSONException e) {				
				e.printStackTrace();
			}
			//System.out.println(jo.toString());	
			Iterator<String> keys=jo.keys();
			String key;
			while (keys.hasNext()){
				key = keys.next();
				if (key.equals("payload")){
					try {
						rawData = jo.getJSONObject(key);
						System.out.println("Successfully get rawData which is " + rawData.toString());
					} catch (JSONException e) {						
						e.printStackTrace();
					}
					//raw data will not be displayed in the table!!
					continue;
				}
				Object o = null;
				try {
					o = jo.get(key);
				} catch (JSONException e) {					
					e.printStackTrace();
				}				
				if (o instanceof String || o instanceof Integer){
					String[] temp = {key, o.toString()};
					model.addRow(temp);
				}	
			}		
			jtable.setModel(model);				
			jlabel.setText(file.getName());
		}else{
		    model.addRow(new String[] {"Method",""});
		    //model.addRow(new String[] {"id",""});
		    model.addRow(new String[] {"Description",""});
		    model.addRow(new String[] {"username",""});
		    model.addRow(new String[] {"password",""});
		    model.addRow(new String[] {"url",""});
		    
		    jlabel.setText("");
		}
		
		jlabel.updateUI();
		jtable.updateUI();
	}
	
	public void init(JSONObject config){		
		model = new DefaultTableModel(null, headings);		
		jtable = new MyFileTable(model);		
		jtable.setPreferredScrollableViewportSize(new Dimension(450, 250));
		this.updateData(loadFilePath);			
		
	    //SETUP THE COMBOBOX FOR METHOD CELL
		Iterator<String> methodIterator = config.keys();
		List<String> methodList = new ArrayList<String>();
		while(methodIterator.hasNext())
			methodList.add(methodIterator.next());
		String[] valueSupport = new String[methodList.size()];
		String[] values = methodList.toArray(valueSupport);
		/*
	    String[] values = new String[] { "Item_Post", "Item_Delete", "Item_Get", "Item_Put", 
	    		"Dataport_Post", "Dataport_Put", "Dataport_Delete", "Dataport_Get","Location_Get", "Location_Put", "Location_Post", "Location_Delete", "Ip_Get", 
	    		"Ipassignments_Post", "Ipassignments_Delete", "Ipassignments_Put", "Ipassignments_Get", "SmartChart_Post", "MobileSearch_Post"
	    		, "MobilePlacement_Post"};
	    		*/
	    jtable.setComboCell(0, 1, new MyComboBoxEditor(values));
	   /* jtable.getCellEditor(0, 1).addCellEditorListener(new CellEditorListener() {

			@Override
			public void editingStopped(ChangeEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void editingCanceled(ChangeEvent e) {
				// TODO Auto-generated method stub
				
			}
	    	
	    });  */
	    
	    JPanel buttonPane = new JPanel();  
	    buttonPane.add(savetoButton);	    
	    buttonPane.add(saveStepButton); 
	    buttonPane.add(clearButton);
	    buttonPane.add(newParamButton);
	    buttonPane.add(newStepButton);
	    buttonPane.add(dataButton);
	    
	    
	    clearButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub				
				int[] selectedIndex = jtable.getSelectedRows();				
				for (int i = selectedIndex.length -1; i >= 0; i--){					
					String temp = (String) model.getValueAt(selectedIndex[i], 0);					
					if (temp.equals("Method") || temp.equals("Description")) //temp.equals("id")
						model.setValueAt("", selectedIndex[i], 1);					
					else
						model.removeRow(selectedIndex[i]);					
					jtable.updateUI();
				}
			}	    	
	    });
	    
	    newParamButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub	
				String[] temp = {"", ""};
				model.addRow(temp);
				jtable.updateUI();				
			}	    	
	    });
	    
	    Box box = new Box(BoxLayout.Y_AXIS);
	    box.add(new JLabel("Request"));
	    box.add(new JScrollPane(jtable));
	    box.add(jlabel);
	    box.add(buttonPane);  
		this.add(box);	
		this.setPreferredSize(new Dimension(400, 350));
	}
}
