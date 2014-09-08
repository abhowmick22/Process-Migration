package distsys.promigr.manager;

import java.lang.reflect.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
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
    	MessageWrap message = null;
    	int port = 50000;
    	InputStream inputStream = null;
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
            case 0 : {
            	try {
    				procId = message.getProcId();
    				dest = message.getDest();z
    				System.out.println("avl keys" + threadMap.keySet());
    				MigratableProcess process = this.threadMap.get(procId).getProcess();
    				process.suspend();
    				Socket clientSocket = new Socket(dest, this.serverPort);
    				//PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream());
    				//printWriter.write("1");
    				//printWriter.write(procId);
    				MessageWrap echoMsg = new MessageWrap();
    				echoMsg.setCommand(1);
    				echoMsg.setProcId(procId);
    				echoMsg.setMigratableProcess(process);
    				
    			    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
    			    
    			    Thread.sleep(1000);
    			    outStream.writeObject(echoMsg);
    		        outStream.close();
    		        clientSocket.close();
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
            case 1 : {
            	
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
            case 2 :
            	String procName;
            	//while(procName != null)
            	
            	
            	break;
            
            default : {
                
            }
        }
        
        
        
    }
}
