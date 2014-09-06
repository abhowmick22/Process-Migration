package distsys.promigr.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;

//import sun.misc.IoTrace;

public class TransactionalFileInputStream extends InputStream implements Serializable
{
    /**
     * 
     */
    //TODO: private static final long serialVersionUID = 1L;
    
    private String filename;
    private int offset;
    private File file;
    /**
     * 
     * @param filename
     */
    public TransactionalFileInputStream(String filename) {
        this.filename = filename;
        this.offset = 0;
        
        this.file = new File(filename);
        
    }
    
    @Override
    public synchronized int read()
        throws IOException
    {
        InputStream is = null;
        try {
            is = new FileInputStream(this.file);
            is.skip(offset);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int c = is.read();
        this.offset++;  
        is.close();
        return c;
    }
    

}
