package NewHarnessRest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

public class cookieGetter {

	
	public String getCookie(String ip){
		
		new SSLVerificationDisabler().disableSslVerification();	
		
		String authString = "admin:raritan";
		String authStringEnc = new String(Base64.encodeBase64(authString
				.getBytes()));
		try {
			URL url = new URL(ip);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Authorization", "Basic " + authStringEnc);
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "application/json");			
			
			con.setDoOutput(false);				
			con.connect();
			//to get the _sessionid
			return con.getHeaderField("Set-Cookie").split(";")[0].split("=")[1];
		} catch (MalformedURLException e1) {			
			e1.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		}
		return null;
		
	}
}
