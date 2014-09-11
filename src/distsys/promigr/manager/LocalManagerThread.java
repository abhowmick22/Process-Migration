package distsys.promigr.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import distsys.promigr.process.MessageWrap;
import distsys.promigr.process.MigratableProcess;

public class LocalManagerThread implements Runnable
{
	private ConcurrentMap<String, ThreadObject> threadMap;
    private Socket clientSocket;
    private int serverPort;
    
    /**
     * Constructor which takes in the socket connection to master, map of 
     * thread running on system and port number on which to communicate with TODO
     */
    protected LocalManagerThread(Socket clientSocket, 
    			ConcurrentMap<String, ThreadObject> threadMap, int serverPort)
    {
        this.clientSocket = clientSocket;
        this.threadMap = threadMap;
        this.serverPort = serverPort;
    }

    /**
     * The run method executes the code for this class.
     */
    @Override
    public void run()
    {
    	ObjectInputStream in = null;
    	InputStream inputStream = null;
    	MessageWrap message = null;
    	String procId, dest = "";
    	int msg = -1;
    	
    	/*
    	 * Reads the message from master which contains instruction and identifies
    	 * type of request.
    	 */
    	try {
			inputStream = clientSocket.getInputStream();
			in = new ObjectInputStream(inputStream);
			message = (MessageWrap) in.readObject();
			msg = message.getCommand();

		} catch (IOException e1) {
			try {
			    inputStream.close();
			    clientSocket.close();
            }
            catch (IOException e) {
                //ignore
            }
		} catch (ClassNotFoundException e) {
			//ignore
		}
    	
    	//Take action according to the type of command received
        switch (msg){                
        	 
            case 0 : {	//MIGRATE: Migrates the indicated process to indicated node	
            	try {
    				procId = message.getProcId();
    				dest = message.getDest();
    				MigratableProcess process = this.threadMap.get(procId).getProcess();
    				if(!(this.threadMap.get(procId).getThread().isAlive())) 
    				{
    				    return;		// do not do anything if the thread is dead
    				}
    				
    				Socket clientSocket = new Socket(dest, this.serverPort);	
    				process.suspend();
    				
    				// Wrap the serialized process in a message
    				MessageWrap echoMsg = new MessageWrap();
    				echoMsg.setCommand(1);
    				echoMsg.setProcId(procId);
    				echoMsg.setMigratableProcess(process);
    				echoMsg.setSourceAddr(InetAddress.getLocalHost().getHostName());
    				
    				//wait for the process to finish before serializing
    				//assume the thread joins within this time
    				this.threadMap.get(procId).getThread().join(1000);  
    				
    				// Write the serialized object
    			    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());

    			    outStream.writeObject(echoMsg);
    			    outStream.flush();
    		        outStream.close();
    		        clientSocket.close();

    		        // update the thread map with the new location of migrated process
    				threadMap.remove(procId);
    				
    			} catch (IOException e) {
    				//ignore
    			}
                catch (InterruptedException e) {
                    //do nothing
                } 
            	break;
            }
            
            case 1 : { //CREATE: Launch a new process, as indicated by master on this node						
            	
            	MigratableProcess recdProcess;
            	try {
            		
    				procId = message.getProcId();
    				recdProcess = (MigratableProcess) message.getProcess();
    				ThreadObject threadObject = new ThreadObject();
    				threadObject.setProcess(recdProcess);
    				Thread processThread = new Thread(recdProcess);				
    				threadObject.setThread(processThread);
    				
    				processThread.start();
    				
    				// update the thread map if successfully launched process
    				threadMap.put(procId, threadObject);

    				// wait for launched process
					processThread.join();
    				
    			} catch (InterruptedException e) {
                    // ignore
                }   	
            	break;
            }
            
            /*
             * Returns the list of active processes to ProcessManagerAssistant,
             * in order to service the user command 'ps'
             */
            case 2 : {
            	
            	// address of master to which active list should be sent
            	dest = message.getSourceAddr();
            	
            	// Check status of each thread in the thread map and update if necessary
            	Set<String> processIds = threadMap.keySet();
            	Map<String, Boolean> procStatus = new HashMap<String, Boolean>();
            	String s;
            	Iterator<String> iter = processIds.iterator();
            	while (iter.hasNext()) {
            		ThreadObject t = threadMap.get(s = iter.next());
            	    if (t.getThread().isAlive()) {
            	        procStatus.put(s, true);
            	    }
            	    else{
            	    	procStatus.put(s, false);
            	    	iter.remove();
            	    }
            	}
            	
            	// Send updated thread map information through a message 
            	// back to master, that is listening on port (serverPort+1)
				try {
					Socket clientSocket = new Socket(dest, this.serverPort + 1);
					ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
					MessageWrap psMsg = new MessageWrap();
					psMsg.setCommand(5);
					psMsg.setProcStatus(procStatus);
					outStream.writeObject(psMsg);
					outStream.flush();
			        outStream.close();
			        clientSocket.close();
				} catch (UnknownHostException e) {
					// ignore
				} catch (IOException e) {
					//ignore
				}       	
            	
            	break;
            }
            default : {
                //should not occur
            }
        }        
    }
}
