package distsys.promigr.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;

public class ProcessManager
{

    public static void main(String[] args) {
        
        System.out.println("-----Welcome-----");
        System.out.println("1. Enter ");    //TODO: new process command
        System.out.println("-----Welcome-----");    //TODO: migrate process command
        System.out.println("-----Welcome-----");    //TODO: process list
        System.out.println("4. Enter \"help\" for this menu.");    //help
        
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
                try {
                    Class clazz = Class.forName(processName);
                    Constructor[] constructors = clazz.getConstructors();
                }
                catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }
    }
    

}