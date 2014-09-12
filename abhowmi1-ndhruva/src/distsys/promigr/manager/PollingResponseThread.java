/**
 * Thread spawned by the local manager on each node to respond to the polling requests sent 
 * by the polling request thread, and ascertain that the local manager on the node is still alive.
 */

package distsys.promigr.manager;

import java.io.IOException;
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
		while(true){
			try {
			    // process manager should be able to connect
				clientSocket = pollingSocket.accept();								
			} catch (IOException e) {
				//ignore
			} 
		}
		
	}

}
