/**
 * MergeFiles reads three different files at once, and merges the files line-by-line into an 
 * output file. 
 */

package distsys.promigr.test;

import java.io.PrintStream;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.Thread;
import java.lang.InterruptedException;

import distsys.promigr.io.TransactionalFileInputStream;
import distsys.promigr.io.TransactionalFileOutputStream;
import distsys.promigr.process.MigratableProcess;

public class MergeFiles implements MigratableProcess
{
	private TransactionalFileInputStream[]  inFile; 	        
    private TransactionalFileOutputStream outFile;    
    private volatile boolean suspending;

    /**
     * Constructor that initializes the input and output file streams.
     * @param args Input and output file names.
     * @throws Exception
     */
    public MergeFiles(String[] args) throws Exception
    {
        if (args.length != 4) {
            System.out.println("usage: MergeFiles <inputFile1> <inputFile2> <inputFile3> <outputFile>");
            throw new Exception("Invalid Arguments");            
        }        
        inFile = new TransactionalFileInputStream[3];;
        for(int i=0; i<3; i++){
        	inFile[i] = new TransactionalFileInputStream(args[i]);
        }
        outFile = new TransactionalFileOutputStream(args[3], false);
    }
    
    /**
     * The run method provides an execution point.
     */    
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
                // Make merging take longer
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }
        } 
        catch (EOFException e) {
            //ignore
        } catch (IOException e) {
            //ignore - assume files are present
        }
        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        while (suspending);
    }
}