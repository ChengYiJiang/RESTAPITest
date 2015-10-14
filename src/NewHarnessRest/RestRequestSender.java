package NewHarnessRest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JTextArea;

import org.apache.commons.codec.binary.Base64;
import org.json.*;



//TODO ADD TO OPTION FOR REQUEST AND RESPONSE FOR OUTPUT WHEN RUNNING WITHOUT GUI



public class RestRequestSender implements Callable<String[]> {
	private JTextArea fromParent;
	String sourcePath = "";
	private String targetURL = "";
	JSONObject inTC;
	List<JSONObject> steps; // incoming steps
	ConcurrentHashMap<String, String> requestOveride = new ConcurrentHashMap<String, String>();
	private List<String> fileLocList;
	String[] validateResult = { "", "PASS", "" };
	RestPropValidation vObj = new RestPropValidation();
	String description = "";
	private boolean requestShow = false;
	private boolean responseShow = false;

	private enum mType {
		Create, Delete, Retrieve, Update
	}
	
	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	// /TODO: MAKE CALLABLE METHOD TO PROCESS A LIST OF STEPS -- OVERIDE, JSON
	// TO 2 JSON: PARAMS AND
	// VALIDATIONS, SEND THE PARAMS
	// /TODO2: SET UP VALIDATION CLASS -- JSON, OVERIDE, RESULT STRING
	// TODO3 : MULTITHREAD, TEST CASE VIEW TO LAUNCH, REPORT INTERGRATION
	// TODO4: GUI AND NGUI

	//this constructor for run without GUI
	public RestRequestSender(String tcPath, List<String> fL, String turl, JTextArea t) {
		this.targetURL = turl;
		this.fileLocList = fL;
		this.sourcePath = tcPath;
		this.validateResult[0] = System.getProperty("line.separator") + "This thread proccessing test case: "+sourcePath + System.getProperty("line.separator");
		this.validateResult[2] = sourcePath;
		this.fromParent = t;
	}
	
	
	//this constructor for run with GUI
	public RestRequestSender(List<String> fL, String turl, boolean request, boolean response) {
		this.targetURL = turl;
		this.fileLocList = fL;
		this.requestShow = request;
		this.responseShow = response;
	}

	/**
	 * @param args
	 * @throws JSONException
	 * @throws Throwable
	 */
	
	//for overide id
	private String overideID(String dynVar){
		if (dynVar.startsWith("**OverideRead")){
			String varName = dynVar.split("_")[1];
			if (vObj.getOverideHM().containsKey(varName))
				return vObj.getOverideHM().get(varName);
			else
				return "No Variable set!";
		}
		return dynVar;		
	}

	
	//return JSONObject[3], [0] is request, [1] is the JSONObject for params validation, [2] is the response JSON
	public JSONObject[] sendReqeust(JSONObject r) throws JSONException,
			MalformedURLException {
		String service = "";
		//read json
		JSONObject[] result = new JSONObject[3];
		String id = ""; //item id
		String portId = ""; //port id
		URL url = null;
		boolean isGet = false;
		String URL = "";
		JSONObject jsonV = r.getJSONObject("validation");	
		//description for future use
		description = r.getString("Description");
		r.remove("Description");
		result[1] = (JSONObject) r.remove("validation");
		//System.out.println("the validation is "+result[1].toString());
		result[0] = r;

		String Method = "";
		JSONObject json = new JSONObject(r.toString());
	
		String method[] = json.get("Method").toString().split("_");		
		json.remove("Method");
		
		if (method[0].equals("Item")) {
			service = "item";
			URL = targetURL + "/items";
			switch (mType.valueOf(method[1])) {
			case Create:
				Method = "POST";
				break;
			case Delete:
				Method = "DELETE";
				id = json.getString("id");
				id = overideID(id);
				URL += "/" + id;
				break;
			case Update:
				Method = "PUT";
				id = json.getString("id");
				id = overideID(id);
				URL += "/" + id;
				break;
			case Retrieve:
				id = json.getString("id");
				id = overideID(id);
				URL += "/" + id;
				Method = "GET";
				isGet = true;
				break;
			default:
				break;
			}
		}else if(method[0].equals("Port")){
			service = "dataport";
			URL = targetURL + "/items";
			id = json.getString("id");
			id = overideID(id);	
			URL += "/" + id + "/dataports";
			switch (mType.valueOf(method[1])) {
			case Create:
				Method = "POST";
				break;
			case Delete:
				Method = "DELETE";
				portId = json.getString("portId");
				portId = overideID(portId);
				URL += "/" + portId;
				break;
			case Update:
				Method = "PUT";
				portId = json.getString("portId");
				portId = overideID(portId);
				URL += "/" + portId;
				break;
			case Retrieve:
				portId = json.getString("portId");
				portId = overideID(portId);
				URL += "/" + portId;
				Method = "GET";
				isGet = true;
				break;
			default:
				break;
			}
		}else if(method[0].equals("Location")){
			service = "location";
			URL = targetURL + "/locations";
			id = json.getString("id");
			id = overideID(id);	
			URL += "/" + id;
			Method = "PUT";
		}
		json.remove("id");   //id can be item id or location id
		if (json.has("portId"))
			json.remove("portId");
		
		//Now for overide params in request body:
		//Iterator all key - value and if the value starts with **Overide
		//Then replace from HashMap
		//TODO # 1----------------------------------------------------------
		Iterator<String> keys = json.keys();
		
		//System.out.println("now size is "+ requestOveride.size());
		while (keys.hasNext() && requestOveride.size() > 0){
			String key = keys.next();			
			if (json.getString(key) != null && json.getString(key).startsWith("**OverideRead")){
				String lookInHM = json.getString(key).split("_")[1];				
				Iterator<String> it3 = requestOveride.keySet().iterator();
				while (it3.hasNext()){
					String ii = it3.next();					
				}				
				String value = this.requestOveride.get(lookInHM);//need check null				
				json.put(key, value);				
			}				
		}
		
		url = new URL(URL);
		//sending request
		String authString = "admin:raritan";
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();		
			con.setRequestMethod(Method);
			con.setRequestProperty("Authorization", "Basic " + authStringEnc);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");
			// very important below: GET should not set setDoOutput(true)
			if (!isGet) {
				con.setDoOutput(true);
				OutputStream os = con.getOutputStream();								
				os.write(json.toString().getBytes("UTF-8"));
				os.flush();
				os.close();
			}
			con.connect();			
			JSONObject responseJSON = null;
			
			//REST RESPONSE
			BufferedReader reader;
			if (con.getResponseCode() == 200) {
				reader = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(
						con.getErrorStream()));
			}
			StringBuilder responseBuilder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				responseBuilder.append(line + "\n");
			}
			if (con.getResponseCode() == 200) {
				if (!con.getRequestMethod().equals("DELETE")){
					responseJSON = new JSONObject(responseBuilder.toString()).getJSONObject(service);	
					//System.out.println("Now the first response is "+con.getRequestMethod()+" " + responseJSON);
				}else{
					//System.out.println(responseBuilder.toString());
					responseJSON = new JSONObject();					
				}
			}else{							
				JSONObject temp = new JSONObject(responseBuilder.toString());
				responseJSON = new JSONObject();
				responseJSON.put("errors", temp.get("errors"));								
			}
			responseJSON.put("responseCode", con.getResponseCode());
			responseJSON.put("responseMessage", con.getResponseMessage());			
			result[2] = responseJSON;
			return result;
		} catch (MalformedURLException e) {	
			JSONObject exceptionJSON =  new JSONObject();
			exceptionJSON.put("exception", e.toString());
			//System.out.println("------------------------------");
			System.out.println(exceptionJSON);
			e.printStackTrace();			
		} catch (IOException e) {
			JSONObject exceptionJSON =  new JSONObject();
			exceptionJSON.put("exception", e.toString());
			//System.out.println("------------------------------");
			System.out.println(exceptionJSON);
			e.printStackTrace();
		}		
		return result;
		

	}


	@Override
	public String[] call() throws Exception {		
		for (int i = 0; i<fileLocList.size(); i++){
			String requestText = readFile(fileLocList.get(i).trim(), StandardCharsets.UTF_8);
			
			JSONObject j = new JSONObject(requestText);
			//return JSONObject[3], [0] is request, [1] is the JSONObject for params validation, [2] is the response JSON
			JSONObject[] theResult = sendReqeust(j);
			
			String[] tempResult = vObj.validateP(theResult[2], theResult[1], fileLocList.get(i), requestOveride, description);
			validateResult[0] += tempResult[0];
			if (requestShow){
				validateResult[0] += System.getProperty("line.separator") + "The request is:" + System.getProperty("line.separator");
				validateResult[0] += new ForMatJSONStr().format(theResult[0].toString());
			}
			if (responseShow){
				validateResult[0] += System.getProperty("line.separator") + "The response is:" + System.getProperty("line.separator");			
				validateResult[0] += new ForMatJSONStr().format(theResult[2].toString());
			}			
			validateResult[0] += "=============================>>>>>  " + tempResult[1];
						
			if (tempResult[1].equals("FAILED"))
				validateResult[1] = "FAILED";
			//combine the overide params map			
			Iterator<String> it = vObj.getOverideHM().keySet().iterator();
			while (it.hasNext()){
				String ii = it.next();				
			}
			requestOveride.putAll(vObj.getOverideHM());
			Iterator<String> it2 = requestOveride.keySet().iterator();
			while (it2.hasNext()){
				String ii = it2.next();				
			}			
		}
		return validateResult;
	}

}
