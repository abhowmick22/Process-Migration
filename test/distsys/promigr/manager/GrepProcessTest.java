package distsys.promigr.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GrepProcessTest
{
    public static void main(String args[]) {
        GrepProcess gp = new GrepProcess();
        Thread t = new Thread(gp);
        t.start();
        t.interrupt();
        //gp.suspend();
        
        try
        {
           FileOutputStream fileOut =
           new FileOutputStream("/tmp/employee.ser");
           ObjectOutputStream out = new ObjectOutputStream(fileOut);
           out.writeObject(gp);
           out.close();
           fileOut.close();
           System.out.printf("Serialized data is saved in /tmp/employee.ser");
        }catch(IOException i)
        {
            i.printStackTrace();
        }
        System.out.println("serialized");
        
        GrepProcess gp2;
        try
        {
           FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
           ObjectInputStream in = new ObjectInputStream(fileIn);
           gp2 = (GrepProcess) in.readObject();
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
        t2.interrupt();
        
        assert(gp.suspended);
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
