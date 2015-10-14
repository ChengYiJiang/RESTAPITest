package NewHarnessRest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ReportViewInMainUI {
	
	private String saveType[] = {"txt"};
	private String reportText = "";
	String time;
	String tU;
	public JTextArea ta;
	private JFileChooser chooser = new JFileChooser(".");
	
	public ReportViewInMainUI(){
		init();
	}
	
	
	public void init(){
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		final JFrame jf = new JFrame("Report");
		jf.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
		jf.setSize(800,600);
		ta = new JTextArea(10,10);
		ta.setText(reportText);
		JScrollPane sp = new JScrollPane(ta);
		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(sp);
		JPanel buttonPanel = new JPanel();
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener()  
        {  
            @SuppressWarnings("unused")
			public void actionPerformed(ActionEvent event)  
            {  
            	chooser.setSelectedFile(new File(time));
            	chooser.setFileFilter(new FileNameExtensionFilter("TXT", saveType));
            	chooser.showSaveDialog(null);
            	
				if (chooser.getSelectedFile() != null) {
					String saveFilePath = chooser.getSelectedFile().getPath();
					System.out.println("path is "+saveFilePath);	
					try {
						if (!saveFilePath.endsWith(".txt"))
							saveFilePath += ".txt";
						FileWriter fw = new FileWriter(saveFilePath);
						if (fw != null){
								fw.append(reportText);
								fw.flush();
								fw.close();
						}else{							
							fw.write(reportText);
						}												
					} catch (IOException e1) {						
						JOptionPane.showMessageDialog(jf, "Save report failed due to IOException...");
						//e1.printStackTrace();
					}
				}
            }  
        }); 
		
		buttonPanel.add(saveButton);
		box.add(buttonPanel);
				
		jf.add(box);
		jf.show();
	}

}
