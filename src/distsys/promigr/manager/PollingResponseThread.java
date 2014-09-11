package distsys.promigr.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PollingResponseThread implements Runnable{

	private ServerSocket pollingSocket = null;
	private Socket clientSocket = null;
	
	public PollingResponseThread(ServerSocket pollingSocket) {
		// TODO Auto-generated constructor stub
		this.pollingSocket= pollingSocket;
	}

	@Override
	public void run() {
		ObjectInputStream in = null;
		String poll;		
		while(true){
			try {
				clientSocket = pollingSocket.accept();
				in = new ObjectInputStream(clientSocket.getInputStream());
				poll = (String) in.readObject();
				in.close();
				//System.out.println(poll + " from PM");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
