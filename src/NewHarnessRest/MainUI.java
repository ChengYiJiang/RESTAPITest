package NewHarnessRest;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.jdbc2.TimestampUtils;


public class MainUI {	
	
	private JFrame mainFrame = new JFrame("Test Case Viewer");		
	PropertiesView PropEditView = new PropertiesView();
	ListFileView listFileView;
	JPopupMenu popupMenu = new JPopupMenu();
	String tURL = "";
	String tempTestCasePath = "";
	JTextField name;
	RestRequestView requestView = new RestRequestView();
	ParamLibsView libsView;
	private static String seperator = System.getProperty("line.separator");
	
	class doubleClick extends MouseAdapter{
		public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() ==2) {
            	String temp = ((JList)e.getSource()).getSelectedValue().toString();   
            	requestView.updateData(temp);  
            	try {
					PropEditView.updateData(temp);
				} catch (IOException e1) {					
					e1.printStackTrace();
				}
            }
        }
	};
	
	
	public MainUI(String tcP){
		this.tempTestCasePath = tcP;
	}
	
	
	public JSONObject getParamsAndProps(){
		JSONObject first = requestView.getParams();	
		try {
			JSONObject second = PropEditView.newGetProps();
			first.put("validation", second);
		} catch (JSONException e1) {			
			e1.printStackTrace();
		}		
		return first;
	}
	
	public void setupButtonListeners(){
		//save test step button
		requestView.saveStepButton.addActionListener(new ActionListener()  
        {  
            public void actionPerformed(ActionEvent event) 
            {              	
            	String s = getParamsAndProps().toString();
            	//System.out.println("it is "+s);
            	JFileChooser chooser = new JFileChooser(".");
            	String saveType[] = { "json" };
				chooser.setFileFilter(new FileNameExtensionFilter("Test Step",
						saveType));
				int result = chooser.showSaveDialog(null);
				
				if (result == JFileChooser.APPROVE_OPTION) {					
					if (chooser.getSelectedFile() != null) {
						String saveFilePath = chooser.getSelectedFile().getPath();
						try {
							if (!saveFilePath.endsWith(".json"))
								saveFilePath += ".json";
							FileWriter fw = new FileWriter(saveFilePath);							
							BufferedWriter out = new BufferedWriter(fw);
						    out.write(s, 0, s.length());
						    out.close();
						    requestView.setSaveFilepath(saveFilePath);
							requestView.updateData(saveFilePath);
						}catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				
            }  
        });
		
		//2
		requestView.savetoButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub	
				String s = getParamsAndProps().toString();
				FileWriter fw = null;
				try {
					fw = new FileWriter(requestView.getSaveFilePath());
					BufferedWriter out = new BufferedWriter(fw);
					out.write(s, 0, s.length());
				    out.close();
				} catch (IOException e) {					
					//e.printStackTrace();
					JOptionPane jop = new JOptionPane();
					jop.showMessageDialog(mainFrame, "File does not exist, please use Save As");
					
				}				
			}	    	
	    });
		
		//3
		requestView.newStepButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub	
				requestView.updateData("");
				requestView.setSaveFilepath("");
				try {
					PropEditView.updateData("");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	    	
	    });
		
		//4
		listFileView.addFromRightButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub	
				if (!requestView.getSaveFilePath().equals("")){
					listFileView.addFromRequestView(requestView.getSaveFilePath());
					listFileView.updateUI();
				}else{
					new JOptionPane().showMessageDialog(mainFrame, "Please save the temp test step first...");
				}
			}	    	
	    });
		
		//5
		libsView.addToRequestButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub	
				try
				{
					int[] index = libsView.getSelectedTable().getSelectedRows();
					for (int i=0; i<index.length; i++){
						String[] temp = new String[2];
						temp[0] = (String) libsView.getSelectedTable().getValueAt(index[i], 0);
						temp[1] = (String) libsView.getSelectedTable().getValueAt(index[i], 1);
						//System.out.println(temp[0]+":"+temp[1]);
						requestView.addNews(temp);
					}
					requestView.updateUI();
				}
				catch(NullPointerException e){
					new JOptionPane().showMessageDialog(mainFrame, "Please load a lib file..."); 
				}
				//System.out.println(index.length);
				
			}	    	
	    });
		
		//6
		libsView.refreshButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0){
				// TODO Auto-generated method stub	
				try {
					libsView.loadLibFromFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}	    	
	    });
		
		//7 Set button
		listFileView.addURLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {	
				String s = listFileView.getURLText();
				if (s.startsWith("https://")){
					listFileView.setTargetURL(s);
					try {
						libsView.reconnDB(s.split("://")[1]);
					} catch (InstantiationException | IllegalAccessException e) {
						new JOptionPane().showMessageDialog(mainFrame, "URL set up successfully but there is some exception for DB connecting");
					} catch (ClassNotFoundException | IOException e) {						
						e.printStackTrace();					
					}
				}
				else{
					listFileView.setTargetURL(s);
					try {
						libsView.reconnDB(s);
					} catch (InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						new JOptionPane().showMessageDialog(mainFrame, "URL set up successfully but there is some exception for DB connecting");
					} catch (ClassNotFoundException | IOException e) {
						// TODO Auto-generated catch block						
						e.printStackTrace();
					}
				}
				
				new JOptionPane().showMessageDialog(mainFrame, "URL set up successfully..."); 
				
			}
		});	
		
		//8
		requestView.dataButton.addActionListener(new ActionListener()  
        {  
            public void actionPerformed(ActionEvent event) 
            {              	
            	JFrame fr = new JFrame("JSON DATA VIEWER");				
				fr.setLayout (new BorderLayout ());
				fr.setSize (800, 600);
				fr.setResizable (false);
				fr.setLocationRelativeTo (null);
				fr.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);	
				
				JSONViewer jsonViewer = new JSONViewer(requestView);				
				fr.add(jsonViewer);
				fr.setVisible (true);			
            }  
        });
		
		
		//9
		libsView.addToDataButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				try
				{
					String temp = "";
					int[] index = libsView.getSelectedTable().getSelectedRows();
					for (int i=0; i<index.length; i++){
						temp += "\"" + libsView.getSelectedTable().getValueAt(index[i], 0) + "\":\""
								+ libsView.getSelectedTable().getValueAt(index[i], 1) + "\"";	
						if (i != index.length -1)
							temp += ",";
						temp += seperator;
					}
					StringSelection stsel = new StringSelection(temp);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stsel, stsel);
					new JOptionPane().showMessageDialog(mainFrame, "Params are in clipboard now..."); 
				}
				catch(NullPointerException e1){
					new JOptionPane().showMessageDialog(mainFrame, "Please load a lib file..."); 
				}
				
			}
			
		});
		
	}
	
	
	
	public void init() throws IOException{	
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (tempTestCasePath.equals(""))
			listFileView = new ListFileView();
		else
			listFileView = new ListFileView(tempTestCasePath);
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, listFileView, new JScrollPane(PropEditView));
		
		Box rightBox = new Box(BoxLayout.Y_AXIS);		
		rightBox.add(new JScrollPane(requestView));
		
		try {
			libsView = new ParamLibsView();
			rightBox.add(libsView);
		} catch (InstantiationException e1) {			
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {			
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {			
			e1.printStackTrace();
		}
		
		listFileView.getJList().addMouseListener(new doubleClick());
		JSplitPane leftAndRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, rightBox);
		mainFrame.add(leftAndRight);
		mainFrame.show();
		leftAndRight.setDividerLocation((int)mainFrame.getSize().getWidth()*45/100);
		left.setDividerLocation((int)mainFrame.getSize().getHeight()*50/100);
		getParamsAndProps();
		setupButtonListeners();
		try {
			PropEditView.updateData("");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void _quickSort(List<String> list, int low, int high) {
		if (low < high) {
			int middle = getMiddle(list, low, high);  
			_quickSort(list, low, middle - 1);        
			_quickSort(list, middle + 1, high);       
		}
	}

	private static int getMiddle(List<String> list, int low, int high) {  //treat as String
		String tmp = list.get(low);    
		while (low < high) {
			while (low < high && (list.get(high).compareToIgnoreCase(tmp) >= 0 && list.get(high).length() == tmp.length() || list.get(high).length() > tmp.length())) {
				high--;
			}
			list.set(low, list.get(high));   
			while (low < high && (list.get(low).compareToIgnoreCase(tmp) < 0 && list.get(low).length() == tmp.length() || list.get(low).length() < tmp.length())) {
				low++;
			}
			list.set(high, list.get(low));   
		}
		list.set(low, tmp);              
		return low;                   
	}
	
	private static void decodeStringList(List<String> rawList) throws UnsupportedEncodingException{
		
		for (int i=0; i<rawList.size(); i++)
			rawList.set(i, java.net.URLDecoder.decode(rawList.get(i), "UTF-8"));
		
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, JSONException {
		// TODO Auto-generated method stub		
		try {
			new MainUI("").init();
		} catch (IOException e) {			
			e.printStackTrace();
		}			
		
		Map<String, Object> abc = new HashMap<String, Object>();
		abc.put("a", "bbb");
		abc.put("a", "ccc");
		abc.put("a", null);
		System.out.println(abc.get("a"));
		
	}	
	
}
