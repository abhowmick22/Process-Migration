/**
 * Acts as the master. It interacts with the user in order to take ‘create’, ‘migrate’ or ‘ps’ 
 * commands. For a create command, it creates the process and sends it to the node mentioned by 
 * the user for execution. In order to migrate a process, it instructs the corresponding node to 
 * migrate to the destination. Finally the ‘ps’ command lists all the processes in the system. 
 */

package distsys.promigr.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import distsys.promigr.process.MessageWrap;
import distsys.promigr.process.MigratableProcess;

public class ProcessManager
{

	private int procCount;
	private ConcurrentMap<String, TableEntry> pmTable;
	private ConcurrentMap<String, Boolean> machineAliveMap;
	
    public static void main(String[] args) {
        
    	ProcessManager manager = new ProcessManager();
        manager.procCount = 0;
        manager.pmTable = new ConcurrentHashMap<String, TableEntry>();
        manager.machineAliveMap = new ConcurrentHashMap<String, Boolean>();
        
    	PollingRequestThread prThread = new PollingRequestThread(manager.machineAliveMap, manager.pmTable);
    	Thread polling = new Thread(prThread);
      	polling.start();
    	
        System.out.println("-----Welcome-----");
        System.out.println("-----Following are the commands that are available to you-----");
        System.out.println("1. Create process: \"create node-name process-name argument-list...\" - creates the process on the machine node-name");  
        System.out.println("2. Migrate process: \"migrate process-id destination-node\" - migrates process-id to destination-node. The process ID will be generated and given to you.");    
        System.out.println("3. Process list: \"ps\" - Will provide list of processes running on different nodes.");    
        System.out.println("4. Help: \"help\" - Gives this menu.");    
        System.out.println("5. Exit: \"exit\" - Exits Process Manager, does not close Local Managers.");
        System.out.println("Available test cases for the \"create\" command (don't forget node name):");
        System.out.println("distsys.promigr.test.GrepProcess <queryString> <inputFile> <outputFile>");
        System.out.println("distsys.promigr.test.MergeFiles <inputFile1> <inputFile2> <inputFile3> <outputFile>");
        System.out.println("distsys.promigr.test.WebPageCopier <website> <outputFile>");
   
        while(true) {          
            System.out.print(">");            
            String command = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));            
            try {
                command = br.readLine();
            }
            catch (IOException e) {
                System.out.println("Input error.");
                continue;
            }
            
            if(command.length()==0) {
                System.out.println("Please enter a command");
            }
            
            String[] commandList = command.split(" ");
            
            if(commandList[0].equals("create")) {   //CREATE
                if(commandList.length < 3) {
                    System.out.println("Please refer \"help\" for correct command options.");
                    continue;
                }
                //create a new process
                String processName = commandList[2];
                String procId = "proc" + manager.procCount;    
                MigratableProcess inst = manager.init(commandList);
                if(inst == null) {
                    System.out.println("Could not create process.");
                    continue;
                }
                
                try {
                    Socket clientSocket = new Socket(commandList[1], 50000);
                    //wrap the object with procId and metadata
                    MessageWrap echoMsg = new MessageWrap();
                    echoMsg.setCommand(1);
                    echoMsg.setProcId(procId);
                    echoMsg.setMigratableProcess(inst);
                    //write the wrap to socket
                    ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                    outStream.writeObject(echoMsg);
                    outStream.flush();
                    outStream.close();
                    clientSocket.close();                    
                    
                    //update active machines table, and the process manager table to keep
                    //track of this newly created process
                    manager.machineAliveMap.put(commandList[1], true);
                    TableEntry entry = new TableEntry();
                    entry.setProcessName(processName);
                    entry.setProcId(procId);
                    entry.setNodeName(commandList[1]);             
                    entry.setStatus(true);
                    entry.setArguments(Arrays.copyOfRange(commandList, 3, commandList.length));     
                    manager.pmTable.put(procId, entry);
                    
                    System.out.println("Please refer to this process as: " + procId);
                    manager.procCount++;
                }
                catch (IOException e) {
                    System.out.println("Can't create process because can't connect to host: " + commandList[1]);                    
                }
                
            } else if(commandList[0].equals("migrate")) {   //MIGRATE
                if(commandList.length != 3) {
                    System.out.println("Please refer \"help\" for correct command options.");
                    continue;
                }
                
                String procId = commandList[1];
                String dest = commandList[2];
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
                    
                    //update the active process and machine lists
                    manager.machineAliveMap.put(commandList[2], true);
                    manager.pmTable.get(procId).setNodeName(commandList[2]); 
                    System.out.println("Migrate complete. Please check \"ps\" to verify if it was successful.");
                }
                catch (UnknownHostException e) {
                    System.out.println("Can't migrate process. Can't resolve IP address.");
                }
                catch (IOException e) {
                    System.out.println("Can't migrate process. Socket connection problem.");
                }
                                                               
            } else if(commandList[0].equals("ps")) {
                
                System.out.println("Gathering information from all active hosts. Please wait...");
                
                //create the command to be sent to every machine asking for process information
                //on each machine
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
                        System.out.println("IP of "+ machine+" can't be determined.");    
                        continue;   //continue with other nodes
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
                        continue;   //continue with other nodes
                    }                        
                }
                
                //wait till all requests have been responded to.             
                try {
                    pmaThread.join(10000);
                }
                catch (InterruptedException e) {
                    System.out.println("Thread interrupted or timed out. Providing the latest cached process list below.");
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
                System.out.println("1. Create process: \"create node-name process-name argument-list...\" - creates the process on the node-name");    
                System.out.println("2. Migrate process: \"migrate process-id destination-node\" - migrates process-id to destination-node. The process ID will be generated and given to you.");
                System.out.println("3. Process list: \"ps\" - Will provide list of processes running on different nodes.");  
                System.out.println("4. Help: \"help\" - Gives this menu.");  
                System.out.println("5. Exit: \"exit\" - Exits Process Manager, does not close Local Managers."); 
                
            } else if(commandList[0].equals("exit")) {
                System.out.println("Alright then, goodbye!");
                System.exit(0);
            } else {
                System.out.println("Command not found. Enter \"help\" to get list of commands.");
            }
            
        }
    }
    
    /**
     * Creates an object for the class that the user provides, which acts as the user process.
     * @param args The command list containing the entire command with list of arguments for the new process.
     * @return Instance of the new process (object of user defined class).
     */
    public MigratableProcess init(String[] args) {
        
        Class<?> clazz;
        try {
            //determine the class
            clazz = Class.forName(args[2]);
        }
        catch (ClassNotFoundException e) {
            System.out.println("Class not found.");
            return null;
        }
        
        Class<?>[] classArray = new Class[] {String[].class};
        String[] argsArray = new String[args.length - 3];
        argsArray = Arrays.copyOfRange(args, 3, args.length);        
        Constructor<?> constructor;
        try {
            //invoke constructor
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
        MigratableProcess inst = null;
        try {
            //instantiate user class
            inst = (MigratableProcess) constructor.newInstance((Object) argsArray);
        }
        catch (IllegalArgumentException e) {            
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