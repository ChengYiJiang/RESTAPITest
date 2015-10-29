package services.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import NewHarnessRest.RESTService;

/*
 * This class will not be used anymore 
 */

public class MobileSearchService extends RESTService{
	
	public MobileSearchService() {
		super();
		this.serviceString = "mobile search";
		System.out.println("Mobile search service set up");
	}

	@Override
	public JSONObject parseLeafJSONData(JSONObject response, String httpMethod) {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public String generateURL() throws JSONException {
		// TODO Auto-generated method stub
		String temp = "api/v1/mobile/search";
		if (rawData.has("searchType") && rawData.get("searchType") != null)
			temp += "/" + rawData.get("searchType") + "?uid=chengyi";
		return temp;
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
