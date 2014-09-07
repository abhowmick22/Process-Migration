package distsys.promigr.manager;

import java.lang.reflect.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GrepProcessTest
{
    public static void main(String args[]) {
        GrepProcess gp = null;
        try {
            gp = new GrepProcess();
        }
        catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        Thread t = new Thread(gp);
        t.start();
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        if(t.isAlive()) {
            gp.suspend();
        }
        //System.exit(0);
        try
        {
           FileOutputStream fileOut =
           new FileOutputStream("/tmp/employee.ser");
           ObjectOutputStream out = new ObjectOutputStream(fileOut);
           out.writeObject(gp);
           out.close();
           fileOut.close();
        }catch(IOException i)
        {
            i.printStackTrace();
        } 
        
        System.out.println("serialized");
        try {
            t.join();
        }
        catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.out.println("JOINED");
        //System.exit(0);
        
        GrepProcess gp2;
        Object rObject;
        try
        {
           FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           gp2 = (GrepProcess) in.readObject();

           //rObject = in.readObject();
           //Class c = GrepProcess.class;
           //gp2 = c.cast(rObject);
           
           //System.out.println(gp2.getClass().getName());
           in.close();
           fileIn.close();
        }catch(IOException i)
        {
           i.printStackTrace();
           return;
        }catch(ClassNotFoundException c)
        {
           System.out.println("Employee class not found");
           c.printStackTrace();
           return;
        }
        System.out.println("deserialized");
        Thread t2 = new Thread(gp2);
        t2.start();
        //t2.interrupt();
        
        //assert(gp.suspended);
        try {
            t.join();
            t2.join();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
