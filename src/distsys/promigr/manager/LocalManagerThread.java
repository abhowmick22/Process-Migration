package distsys.promigr.manager;

import java.lang.reflect.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
    
    protected LocalManagerThread(Socket clientSocket, ConcurrentMap<String, ThreadObject> threadMap, int serverPort) {
        this.clientSocket = clientSocket;
        this.threadMap = threadMap;
        this.serverPort = serverPort;
    }

    @Override
    public void run()
    {
    	ObjectInputStream in = null;
    	InputStream inputStream = null;
    	ObjectInputStream ackIn = null;
    	MessageWrap message = null;
    	String procId, dest = "", returnAddr = "";
    	int msg = -1;
    	boolean ackTrue = false;
    	
		try {
			inputStream = clientSocket.getInputStream();
			in = new ObjectInputStream(inputStream);
			//System.out.println(in.readLine());
			
			message = (MessageWrap) in.readObject();
			msg = message.getCommand();
			//System.out.println(msg);
		} catch (IOException e1) {
			try {
			    inputStream.close();
			    clientSocket.close();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        switch (msg){        
            case 0 : {						// Migrate process
            	try {
    				procId = message.getProcId();
    				dest = message.getDest();
    				returnAddr = message.getSourceAddr();
    				//System.out.println(returnAddr);
    				//System.out.println("avl keys" + threadMap.keySet());
    				MigratableProcess process = this.threadMap.get(procId).getProcess();
    				if(!(this.threadMap.get(procId).getThread().isAlive())) {
    				    //the thread is no longer active. hence the process terminated
    				    //TODO: ack? or just mention in report that there should be an ack but there isn't.
    				    //      hence user has to reply on ps to check if the process migrated
    				    //      also, we assume that polling is quick enough to assume that it has updated status of finished process
    				    return;
    				}
    				
    				Socket clientSocket = new Socket(dest, this.serverPort);
    				
    				process.suspend();
    				MessageWrap echoMsg = new MessageWrap();
    				echoMsg.setCommand(1);
    				echoMsg.setProcId(procId);
    				echoMsg.setMigratableProcess(process);
    				echoMsg.setSourceAddr(InetAddress.getLocalHost().getHostName());
    				//System.out.println(echoMsg);
    				
    			    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
    			    
    			    Thread.sleep(1000);  //TODO: remove this - think of something better to delay
    			    outStream.writeObject(echoMsg);
    		        outStream.close();
    		        clientSocket.close();
    		        
    		        // Wait for ack message
    		        //ServerSocket socket = new ServerSocket(this.serverPort + 1);
    		        //Socket ackSocket = socket.accept();
    				//ackIn = new ObjectInputStream(ackSocket.getInputStream());
    				
    				
						//MessageWrap ackMessage = (MessageWrap) ackIn.readObject();
						//System.out.println("ack message : " + ackMessage);
//						ackMessage.setSourceAddr(InetAddress.getLocalHost().getHostName());
//						ackIn.close();
//						ackSocket.close();
//						socket.close();
//						ackTrue = ackMessage.getAck();
//						System.out.println("ack received is " + ackTrue);
//						Socket returnSocket = new Socket(returnAddr, this.serverPort + 1);
//						ObjectOutputStream returnOutStream = new ObjectOutputStream(returnSocket.getOutputStream());
//						returnOutStream.writeObject(ackMessage);
//						returnOutStream.close();
//						returnSocket.close();

    				// if we receive  positive ack, then update the thread map
    			//if(ackTrue){
    				threadMap.remove(procId);
    				//System.out.println("removing keys" + threadMap.keySet());
    				//System.out.println(threadMap);
    			//}
    				
    			} catch (IOException e) {
    				//System.out.println("Cannot connect to "+ dest+". Please try again after making sure it is up.");
    			    //TODO: do nothing; could have acked back saying node not found
    			}
                catch (InterruptedException e) {
                    //do nothing
                    //TODO: anything?
                } 
            	break;
            }
            case 1 : {						// Create process
            	
            	MigratableProcess recdProcess;
            	Class<?> myClass;
            	String className;
     
            	 try {
    				procId = message.getProcId();
    				returnAddr = message.getSourceAddr();
            		recdProcess = (MigratableProcess) message.getProcess();
    				ThreadObject threadObject = new ThreadObject();
    				threadObject.setProcess(recdProcess);
    				Thread processThread = new Thread(recdProcess);				
    				threadObject.setThread(processThread);
    				
    				processThread.start();
    				
//    				ackTrue = processThread.isAlive();
//    				MessageWrap ackMessage = new MessageWrap();
//					ackMessage.setSourceAddr(InetAddress.getLocalHost().getHostName());
//					ackMessage.setCommand(4);
//					ackMessage.setAck(ackTrue);
	
//					Socket returnSocket = new Socket(returnAddr, this.serverPort + 1);
//					ObjectOutputStream returnOutStream = new ObjectOutputStream(returnSocket.getOutputStream());
//					returnOutStream.writeObject(ackMessage);
//					returnOutStream.close();
//					returnSocket.close();

					// if successfully created process, then update the thread map
					//if(ackTrue){
    				threadMap.put(procId, threadObject);
    				//System.out.println("added keys " + threadMap.keySet());
					//}
					
					processThread.join();
    				
    			} catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }   	
            	break;
            }
            case 2 :						// Return active list in response to a ps command
            	dest = message.getSourceAddr();
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
            	
				try {
					Socket clientSocket = new Socket(dest, this.serverPort + 1);
					ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
					MessageWrap psMsg = new MessageWrap();
					psMsg.setCommand(5);
					psMsg.setProcStatus(procStatus);
					outStream.writeObject(psMsg);
			        outStream.close();
			        clientSocket.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}       	
            	
            	break;
            
            default : {
                
            }
        }
        
        
        
    }
}
