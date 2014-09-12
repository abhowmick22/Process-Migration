/**
 * WebPageCopier read from a page on the world wide web, and copies the contents of the page to an 
 * output file. 
 */

package distsys.promigr.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import distsys.promigr.io.TransactionalFileOutputStream;
import distsys.promigr.process.MigratableProcess;


public class WebPageCopier implements MigratableProcess {
        
    private String url; 
    private TransactionalFileOutputStream outFile;
    private long skip;
    private volatile boolean suspending;
    
    /**
     * Constructor that initializes the input url and output file stream.
     * @param args Array of url and output file.
     * @throws Exception
     */
    public WebPageCopier(String args[]) throws Exception
    {
        if (args.length != 2) {
            System.out.println("usage: WebPageCopier <website> <outputFile>");
            throw new Exception("Invalid Arguments");
        }
        
        this.url = args[0];
        this.outFile = new TransactionalFileOutputStream(args[1], true);
        this.skip = 0;
    }
    
    /**
     * The run method provides an execution point.
     */
    public void run()
    {
        PrintStream out = new PrintStream(outFile);
        try {
            URL url = new URL(this.url);
            while (!suspending) {
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                //skip on the input stream to the point that we last stopped reading.
                //need to do this here because we are not migrating url connections.
                in.skip(this.skip);
                String line = in.readLine();
                
                if (line == null) break;
                
                out.println(line);                
                this.skip+=line.length()+1;
                
                // Make process take longer
                in.close();                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }          
        }
        catch (MalformedURLException e) {
            // ignore
        }
        catch (IOException e) {
            // ignore
        }
        
        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        while (suspending);
    }
}

