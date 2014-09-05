package distsys.promigr.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements Serializable
{
    /**
     * 
     */
    //TODO: private static final long serialVersionUID = 1L;
    
    private String filename;
    private int offset;
    
    /**
     * 
     * @param filename
     */
    public TransactionalFileInputStream(String filename) {
        this.filename = filename;
        this.offset = 0;
        
        
    }
    
    public InputStream fileOpen(String filename, int offset) throws FileNotFoundException{
        return null;
        
        
        
    }
    
    @Override
    public int read()
        throws IOException
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
