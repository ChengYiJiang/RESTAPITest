package NewHarnessRest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JTextArea;



public class MultiThreadRest {

	/**
	 * @param args
	 */
	
	private JTextArea ta;
	String seperator = System.getProperty("line.separator");
	private String targetURL = "";
	private List<String> files;
	private int num = 0;
	private boolean reqShow = false;
	private boolean resShow = false;
	
	
	public MultiThreadRest(List<String> fList, String url, int number, boolean req, boolean res) {
		this.files = fList;
		targetURL = url;
		this.num = number;
		this.reqShow = req;
		this.resShow = res;
	}
	
	
	// OLD FUNCTION THAT WILL BLOCK THE MAIN UI THREAD THAT IS NOT USED ANYMORE IN UI
	// BUT THIS FUNCTION IS TILL USED FOR RUN WITHOUT GUI
	public ArrayList<String[]> runTestCases(List<String> tcList) throws InterruptedException, ExecutionException {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (!targetURL.equals("")) {
			new SSLVerificationDisabler().disableSslVerification();
			ExecutorService es = Executors.newFixedThreadPool(5);
			ArrayList<Future<String[]>> results = new ArrayList<Future<String[]>>();
			BufferedReader br = null;

			CompletionService<String[]> cs = new ExecutorCompletionService<String[]>(es);//(pool);
			for (int i = 0; i < tcList.size(); i++) {
				List<String> toThread = new ArrayList<String>();
				//System.out.println("size is: "+tcList.size()+" index is :" + i);
				try {					
					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(tcList.get(i))));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					for (String line = br.readLine(); line != null; line = br
							.readLine()) {
						if (!line.equals("\n") && !line.equals("\r\n")
								&& !line.equals(""))
							toThread.add(line.trim());						
					}
				} catch (IOException e) {					
					e.printStackTrace();
				}
				//es.submit(new RestRequestSender(tcList.get(i), toThread, targetURL, ta));
				results.add(es.submit(new RestRequestSender(tcList.get(i), toThread, targetURL, ta)));
				
				//Future<String[]> singleResult = es.submit(new RestRequestSender(tcList.get(i), toThread, targetURL, ta));
				System.out.println("File #"+ (i+1) +" Submitted to the pool...");
			}
			
			System.out.println("shut down pool");
			es.shutdown();
				
			
			System.out.println("Please wait for a while for "+tcList.size()+" test cases...");
			String textForReport = "";
			
			for (Future<String[]> one : results){
				String[] first = one.get();
				//ta.append(first[0]);
				System.out.println("****************************");
				System.out.println("get one");				
				result.add(first);
				textForReport += seperator + first[0];
			}			
			
		}
		return result;
	}	

}
