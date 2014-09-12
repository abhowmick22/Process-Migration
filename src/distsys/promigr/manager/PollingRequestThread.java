/**
 * Thread spawned by the process manager in order to keep track of the nodes that are alive. 
 */

package distsys.promigr.manager;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentMap;

public class PollingRequestThread implements Runnable
{

	private Socket clientSocket = null;
	private ConcurrentMap<String, Boolean> machineAliveMap;
	private ConcurrentMap<String, TableEntry> pmTable;
	private int serverPort;
	
	/**
	 * Constructor that initializes the local machine alive map, server port number and process manager table.
	 * @param machineAliveMap The map of process ID and active machines.
	 * @param pmTable The Map of process ID and metadata like node name, process name etc.
	 * @param serverPort Server's port number.
	 */
	public PollingRequestThread(ConcurrentMap<String, Boolean> machineAliveMap, 
								ConcurrentMap<String, TableEntry> pmTable, 
								int serverPort) {
		this.machineAliveMap = machineAliveMap;
		this.serverPort = serverPort;
		this.pmTable = pmTable;
	}

	/**
	 * The run method executes for this class.
	 */
	@Override
	public void run() {
		String poll = "ping";
		String currMachine = null;
		while(true){
			try {
			    //poll every 2.5s
				Thread.sleep(2500);
				for(String localMachine : machineAliveMap.keySet()) {
					currMachine = localMachine;
					if(machineAliveMap.get(localMachine)){
						clientSocket = new Socket(localMachine, serverPort);						
						ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
						outStream.writeObject(poll);
						outStream.flush();
						outStream.close();
					}
				}
				
			} catch (IOException e) {
				// update machineAliveMap
				machineAliveMap.put(currMachine, false);
				for(String procId : pmTable.keySet()) {
                    if(pmTable.get(procId).getNodeName().equals(currMachine)) {
                        pmTable.get(procId).setStatus(false);
                    }
                }
				System.out.println(currMachine + " is down. Oh no.");
				System.out.print(">");
			} catch (InterruptedException e) {
				//ignore because this thread won't be interrupted
			}
		}
		
	}

}
