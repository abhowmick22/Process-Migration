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
    
    @Override
    public void run()
    {
        PrintStream out = new PrintStream(outFile);
        
        try {
            URL url = new URL(this.url);
            while (!suspending) {
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                in.skip(this.skip);
                String line = in.readLine();
                
                if (line == null) break;
                
                out.println(line);                
                this.skip+=line.length()+1;
                // Make process take longer so that we don't require extremely large files for interesting results
                in.close();                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore it
                }
            }          
        }
        catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        suspending = false;
    }

    public void suspend()
    {
        suspending = true;
        while (suspending);
    }
}

