package distsys.promigr.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TransactionalFileOutputStream extends OutputStream
{

    /**
     * 
     */
    //TODO: private static final long serialVersionUID = 1L;
    
    private String filename;
    private File file;
    private boolean append;
    /**
     * 
     * @param filename
     */
    public TransactionalFileOutputStream(String filename, boolean append) {
        this.filename = filename;
        this.append = append;
        this.file = new File(filename);                
    }
    
    @Override
    public synchronized void write(int b)
        throws IOException
    {
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(this.file, this.append);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        outStream.write(b);
        outStream.flush();
        outStream.close();
        this.append = true;     //because you need to append every time write() is called after the first write.
    }

}
