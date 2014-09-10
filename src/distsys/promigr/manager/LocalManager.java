package distsys.promigr.manager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
