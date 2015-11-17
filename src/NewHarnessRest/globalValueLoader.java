package NewHarnessRest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class globalValueLoader {

	
	public JSONObject loadGlobalValues(String path){
		JSONObject result = null;
		try{
			FileReader fr = new FileReader(path); 
			String line = null;
	        StringBuffer strBuffer = new StringBuffer();		
			BufferedReader br = new BufferedReader(fr);
			while ((line = br.readLine()) != null)
			{
	            strBuffer.append(line + System.getProperty("line.separator"));
	        }         
	        br.close();
	        result = new JSONObject(strBuffer.toString());
		}catch (JSONException e){
			System.out.println("Unfortunately you did something wrong.");			
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return result;
		
	}
}
