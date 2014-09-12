/**
 * Acts as the slave. It receives process creation and migration requests from the master and 
 * performs the corresponding action. Additionally, it generates a report of the processes that are 
 * currently running on it when the master asks for that information.
 */

package distsys.promigr.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalManager
{
	public static ConcurrentMap<String, ThreadObject> threadMap;	
	
    public static void main(String[] args) {
        threadMap = new ConcurrentHashMap<String, ThreadObject>();        	
        ServerSocket pollingSocket = null;
        ServerSocket serverSocket = null;
        
        /*
         * New server socket to listen for polling requests from master in a new
         * thread.
         */
		try {
			pollingSocket = new ServerSocket(50002);
	        PollingResponseThread prThread = new PollingResponseThread(pollingSocket);
	        Thread polling = new Thread(prThread);
	        polling.start();
		} catch (IOException e1) {
			// can't do much on the local manager's side
			// ignore
		}

		// Server socket to listen for instruction messages from the master.
		try {
            serverSocket = new ServerSocket(50000);
        }
        catch (IOException e) {
            // can't do much on the local manager's side
            // ignore
        }          
        
        while(true) {
        	//Listen for new requests from process manager
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            }
            catch (IOException e) {
                //ignore
            }
            
            //Start a new thread to process to process each instruction 
            //from the master
            if(clientSocket != null) {
                LocalManagerThread lmthread = new LocalManagerThread(clientSocket, threadMap);
                Thread thread = new Thread(lmthread);
                thread.start();
            }
        }
        
    }

}
