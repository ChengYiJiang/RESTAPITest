package NewHarnessRest;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.swing.JTextArea;
import javax.swing.text.html.HTMLDocument.Iterator;


public class RestRunnerWatcher implements Runnable{
	static String seperator = System.getProperty("line.separator");
	private CountDownLatch downLatch;
	public JTextArea jta;
	ArrayList<String> failedResultList;
	private int tcSize;
	
	public RestRunnerWatcher(CountDownLatch l, JTextArea j, ArrayList<String> f, int s){
		this.downLatch = l;
		this.jta = j;
		this.failedResultList = f;
		this.tcSize = s;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			this.downLatch.await();
		}catch(InterruptedException e){
			
		}
		
		jta.append(seperator + seperator + "FINISHED!!!" + seperator);
		jta.append("FAILED cases " + failedResultList.size() + " of " +tcSize + ":" + seperator);
		System.out.println("FAILED cases " + failedResultList.size() + " of " +tcSize + ":" + seperator);
		
		java.util.Iterator<String> it = failedResultList.iterator();
		while(it.hasNext()){
			String temp = it.next();
			jta.append(temp + seperator);
		}
		jta.append(seperator + "========================================================="+seperator);
	}

}
