package NewHarnessRest;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.json.JSONException;
import org.json.JSONObject;


public class MainUI {	
	
	private JFrame mainFrame = new JFrame("Test Case Viewer");		
	private PropertiesView PropEditView = new PropertiesView();
	private ListFileView listFileView;
	private JPopupMenu popupMenu = new JPopupMenu();
	private String tURL = "";
	private String tempTestCasePath = "";
	private JTextField name;
	private RestRequestView requestView;
	private ParamLibsView libsView;
	private JSONObject configJSON;
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
		requestView = new RestRequestView();
	}
	
	public MainUI(String tcP, JSONObject config) throws IOException, JSONException{
		this.tempTestCasePath = tcP;
		
		//MAKE SURE YOUR JAR FILE AND CONFIG FILE IS IN SAME FOLDER
		FileReader fr = new FileReader(System.getProperty("user.dir") + "\\config.json");
		
		String line = null;
        StringBuffer strBuffer = new StringBuffer();		
		BufferedReader br = new BufferedReader(fr);
		while ((line = br.readLine()) != null)
        {
            strBuffer.append(line + System.getProperty("line.separator"));
        }         
        br.close();
        this.configJSON = new JSONObject(strBuffer.toString());
		requestView = new RestRequestView(configJSON);
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
				chooser.setFileFilter(new FileNameExtensionFilter("Test Step", saveType));
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
		
		/*
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
					listFileView.setTargetURL("https://" + s);
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
		*/
		
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
		
		/*
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
		*/		
	}
	
	public void init() throws Throwable{	
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		if (tempTestCasePath.equals(""))
			listFileView = new ListFileView();
		else
			listFileView = new ListFileView(tempTestCasePath);
		listFileView.setConfig(configJSON);
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, listFileView, new JScrollPane(PropEditView));
		
		Box rightBox = new Box(BoxLayout.Y_AXIS);		
		rightBox.add(new JScrollPane(requestView));
		
		//libsView = new ParamLibsView();
		//rightBox.add(libsView);
		
		listFileView.getJList().addMouseListener(new doubleClick());
		JSplitPane leftAndRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, left, rightBox);
		mainFrame.add(leftAndRight);
		mainFrame.show();
		leftAndRight.setDividerLocation((int)mainFrame.getSize().getWidth()*45/100);
		left.setDividerLocation((int)mainFrame.getSize().getHeight()*50/100);
		getParamsAndProps();
		setupButtonListeners();
		
		PropEditView.updateData("");
		
	}
	
	private void wrappStepsToCase(){
		
	}
	
	
	public static void main(String[] args) throws Throwable {		
		try {
			new MainUI("", null).init();
		} catch (IOException e) {			
			e.printStackTrace();
		}	
	}	
	
}
