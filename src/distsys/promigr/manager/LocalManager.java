package distsys.promigr.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LocalManager
{
	
	public volatile static Map<String, Thread> threadMap;
	
    public static void main(String[] args) throws IOException {
        threadMap = new HashMap<String, Thread>();
        	
        //create server socket
        ServerSocket serverSocket = new ServerSocket();        
        while(true) {
            synchronized(LocalManager.class) {
                 Socket clientSocket = serverSocket.accept();
                 LocalManagerThread lmthread = new LocalManagerThread(clientSocket, threadMap);
                 Thread thread = new Thread(lmthread);
                 thread.start();
            }
        }
        
        
    }

}
