package distsys.promigr.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import distsys.promigr.process.MessageWrap;

public class ProcessManagerAssistant implements Runnable
{
    private ConcurrentMap<String, TableEntry> pmTable;
    private ConcurrentMap<String, Boolean> machineAliveMap;
    
    public ProcessManagerAssistant(ConcurrentMap<String, TableEntry> pmTable,
                                   ConcurrentMap<String, Boolean> machineAliveMap) 
    {
        this.pmTable = pmTable;
        this.machineAliveMap = machineAliveMap;
    }
    
    @Override
    public void run()
    {
        //receive the replies for "ps" command using a different port, 
        //assuming the queued replies from different machines don't overflow the buffer
       
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(50001);            
        }
        catch (IOException e1) {
            System.out.println("Some communication/socket exception occured.");
            return;
            //TODO: communicate this to ProcessManager
        }
        
        for(String machineName : this.machineAliveMap.keySet()) {
            try {
                Socket clientSocket = serverSocket.accept();
                InputStream inputStream = clientSocket.getInputStream();
                ObjectInputStream objectStream = new ObjectInputStream(inputStream);                
                MessageWrap message = (MessageWrap) objectStream.readObject();
                int command = message.getCommand();
                if(command != 5) {
                    //something went wrong. shouldn't happen
                    //TODO: handle this
                    return;
                }
                
                Map<String, Boolean> processMap = message.getProcStatus();
                for(String processId : processMap.keySet()) {
                    //update pmTable for reference in ProcessManager
                    this.pmTable.get(processId).setStatus(processMap.get(processId));
                } 
            }
            catch (IOException e) {
                //machine dead most likely or lost connectivity before polling could
                //update the machineAliveMap
                this.machineAliveMap.put(machineName, false);          
                //need to update the status of all processes running on this machine
                for(String procId : this.pmTable.keySet()) {
                    if(this.pmTable.get(procId).getNodeName().equals(machineName)) {
                        this.pmTable.get(procId).setStatus(false);
                    }
                }
                
            }
            catch (ClassNotFoundException e) {
                System.out.println("Message communication problem. Sorry.");
                //TODO: communicate this to ProcessManager
            }
                                   
        }
        try {
            serverSocket.close();
        }
        catch (IOException e) {
            System.out.println("Problem closing socket. Might make \"ps\" command useless.");
        }
    }
}
