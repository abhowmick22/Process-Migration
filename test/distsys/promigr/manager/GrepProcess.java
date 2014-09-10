package distsys.promigr.manager;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.util.Map;

import distsys.promigr.io.TransactionalFileInputStream;
import distsys.promigr.io.TransactionalFileOutputStream;
import distsys.promigr.process.MigratableProcess;


public class GrepProcess implements MigratableProcess
{

    private TransactionalFileInputStream  inFile;
    private TransactionalFileOutputStream outFile;
    private String query;

    private volatile boolean suspending;

    public GrepProcess(String args[]) throws Exception
    {
        if (args.length != 3) {
            System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
            throw new Exception("Invalid Arguments");
            //TODO: handle this exception in localmanagerthread.java
        }
        
        query = args[0];
        inFile = new TransactionalFileInputStream(args[1]);
        outFile = new TransactionalFileOutputStream(args[2], false);
    }

    public void run()
    {
        PrintStream out = new PrintStream(outFile);
        DataInputStream in = new DataInputStream(inFile);

        try {
            while (!suspending) {
                String line = in.readLine();

                if (line == null) break;
                
                //ND: if (line.contains(query)) {
                    out.println(line);
                    System.out.println(line);   //ND
                //ND: }
                
                // Make grep take longer so that we don't require extremely large files for interesting results
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        } catch (EOFException e) {
            //End of File
        } catch (IOException e) {
            System.out.println ("GrepProcess: Error: " + e);
        }


        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        while (suspending);
    }
 

  
    
}

/*
private TransactionalFileInputStream  inFile;
    private TransactionalFileOutputStream outFile;
    private String query;

    private volatile boolean suspending;

    public GrepProcess(String args[]) throws Exception
    {
        if (args.length != 3) {
            System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
            throw new Exception("Invalid Arguments");
        }
        
        query = args[0];
        inFile = new TransactionalFileInputStream(args[1]);
        outFile = new TransactionalFileOutputStream(args[2], false);
    }

    public void run()
    {
        PrintStream out = new PrintStream(outFile);
        DataInputStream in = new DataInputStream(inFile);

        try {
            while (!suspending) {
                String line = in.readLine();

                if (line == null) break;
                
                if (line.contains(query)) {
                    out.println(line);
                }
                
                // Make grep take longer so that we don't require extremely large files for interesting results
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        } catch (EOFException e) {
            //End of File
        } catch (IOException e) {
            System.out.println ("GrepProcess: Error: " + e);
        }


        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        while (suspending);
    }
*/