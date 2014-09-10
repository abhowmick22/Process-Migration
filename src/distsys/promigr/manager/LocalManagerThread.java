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

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

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
    	InputStream ackInputStream = null;
    	MessageWrap message = null;
    	String procId, dest;
    	int msg = -1;
    	
		try {
			inputStream = clientSocket.getInputStream();
			in = new ObjectInputStream(inputStream);
			//System.out.println(in.readLine());
			
			message = (MessageWrap) in.readObject();
			msg = message.getCommand();
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
    				String returnAddr = message.getSourceAddr();
    				System.out.println("avl keys" + threadMap.keySet());
    				MigratableProcess process = this.threadMap.get(procId).getProcess();
    				process.suspend();
    				Socket clientSocket = new Socket(dest, this.serverPort);
    				ServerSocket socket = new ServerSocket(this.serverPort);
    				MessageWrap echoMsg = new MessageWrap();
    				echoMsg.setCommand(1);
    				echoMsg.setProcId(procId);
    				echoMsg.setMigratableProcess(process);
    				echoMsg.setSourceAddr(InetAddress.getLocalHost().getHostName());
    				
    			    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
    			    
    			    Thread.sleep(1000);  //TODO: remove this - think of something better to delay
    			    outStream.writeObject(echoMsg);
    		        outStream.close();
    		        clientSocket.close();
    		        
    		        // Wait for ack message
    		        Socket ackSocket = socket.accept();
    		        ackInputStream = ackSocket.getInputStream();
    				ackIn = new ObjectInputStream(inputStream);
    				try {
						MessageWrap ackMessage = (MessageWrap) ackIn.readObject();
						ackSocket.close();
						Socket returnSocket = new Socket
						// Now forward the ack message to 
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace()
					}
    				
    				
    				socket.close();
    				
    				// if we receive  positive ack, then update the thread map
    				threadMap.remove(procId);
    				System.out.println("removing keys" + threadMap.keySet());
    				System.out.println(threadMap);
    				
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	break;
            }
            case 1 : {						// Create process
            	
            	MigratableProcess recdProcess;
            	Class<?> myClass;
            	String className;
     
            	 try {
    				procId = message.getProcId();
            		recdProcess = (MigratableProcess) message.getProcess();
    				ThreadObject threadObject = new ThreadObject();
    				threadObject.setProcess(recdProcess);
    				Thread processThread = new Thread(recdProcess);
    				
    				threadObject.setThread(processThread);
    				threadMap.put(procId, threadObject);
    				System.out.println("added keys " + threadMap.keySet());
    				processThread.start();
    				processThread.join();
    				
    			} catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }   	
            	break;
            }
            case 2 :						// Return active list in response to a ps command
            	dest = message.getDest();
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
