package NewHarnessRest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import javax.swing.JTextArea;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RestRun implements Runnable {

	private static String seperator = System.getProperty("line.separator");
	public JTextArea jta; // THIS IS THE GUI COMPONENT THE THREAD WILL APPEND RESULT AT
	public List<String> FailedRecord;
	private CountDownLatch downLatch;
	private String sourcePath = "";
	private String targetURL = "";
	private ConcurrentHashMap<String, String> requestOveride = new ConcurrentHashMap<String, String>();
	private List<String> fileLocList;
	private String[] validateResult = { "", "PASS", "" };
	private RestPropValidation vObj = new RestPropValidation();
	private String description = "";
	private RESTServiceFactory factory = new RESTServiceFactory();
	private boolean requestShow = false;
	private boolean responseShow = false;
	private String _sessionID = "";
	private JSONObject config;
	private cookieGetter cg = new cookieGetter();

	private enum mType {
		Post, Delete, Get, Put
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public RestRun(String tcPath, String turl, JTextArea t, List<String> a, CountDownLatch l, boolean request, boolean response, String ssID, JSONObject config) {
		fileLocList = new ArrayList<String>();
		_sessionID = ssID;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tcPath)));
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (!line.equals("\n") && !line.equals("\r\n") && !line.equals(""))
					fileLocList.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.targetURL = turl;
		this.sourcePath = tcPath;
		this.validateResult[0] = seperator + seperator + "This thread proccessing test case: " + sourcePath;
		this.validateResult[2] = sourcePath;
		this.jta = t;
		this.FailedRecord = a;
		this.downLatch = l;
		this.requestShow = request;
		this.responseShow = response;
		this.config = config;
	}

	public RestRun(String turl, JTextArea t, List<String> steps, boolean request, boolean response, JSONObject config) {
		this.fileLocList = steps;
		this.targetURL = turl;
		this.jta = t;
		this.requestShow = request;
		this.responseShow = response;
		this.config = config;
	}

	// for overide params
	private void overideParam(JSONObject obj) throws Throwable {
		Iterator<String> keys = obj.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (obj.get(key) instanceof String) {
				if (obj.get(key).toString().startsWith("**OverideRead")) {
					String varName = obj.get(key).toString().split("_")[1];
					if (vObj.getOverideHM().containsKey(varName)) {
						obj.put(key, vObj.getOverideHM().get(varName));
					} else {
						//obj.put(key, "No Variable overided!");
						System.out.println("Exception here, there is no value for override param: " + key);
					}
				}
			} else if (obj.get(key) instanceof JSONObject) {
				overideParam(obj.getJSONObject(key));
			} else if (obj.get(key) instanceof JSONArray) {
				JSONArray temp = (JSONArray) obj.get(key);
				for (int i = 0; i < temp.length(); i++) {
					// TODO: HANDLE OBJECT BUT NOT JSONOBJECT
					if (temp.get(i) instanceof JSONObject)
						overideParam(temp.getJSONObject(i));
				}
			}
		}

	}

	// return JSONObject[3], [0] is request, [1] is the JSONObject for params
	// validation, [2] is the response JSON
	public JSONObject[] sendReqeust(JSONObject r, JSONObject config) throws Throwable {

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
		result[0] = r;

		// System.out.println("THe request before overide is " + r.toString());
		overideParam(r);
		// now r has been overided
		// System.out.println("THe request after overide is " + r.toString());
		String Method = "";
		JSONObject json = new JSONObject(r.toString());

		String method[] = json.get("Method").toString().split("_");

		// ---------------------------
		// NOW METHOD IS STILL IN THE JSON AND FOR SERVICE TO PROCESS LIKE
		// GENERATE THE URL
		// Now for overide params in request body:
		// Iterator all key - value and if the value starts with **Overide
		// Then replace from HashMap

		Iterator<String> keys = json.keys();
		while (keys.hasNext() && requestOveride.size() > 0) {
			String key = keys.next();
			if (json.getString(key) != null && json.getString(key).startsWith("**OverideRead")) {
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
		String nURL = factory.generateURL(r, config);
		// System.out.println("Generate URL: " + nURL);
		String nPayload = r.get("payload").toString();
		// System.out.println("Generate payload: " + nPayload);

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

		URL url = null;
		if (r.has("url") && !StringUtils.isEmpty(r.getString("url"))) {
			url = new URL(r.getString("url") + nURL);
		} else
			url = new URL(targetURL + nURL);

		System.out.println("THE FINAL URL IS: " + r.getString("url") + nURL);

		String authString = null;
		if (checkHasProperty("username", r) && checkHasProperty("password", r)) {
			authString = r.getString("username") + ":" + r.getString("password");
			// _sessionID = cg.getCookie(r.getString("url"),
			// r.getString("username"), r.getString("password"));
		} else
			authString = "admin:sunbird";
		// String authString = "admin:raritan";
		String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			System.out.println("URL IS " + con.getURL().getPath());

			System.out.println("cookie IS " + _sessionID);
			// if (!_sessionID.equals("") ) //&& sv.isCookieNeeded())
			// con.setRequestProperty("Cookie", _sessionID);

			con.setRequestMethod(Method);
			con.setRequestProperty("Authorization", "Basic " + authStringEnc);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");
			// very important below: GET should not set setDoOutput(true)
			if (!isGet) {
				con.setDoOutput(true);
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
				reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {
				reader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			StringBuilder responseBuilder = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				responseBuilder.append(line + "\n");
			}

			if (con.getResponseCode() == 200) {
				if (responseBuilder.length() >= 2) {
					responseJSON = factory.parseLeafJSONData(new JSONObject(responseBuilder.toString()), json.get("Method").toString(), config);
				} else
					responseJSON = factory.parseLeafJSONData(new JSONObject(), json.get("Method").toString(), config);

			} else {
				System.out.println(responseBuilder.toString());
				JSONObject temp = new JSONObject(responseBuilder.toString());
				responseJSON = new JSONObject();
				responseJSON.put("errors", temp.get("errors"));
			}

			// put responsecode and msg into the responseJSON
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
	public void run() {
		// TODO Auto-generated method stub
		for (int i = 0; i < fileLocList.size(); i++) {
			String requestText = null;
			try {
				requestText = readFile(fileLocList.get(i).trim(), StandardCharsets.UTF_8);
				JSONObject j = new JSONObject(requestText);
				JSONObject[] theResult = sendReqeust(j, config);
				String[] tempResult = vObj.validateP(theResult[2], theResult[1], fileLocList.get(i), requestOveride, description);
				validateResult[0] += tempResult[0];
				if (requestShow) {
					validateResult[0] += System.getProperty("line.separator") + "The request payload is:" + System.getProperty("line.separator");
					validateResult[0] += new ForMatJSONStr().format(theResult[0].getJSONObject("payload").toString());
				}
				if (responseShow) {
					validateResult[0] += System.getProperty("line.separator") + "The response is:" + System.getProperty("line.separator");
					validateResult[0] += new ForMatJSONStr().format(theResult[2].toString());
				}
				validateResult[0] += "=============================>>>>>  " + tempResult[1];
				if (tempResult[1].equals("FAILED")) {
					validateResult[1] = "FAILED";
				}
			} catch (IOException e) {
				validateResult[0] += seperator + "There is IOException for this step...";
				validateResult[1] = "FAILED";
				e.printStackTrace();
			} catch (JSONException e) {
				validateResult[0] += seperator + "There is JSONException for this step... The step file may be broken";
				validateResult[1] = "FAILED";
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}

			// combine the overide params map
			Iterator<String> it = vObj.getOverideHM().keySet().iterator();
			while (it.hasNext()) {
				String ii = it.next();
			}
			requestOveride.putAll(vObj.getOverideHM());
			Iterator<String> it2 = requestOveride.keySet().iterator();
			while (it2.hasNext()) {
				String ii = it2.next();
			}
		}
		jta.append(validateResult[0]);
		if (validateResult[1].equals("FAILED")) {
			FailedRecord.add(sourcePath);
		}
		this.downLatch.countDown();
	}

	private boolean checkHasProperty(String key, JSONObject j) throws JSONException {
		if (j.has(key) && j.get(key) != null)
			return true;
		return false;
	}

}
