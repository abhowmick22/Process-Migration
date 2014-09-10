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
    //private ConcurrentMap<String, Boolean> machineAliveMap;
    private int expectedReplies;
    
    public ProcessManagerAssistant(ConcurrentMap<String, TableEntry> pmTable,
      //                             ConcurrentMap<String, Boolean> machineAliveMap, 
                                   int expectedReplies) {
        this.pmTable = pmTable;
        //this.machineAliveMap = machineAliveMap;
        this.expectedReplies = expectedReplies;
    }
    
    @Override
    public void run()
    {
        //receive the replies for "ps" command using a different port, 
        //assuming the queued replies from different machines don't overflow the buffer
       
        
        try {
            ServerSocket serverSocket = new ServerSocket(50001);
            for(int i=0; i<this.expectedReplies; i++) {
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
                
                Map<String, Boolean> processMap = (Map<String, Boolean>) objectStream.readObject();
                for(String processId : processMap.keySet()) {
                    //update pmTable for reference in ProcessManager
                    this.pmTable.get(processId).setStatus(processMap.get(processId));
                }                        
            }
        }
        catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
        }
        catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }            
    }
}
