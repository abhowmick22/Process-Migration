package distsys.promigr.manager;

import java.io.IOException;
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
        
        
    }
}
