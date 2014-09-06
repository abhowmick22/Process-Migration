package distsys.promigr.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalManagerThread implements Runnable
{

	
    private Socket clientSocket;
    protected LocalManagerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
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
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	break;
        case 1 : 
        	ObjectInputStream objectIn= null;
        	Object process = null;
 
        	 try {
				objectIn = new ObjectInputStream(inputStream);
				process = objectIn.readObject();
				// Launch the process as a migratableProcess in this thread
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
        	while(procName != null)
        	
        	
        	break;
        }
        
        
    }
}
