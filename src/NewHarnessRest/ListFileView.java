package NewHarnessRest;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ListFileView extends JPanel {
	private JFileChooser chooser = new JFileChooser(".");
	private JList<String> jl;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private File[] fileList;
	private String url = "";
	public JButton addFromRightButton = new JButton("Add Step from Right");
	public JButton addURLButton = new JButton("Set");
	private JTextField name = new JTextField(30);
	private JTextField userName = new JTextField(30);
	private JTextField passWord = new JTextField(30);
	public JCheckBox requestRaw = new JCheckBox("Show Raw Request"); 
	public JCheckBox responseRaw = new JCheckBox("Show Raw Response");
	
	
	public ListFileView() {
		init();
	}

	public ListFileView(String fP) throws IOException {
		if (!fP.equals("")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(fP)));
			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				listModel.addElement(line);
			}
		}
		init();
	}

	public String getURLText() {
		String s = name.getText();		
		return name.getText();
	}

	public void setTargetURL(String u) {
		url = u;
		name.setText(url);
		name.updateUI();
	}

	public JList getJList() {
		return jl;
	}

	public void addFromRequestView(String s) {
		if (s != "" && s != null)
			listModel.addElement(s);
	}

	public void init() {
		Box box = new Box(BoxLayout.Y_AXIS);
		JPanel setPanel = new JPanel();

		setPanel.add(name);

		setPanel.add(addURLButton);
		name.setText("Please input the target address");
		box.add(setPanel);
		jl = new JList<String>(listModel);
		jl.setVisibleRowCount(7);
		jl.setSize(new Dimension(416, 180));
		jl.setDragEnabled(true);
		jl.setTransferHandler(new ListTransferHandler());
		jl.setDropMode(DropMode.INSERT);

		chooser.setMultiSelectionEnabled(true);

		JPanel buttonPanel = new JPanel();
		JButton openListButton = new JButton("Add Step");
		openListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				String saveType[] = { "json" };
				chooser.setFileFilter(new FileNameExtensionFilter("Test Step",
						saveType));
				int result = chooser.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile() != null) {
						fileList = chooser.getSelectedFiles();
					}
					for (int i = 0; i < fileList.length; i++) {
						listModel.addElement(fileList[i].getPath());
					}
					jl.updateUI();
				}
			}
		});

		JButton removeButton = new JButton("Remove Step");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i = jl.getSelectedIndices().length - 1; i >= 0; i--) {
					listModel.removeElementAt(jl.getSelectedIndices()[i]);
				}
				jl.updateUI();
			}
		});

		JButton runRestButton = new JButton("Run");
		runRestButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
				new SSLVerificationDisabler().disableSslVerification(); // disabling HTTPS checks
				ReportViewInMainUI reportView = new ReportViewInMainUI();
				reportView.ta.append("Please wait for a while when processing the requestes......");
				ExecutorService pool = Executors.newCachedThreadPool();
				pool.submit(new RestRun(url, reportView.ta, jl.getSelectedValuesList(), requestRaw.isSelected(), responseRaw.isSelected()));			
				
			}
		});

		JButton saveTestCaseButton = new JButton("Save Case");
		saveTestCaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {

				JFileChooser chooser1 = new JFileChooser(".");
				chooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
				String saveType[] = { "tc" };
				chooser1.setFileFilter(new FileNameExtensionFilter("tc",
						saveType));
				int result = chooser1.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					if (chooser1.getSelectedFile() != null) {
						String saveFilePath = chooser1.getSelectedFile()
								.getPath();
						try {
							if (!saveFilePath.endsWith(".tc"))
								saveFilePath += ".tc";
							FileWriter fw = new FileWriter(saveFilePath);
							if (!listModel.isEmpty()) {
								for (int i = 0; i < listModel.size(); i++)
									fw.write(listModel.get(i)
											+ System.getProperty("line.separator"));
							}
							fw.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});

		JButton openTestCaseButton = new JButton("Open Case");
		openTestCaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser2 = new JFileChooser(".");
				chooser2.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser2.setMultiSelectionEnabled(false);
				String saveType[] = { "tc" };
				chooser2.setFileFilter(new FileNameExtensionFilter("tc",
						saveType));
				int result = chooser2.showOpenDialog(null);
				BufferedReader br = null;
				if (result == JFileChooser.APPROVE_OPTION) {
					if (chooser2.getSelectedFile() != null) {
						listModel.clear();
						try {
							br = new BufferedReader(new InputStreamReader(
									new FileInputStream(chooser2
											.getSelectedFile())));
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						try {
							for (String line = br.readLine(); line != null; line = br
									.readLine()) {
								listModel.addElement(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
						jl.updateUI();
					}
				}
			}
		});

		buttonPanel.add(openListButton);
		buttonPanel.add(removeButton);		
		buttonPanel.add(saveTestCaseButton);
		buttonPanel.add(openTestCaseButton);
		buttonPanel.add(addFromRightButton);
		
		//adding checkBox
		JPanel checkBoxPane = new JPanel();
		checkBoxPane.add(runRestButton);		
		
		checkBoxPane.add(requestRaw);
		checkBoxPane.add(responseRaw);
		
		box.add(new JLabel("Test Step:"));
		box.add(new JScrollPane(jl));
		box.add(buttonPanel);
		box.add(checkBoxPane);
		box.setPreferredSize(new Dimension(416, 350));
		this.add(box);
		this.setPreferredSize(new Dimension(416, 350));
	}
}
