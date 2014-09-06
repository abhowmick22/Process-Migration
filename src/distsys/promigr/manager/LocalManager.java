package distsys.promigr.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalManager
{
    public static void main(String[] args) throws IOException {
        
        //create server socket
        ServerSocket serverSocket = new ServerSocket();        
        while(true) {
            synchronized(LocalManager.class) {
                 Socket clientSocket = serverSocket.accept();
                 LocalManagerThread lmthread = new LocalManagerThread(clientSocket);
                 Thread thread = new Thread(lmthread);
                 thread.start();
            }
        }
        
        
    }

}
