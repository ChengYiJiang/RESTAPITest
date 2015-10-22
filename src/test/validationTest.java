package test;



import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import NewHarnessRest.RestPropValidation;

public class validationTest {
	private static RestPropValidation testEngine = new RestPropValidation();
	public static JSONObject responseJSON;
	public static JSONObject validationJSON;
	
	public static void init() throws JSONException{
		responseJSON = new JSONObject("{\"first\": \"first1\",\"kkk\":{\"limit\": 50,\"locations\":{\"abc\": \"abc1\",\"bcd\": \"bcd1\"},\"searchString\": \"CHASSIS1\",\"offset\": 0}}");
		validationJSON = new JSONObject("{\"1\":{\"2\":{\"abc\": \"abc1\"},\"0\":{\"first\": \"first1\"}}}");
	}
	
	public static void main(String[] args) throws JSONException, FileNotFoundException, IOException {
		init();
		testEngine.validateP(responseJSON, validationJSON, null, null, null);
	}	

}
