package services.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import NewHarnessRest.RESTService;

public class MobilePlacementService extends RESTService {
	
	public MobilePlacementService() {
		super();
		this.serviceString = "mobile placement";
		System.out.println("Mobile placement service set up");
	}

	@Override
	public JSONObject parseLeafJSONData(JSONObject response, String httpMethod) {
		// TODO Auto-generated method stub
		return response;
	}

	@Override
	public String generateURL() throws Throwable {
		// TODO Auto-generated method stub
		String temp = "api/v1/mobile/getavailable/position";
		if (rawData.has("modelId") && rawData.get("modelId") != null)
			temp += "/" + rawData.get("modelId"); 
		if (rawData.has("itemId") && rawData.get("itemId") != null)
			temp += "?itemId=" + rawData.get("itemId");
		temp += "&uid=chengyi";
		return temp;
	}

	@Override
	public JSONObject generatePayload() throws JSONException {
		// TODO Auto-generated method stub
		return rawData.getJSONObject("payload");
	}

}
