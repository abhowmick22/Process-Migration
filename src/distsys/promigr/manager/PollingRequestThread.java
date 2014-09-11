package distsys.promigr.manager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

public class PollingRequestThread implements Runnable{

	private Socket clientSocket = null;
	private ConcurrentMap<String, Boolean> machineAliveMap;
	private ConcurrentMap<String, TableEntry> pmTable;
	private int serverPort;
	
	public PollingRequestThread(ConcurrentMap<String, Boolean> machineAliveMap, 
								ConcurrentMap<String, TableEntry> pmTable, int serverPort) {
		// TODO Auto-generated constructor stub
		this.machineAliveMap = machineAliveMap;
		this.serverPort = serverPort;
		this.pmTable = pmTable;
	}

	@Override
	public void run() {
		String poll = "ping";
		String currMachine = null;
		while(true){
			try {
				Thread.sleep(5000);
				
				// start polling
				//System.out.println("polling loop");
				
				for(String localMachine : machineAliveMap.keySet()) {
					currMachine = localMachine;
					if(machineAliveMap.get(localMachine)){
						clientSocket = new Socket(localMachine, serverPort);
						//System.out.println("polling " + localMachine);
						ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
						outStream.writeObject(poll);
					}
				}
				
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				// update machineAliveMap
				machineAliveMap.put(currMachine, false);
				for(String procId : pmTable.keySet()) {
                    if(pmTable.get(procId).getNodeName().equals(currMachine)) {
                        pmTable.get(procId).setStatus(false);
                    }
                }
				System.out.println(currMachine + " is down. Oh no.");
				//e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
