package NewHarnessRest;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import org.json.JSONException;
import org.json.JSONObject;

public class ParamLibsView extends JPanel {
	JTabbedPane tabPane;
	private LinkedHashMap<String, DefaultTableModel> modelMap;
	private LinkedHashMap<String, MyLibsTable> tableMap = new LinkedHashMap<String, MyLibsTable>();
	private String[] headings = { "Param", "Value" };
	ArrayList<String> makeList = new ArrayList<String>();
	Object[] makeArray;
	public JButton addToRequestButton = new JButton("Add to request");
	public JButton refreshButton = new JButton("Refresh");
	private JButton loadLibButton = new JButton("Load Lib");
	public JButton addToDataButton = new JButton("Copy");
	String url = "";
	String libFilePath = "";
	String tabName = "";
	JFileChooser chooser;
	boolean isLibLoaded = false;
	boolean isURLSet = false;

	public MyLibsTable getSelectedTable() {

		return tableMap.get(tabPane.getTitleAt(tabPane.getSelectedIndex()));

	}
	
	public void showDefaultTable(){
		tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		MyLibsTable initTable = new MyLibsTable(null, "");
		initTable.setPreferredScrollableViewportSize(new Dimension(450, 200));
		tabPane.addTab("No Libs Loaded", new JScrollPane(initTable));
		this.removeAll();
		this.add(tabPane);
		tabPane.updateUI();
	}
	
	public void addButtons(){
		JPanel buttonPane = new JPanel();
		buttonPane.add(addToRequestButton);
		buttonPane.add(refreshButton);
		buttonPane.add(loadLibButton);
		buttonPane.add(addToDataButton);
		
		loadLibButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(false);

				String saveType[] = { "txt" };
				chooser.setFileFilter(new FileNameExtensionFilter("txt",
						saveType));
				int result = chooser.showOpenDialog(null);
				BufferedReader br = null;
				
				if (result == JFileChooser.APPROVE_OPTION) {					
					if (chooser.getSelectedFile() != null) {						
						File file = chooser.getSelectedFile();
						try {
							libFilePath = file.getPath();							
							reconnDB(url);
						} catch (IOException e) {							
							e.printStackTrace();
						} catch (InstantiationException e) {							
							e.printStackTrace();
						} catch (IllegalAccessException e) {							
							e.printStackTrace();
						} catch (ClassNotFoundException e) {							
							e.printStackTrace();
						}
					}
				}else{
					chooser.cancelSelection();
				}
			}
		});
		
		
		
		this.add(buttonPane);
	}

	public void reconnDB(String s) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, IOException {		
		url = s;
		isURLSet = true;
		loadLibFromFile();
		if (tableMap.containsKey("itemsLibsTable")) {
			tableMap.get("itemsLibsTable").updateMake(url);
		}
		tabPane.updateUI();

	}

	public ParamLibsView() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		init();
	}

	public void init() throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		modelMap = new LinkedHashMap<String, DefaultTableModel>();
		chooser = new JFileChooser(".");
		showDefaultTable();
		addButtons();
		this.setPreferredSize(new Dimension(400, 300));
		tabPane.updateUI();
	}

	private void clearModel(DefaultTableModel m) {
		for (int i = m.getRowCount() - 1; i >= 0; i--) {
			m.removeRow(i);
		}
	}

	public void loadLibFromFile() throws IOException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub		
		BufferedReader br = null;

		if (!libFilePath.equals("")) {			
			tabPane.removeAll();
			tableMap.clear();
			modelMap.clear();			
			Iterator<String> it = modelMap.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				clearModel(modelMap.get(key));
			}			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					libFilePath)));
			String tabName = "";
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (line.equals(""))
					continue;
				if (line.endsWith(":")) {
					tabName = line.substring(0, line.length() - 1);					
					modelMap.put(tabName + "Model", new DefaultTableModel(null,	headings));					
					if (tabName.equals("items")) {						
						tableMap.put(tabName + "LibsTable", new MyLibsTable(modelMap.get(tabName + "Model"), url));
						modelMap.get(tabName + "Model").addTableModelListener(
								new TableModelListener() {
									@Override
									public void tableChanged(TableModelEvent e) {
										// TODO Auto-generated method stub
										if (e.getType() == TableModelEvent.UPDATE) {
											String value = tableMap.get("itemsLibsTable").getValueAt(e.getLastRow(),e.getColumn())
													.toString();
											if (!value.equals("")) {
												tableMap.get("itemsLibsTable").updateModelComboBox(value);
											}
										}
									}
								});
					} else
						tableMap.put(tabName + "LibsTable", new MyLibsTable(modelMap.get(tabName + "Model"), null));
					continue;
				}
				String[] temp = { line, "" };
				modelMap.get(tabName + "Model").addRow(temp);
			}
			
			Iterator<String> iterator = tableMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				tableMap.get(key).setPreferredScrollableViewportSize(
						new Dimension(450, 200));
				tabPane.addTab(key, new JScrollPane(tableMap.get(key)));
			}			
			tabPane.updateUI();
		} else {
			//System.out.println("empty");			
		}
	}
}
