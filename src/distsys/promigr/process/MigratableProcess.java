package distsys.promigr.process;

import java.io.Serializable;

public interface MigratableProcess extends Runnable, Serializable
{
    /**
     * Suspends the current thread executing the migratable process.
     */
    public void suspend();
    
    /**
     * Returns the string representation of the object. Needs to be implemented by overriding the
     * default java.lang.Object method.
     * @return String representation of the object.
     */
    public String toString();
}
