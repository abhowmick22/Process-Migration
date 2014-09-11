package distsys.promigr.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LocalManager
{
	
	public static ConcurrentMap<String, ThreadObject> threadMap;
	private final static int serverPort = 50000;
	
    public static void main(String[] args) {
        threadMap = new ConcurrentHashMap<String, ThreadObject>();
        	
        //TODO: remove "serverPort" and write 50000?
        ServerSocket pollingSocket = null;
        ServerSocket serverSocket = null;
        
        /**
         * New server socket to listen for polling requests from master in a new
         * thread.
         */
		try {
			pollingSocket = new ServerSocket(serverPort + 2);
	        PollingResponseThread prThread = new PollingResponseThread(pollingSocket);
	        Thread polling = new Thread(prThread);
	        polling.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/**
		 * Server socket to listen for instruction messages from the master.
		 */
        try {
            serverSocket = new ServerSocket(serverPort);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            //System.out.println("Can't create local server.");
        }          
        
        while(true) {
        	
        	/**
        	 * Listen for new requests
        	 */
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                //System.out.println("Can't accept connections.");
            }
            
            /**
             * Start a new thread to process to process each instruction 
             * from the master
             */
            if(clientSocket != null) {
                LocalManagerThread lmthread = new LocalManagerThread(clientSocket,
                												threadMap, serverPort);
                Thread thread = new Thread(lmthread);
                thread.start();
            }
        }
        
    }

}
