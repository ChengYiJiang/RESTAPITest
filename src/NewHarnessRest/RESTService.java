package NewHarnessRest;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class RESTService {
	
	protected JSONObject rawData = new JSONObject();
	
	protected List<String> requiredURLParams;
	
	public RESTService(){		
		
	}
	
	public void refreshData(JSONObject o){				
		this.rawData = o;		
	}
	
	protected String serviceString;
	
	public String getServiceString(){
		return serviceString;
	}
	
	private boolean needSSID = false;
	
	public boolean isCookieNeeded(){
		return needSSID;
	}
	
	
	abstract public JSONObject parseLeafJSONData(JSONObject response, String httpMethod);
	abstract public String generateURL() throws Throwable;
	abstract public JSONObject generatePayload() throws Throwable;
}
