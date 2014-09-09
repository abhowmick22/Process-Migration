package distsys.promigr.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

//import org.apache.commons.lang.ArrayUtils;







import distsys.promigr.process.MessageWrap;
import distsys.promigr.process.MigratableProcess;

public class ProcessManager<T>
{

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
        System.out.println("-----Welcome-----");
        System.out.println("1. Enter ");    //TODO: new process command
        System.out.println("-----Welcome-----");    //TODO: migrate process command
        System.out.println("-----Welcome-----");    //TODO: process list
        System.out.println("4. Enter \"help\" for this menu.");    //help
        
        ProcessManager<MigratableProcess> manager = new ProcessManager<MigratableProcess>();
        
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
                String procId = processName;    //TODO: change this
                MigratableProcess inst = manager.init(commandList);
                Socket clientSocket = new Socket(InetAddress.getByName(null), 50000);

                MessageWrap echoMsg = new MessageWrap();
                echoMsg.setCommand(1);
                echoMsg.setProcId(procId);
                echoMsg.setMigratableProcess(inst);
                
                ObjectOutputStream outStream = new ObjectOutputStream(clientSocket.getOutputStream());
                
                outStream.writeObject(echoMsg);
                outStream.close();
                clientSocket.close();
                
            }
            
        }
    }
    
    public T init(String[] args) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        
        Class<?> clazz = Class.forName(args[1]);
        
        Class<?>[] classArray = new Class[args.length-2];
        String[] argsArray = new String[args.length-2];
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