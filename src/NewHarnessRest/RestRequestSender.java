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
import org.apache.commons.lang.StringUtils;
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
	RESTServiceFactory factory = new RESTServiceFactory();
	private String _sessionID = "";
	private JSONObject config;
	private enum mType {
		Post, Delete, Get, Put
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
	public RestRequestSender(String tcPath, List<String> fL, String turl, JTextArea t, JSONObject config) {
		this.targetURL = turl;
		this.fileLocList = fL;
		this.sourcePath = tcPath;
		this.validateResult[0] = System.getProperty("line.separator") + "This thread proccessing test case: "+sourcePath + System.getProperty("line.separator");
		this.validateResult[2] = sourcePath;
		this.fromParent = t;
		this.config = config;
	}
	
	
	//this constructor for run with GUI
	public RestRequestSender(List<String> fL, String turl, boolean request, boolean response, JSONObject config) {
		this.targetURL = turl;
		this.fileLocList = fL;
		this.requestShow = request;
		this.responseShow = response;
		this.config = config;
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
	public JSONObject[] sendReqeust(JSONObject r) throws Throwable {
		
		// start reading json
JSONObject[] result = new JSONObject[3];
		
		boolean isGet = false;
		String URL = null;
		if (r.has("url"))
				 URL = r.getString("url");
		JSONObject jsonV = r.getJSONObject("validation");
		// description for future use
		description = r.getString("Description");
		r.remove("Description");
		result[1] = (JSONObject) r.remove("validation");
		// System.out.println("the validation is "+result[1].toString());
		result[0] = r; // now r contains "id"
		
		System.out.println("THe request before overide is " + r.toString());
		overideParam(r);		
		//now r has been overided
		System.out.println("THe request after overide is " + r.toString());
		String Method = "";
		JSONObject json = new JSONObject(r.toString());
		//json.get("Method").
		String method[] = json.get("Method").toString().split("_");
		
		//---------------------------
		//NOW METHOD IS STILL IN THE JSON AND FOR SERVICE TO PROCESS LIKE GENERATE THE URL
		//json.remove("Method");
		// Now for overide params in request body:
		// Iterator all key - value and if the value starts with **Overide
		// Then replace from HashMap
		
		//TODO!!!!!  REVERSE JSONrawData and replace all Overide
		//MAYBE USE FUNCTION ABOVE
		Iterator<String> keys = json.keys();
		while (keys.hasNext() && requestOveride.size() > 0) {
			String key = keys.next();
			if (json.getString(key) != null
					&& json.getString(key).startsWith("**OverideRead")) {
				String lookInHM = json.getString(key).split("_")[1];
				Iterator<String> it3 = requestOveride.keySet().iterator();
				while (it3.hasNext()) {
					String ii = it3.next();
				}
				String value = this.requestOveride.get(lookInHM);// need check
				json.put(key, value);
			}
		}

		// json is the JSONObject that has been overided
		//RESTService sv = factory.getService(method[0], json);
		//RESTService sv = factory.getService(json.get("Method").toString(), json, config);
		//TODO: just use this factory to generate url and payload by the config JSON
		//String nURL = sv.generateURL();
		String nURL = factory.generateURL(r, config);
		System.out.println("Generate URL: " + nURL);
		String nPayload = r.get("payload").toString();
		//String nPayload = sv.generatePayload().toString();
		System.out.println("Generate payload: " + nPayload);

		switch (mType.valueOf(method[1])) {
		case Post:
			Method = "POST";
			break;
		case Delete:
			Method = "DELETE";
			break;
		case Put:
			Method = "PUT";
			break;
		case Get:
			Method = "GET";
			isGet = true;
			break;
		default:
			break;
		}

		//service = sv.getServiceString();
		URL url = null;
		if (r.has("url") && !StringUtils.isEmpty(r.getString("url"))){			
			url = new URL(r.getString("url") + nURL);			
		}
		else
			url = new URL(targetURL + nURL);
		
		String authString = null;
		if (checkHasProperty("username", r) && checkHasProperty("password", r))
			authString = r.getString("username")+":"+r.getString("password");
		else
			authString = "admin:sunbird";
		//String authString = "admin:raritan";
		String authStringEnc = new String(Base64.encodeBase64(authString
				.getBytes()));
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			System.out.println("URL IS " + con.getURL().getPath());
			
			if(!_sessionID.equals("") ) //&& sv.isCookieNeeded())
				con.setRequestProperty("Cookie", _sessionID);
			
			System.out.println("Add cookie with " + _sessionID);
			con.setRequestMethod(Method);
			con.setRequestProperty("Authorization", "Basic " + authStringEnc);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");
			// very important below: GET should not set setDoOutput(true)
			if (!isGet) {
				con.setDoOutput(true);
				//con.getou
				OutputStream os = con.getOutputStream();
				os.write(nPayload.getBytes("UTF-8"));
				os.flush();
				os.close();
			}
			con.connect();	

			JSONObject responseJSON = null;

			// REST RESPONSE
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
				if (responseBuilder.length() >= 2){
					responseJSON = factory.parseLeafJSONData(new JSONObject(responseBuilder.toString()), json.get("Method").toString(), config);
				}
				else
					responseJSON = factory.parseLeafJSONData(new JSONObject(), json.get("Method").toString(), config);				
				
			} else {
				System.out.println(responseBuilder.toString());
				JSONObject temp = new JSONObject(responseBuilder.toString());
				responseJSON = new JSONObject();
				responseJSON.put("errors", temp.get("errors"));
			}
			
			//put responsecode and msg into the responseJSON
			responseJSON.put("responseCode", con.getResponseCode());
			responseJSON.put("responseMessage", con.getResponseMessage());
			result[2] = responseJSON;
			System.out.println("The response is: " + responseJSON);
			return result;
		} catch (MalformedURLException e) {
			JSONObject exceptionJSON = new JSONObject();
			exceptionJSON.put("exception", e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			JSONObject exceptionJSON = new JSONObject();
			exceptionJSON.put("exception", e.toString());
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
			JSONObject[] theResult = null;
			try {
				theResult = sendReqeust(j);
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
	
	private void overideParam(JSONObject obj) throws JSONException {
		Iterator<String> keys = obj.keys();
		while(keys.hasNext()){
			String key = keys.next();
			if (obj.get(key) instanceof String){
				if (obj.get(key).toString().startsWith("**OverideRead")) {
					String varName = obj.get(key).toString().split("_")[1];
					if (vObj.getOverideHM().containsKey(varName)){
						//obj.remove(key);
						//use iterator.remove
						//keys.remove();
						obj.put(key, vObj.getOverideHM().get(varName));
					}						
					else{
						//obj.remove(key);
						//keys.remove();
						obj.put(key, "No Variable overided!");
					}						
				}
			} else if (obj.get(key) instanceof JSONObject){
				overideParam(obj.getJSONObject(key));
			} else if (obj.get(key) instanceof JSONArray){
				JSONArray temp = (JSONArray) obj.get(key);
				for (int i=0; i < temp.length(); i++){
					//TODO: HANDLE OBJECT BUT NOT JSONOBJECT
					if (temp.get(i) instanceof JSONObject)
						overideParam(temp.getJSONObject(i));
				}
			}
		}		
	}
	
	private boolean checkHasProperty(String key, JSONObject j) throws JSONException{
		if (j.has(key) && j.get(key) != null)
			return true;
		return false;
	}

}
