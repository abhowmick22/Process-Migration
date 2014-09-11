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
        	
        //create server socket
        //TODO: remove "serverPort" and write 50000?
        
        // Set up a polling response thread
        ServerSocket pollingSocket = null;
		try {
			pollingSocket = new ServerSocket(serverPort + 2);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        PollingResponseThread prThread = new PollingResponseThread(pollingSocket);
        Thread polling = new Thread(prThread);
        polling.start();
        
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            //System.out.println("Can't create local server.");
        }          
        while(true) {
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                //System.out.println("Can't accept connections.");
            }
            if(clientSocket != null) {
                LocalManagerThread lmthread = new LocalManagerThread(clientSocket, threadMap, serverPort);
                Thread thread = new Thread(lmthread);
                thread.start();
            }
        }
        
    }

}
