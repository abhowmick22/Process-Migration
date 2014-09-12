/**
 * Generates an output stream through which the user can write to a file. It also appends to a file
 * if the user asks for that functionality. 
 */

package distsys.promigr.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable
{
    private File file;
    private boolean append;
    
    /**
     * Constructor that initializes the file using the file name and the append instruction.
     * @param filename The name of the file to be opened.
     * @param append Whether the file should be opened in the append mode.
     */
    public TransactionalFileOutputStream(String filename, boolean append) {
        this.append = append;
        this.file = new File(filename);                
    }
    
    /**
     * Writes a character to the open file.
     * @param b ASCII value of the character to be written.  
     */
    @Override
    public synchronized void write(int b)
        throws IOException
    {
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(this.file, this.append);
            outStream.write(b);
            outStream.flush();
            outStream.close();
            this.append = true;     //because you need to append every time write() is called after the first write.
        }
        catch (IOException e) {
            //ignore
        }        
    }
}
