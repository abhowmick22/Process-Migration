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
	
    public static void main(String[] args) throws IOException {
        threadMap = new ConcurrentHashMap<String, ThreadObject>();
        	
        //create server socket
        ServerSocket serverSocket = new ServerSocket(serverPort);          
        while(true) {
            synchronized(LocalManager.class) {
                 Socket clientSocket = serverSocket.accept();
                 LocalManagerThread lmthread = new LocalManagerThread(clientSocket, threadMap, serverPort);
                 Thread thread = new Thread(lmthread);
                 thread.start();
            }
        }
        
        
    }

}
