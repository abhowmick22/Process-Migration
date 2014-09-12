/**
 * Generates an input stream through which the user can read a file. It also maintains the file 
 * offset so that when the user migrates the process from node A to node B, then node B starts 
 * reading the file from where A stopped reading it.
 */

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
        int c = 0;
        try {
            is = new FileInputStream(this.file);
            is.skip(offset);
            c = is.read();
            this.offset++;
            is.close();
        }
        catch (IOException e) {
            //ignore
        }
        return c;
    }
    
}
