package NewHarnessRest;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.text.TextAction;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import sun.org.mozilla.javascript.internal.Decompiler;
import sun.org.mozilla.javascript.internal.IdScriptableObject;
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeFunction;
import sun.org.mozilla.javascript.internal.NativeObject;
import sun.org.mozilla.javascript.internal.UintMap;
import sun.org.mozilla.javascript.internal.Undefined;

public class JSONViewer extends JPanel implements ActionListener,
		ChangeListener, TreeSelectionListener {
	private static final long serialVersionUID = 1L;
	private static final String TITLE = "JSON Data Viewer";
	private static final String FORMAT = "Format";
	private static final String PASTE = "Paste";
	private static final String COPY = "Copy";
	private static final String CUT = "Cut";
	private static final String CLEAR = "Clear";
	private static final String DELSPACE = "Remove Space";
	private static final String DELSPACEANDCONVERT = "Remove Space + Escape character";
	private static final String ABOUT = "About";
	private static final String SAVE = "Save";
	private static final String LINE = System.getProperty("line.separator");
	private Hashtable<Object, Action> commands = new Hashtable<Object, Action>();
	private JTextArea textArea = new JTextArea();
	private DefaultMutableTreeNode rootNode;
	private DefaultTreeModel treeModel;
	private JTree tree;
	private JTable table;
	private String[] columnNames = { "Key", "Value" };
	private Object[][] data = new Object[0][];
	private Font font = new Font("Consolas", Font.BOLD, 15);
	private JLabel status;
	private String saveFilePath;
	public RestRequestView fromMain;
	

	
	public JSONViewer(RestRequestView jsonData) {
		this.fromMain = jsonData;
		setLayout(new BorderLayout());
		setSize(800, 600);		
		this.add (makeTab(), BorderLayout.CENTER);
		this.add (makeStatus(), BorderLayout.SOUTH);		
		textArea.setText(jsonData.getRawJSONData().toString());		
	}
	

	private IdScriptableObject parse(String jsonstr) {
		
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("js");
		try {
			status.setForeground(Color.GREEN);
			status.setText("Success Parse JSON");
			
			return (IdScriptableObject) se.eval(jsonstr);
		} catch (Exception e) {
			status.setForeground(Color.RED);
			status.setText(e.toString().replaceAll(".*\\:(.*)", "$1"));
			return null;
		}
	}

	JPanel makeTab() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);
		tabbedPane.addTab("JSON Data", null, makeDataPanel(), "JSON Data");
		tabbedPane.addTab("View", null, makeViewPanel(), "View");
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		panel.add(tabbedPane);
		return panel;
	}

	private void append(Object obj, DefaultMutableTreeNode parent, String tip) {
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(tip);
		if (obj instanceof NativeObject) {
			node = new DefaultMutableTreeNode("{ }" + tip);
			Object[] ids = ((IdScriptableObject) (obj)).getIds();
			int len = ids.length;
			for (int i = 0; i < len; i++) {
				String param = ids[i].toString();
				Object object = ((IdScriptableObject) (obj)).get(param, null);
				if (object instanceof Undefined) {
					object = "undefined";
				} else if (null == object) {
					object = "null";
				} else if (object instanceof NativeFunction) {
					object = ((NativeFunction) (object)).getEncodedSource();
					object = Decompiler.decompile(object.toString(), -1,
							new UintMap());
					object = object.toString().replaceAll("^\\((.*)\\)$", "$1")
							.replaceAll("\"", "\'");
				}
				if (param.matches("^[\\d+\\.]+$")) {
					object = ((IdScriptableObject) (obj)).get(
							Integer.parseInt(param), null);
				}
				String value = object.toString().replaceAll("^(.+)\\.0$", "$1");
				String val = "";
				if (!(object instanceof NativeArray)
						&& !(object instanceof NativeObject)) {
					val = param + ": \"" + value + "\"";
				} else {
					val = param;
				}
				append(object, node, val);
			}
		} else if (obj instanceof NativeArray) {
			node = new DefaultMutableTreeNode("[ ]" + tip);
			Object[] ids = ((IdScriptableObject) (obj)).getIds();
			int len = ids.length;
			for (int i = 0; i < len; i++) {
				IdScriptableObject io = (IdScriptableObject) (obj);
				Object object = io.get(i, null);
				if (object instanceof Undefined) {
					object = "undefined";
				} else if (null == object) {
					object = "null";
				} else if (object instanceof NativeFunction) {
					object = ((NativeFunction) (object)).getEncodedSource();
					object = Decompiler.decompile(object.toString(), -1,
							new UintMap());
					object = object.toString().replaceAll("^\\((.*)\\)$", "$1")
							.replaceAll("\"", "\'");
				}
				String value = object.toString().replaceAll("^(.+)\\.0$", "$1");
				String val = "";
				if (!(object instanceof NativeArray)
						&& !(object instanceof NativeObject)) {
					val = i + ": \"" + value + "\"";
				} else {
					val = i + "";
				}
				append(object, node, val);
			}
		}
		parent.add(node);
	}

	private JComponent makeViewPanel() {
		rootNode = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setFont(font);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		Icon icon = null;
		renderer.setLeafIcon(icon);
		renderer.setClosedIcon(icon);
		renderer.setOpenIcon(icon);
		tree.setCellRenderer(renderer);
		JScrollPane scrollPane = new JScrollPane(tree);

		TableModel dataModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return columnNames.length;
			}

			public int getRowCount() {
				return data.length;
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}

			public String getColumnName(int column) {
				return columnNames[column];
			}
		};
		table = new JTable(dataModel);
		table.setFont(font);
		table.setRowHeight(40);
		JTableHeader header = table.getTableHeader();
		header.setPreferredSize(new Dimension(header.getWidth(), 30));
		header.setReorderingAllowed(false);
		header.setResizingAllowed(false);
		JScrollPane pane = new JScrollPane(table);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				scrollPane, pane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(500);
		return splitPane;
	}

	private JComponent makeDataPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		JToolBar bar = new JToolBar(JToolBar.HORIZONTAL);
		bar.setFloatable(false);

		initActions();
		JButton paste = new JButton(PASTE);
		paste.addActionListener((Action) commands.get("paste-from-clipboard"));
		bar.add(paste);
		JButton copy = new JButton(COPY);
		copy.addActionListener((Action) commands.get("copy-to-clipboard"));
		bar.add(copy);
		JButton cut = new JButton(CUT);
		cut.addActionListener((Action) commands.get("cut-to-clipboard"));
		bar.add(cut);
		JButton clear = new JButton(CLEAR);
		clear.addActionListener(this);
		bar.add(clear);
		JButton save = new JButton(SAVE);
		
		
		
		//TODO HERE IS BUG!!!!!!!!!!!!!!!!
		save.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if (StringUtils.isEmpty(textArea.getText()))
					fromMain.setRawJSONData(null);
				else
					try {
						fromMain.setRawJSONData(new JSONObject(textArea.getText()));
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}						
			}			
		});
		
		
		bar.add(save);
		bar.add(Box.createHorizontalStrut(10));
		JButton format = new JButton(FORMAT);
		format.addActionListener(this);
		bar.add(format);
		bar.add(Box.createHorizontalStrut(5));
		JButton delSpace = new JButton(DELSPACE);
		delSpace.addActionListener(this);
		bar.add(delSpace);
		bar.add(Box.createHorizontalStrut(5));
		JButton delSpaceAndConvert = new JButton(DELSPACEANDCONVERT);
		delSpaceAndConvert.addActionListener(this);
		bar.add(delSpaceAndConvert);
		bar.add(Box.createHorizontalStrut(356));
		// JButton about = new JButton (ABOUT);
		// about.addActionListener (this);
		// bar.add (about);
		
		panel.add(bar, BorderLayout.NORTH);
		textArea.setToolTipText("Paste JSON string here");
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setFont(font);
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	private void initActions() {
		Action[] actions = getActions();
		for (int i = 0; i < actions.length; i++) {
			Action a = actions[i];
			commands.put(a.getValue(Action.NAME), a);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String txt = e.getActionCommand();
		if (FORMAT.equals(txt)) {
			String text = reverseFormatedJSON();
			Object obj = parse("(" + text + ")");
			if (null == obj) {
				return;
			}
			textArea.setText("");
			formatJSON(obj, 0, "", "", false, true, false, false);
		} else if (DELSPACE.equals(txt)) {
			String text = reverseFormatedJSON();
			Object obj = parse("(" + text + ")");
			if (null == obj) {
				return;
			}
			textArea.setText("");
			formatJSON(obj, 0, "", "", false, true, true, false);
		} else if (DELSPACEANDCONVERT.equals(txt)) {
			String text = reverseFormatedJSON();
			Object obj = parse("(" + text + ")");
			if (null == obj) {
				return;
			}
			textArea.setText("");
			formatJSON(obj, 0, "", "", false, true, true, true);
		} else if (ABOUT.equals(txt)) {
			JDialog dialog = new JDialog();
			dialog.setTitle(TITLE);
			dialog.setSize(640, 370);
			dialog.setLayout(new GridLayout(1, 1));
			dialog.setModal(true);
			dialog.setLocationRelativeTo(this);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			JTabbedPane pane = new JTabbedPane();
			pane.addTab("About JSON	", initAbout("AboutJSON.html"));
			dialog.add(pane);
			pane.addTab("Example", initAbout("AboutExample.html"));
			dialog.add(pane);
			pane.addTab("About Offline JSON Viewer",
					initAbout("AboutOffline.html"));
			dialog.add(pane);
			dialog.setVisible(true);
		} else if (CLEAR.equals(txt)) {
			textArea.setText("");
		}
	}

	private JComponent initAbout(String url) {
		JEditorPane editorPane = new JEditorPane();
		editorPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						String command = "explorer.exe "
								+ e.getURL().toString();
						Runtime.getRuntime().exec(command);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		editorPane.setEditable(false);
		java.net.URL aboutURL = JSONViewer.class.getResource(url);
		if (null != aboutURL) {
			try {
				editorPane.setPage(aboutURL);
			} catch (IOException e) {
			}
		}
		JScrollPane editorScrollPane = new JScrollPane(editorPane);
		return editorScrollPane;
	}

	private String reverseFormatedJSON() {
		return textArea.getText().trim()
				.replaceAll("\\\\(\"[^\\,]*)\\\\(\")", "$1$2")
				.replaceAll("\\\\(\"[^\"]+)\\\\(\")", "$1$2");
	}

	private Action[] getActions() {
		return TextAction.augmentList(textArea.getActions(), new Action[] {});
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		String jsonstr = reverseFormatedJSON();
		
		if ("".equals(jsonstr) || jsonstr == null) {
			return;
		}
		if (rootNode != null)
			rootNode.removeAllChildren();
		Object object = parse("(" + jsonstr + ")");
		//Object object = jsonstr;
		if (null != object) {
			append(object, rootNode, "JSON");
		} else {
			JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
			if (tabbedPane.getSelectedIndex() == 1) {
				data = new Object[0][];
				SwingUtilities.updateComponentTreeUI(table);
				JOptionPane.showMessageDialog(null, "JSON format error!.",
						"Oops", JOptionPane.ERROR_MESSAGE);
			}
		}
		treeModel.reload();
		expandTree(tree, new TreePath(rootNode));
	}

	private void expandTree(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
			TreeNode n = (TreeNode) e.nextElement();
			TreePath path = parent.pathByAddingChild(n);
			expandTree(tree, path);
		}
		tree.expandPath(parent);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		try {
			DefaultMutableTreeNode treePath = (DefaultMutableTreeNode) e
					.getNewLeadSelectionPath().getLastPathComponent();
			TreeNode treeNode = treePath.getParent();
			int count = treeNode.getChildCount();
			data = new Object[count][];
			for (int i = 0; i < count; i++) {
				String kv = treeNode.getChildAt(i).toString();
				int index = kv.indexOf(":");
				if (index != -1) {
					data[i] = new Object[] {
							kv.substring(0, index),
							kv.substring(index + 1).replaceAll("^\\s*\"|\"$",
									"") };
				} else {
					String reg = "([\\{\\[])[^\\{\\}\\[\\]]*([\\}\\]])(.+)";
					data[i] = new Object[] { kv.replaceAll(reg, "$3"),
							kv.replaceAll(reg, "$1...$2") };
				}
			}
			SwingUtilities.updateComponentTreeUI(table);
		} catch (Exception ignore) {
			return;
		}
	}

	JPanel makeStatus() {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(800, 20));
		status = new JLabel();
		status.setSize(790, 19);
		panel.add(status);
		return panel;
	}

	private void formatJSON(Object obj, int level, String k, Object v,
			boolean isObject, boolean isLast, boolean isDeletedSpace,
			boolean isDeleteAndCont) {
		if (!isDeletedSpace) {
			for (int i = 0; i < level; i++) {
				textArea.append("\t");
			}
		}
		if (obj instanceof NativeObject) {
			if (!isDeletedSpace) {
				String temp = "";
				for (int i = 0; i < level; i++) {
					temp += "\t";
				}
				String x = isObject ? "\"" + k + "\":" + LINE + temp + "{"
						: "{";
				textArea.append(x + LINE);
			} else {
				String x = "";
				if (!isDeleteAndCont) {
					x = isObject ? "\"" + k + "\":{" : "{";
				} else {
					x = isObject ? "\\\"" + k + "\\\":{" : "{";
				}
				textArea.append(x);
			}
			Object[] ids = ((IdScriptableObject) (obj)).getIds();
			int len = ids.length;
			int lev = level + 1;
			for (int i = 0; i < len; i++) {
				String key = ids[i].toString();
				Object object = ((IdScriptableObject) (obj)).get(key, null);
				if (object instanceof Undefined) {
					object = "undefined";
				} else if (null == object) {
					object = null;
				} else if (object instanceof NativeFunction) {
					object = ((NativeFunction) (object)).getEncodedSource();
					object = Decompiler.decompile(object.toString(), -1,
							new UintMap());
					object = object.toString().replaceAll("^\\((.*)\\)$", "$1")
							.replaceAll("\"", "\'");
				}
				if (key.matches("^[\\d+\\.]+$")) {
					object = ((IdScriptableObject) (obj)).get(
							Integer.parseInt(key), null);
				}
				/*
				String value = null;
				if (object != null)
					value = object.toString().replaceAll("^(.+)\\.0$", "$1");
				else
					value = null;
					*/
				Object value = object;
				formatJSON(object, lev, key, value, true, i == len - 1,
						isDeletedSpace, isDeleteAndCont);
			}
			if (!isDeletedSpace) {
				for (int i = 0; i < level; i++) {
					textArea.append("\t");
				}
				if (isLast) {
					textArea.append("}" + LINE);
				} else {
					textArea.append("}," + LINE);
				}
			} else {
				if (isLast) {
					textArea.append("}");
				} else {
					textArea.append("},");
				}
			}
			isObject = true;
		} else if (obj instanceof NativeArray) {
			if (!isDeletedSpace) {
				String temp = "";
				for (int i = 0; i < level; i++) {
					temp += "\t";
				}
				String x = isObject ? "\"" + k + "\":" + LINE + temp + "["
						: "[";
				textArea.append(x + LINE);
			} else {
				String x = "";
				if (!isDeleteAndCont) {
					x = isObject ? "\"" + k + "\":[" : "[";
				} else {
					x = isObject ? "\\\"" + k + "\\\":[" : "[";
				}
				textArea.append(x);
			}
			Object[] ids = ((IdScriptableObject) (obj)).getIds();
			int len = ids.length;
			int lev = level + 1;
			for (int i = 0; i < len; i++) {
				IdScriptableObject io = (IdScriptableObject) (obj);
				Object object = io.get(i, null);
				if (object instanceof Undefined) {
					object = "undefined";
				} else if (null == object) {
					object = null;
				} else if (object instanceof NativeFunction) {
					object = ((NativeFunction) (object)).getEncodedSource();
					object = Decompiler.decompile(object.toString(), -1,
							new UintMap());
					object = object.toString().replaceAll("^\\((.*)\\)$", "$1")
							.replaceAll("\"", "\'");
				}
				String value = null;
				if (object != null)
					value = object.toString().replaceAll("^(.+)\\.0$", "$1");
				else
					value = null;
				formatJSON(object, lev, i + "", value, false, i == len - 1,
						isDeletedSpace, isDeleteAndCont);
			}
			if (!isDeletedSpace) {
				for (int i = 0; i < level; i++) {
					textArea.append("\t");
				}
				if (isLast) {
					textArea.append("]" + LINE);
				} else {
					textArea.append("]," + LINE);
				}
			} else {
				if (isLast) {
					textArea.append("]");
				} else {
					textArea.append("],");
				}
			}
			isObject = false;
		} else {
			String vv = "";			
			if (v != null){
				if (v instanceof String){
					String vS = v.toString().replaceAll("(\"|\\\\)", "\\\\$1");
					//String vv = "";
					if (!isDeleteAndCont) {
						vv = isObject ? ("\"" + k + "\": \"" + vS + "\"") : ("\"" + vS + "\"");
					} else {
						vv = isObject ? ("\\\"" + k + "\\\": \\\"" + vS + "\\\"") : ("\\\"" + vS + "\\\"");
					}
				} else if (v instanceof Number){
					String vS = "";
					if (isInteger(v)){
						vS = String.valueOf(Integer.parseInt(v.toString().replace(".0", "")));
					}
					else if (isDouble(v)){
						vS = String.valueOf(Double.parseDouble(v.toString()));
					}
					if (!isDeleteAndCont) {
						vv = isObject ? ("\"" + k + "\": " + vS) : (v.toString());
					} else {
						vv = isObject ? ("\\\"" + k + "\\\": " + vS) : (v.toString());
					}
				}
				vv = isLast ? vv : vv + ",";
				if (!isDeletedSpace) {
					textArea.append(vv + LINE);
				} else {
					textArea.append(vv);
				}
			} else {  //modify here for null Object				
				if (!isDeleteAndCont) {
					vv = isObject ? ("\"" + k + "\": null")
							: ("null");
					System.out.println("here1");
				} else {
					vv = isObject ? ("\\\"" + k + "\\\": null")	: ("null");
					System.out.println("here2");
				}				
				vv = isLast ? vv : vv + ",";
				if (!isDeletedSpace) {
					textArea.append(vv + LINE);
				} else {
					textArea.append(vv);
				}
			}
		}
	}
	
	private boolean isInteger(Object o){
		try {
			String oo = "";			
			if ((o.toString().endsWith(".0")))
				oo = o.toString().replace(".0", "");			
			Integer.parseInt(oo); 
			return true; 
		} catch (NumberFormatException e) { 
			return false; 
		}
	}
	
	private boolean isDouble(Object o) { 
		try { 
			Double.parseDouble(o.toString()); 
			if (o.toString().contains(".")) 
				return true; 		
		} catch (NumberFormatException e) { 
			return false; 
		} 
		return false;
	}

}