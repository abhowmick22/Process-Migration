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
        
    	ProcessManager<MigratableProcess> manager = new ProcessManager<MigratableProcess>();
        manager.procCount = 0;
        manager.pmTable = new ConcurrentHashMap<String, TableEntry>();
        manager.machineAliveMap = new ConcurrentHashMap<String, Boolean>();
        //TODO: create new thread to poll the machines that will be running processes later
    	
    	PollingRequestThread prThread = new PollingRequestThread(manager.machineAliveMap, manager.pmTable, 50002);
    	Thread polling = new Thread(prThread);
      	polling.start();
    	
        System.out.println("-----Welcome-----");
        System.out.println("-----Following are the commands that are available to you-----");
        System.out.println("1. Create process: \"create node-name process-name argument-list...\" - creates the process on the machine node-name");    //TODO: new process command
        System.out.println("2. Migrate process: \"migrate process-id destination-node\" - The process ID will be generated and given to you.");    //TODO: migrate process command
        System.out.println("3. Process list: \"ps\" - Will provide list of processes running on different nodes.");    //TODO: process list
        System.out.println("4. Help: \"help\" - Gives this menu.");    //help
        System.out.println("5. Exit: \"exit\" - Exits Process Manager, does not close Local Managers.");
        System.out.println("Available test cases for the \"create\" command:");
        System.out.println("distsys.promigr.test.GrepProcess <queryString> <inputFile> <outputFile>");
        System.out.println("distsys.promigr.test.MergeFiles <inputFile1> <inputFile2> <inputFile3> <outputFile>");
        System.out.println("distsys.promigr.test.WebPageCopier <website> <outputFile>");
   
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
                if(commandList.length < 3) {
                    System.out.println("Please refer \"help\" for correct command options.");
                    continue;
                }
                //create a new process
                String processName = commandList[2];
                String procId = "proc" + manager.procCount;    //TODO: change this
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
                    outStream.flush();
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
                    
                    System.out.println("Please refer to this process as: " + procId);
                    manager.procCount++;
                }
                catch (IOException e) {
                    System.out.println("Can't create process because can't connect to host:" + commandList[1]);                    
                }
                // TODO : Add functionality of user input for port
                
                
            } else if(commandList[0].equals("migrate")) {
                if(commandList.length != 3) {
                    System.out.println("Please refer \"help\" for correct command options.");
                    continue;
                }
                //create a new process
                String procId = commandList[1];
                String dest = commandList[2];
                //String procId = "proc" + manager.procCount;    //TODO: change this
                // MigratableProcess inst = manager.init(commandList);
                // access MigratableProcess inst from table with procId
                //System.out.println(procId);
                if(!manager.machineAliveMap.containsKey(dest)) {
                    System.out.println("The destination doesn't seem to be on our records. Make sure to check process migration through \"ps\""); 
                }
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
                
                //check if the destination to which the user is migrating the process is active.
                //if not, don't migrate the process
                Socket clientSocket;
                try {
                    clientSocket = new Socket(commandList[2], 50000);
                    clientSocket.close();
                }
                catch (UnknownHostException e1) {
                    System.out.println("Can't resolve IP of destination you are trying to migrate to.");
                    continue;
                }
                catch (IOException e1) {
                    System.out.println("Seems the destination to which you are migrating is down.");
                    continue;
                }
                
                //migrate the process
                try {
                    clientSocket = new Socket(curr, 50000);
                    MessageWrap echoMsg = new MessageWrap();
                    echoMsg.setCommand(0);
                    echoMsg.setProcId(procId);
                    echoMsg.setDest(commandList[2]);
                    
                    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    
                    outStream.writeObject(echoMsg);
                    outStream.flush();
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
                    System.out.println("Can't migrate process. Can't resolve IP address.");
                }
                catch (IOException e) {
                    System.out.println("Can't migrate process. Socket connection problem.");
                }
                // TODO : Add functionality of user input for port
                                                               
            } else if(commandList[0].equals("ps")) {
                
                System.out.println("Gathering information from all active hosts. Please wait...");
                
                //create the command to be sent to every machine
                MessageWrap echoMsg = new MessageWrap();
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
                        outStream.flush();
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
                
                //wait till all requests have been responded to.             
                try {
                    pmaThread.join(10000);
                }
                catch (InterruptedException e) {
                    System.out.println("Thread interrupted or timed out. Providing the cached process list below.");
                    //TODO: need anything else to be done?
                }
                
                //pmTable updated; now print the details of all processes
                System.out.println("List of active processes:");
                System.out.println("Process ID\t NodeName\t ProcessName\t Arguments");
                for(String procId : manager.pmTable.keySet()) {
                    TableEntry tableEntry = manager.pmTable.get(procId);
                    if(tableEntry.getStatus()) {
                        //process is alive
                        System.out.println(tableEntry.getProcId()+" |\t "+
                                            tableEntry.getNodeName()+" |\t "+
                                            tableEntry.getProcessName()+" |\t "+
                                            Arrays.toString(tableEntry.getArguments()));
                        
                    }
                }                                
            } else if(commandList[0].equals("help")) {
                System.out.println("-----Following are the commands that are available to you-----");
                System.out.println("1. Create process: \"create node-name process-name argument-list...\" - creates the process on the node-name");    //TODO: new process command
                System.out.println("2. Migrate process: \"migrate process-id destination-node\" - The process ID will be generated and given to you.");    //TODO: migrate process command
                System.out.println("3. Process list: \"ps\" - Will provide list of processes running on different nodes.");    //TODO: process list
                System.out.println("4. Help: \"help\" - Gives this menu.");    //help
                System.out.println("5. Exit: \"exit\" - Exits Process Manager, does not close Local Managers."); 
                
            } else if(commandList[0].equals("exit")) {
                System.out.println("Alright then, goodbye!");
                return;
            } else {
                System.out.println("Command not found. Enter \"help\" to get list of commands.");
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