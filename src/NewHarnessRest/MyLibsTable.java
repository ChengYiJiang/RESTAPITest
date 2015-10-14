package NewHarnessRest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class MyLibsTable extends JTable {
	private static final long serialVersionUID = 1L;
	private int myRow = -1, myCol = -1;
	TableCellEditor myEditor;
	List<String> modelList = new ArrayList<String>();
	DefaultCellEditor dce1;
	DefaultCellEditor dce2;
	// hard coding stuff :<
	DefaultCellEditor dceMedia = new DefaultCellEditor(
			new JAutoCompleteComboBox(new String[] { "Coax", "Multi_Mode",
					"Single_Mode", "Twisted Pair" }));
	DefaultCellEditor dceProtocol = new DefaultCellEditor(
			new JAutoCompleteComboBox(
					new String[] { "Ethernet/IP", "FCoE", "FibreChannel",
							"HSSI", "InfiniBand", "iSCSI", "KVM", "n/a", "SAS",
							"SCSI-1", "SCSI-2", "SCSI-3", "Serial RS232" }));
	DefaultCellEditor dceDataRate = new DefaultCellEditor(
			new JAutoCompleteComboBox(new String[] { "1000 Base-Lx",
					"1000 Base-Sx", "1000 Base-Zx", "100/1000/10G Base-T",
					"100 Base-Fx", "100 Base-T", "100G Base-T",
					"100/1000/1G Base-T", "10/100 Base-T", "10 Base-Fx",
					"10 Base-T", "10G Base-Lx", "10G Base-Sx", "10G Base-T",
					"10G Base-Zx", "1G Base-T", "2 Gbps", "3 Gbps",
					"40G Base-Lx", "40G Base-T", "45 Mbps", "4 Gbps",
					"56 Gbps", "6 Gbps", "80 Mbps", "8 Gbps", "9600 Baud",
					"n/a" }));
	String targetURL = "";

	public MyLibsTable(DefaultTableModel model, String u) {
		// TODO Auto-generated constructor stub
		super(model);
		this.targetURL = u;
	}

	public void updateMake(String url) {
		ArrayList<String> makeList = new ArrayList<String>();
		if (!url.equals("")) {
			try {
				Class.forName("org.postgresql.Driver").newInstance();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String dbUrl = "jdbc:postgresql://" + url + ":5432/raritan";
			try {
				Connection con = DriverManager.getConnection(dbUrl, "dctrack",
						"(ManageMyStuff)!");
				Statement st = con.createStatement();
				String sql = "SELECT distinct sys_model_mfr_name FROM dct_models order by sys_model_mfr_name";
				ResultSet rs = st.executeQuery(sql);

				while (rs.next()) {
					makeList.add(rs.getString(1));
				}
				rs.close();
				st.close();
				con.close();
			} catch (SQLException e) {
				System.out.println("Failed in connecting database...");
			}

			Object[] makeArray = makeList.toArray();
			JAutoCompleteComboBox makeComboBox = new JAutoCompleteComboBox(
					makeArray);
			dce1 = new DefaultCellEditor(makeComboBox);
			System.out.println("finish update make");
		}
	}

	public void updateModelComboBox(String make) {
		try {
			Class.forName("org.postgresql.Driver").newInstance();
			String dbUrl = "jdbc:postgresql://192.168.62.187:5432/raritan";
			Connection con = DriverManager.getConnection(dbUrl, "dctrack",
					"(ManageMyStuff)!");
			Statement st = con.createStatement();
			String sql = "SELECT distinct model_name FROM dct_models WHERE sys_model_mfr_name='"
					+ make + "' order by model_name";
			ResultSet rs = st.executeQuery(sql);
			modelList.clear();
			while (rs.next()) {
				modelList.add(rs.getString(1));
			}
			rs.close();
			st.close();
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Object[] modelArray = modelList.toArray();
		JAutoCompleteComboBox comboBox2 = new JAutoCompleteComboBox(modelArray);
		dce2 = new DefaultCellEditor(comboBox2);
		System.out.println("finish update model");
	}

	public void setComboCell(int r, int c, TableCellEditor ce) {
		this.myRow = r;
		this.myCol = c;
		this.myEditor = ce;
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		int modelColumn = convertColumnIndexToModel(column);
		if (modelColumn == 1 && this.getValueAt(row, 0).equals("cmbMake"))
			return dce1;
		else if (modelColumn == 1 && this.getValueAt(row, 0).equals("cmbModel")) {
			return dce2;
		} else if (modelColumn == 1 && this.getValueAt(row, 0).equals("media"))
			return dceMedia;
		else if (modelColumn == 1 && this.getValueAt(row, 0).equals("protocol"))
			return dceProtocol;
		else if (modelColumn == 1 && this.getValueAt(row, 0).equals("dataRate"))
			return dceDataRate;
		return super.getCellEditor(row, column);
	}

}
