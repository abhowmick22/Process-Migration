package distsys.promigr.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class PollingRequestThread implements Runnable{

	private Socket clientSocket = null;
	private ConcurrentMap<String, Boolean> machineAliveMap;
	private int serverPort;
	
	public PollingRequestThread(ConcurrentMap<String, Boolean> machineAliveMap, int serverPort) {
		// TODO Auto-generated constructor stub
		this.machineAliveMap = machineAliveMap;
		this.serverPort = serverPort;
	}

	@Override
	public void run() {
		ObjectInputStream in = null;
		String poll = "ping";		
		String localMachine = null;
		while(true){
			try {
				Thread.sleep(5000);
				
				// start polling
				Set<String> localMachines = machineAliveMap.keySet();
				Iterator<String> iter = localMachines.iterator();
				while (iter.hasNext()) {
					localMachine = iter.next();
					clientSocket = new Socket(iter.next(), serverPort);
					ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
					outStream.writeObject(poll);
				}
				
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				// update machineAliveMap
				machineAliveMap.remove(localMachine);
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
