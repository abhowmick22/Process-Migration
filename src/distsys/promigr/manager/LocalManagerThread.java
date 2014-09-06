package distsys.promigr.manager;

import java.lang.reflect.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class LocalManagerThread implements Runnable
{

	private Map<String, Thread> threadMap;
    private Socket clientSocket;
    protected LocalManagerThread(Socket clientSocket, Map<String, Thread> threadMap) {
        this.clientSocket = clientSocket;
        this.threadMap = threadMap;
    }

    @Override
    public void run()
    {
    	BufferedReader in = null;
    	int msg = -1;
    	InputStream inputStream = null;
    	String procId, dest;
		try {
			inputStream = clientSocket.getInputStream();
			in = new BufferedReader(
			    new InputStreamReader(inputStream));
			msg = Integer.parseInt(in.readLine());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        switch (msg){
        
        case 0 : 
        	try {
				procId = in.readLine().trim();
				dest = in.readLine().trim();
			
				// Migrate a process from this to dest
				
				
				
				//Map<Thread, StackTraceElement[]> map = this.getAllStackTraces();
				//System.out.println(map);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	break;
        case 1 : 
        	ObjectInputStream objectIn= null;
        	Object recdObject;
        	Class<?> myClass;
        	String className;
 
        	 try {
				objectIn = new ObjectInputStream(inputStream);
				procId = in.readLine().trim();
				className = in.readLine().trim();			// class name
				recdObject = objectIn.readObject();
				//myClass = process.getClass();
				Class clazz = Class.forName(className);
				clazz.cast(recdObject);
								
				// Launch the process as a migratableProcess in this thread
				//Thread t = new Thread((myClass)process);
				//threadMap.put(procId, t);
				
				
				
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   	
        	break;
        	
        case 2 :
        	String procName ;
        	//while(procName != null)
        	
        	
        	break;
        }
        
        
    }
}
