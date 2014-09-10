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
import java.net.UnknownHostException;
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
	
    public static void main(String[] args) {
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
                String processName = commandList[2];
                String procId = "proc" + manager.procCount++;    //TODO: change this
                MigratableProcess inst = manager.init(commandList);
                if(inst == null) {
                    System.out.println("Could not create process. May not be implementing MigratableProcess");
                    continue;
                }
                
                try {
                    Socket clientSocket = new Socket(commandList[1], 50000);
                    MessageWrap echoMsg = new MessageWrap();
                    echoMsg.setCommand(1);
                    echoMsg.setProcId(procId);
                    echoMsg.setMigratableProcess(inst);
                    
                    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    
                    outStream.writeObject(echoMsg);
                    outStream.close();
                    clientSocket.close();
                    
                    //wait till ack is received for this
//                    ServerSocket serverSocket = new ServerSocket(50001);
//                    clientSocket.setSoTimeout(3000); //TODO: test timeout                      
//                    clientSocket = serverSocket.accept();
//                    
//                    ObjectInputStream objectStream = new ObjectInputStream(clientSocket.getInputStream());
//                    MessageWrap messageWrap = (MessageWrap) objectStream.readObject();
//                    if(messageWrap.getCommand() != 4) {
//                        //TODO: something went wrong. this shouldn't happen. fix it
//                        continue;
//                    } else if(!messageWrap.getAck()) {
//                        //ack failed
//                        System.out.println("Failed to create process. Destination may be down.");
//                        continue;
//                    }
//                    
//                    //got acknowledgement of process creation; need to update tables
//                    System.out.println("Process successfully created.");
                    
                    manager.machineAliveMap.put(commandList[1], true);
                    TableEntry entry = new TableEntry();
                    entry.setProcessName(processName);
                    entry.setProcId(procId);
                    entry.setNodeName(commandList[1]);             
                    entry.setStatus(true);
                    entry.setArguments(Arrays.copyOfRange(commandList, 3, commandList.length - 1));     
                    manager.pmTable.put(procId, entry);
                }
                catch (UnknownHostException e) {
                    System.out.println("Can't create process. Unknown host.");                    
                }
                catch (IOException e) {
                    System.out.println("Can't create process. Some IO problem.");
                }
                // TODO : Add functionality of user input for port
                
                
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
                
                try {
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
//                    ServerSocket serverSocket = new ServerSocket(50001); 
//                    clientSocket.setSoTimeout(3000); //TODO: test timeout  
//                    clientSocket = serverSocket.accept();
//                    ObjectInputStream objectStream = new ObjectInputStream(clientSocket.getInputStream());
//                    MessageWrap messageWrap = (MessageWrap) objectStream.readObject();
//                    if(messageWrap.getCommand() != 4) {
//                        //TODO: something went wrong. this shouldn't happen. fix it
//                        continue;
//                    } else if(!messageWrap.getAck()) {
//                        //ack failed
//                        System.out.println("Failed to migrate process. Source or destination may be down.");
//                        continue;
//                    }
//                    
//                    //got acknowledgement of migration; now update tables
//                    System.out.println("Process successfully migrated.");
                    
                    manager.machineAliveMap.put(commandList[2], true);
                    manager.pmTable.get(procId).setNodeName(commandList[2]); 
                }
                catch (UnknownHostException e) {
                    if(manager.machineAliveMap.containsKey(commandList[2])) {
                        manager.machineAliveMap.put(commandList[2], false);
                    }
                    System.out.println("Can't migrate process. Unknown host or host not alive.");
                }
                catch (IOException e) {
                    System.out.println("Can't migrate process. Some IO problem.");
                }
                // TODO : Add functionality of user input for port
                                                               
            } else if(commandList[0].equals("ps")) {
                //create the command to be sent to every machine
                MessageWrap echoMsg = new MessageWrap();
                //get total number of alive machines
                int aliveCount = 0;                
                for(Boolean isAlive : manager.machineAliveMap.values()) {
                    if(isAlive) {
                        aliveCount++;
                    }
                }
                
                echoMsg.setCommand(2);
                try {
                    echoMsg.setSourceAddr(InetAddress.getLocalHost().getHostName());
                }
                catch (UnknownHostException e1) {
                    System.out.println("Cannot get local host information.");
                    continue;
                }
                //new Thread to process the reply from every machine
                ProcessManagerAssistant pmAssistant = new ProcessManagerAssistant(manager.pmTable, manager.machineAliveMap);
                Thread pmaThread = new Thread(pmAssistant);
                pmaThread.start();
                                
                for(String machine : manager.machineAliveMap.keySet()) {                 
                    //send process list requests to every node                                            
                    try {
                        Socket clientSocket = new Socket(machine, 50000);
                        ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                        outStream.writeObject(echoMsg);
                        outStream.close();
                        clientSocket.close();   
                    }
                    catch (UnknownHostException e) {
                        //shouldn't occur
                        System.out.println("IP of "+ machine+" can't be determined.");                            
                    }
                    catch (IOException e) {
                        //machine dead most likely or lost connectivity before polling could
                        //update the machineAliveMap
                        manager.machineAliveMap.put(machine, false);          
                        //need to update the status of all processes running on this machine
                        for(String procId : manager.pmTable.keySet()) {
                            if(manager.pmTable.get(procId).getNodeName().equals(machine)) {
                                manager.pmTable.get(procId).setStatus(false);
                            }
                        }
                              
                    }                                                                                 
                }
                
                //wait till all requests have been responded to                
                try {
                    pmaThread.join();
                }
                catch (InterruptedException e) {
                    System.out.println("Thread interrupted.");
                    //TODO: need anything else to be done?
                }
                
                //pmTable updated; now print the details of all processes
                System.out.println("List of active processes:");
                System.out.println("Process ID\t NodeName\t ProcessName\t Arguments");
                for(String procId : manager.pmTable.keySet()) {
                    TableEntry tableEntry = manager.pmTable.get(procId);
                    if(tableEntry.getStatus()) {
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
    public T init(String[] args) {
        
        Class<?> clazz;
        try {
            clazz = Class.forName(args[2]);
        }
        catch (ClassNotFoundException e) {
            System.out.println("Class not found.");
            return null;
        }
        
        //Class<?>[] classArray = new Class[args.length-3];
        Class<?>[] classArray = new Class[] {String[].class};
        String[] argsArray = new String[args.length - 3];
        //argsArray = Arrays.copyOfRange(args, 3, args.length - 1);
        //create node distsys.promigr.manager.GrepProcess query a b
        //TODO: arraycopy
        for(int i=3;i<args.length;i++) {
            //classArray[i-3] = String.class;
            argsArray[i-3] = args[i];
        }
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(classArray);
        }
        catch (SecurityException e) {
            System.out.println("Security concerns.");
            return null;
        }
        catch (NoSuchMethodException e) {
            System.out.println("No such constructor found.");
            return null;
        }
        T inst = null;
        try {
            inst = (T) constructor.newInstance((Object) argsArray);
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.out.println("Illegal args.");
            return null;
        }
        catch (InstantiationException e) {
            System.out.println("Can't instantiate.");
            return null;
        }
        catch (IllegalAccessException e) {
            System.out.println("Illegal access.");
            return null;
        }
        catch (InvocationTargetException e) {
            System.out.println("Invocation target exception.");
            return null;
        }
        
        if(!(inst instanceof MigratableProcess)) {
            System.out.println("ERROR: Please pass instance of Class implementing MigratableProcess.");
            return null;
        }
        
        return inst;
     }
    

}