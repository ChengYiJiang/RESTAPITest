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
import java.net.URISyntaxException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ListTestCaseView extends JPanel {
	private JFileChooser chooser = new JFileChooser(".");
	private JList<String> jl;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();	
	private String url = "";	

	public ListTestCaseView() {
		init();
	}

	public ListTestCaseView(String fP) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fP)));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			listModel.addElement(line);
		}
		init();
	}

	public void setTargetURL(String u) {
		url = u;
	}

	public JList getJList() {
		return jl;
	}

	public void init() {
		Box box = new Box(BoxLayout.Y_AXIS);
		jl = new JList<String>(listModel);
		jl.setVisibleRowCount(10);
		jl.setSize(new Dimension(416, 180));
		jl.setDragEnabled(true);
		jl.setTransferHandler(new ListTransferHandler());
		jl.setDropMode(DropMode.INSERT);
		

		chooser.setMultiSelectionEnabled(true);

		JPanel buttonPanel = new JPanel();		

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				for (int i = jl.getSelectedIndices().length - 1; i >= 0; i--) {
					listModel.removeElementAt(jl.getSelectedIndices()[i]);
				}
				jl.updateUI();
			}
		});

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					new MainUI(jl.getSelectedValue()).init();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		});


		JButton openTestCaseButton = new JButton("Add");
		openTestCaseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JFileChooser chooser2 = new JFileChooser(".");
				chooser2.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser2.setMultiSelectionEnabled(true);

				String saveType[] = { "tc" };
				chooser2.setFileFilter(new FileNameExtensionFilter("tc",
						saveType));
				int result = chooser2.showOpenDialog(null);
				BufferedReader br = null;
				if (result == JFileChooser.APPROVE_OPTION) {
					//System.out.println("result is "+result);
					if (chooser2.getSelectedFiles() != null) {	
						File[] fList = chooser2.getSelectedFiles();
						for (int i = 0; i < fList.length; i++) {
							listModel.addElement(fList[i].getPath());
						}						
						jl.updateUI();
					}
				}
			}
		});

		JButton createTCButton = new JButton("Create");
		createTCButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {								
				try {
					new MainUI("").init();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
		});
		
		buttonPanel.add(removeButton);
		buttonPanel.add(editButton);		
		buttonPanel.add(openTestCaseButton);
		buttonPanel.add(createTCButton);
		box.add(new JScrollPane(jl));
		box.add(buttonPanel);
		box.setPreferredSize(new Dimension(416, 250));
		this.add(box);
	}

}
