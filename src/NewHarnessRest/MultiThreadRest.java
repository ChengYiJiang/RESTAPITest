package NewHarnessRest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONObject;

/*
 * This class works as a mid layer between launcher and actually RestRequestSenders
 */


public class MultiThreadRest {
	
	String seperator = System.getProperty("line.separator");
	private String targetURL = "";
	private List<String> files;
	private int num = 0;
	private boolean reqShow = false;
	private boolean resShow = false;
	private JSONObject config;
	
	
	public MultiThreadRest(List<String> fList, String url, int number, boolean req, boolean res) {
		this.files = fList;
		this.targetURL = url;
		this.num = number;
		this.reqShow = req;
		this.resShow = res;
	}
	
	public MultiThreadRest(List<String> fList, String url, int number, boolean req, boolean res, JSONObject config) {
		this.files = fList;   //path of steps passed by test case
		this.targetURL = url; //targetURL can be null since it can be assigned in test step
		this.num = number;  //number of threads
		this.reqShow = req;
		this.resShow = res;   //these two are currently not used
		this.config = config;  //config json passed in
	}
	
	
	// OLD FUNCTION THAT WILL BLOCK THE MAIN UI THREAD THAT IS NOT USED ANYMORE IN UI
	// BUT THIS FUNCTION IS TILL USED FOR RUN WITHOUT GUI
	public ArrayList<String[]> runTestCases(List<String> tcList) throws InterruptedException, ExecutionException {
		ArrayList<String[]> result = new ArrayList<String[]>();	
		
		new SSLVerificationDisabler().disableSslVerification();
		ExecutorService es = Executors.newFixedThreadPool(5);
		ArrayList<Future<String[]>> results = new ArrayList<Future<String[]>>();
		BufferedReader br = null;

		CompletionService<String[]> cs = new ExecutorCompletionService<String[]>(es);//(pool);
		for (int i = 0; i < tcList.size(); i++) {
			List<String> toThread = new ArrayList<String>();
				//System.out.println("size is: "+tcList.size()+" index is :" + i);
			try {					
				br = new BufferedReader(new InputStreamReader(new FileInputStream(tcList.get(i))));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				for (String line = br.readLine(); line != null; line = br.readLine()) {
					if (!line.equals("\n") && !line.equals("\r\n") && !line.equals(""))
							toThread.add(line.trim());						
				}
			} catch (IOException e) {					
				e.printStackTrace();
			}			
			results.add(es.submit(new RestRequestSender(tcList.get(i), toThread, targetURL, config)));
				
			//Future<String[]> singleResult = es.submit(new RestRequestSender(tcList.get(i), toThread, targetURL, ta));
			System.out.println("File #"+ (i+1) +" Submitted to the pool...");
		}
			
		System.out.println("shut down pool");
		es.shutdown();				
			
		System.out.println("Please wait for a while for "+tcList.size()+" test cases...");
		String textForReport = "";
			
		for (Future<String[]> one : results){
			String[] first = one.get();				
			System.out.println("****************************");						
			result.add(first);
			textForReport += seperator + first[0];
		}	
		
		return result;
	}	

}
