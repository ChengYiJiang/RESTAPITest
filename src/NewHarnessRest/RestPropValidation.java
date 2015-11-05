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
	private Map<String, List<String>> checkGroup;
	
	public RestPropValidation() {
		checkGroup = new HashMap<String, List<String>>();
	}

	public HashMap<String, String> getOverideHM() {
		return overideProps;
	}

	// r is the response JSONObject, json is the validation JSONObject :<
	public String[] validateP(JSONObject r, JSONObject json, String path, ConcurrentHashMap<String, String> requestOveride, String d) throws FileNotFoundException, IOException, JSONException {
		
		correctGroup.clear();		
		if (requestOveride != null)
			overideProps.putAll(requestOveride);		
		
		Iterator<String> preCheck = json.keys();
		while (preCheck.hasNext()) {
			String temp = preCheck.next();
			if (!temp.equals("0")) {
				checkGroup.put(temp, new ArrayList<String>());
			}
		}		

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
	public void searchGroupingValidation(JSONObject r, JSONObject vSub, String vKey, int offset) throws JSONException {
		boolean result = true;	
		//System.out.println("maxDeepth is: " + maxDeepth);
		String levelOne = "";
		
		//loop the VALIDATION JSON
		Iterator<String> bigIter = vSub.keys();   //this bigIter is actually the level like 3_1 so bigIter is 1
		while (bigIter.hasNext()) {
			String bigKey = bigIter.next();
			//System.out.println("Level is " + bigKey + " and offset: " + String.valueOf(offset));
			//System.out.println("The validation JSON for level " + bigKey + " is: " + vSub.get(bigKey));
			if (bigKey.equals(String.valueOf(offset))){
				//System.out.println("Now we want to validate this level since offset: " + offset + " equals bigKey");
				//System.out.println("VERIFY in vSub: " + vSub);
				levelOne = bigKey;
			}
		}		
		
		if (vSub.has(levelOne) && !checkGroup.get(vKey).contains(levelOne)) { 
			JSONObject temp = vSub.getJSONObject(levelOne); // json
			Iterator<String> groupIter = temp.keys(); // in group that (id -
														// offset == 1)
			System.out.println("response here is " + r);
			//System.out.println("vSub has levelOne: " + levelOne);
			System.out.println("vSub has levelOne: " + levelOne + " and the validation json is: " + temp);
			while (groupIter.hasNext()) {   //loop the validation json for that 
				String key1 = groupIter.next(); // pair-value
				System.out.println("The key in validation json we are validating now is: " + key1);

				if (r.has(key1)) {
					System.out.println("Found key " + key1 + " and the value is " + r.get(key1));
					//System.out.println("HERE RESPONSE IS: " + r); 
					if (r.get(key1) instanceof String || r.get(key1) instanceof Number || JSONObject.NULL.equals(r.get(key1)) || r.get(key1) instanceof Boolean) {
						System.out.println("And key in response is a string with data " + r.get(key1));
						if (temp.get(key1).toString().startsWith("**OverideSave")) {
							String varName = temp.get(key1).toString().split("_")[1];
							overideProps.put(varName, r.get(key1).toString());
							System.out.println("YYYYYYYYYYYYYYYYY!!!!!!!!!!!!    we overide save one of " + r.get(key1).toString());
							if (!checkGroup.get(vKey).contains(levelOne))
								checkGroup.get(vKey).add(levelOne);
						} else if (temp.get(key1).toString().startsWith("**OverideRead")) {
							String varName = temp.get(key1).toString().split("_")[1];
							String value = overideProps.get(varName);
							// compare
							if (r.get(key1).toString().equals(value)) {
								if (!checkGroup.get(vKey).contains(levelOne))
									checkGroup.get(vKey).add(levelOne);
								//return false;
							}
						} else if (!r.get(key1).toString().equals(temp.get(key1).toString())) { // not
							// overide
							System.out.println("The key " + key1 + " with value " + r.get(key1) + " does not match that in map: " + temp.get(key1).toString());
							//System.out.println("--------------------------------------");
							//System.out.println("here we return false3");
							//System.out.println("result before is: " + result);
							result = false;//not equals to validation data
						}
					} else if (r.get(key1).toString().equals("null")){ // key good but value is not a string
						if (!temp.get(key1).toString().equals("null")){	
							result = false;
							//System.out.println("validation is null BUT RESPONSE IS NOT NULL!");
							//System.out.println("--------------------------------------");												
						}
					
					} else { // key good but value is not a string
						//System.out.println("Now we like do as toString() and compare whole string");
						//System.out.println("In Validation JSON the value should be " + temp.get(key1).toString());
						if (!r.get(key1).toString().contains(temp.get(key1).toString())){	
							//System.out.println("--------------------------------------");
							result = false;			
						}
					}
				} else { // even not contain the key
					//System.out.println("In offset: " + offset + " The key " + key1 + " is not found");
					//System.out.println("--------------------------------------");
					checkGroup.get(vKey).clear();
					result = false;										
				}
			}
			//  the level that (id - offset == 1) is complete and good
		} else{
			//System.out.println("Cannot find level or level already verified: " + levelOne);
			result = false;			
		}
		
		//========= NOW LEVEL offset HAS THE RIGHT VALIDATION KEY-VALUE PAIR
			// the the group that (id - offset == 1) is complete and good
		//System.out.println("Finish the group for offset " + offset + " and this is level " + levelOne);
		//wentVLevels.add(levelOne);
		if (result && !checkGroup.get(vKey).contains(levelOne))
			checkGroup.get(vKey).add(levelOne);
		//else //if (!checkGroup.get(vKey).contains(levelOne)){
			//System.out.println("checkGroup with " + vKey + " already has level: " + levelOne + " but result is false");
			//checkGroup.get(vKey).clear();
		//}
		//checkGroup.get(vKey).clear();
		//System.out.println("THIS IS WHEN WE SUCCESSFULLY VERIFIED ONE LEVEL: " + levelOne);
		System.out.println("checkGroup is:\n" + checkGroup);			
		//System.out.println("---------------------------------------------------------------------");
		
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
						searchGroupingValidation(r.getJSONObject(tmpStr), vSub, vKey, offset + 1);
					} else if ((r.get(tmpStr) instanceof JSONArray)) {
						maxDeepth ++;
						System.out.println("IT IS A JSON Array with key: " + tmpStr);
						JSONArray jArray = r.getJSONArray(tmpStr);
						boolean tempResult = false;
						for (int i = 0; i < jArray.length(); i++) {
							Object obj = jArray.get(i);
							//System.out.println("!!!!!! IN " + tmpStr + " WE GET ONE OBJECT WHICH IS: " + obj + " and vSub is: " + vSub);
							if (obj instanceof JSONObject) {
								searchGroupingValidation(jArray.getJSONObject(i), vSub, vKey, offset + 1);
								
							} else {
								//System.out.println("why it is not jsonobject???");
							}
						}
						
					} else {						
						// TODO: HERE FOR SMART CHART
					}
				}
			}
		}		
	}

	public void corrArray(JSONArray array, JSONObject v, String key) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			//System.out.println("THE ELEMENT in JSONArray with index: " + i + " is " + array.get(i));
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
				//System.out.println("tostringvalue is here with key is " + key + " and index " + i + " value is " + toStringValue);
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
				//System.out.println("The response here is: " + r);
				//System.out.println("In corr() the maxDeepthInValidation is " + maxDeepthInValidation);
				//System.out.println("And now the maxDeepth = " + maxDeepth);
				
				searchGroupingValidation(r, v.getJSONObject(vKey), vKey, 0);
				if (checkGroup.get(vKey).size() == v.getJSONObject(vKey).length()){
					System.out.println("GROUP: " + vKey + " IS GOOD!");
					correctGroup.add(vKey);
				}			
				// System.out.println("Found group " + vKey + " And they are correct"); 

				// TODO: PRINT GROUP DETAILS MAY BE NOT
			}
		}

		// now let's go for group 0	
		Iterator<String> responseIterator = r.keys();
		while (responseIterator.hasNext()) {

			String key = responseIterator.next();
			// System.out.println("now start searching a pair in group 0 with the key " + key);
			Object o = r.get(key);
			String toStringValue = null;
			if (o instanceof JSONObject) { // reversal
				System.out.println("Key is " + key + " which get an JSONObject");
				corr((JSONObject) o, v);
			} else if (o instanceof JSONArray) {
				System.out.println("Key is " + key + " which get an JSONArray");
				JSONArray tmp = (JSONArray) o;
				corrArray(tmp, v, key); // now using another recursive function

			} else if (o instanceof List<?>) {  //TODO: should not check like this
				//toStringValue = String.valueOf(((List) o).get(0));
				//TODO: FOR HERE MAYBE IMPLEMENT toString() || contains()
				//TODO: FOR HERE MAYBE IMPLEMENT get(index)
				//System.out.println("Key is " + key + " which get an List<?>");
				//System.out.println("tostringvalue ArrayList[0] is " + ((List<?>) o).toString());
			} else {// if (o instanceof String || o instanceof Integer ) {					
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
	
	//TODO: validate as String
	private void validateToString(){
		
	}
	
}
