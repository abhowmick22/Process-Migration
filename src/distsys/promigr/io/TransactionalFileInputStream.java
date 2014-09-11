package distsys.promigr.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

//import sun.misc.IoTrace;

public class TransactionalFileInputStream extends InputStream implements Serializable
{    
    private int offset;
    private File file;
    
    /**
     * Constructor that takes the file name, and creates the corresponding file. 
     * @param filename The file to be read.
     */
    public TransactionalFileInputStream(String filename) {
        this.offset = 0;
        this.file = new File(filename);        
    }
    
    /**
     * Reads the file one character at a time and returns the ASCII value. 
     * @return The ASCII value of the character read from the file.
     */
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
