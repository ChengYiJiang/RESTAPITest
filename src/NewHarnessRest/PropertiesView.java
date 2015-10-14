package NewHarnessRest;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PropertiesView extends JPanel {

	private MyPropertiesTable jtable;
	private String[] headings = { "Property", "Value", "Group" };
	private HashMap<String, String> hm = new HashMap<String, String>();
	private String propPath = "";
	Properties prop = new Properties();
	private DefaultTableModel model;

	public PropertiesView() {
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addNew(String[] s) {
		if (s.length != 2)
			return;
		else {
			model.addRow(s);
			jtable.updateUI();
		}

	}

	public void init() throws IOException {
		Box box = new Box(BoxLayout.Y_AXIS);
		box.add(new JLabel("Validation"));
		model = new DefaultTableModel(null, headings);
		jtable = new MyPropertiesTable(model);
		jtable.setPreferredScrollableViewportSize(new Dimension(400, 200));

		box.add(new JScrollPane(jtable));

		// For Buttons
		JPanel buttonPanel = new JPanel();
		JButton newPropButton = new JButton("New");
		newPropButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String[] s = { "New Property", "New Value", "" };
				model.addRow(s);
				jtable.updateUI();
			}
		});

		JButton deletePropButton = new JButton("Delete");
		deletePropButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int[] selected = jtable.getSelectedRows();
				for (int i = selected.length - 1; i >= 0; i--)
					model.removeRow(selected[i]);
			}
		});

		// TODO------------------------------------------------
		buttonPanel.add(newPropButton);
		buttonPanel.add(deletePropButton);

		box.add(buttonPanel);
		this.add(box);

		jtable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				// TODO Auto-generated method stub
				if (e.getType() == TableModelEvent.UPDATE) {
					String value = jtable.getValueAt(e.getLastRow(),
							e.getColumn()).toString();
					if (!value.equals("")) {
						jtable.updateMessageComboBox(value);
					}
				}

			}

		});
	}

	public JSONObject getProps() {
		// need do something for grouping
		HashMap<String, String> hm = new HashMap<String, String>();
		for (int i = 0; i < model.getRowCount(); i++) {
			if (model.getValueAt(i, 2).toString().length() > 0)
				hm.put("GPID_" + model.getValueAt(i, 2).toString() + "_"
						+ model.getValueAt(i, 0).toString(),
						model.getValueAt(i, 1).toString());
			else
				hm.put(model.getValueAt(i, 0).toString(), model
						.getValueAt(i, 1).toString());
		}
		JSONObject result = new JSONObject(hm);
		System.out.println("bbb " + result);
		return result;
	}

	public JSONObject newGetProps() throws JSONException {
		// need do something for grouping
		JSONObject result = new JSONObject();
		HashMap<String, String> hm = new HashMap<String, String>();
		for (int i = 0; i < model.getRowCount(); i++) {
			if (model.getValueAt(i, 2).toString().length() > 0) {
				String groupNum = model.getValueAt(i, 2).toString().split("_")[0];
				String levelNum = model.getValueAt(i, 2).toString().split("_")[1];
				if (!result.has(groupNum)) {
					JSONObject tmpJSON = new JSONObject();
					JSONObject childJSON = new JSONObject();

					childJSON.put(model.getValueAt(i, 0).toString(), model
							.getValueAt(i, 1).toString());
					tmpJSON.put(levelNum, childJSON);
					result.put(groupNum, tmpJSON);
				} else {
					if (result.getJSONObject(groupNum).has(levelNum)) {
						result.getJSONObject(groupNum)
								.getJSONObject(levelNum)
								.put(model.getValueAt(i, 0).toString(),
										model.getValueAt(i, 1).toString());
					} else {
						JSONObject tmp = new JSONObject();
						tmp.put(model.getValueAt(i, 0).toString(), model
								.getValueAt(i, 1).toString());

						result.getJSONObject(groupNum).put(levelNum, tmp);
					}
				}
			} else {
				if (!result.has("0")) {
					JSONObject tmpJSON = new JSONObject();
					tmpJSON.put(model.getValueAt(i, 0).toString(), model
							.getValueAt(i, 1).toString());
					result.put("0", tmpJSON);
				} else {
					result.getJSONObject("0").put(
							model.getValueAt(i, 0).toString(),
							model.getValueAt(i, 1).toString());
				}
			}

		}
		System.out.println("bbb " + result);
		return result;
	}

	public void updateData(String path) throws IOException {
		while (model.getRowCount() > 0)
			model.removeRow(model.getRowCount() - 1);
		if (!path.equals("")) {
			File file = new File(path);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// System.out.println(str);

			try {
				JSONObject jo = new JSONObject(str);
				JSONObject input = jo.getJSONObject("validation");
				Iterator<String> groupKeys = input.keys();
				while (groupKeys.hasNext()) {
					String key = groupKeys.next();
					JSONObject o = input.getJSONObject(key);
					Iterator<String> levelKeys = o.keys();
					if (!key.equals("0")) {
						while (levelKeys.hasNext()) {
							String tmp[] = { "", "", "" };
							String level = levelKeys.next();
							tmp[2] = key + "_" + level;
							// now loop in the level json
							Iterator<String> insideLevel = o.getJSONObject(
									level).keys();
							while (insideLevel.hasNext()) {
								String inLevel = insideLevel.next();
								tmp[0] = inLevel;
								tmp[1] = o.getJSONObject(level).getString(
										inLevel);
								model.addRow(tmp);
							}
						}
					} else {
						//System.out.println("level key is " + levelKeys);
						while (levelKeys.hasNext()) {
							String zeroLevelKey = levelKeys.next();
							String tmp[] = {"","",""};
							tmp[0] = zeroLevelKey;
							tmp[1] = o.getString(zeroLevelKey);
							model.addRow(tmp);
						}
					}

				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

		} else {
			model.addRow(new String[] { "responseCode", "", "" });
			model.addRow(new String[] { "responseMessage", "", "" });
		}
		jtable.updateUI();
	}
}
