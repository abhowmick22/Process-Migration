package distsys.promigr.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PollingResponseThread implements Runnable{

	private ServerSocket pollingSocket = null;
	private Socket clientSocket = null;
	
	/**
	 * Constructor that initializes the server socket.
	 * @param pollingSocket Server socket.
	 */
	public PollingResponseThread(ServerSocket pollingSocket) {
		this.pollingSocket= pollingSocket;
	}

	/**
     * The run method executes for this class.
     */
	@Override
	public void run() {
		ObjectInputStream in = null;		
		while(true){
			try {
				clientSocket = pollingSocket.accept();
				in = new ObjectInputStream(clientSocket.getInputStream());				
				//dummy object, do nothing      TODO
				in.close();				
			} catch (IOException e) {
				//ignore
			} 
		}
		
	}

}
