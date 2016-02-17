package test;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

import NewHarnessRest.RestPropValidation;

public class validationTest {
	private static RestPropValidation testEngine = new RestPropValidation();
	public static JSONObject responseJSON;
	public static JSONObject validationJSON;
	
	public static void init() throws JSONException{
		
		responseJSON = new JSONObject("{\"first\": \"first1\",\"kkk\":{\"limit\": 50,\"locations\":{\"abc\": \"abc1\",\"bcd\": \"bcd1\"},\"searchString\": \"CHASSIS1\",\"offset\": 0}}");
		validationJSON = new JSONObject("{\"1\":{\"2\":{\"abc\": \"abc1\"},\"0\":{\"first\": \"first1\"}}}");
	}
	
	
	
	
	public static void main(String[] args) throws JSONException, FileNotFoundException, IOException, ParseException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		init();
		validationTest v = new validationTest();
		TestClass a = new TestClass();
		String g = "go";
		Method m = a.getClass().getDeclaredMethod(g, null);
		System.out.println(m.invoke(a, null));
		
		
		String sr = "LKSData.com.raritan.tdz.domain.ModelDataPorts.faceLookup";
		String[] ss = sr.split("\\.");
		
		System.out.print("ss: " + ss.length);
		System.out.println(ss[ss.length-1]);
		
		
		testEngine.validateP(responseJSON, validationJSON, null, null, null);
		//SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss Z" );
		SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy" );
		TimeZone tz = TimeZone.getTimeZone("GMT"+String.valueOf(2));
		String t = "12/8/2015 12:6:57";
		String tt = "12/08/2015";
		Calendar cal = Calendar.getInstance(tz);
		Date d = sdf.parse(tt);
		Timestamp ts = new Timestamp(d.getTime());
		//ts = Timestamp.valueOf(t);
		System.out.println("date: " + d.toGMTString());
		System.out.println("Timestamp: " + ts.toString());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("a","aa");
		map.put("b", "bb");
		map.put("c", null);
		
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next(); 
			//Object key = entry.getKey();
			if (entry.getValue() == null)
				iter.remove();
		}
		
		Iterator iter2 = map.entrySet().iterator();
		while (iter2.hasNext()) {
			Map.Entry entry = (Map.Entry) iter2.next(); 
			//Object key = entry.getKey();
			System.out.println(entry.getKey()+":"+entry.getValue());
		}
		//int a = 55;
		//System.out.println(new Long(55).intValue() == a);
		//System.out.println("it is here: " + sdf.format(null));
		
	}	

}
