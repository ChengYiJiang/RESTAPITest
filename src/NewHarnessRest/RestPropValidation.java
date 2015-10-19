package NewHarnessRest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.map.AnnotationIntrospector.Pair;
import org.json.*;

//corr() is the entry, will iterate the response json
//for each level in JSON, it will first use searchGroup() to check if there is group validation matching
//then corr will do like plain key-value pair validation in GROUP 0
public class RestPropValidation {

	private HashMap<String, String> overideProps = new HashMap<String, String>();
	String seperator = System.getProperty("line.separator");
	private String fileLoc = "";
	String[] result = new String[3];
	String tcPath;
	private HashSet<String> correctGroup = new HashSet<String>();
	private int maxDeepth;
	private int sizeOfV;
	public RestPropValidation() {
		
	}

	public HashMap<String, String> getOverideHM() {
		return overideProps;
	}

	// r is the response JSONObject, json is the validation JSONObject :<
	public String[] validateP(JSONObject r, JSONObject json, String path, ConcurrentHashMap<String, String> requestOveride, String d) throws FileNotFoundException, IOException, JSONException {
		//maxDeepth = 0;
		correctGroup.clear();
		//overideProps.putAll(requestOveride);
		

		result[0] = seperator + "-------------------------------------------" + seperator + "Validation result for " + path + ":" + seperator + d + seperator + "-----------------------------------------" + "-------------------------------------------";
		result[1] = "PASS";
		result[2] = "";

		corr(r, json); // result in input in the result[]

		Iterator<String> finalCheck = json.keys();
		while (finalCheck.hasNext()) {
			String temp = finalCheck.next();
			if (!temp.equals("0")) {
				if (!correctGroup.contains(temp)) {
					result[1] = "FAILED";
					result[0] += seperator + "Group " + temp + " is not found or is not correct";
				} else {
					result[0] += seperator + "Group " + temp + " is found and correct";
				}
			}
		}
		if (json.has("0")) {
			Iterator<String> finalZeroCheck = json.getJSONObject("0").keys();
			while (finalZeroCheck.hasNext()) {
				String temp = finalZeroCheck.next();
				result[1] = "FAILED";
				result[0] += seperator + "Pair " + temp + " is expected to be " + json.getJSONObject("0").getString(temp) + " but is not found or is not correct";
			}
		}
		result[0] += seperator;
		return result;
	}

	// vSub is the JSONObject with key group id. it only contains levels!!!
	public boolean searchGroupingValidation(JSONObject r, JSONObject vSub, int offset) throws JSONException {
		boolean result = true;
		//sizeOfV = vSub.
		System.out.println("maxDeepth is: " + maxDeepth);
		String levelOne = "";
		
		//loop the VALIDATION JSON
		Iterator<String> bigIter = vSub.keys();   //this bigIter is actually the level like 3_1 so bigIter is 1
		while (bigIter.hasNext()) {
			String bigKey = bigIter.next();
			System.out.println("Level is " + bigKey + " and offset: " + String.valueOf(offset));
			System.out.println("The validation JSON for Bigkey " + bigKey + " is: " + vSub.get(bigKey));
			if (bigKey.equals(String.valueOf(offset))){
				System.out.println("Now we want to validate this level since offset: " + offset + " equals bigKey");
				levelOne = bigKey;
			}
		}
		// Pair<String, String> p = new
		if (vSub.has(levelOne)) { // only check for group that
									// (id - offset == 1)
			JSONObject temp = vSub.getJSONObject(levelOne); // json
			Iterator<String> groupIter = temp.keys(); // in group that (id -
														// offset == 1)
			System.out.println("vSub has levelOne: " + levelOne);
			while (groupIter.hasNext()) {   //loop the validation json for that 
				String key1 = groupIter.next(); // pair-value
				System.out.println("The key in grouping level map is " + key1 + " and now offset is "+offset + " now maxDeepth is " + maxDeepth);

				if (r.has(key1)) {
					System.out.println("Found key " + key1);
					if (r.get(key1) instanceof String || r.get(key1) instanceof Integer) {
						System.out.println("And key in response is a string with data " + r.get(key1));
						if (temp.get(key1).toString().startsWith("**OverideSave")) {
							String varName = temp.get(key1).toString().split("_")[1];
							overideProps.put(varName, r.get(key1).toString());
							System.out.println("YYYYYYYYYYYYYYYYY!!!!!!!!!!!!    we overide save one of " + r.get(key1).toString());
						} else if (temp.get(key1).toString().startsWith("**OverideRead")) {
							String varName = temp.get(key1).toString().split("_")[1];
							String value = overideProps.get(varName);
							// compare
							if (!r.get(key1).toString().equals(value)) {
								return false;
							}
						} else if (!r.get(key1).toString().equals(temp.get(key1).toString())) { // not
							// overide
							System.out.println("The key " + key1 +
							" with value " + r.get(key1) +
							" does not match that in map");
							
							return false; // not equals to validation data
						}
					} else { // key good but value is not a string
						System.out.println("The key " + key1 +
						" is not a string does not match that in map " +
						temp.get(key1).toString());
						return false;
					}
				} else { // even not contain the key
					System.out.println("In offset: " + offset + " The key " + key1 + " is not found");
					return false;
				}
			}
			//  the level that (id - offset == 1) is complete and good
		} else{
			System.out.println("Cannot find level: " + levelOne);
			//return false;// end of if
		}
		
		//========= NOW LEVEL offset HAS THE RIGHT VALIDATION KEY-VALUE PAIR
			// the the group that (id - offset == 1) is complete and good
		System.out.println("Finish the group for offset " + offset);
		// start go larger offset
		
		//KEEP GOING FOR NEXT LEVEL
		Iterator<String> vJSONIter = vSub.keys(); // it is level
		while (vJSONIter.hasNext()) {
			String checkIfHasLargerLayerNum = vJSONIter.next();
			if (Integer.parseInt(checkIfHasLargerLayerNum) > offset) {
				Iterator<String> rJSONIter = r.keys();
				while (rJSONIter.hasNext()) {
					String tmpStr = rJSONIter.next();
					// check if it is a json object or json array
					if (r.get(tmpStr) instanceof JSONObject) {
						maxDeepth ++;
						System.out.println("IT IS A JSON Object with key: " + tmpStr);
						result = result & searchGroupingValidation(r.getJSONObject(tmpStr), vSub, offset + 1);
					} else if ((r.get(tmpStr) instanceof JSONArray)) {
						maxDeepth ++;
						System.out.println("IT IS A JSON Array with key: " + tmpStr);
						JSONArray jArray = r.getJSONArray(tmpStr);
						boolean tempResult = false;
						for (int i = 0; i < jArray.length(); i++) {
							Object obj = jArray.get(i);
							if (obj instanceof JSONObject) {
								boolean tmpBoolean = searchGroupingValidation(jArray.getJSONObject(i), vSub, offset + 1);
								tempResult = tempResult || tmpBoolean;
							}
						}
						result = result & tempResult;
					} else {
						// TODO: HERE FOR SMART CHART
					}
				}
			}
		}
		//if (maxDeepth < offset) maxDeepth++;
		return result;
	}

	public void corrArray(JSONArray array, JSONObject v, String key) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			if (array.get(i) instanceof JSONObject)
				corr(array.getJSONObject(i), v);
			else if (array.get(i) instanceof JSONArray)
				corrArray(array.getJSONArray(i), v, key);
			else {
				// for case it is an normal array, inside array there should not
				// be name-value pairs
				// so we do not support override function for this case
				String toStringValue = String.valueOf(array.get(i));
				// JSONObject temp = new JSONObject(toStringValue);
				System.out.println("tostringvalue is here!!!!!!!!!!!! with key is " + key + " and index " + i + " length is " + toStringValue);
				// }
				if (v.has("0") && v.getJSONObject("0").has(key) && toStringValue.equals(v.getJSONObject("0").get(key).toString())) {
					v.getJSONObject("0").remove(key);
					result[0] += seperator + "Found key-value pair: " + key + ":" + toStringValue;
					result[0] += "  and value as expected.";
				}
			}
		}
	}

	public void corr(JSONObject r, JSONObject v) throws JSONException {

		Iterator<String> vIterator = v.keys(); // for each group
		while (vIterator.hasNext()) {
			String vKey = vIterator.next();
			//System.out.println("vKey is " + vKey);
			if (!vKey.equals("0") && !correctGroup.contains(vKey)){
				maxDeepth = 0;
				JSONObject levelsForGroup = v.getJSONObject(vKey);
				Iterator<String> levelsInterator = levelsForGroup.keys();
				int maxDeepthInValidation = 0;
				while (levelsInterator.hasNext()){
					String levelKey = levelsInterator.next();
					if (Integer.valueOf(levelKey) > maxDeepthInValidation)
						maxDeepthInValidation = Integer.valueOf(levelKey);
				}
				System.out.println("============================================================================");
				System.out.println("NOW START VALIDATING GROUP: " + vKey);
				System.out.println("The response here is: " + r);
				System.out.println("In corr() the maxDeepthInValidation is " + maxDeepthInValidation);
				System.out.println("And now the maxDeepth = " + maxDeepth);
				if (searchGroupingValidation(r, v.getJSONObject(vKey), 0) && maxDeepth >= maxDeepthInValidation) { // IMPORTANT
					System.out.println("GROUP: " + vKey + " IS GOOD!");
					correctGroup.add(vKey);
				}
			//}
				// System.out.println("Found group " + vKey +
				// " And they are correct"); // HERE!!!
				

				// TODO: PRINT GROUP DETAILS MAY BE NOT
			}
		}

		// now let's go for group 0
		//System.out.println("now let's go for group 0");

		Iterator<String> responseIterator = r.keys();
		while (responseIterator.hasNext()) {

			String key = responseIterator.next();
			// System.out.println("now start searching a pair in group 0 with the key "
			// + key);
			Object o = r.get(key);
			String toStringValue = null;
			if (o instanceof JSONObject) { // reversal
				System.out.println("Key is " + key + " which get an JSONObject");
				corr((JSONObject) o, v);
			} else if (o instanceof JSONArray) {
				System.out.println("Key is " + key + " which get an JSONArray");
				JSONArray tmp = (JSONArray) o;
				corrArray(tmp, v, key); // now using another recursive function

				/*
				 * for (int i = 0; i < tmp.length(); i++){ if (tmp.get(i)
				 * instanceof JSONObject) corr(tmp.getJSONObject(i), v); //if
				 * (tmp.get(i) instanceof JSONArray) //TODO: HERE!!!!!!!!! }
				 */
			} else if (o instanceof List<?>) {  //TODO: should not check like this
				//toStringValue = String.valueOf(((List) o).get(0));
				//TODO: FOR HERE MAYBE IMPLEMENT toString() || contains()
				//TODO: FOR HERE MAYBE IMPLEMENT get(index)
				//System.out.println("Key is " + key + " which get an List<?>");
				//System.out.println("tostringvalue ArrayList[0] is " + ((List<?>) o).toString());
			} else {// if (o instanceof String || o instanceof Integer ) {
					// from response

				// if (o instanceof Integer) {
				//System.out.println("Key is " + key + " which get an String/Integer/Long/NULL");
				toStringValue = String.valueOf(o);
				//System.out.println("tostringvalue is " + toStringValue +" " + o.getClass());
				// }
				if (v.has("0") && v.getJSONObject("0").has(key)) {
					// System.out.println("now we found the pair of " + key + " in group 0");
					if (v.getJSONObject("0").get(key).toString().startsWith("**OverideSave")) { // overide save, do not validate
						String varName = v.getJSONObject("0").get(key).toString().split("_")[1];
						overideProps.put(varName, o.toString());
					} else if (v.getJSONObject("0").get(key).toString().startsWith("**OverideRead")) { // overide read, do validate
						String varName = v.getJSONObject("0").get(key).toString().toString().split("_")[1];
						String value = overideProps.get(varName); // find from NOGROUP
						// if (o.toString().equals(value)) {
						if (toStringValue.equals(value)) {
							v.getJSONObject("0").remove(key);
							result[0] += seperator + "Found key-value pair: " + key + ":" + o.toString();
							result[0] += "  and value as expected.";
						}
					} else {
						// System.out.println("do not need overide");
						if (o.toString().equals(v.getJSONObject("0").get(key).toString())) {
							v.getJSONObject("0").remove(key);
							result[0] += seperator + "Found key-value pair: " + key + ":" + o.toString();
							result[0] += "  and value as expected.";
						}
					}
				} else {
				}
			}
		}
	}
	
	private void validateToString(){
		
	}
	
}
