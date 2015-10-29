package NewHarnessRest;

import org.json.JSONException;
import org.json.JSONObject;


/*
 * This class will not be used anymore 
 */

public class SmartChartService extends RESTService{
	
	public SmartChartService() {
		super();
		this.serviceString = "smart chart";
		System.out.println("Smart chart service set up");
	}

	@Override
	public JSONObject parseLeafJSONData(JSONObject response, String httpMethod) {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public String generateURL() {
		// TODO Auto-generated method stub
		return "/api/v1/measurements";
	}

	@Override
	public JSONObject generatePayload() {
		// TODO Auto-generated method stub
				try {
					return rawData.getJSONObject("payload");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
	}

}
