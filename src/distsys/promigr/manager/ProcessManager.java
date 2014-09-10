package distsys.promigr.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.lang.ArrayUtils;







import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import distsys.promigr.process.MessageWrap;
import distsys.promigr.process.MigratableProcess;

public class ProcessManager<T>
{

	private int procCount;
	private ConcurrentMap<String, TableEntry> pmTable;
	private ConcurrentMap<String, Boolean> machineAliveMap;
	
    public static void main(String[] args) throws Exception {
        /*
        Thread serverThread = new Thread() {
            public void run() {
                try {
                    LocalManager.main(new String[2]);
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        */
    	System.out.println(InetAddress.getLocalHost().getHostName());
        System.out.println("-----Welcome-----");
        System.out.println("1. Enter ");    //TODO: new process command
        System.out.println("-----Welcome-----");    //TODO: migrate process command
        System.out.println("-----Welcome-----");    //TODO: process list
        System.out.println("4. Enter \"help\" for this menu.");    //help
        
        ProcessManager<MigratableProcess> manager = new ProcessManager<MigratableProcess>();
        manager.procCount = 0;
        manager.pmTable = new ConcurrentHashMap<String, TableEntry>();
        manager.machineAliveMap = new ConcurrentHashMap<String, Boolean>();
        //TODO: create new thread to poll the machines that will be running processes later
        
        while(true) {
            
            String command = "";
            
            System.out.print(">");
            System.out.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            
            try {
                command = br.readLine();
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            if(command.length()==0) {
                System.out.println("Please enter a command");
            }
            
            String[] commandList = command.split(" ");
            
            if(commandList[0].equals("create")) {
                //create a new process
                String processName = commandList[1];
                String procId = "proc" + manager.procCount++;    //TODO: change this
                MigratableProcess inst = manager.init(commandList);
                if(inst.equals(null)) {
                    System.out.println("Could not create process. May not be implementing MigratableProcess");
                    continue;
                }
                Socket clientSocket = new Socket(commandList[2], 50000);
                // TODO : Add functionality of user input for port

                MessageWrap echoMsg = new MessageWrap();
                echoMsg.setCommand(1);
                echoMsg.setProcId(procId);
                echoMsg.setMigratableProcess(inst);
                
                ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                
                outStream.writeObject(echoMsg);
                outStream.close();
                clientSocket.close();
                
                //wait till ack is received for this
                ServerSocket serverSocket = new ServerSocket(50001); 
                clientSocket = serverSocket.accept();
                //TODO: add timeout
                ObjectInputStream objectStream = new ObjectInputStream(clientSocket.getInputStream());
                MessageWrap messageWrap = (MessageWrap) objectStream.readObject();
                if(messageWrap.getCommand() != 4) {
                    //TODO: something went wrong. this shouldn't happen. fix it
                    continue;
                } else if(!messageWrap.getAck()) {
                    //ack failed
                    System.out.println("Failed to create process. Destination may be down.");
                    continue;
                }
                
                //got acknowledgement of process creation; need to update tables
                System.out.println("Process successfully created.");                
                manager.machineAliveMap.put(commandList[2], true);
                TableEntry entry = new TableEntry();
                entry.setProcessName(processName);
                entry.setProcId(procId);
                entry.setNodeName(commandList[2]);             
                entry.setStatus(true);
                entry.setArguments(Arrays.copyOfRange(commandList, 2, commandList.length - 1));     
                manager.pmTable.put(procId, entry);
                
                
            } else if(commandList[0].equals("migrate")) {
                //create a new process
                String procId = commandList[1];
                //String procId = "proc" + manager.procCount;    //TODO: change this
                // MigratableProcess inst = manager.init(commandList);
                // access MigratableProcess inst from table with procId
                //System.out.println(procId);
                if(!manager.pmTable.get(procId).getStatus()) {
                    //process no longer running
                    System.out.println("This process is no longer running.");
                    continue;
                }
                String curr = manager.pmTable.get(procId).getNodeName();
                if(!manager.machineAliveMap.get(curr)) {
                    //node not functioning
                    //this should not happen because polling made sure that the process status was marked false
                    //TODO: do the above; or just mark the node not running and then this if should come into picture
                    System.out.println("The source node of the process is no longer functioning");
                    continue;
                }
                
                Socket clientSocket = new Socket(curr, 50000);
                
                MessageWrap echoMsg = new MessageWrap();
                echoMsg.setCommand(0);
                echoMsg.setProcId(procId);
                echoMsg.setDest(commandList[2]);
                
                ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                
                outStream.writeObject(echoMsg);
                outStream.close();
                clientSocket.close();
                
                //wait till ack is received for this
                ServerSocket serverSocket = new ServerSocket(50001); 
                clientSocket = serverSocket.accept();
                //TODO: add timeout (AT ALL SERVERSOCKETS)                
                ObjectInputStream objectStream = new ObjectInputStream(clientSocket.getInputStream());
                MessageWrap messageWrap = (MessageWrap) objectStream.readObject();
                if(messageWrap.getCommand() != 4) {
                    //TODO: something went wrong. this shouldn't happen. fix it
                    continue;
                } else if(!messageWrap.getAck()) {
                    //ack failed
                    System.out.println("Failed to migrate process. Source or destination may be down.");
                    continue;
                }
                
                //got acknowledgement of migration; need to update tables
                // TODO: Update: pmTable and machineAliveMap 
                System.out.println("Process successfully migrated.");                
                manager.machineAliveMap.put(commandList[2], true);
                manager.pmTable.get(procId).setNodeName(commandList[2]);                
                
            } else if(commandList[0].equals("ps")) {
                //create the command to be sent to every machine
                MessageWrap echoMsg = new MessageWrap();
                echoMsg.setCommand(2);
                echoMsg.setDest(InetAddress.getLocalHost().getHostName());
                
                //get total number of alive machines
                int aliveCount = 0;
                for(Boolean isAlive : manager.machineAliveMap.values()) {
                    if(isAlive) {
                        aliveCount++;
                    }
                }
                
                //new Thread to process the reply from every machine                
                ProcessManagerAssistant pmAssistant = new ProcessManagerAssistant(manager.pmTable, aliveCount);
                Thread pmaThread = new Thread(pmAssistant);
                pmaThread.start();
                //send process list requests to every node
                for(String machine : manager.machineAliveMap.keySet()) {
                    Socket clientSocket = new Socket(machine, 50000);
                    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    outStream.writeObject(echoMsg);
                    outStream.close();
                    clientSocket.close();                    
                }                
                //wait till all requests have been responded to
                pmaThread.join();
                
                //pmTable updated; now print the details of all processes
                System.out.println("List of active processes:");
                System.out.println("Process ID\t NodeName\t ProcessName\t Arguments");
                for(String procId : manager.pmTable.keySet()) {
                    TableEntry tableEntry = manager.pmTable.get(procId);
                    if(!tableEntry.getStatus()) {
                        //process is alive
                        System.out.println(tableEntry.getProcId()+"\t "+
                                            tableEntry.getProcessName()+"\t "+
                                            tableEntry.getNodeName()+"\t "+
                                            Arrays.toString(tableEntry.getArguments()));
                        
                    }
                }
                
            }
            
        }
    }
    
    //TODO: remove T and make it MigratableProcess
    public T init(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        
        Class<?> clazz = Class.forName(args[1]);
        
        Class<?>[] classArray = new Class[args.length-2];
        String[] argsArray = new String[args.length-2];
        //TODO: arraycopy
        for(int i=2;i<args.length;i++) {
            classArray[i-2] = String.class;
            argsArray[i-2] = args[i];
        }
        Constructor<?> constructor = clazz.getConstructor(classArray);
        T inst = (T) constructor.newInstance(argsArray);
        
        if(!(inst instanceof MigratableProcess)) {
            System.out.println("ERROR: Please pass instance of Class implementing MigratableProcess.");
            return null;
        }
        
        return inst;
     }
    

}