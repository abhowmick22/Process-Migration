package distsys.promigr.test;

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


public class MergeFiles implements MigratableProcess
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TransactionalFileInputStream[]  inFile = new 
    						TransactionalFileInputStream[3];
    private TransactionalFileOutputStream outFile;
    
    private volatile boolean suspending;

    public MergeFiles(String[] args) throws Exception
    {
        if (args.length != 4) {
            System.out.println("usage: MergeFiles <inputFile1> <inputFile2> <inputFile3> <outputFile>");
            throw new Exception("Invalid Arguments");
            //TODO: handle this exception in localmanagerthread.java
        }
        
        
        for(int i=0; i<3; i++){
        	inFile[i] = new TransactionalFileInputStream(args[i]);
        }
        outFile = new TransactionalFileOutputStream(args[3], false);
    }

    public void run()
    {
        PrintStream out = new PrintStream(outFile);
        DataInputStream[] in = new DataInputStream[3];;

        for(int i=0; i<3; i++){
        	in[i] = new DataInputStream(inFile[i]);
        }

        try {
        	String line;
            while (!suspending) {
            	
            	for(int i=0; i<3; i++){
            		line = in[i].readLine();
            		if (line == null) break;
            		out.println(line);
            	}

                
                
                //ND: if (line.contains(query)) {
                    //out.println(line);
                    //System.out.println(line);   //ND
                //ND: }
                
                // Make grep take longer so that we don't require extremely large files for interesting results
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        } 
        catch (EOFException e) {
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