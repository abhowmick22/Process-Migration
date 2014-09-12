/**
 * It keeps track of the MigratableProcess (explained in next package) object and thread associated
 * with each process. This is useful when the local manager wants to migrate one of its processes 
 * to another node. It can help check if the thread running that process (object) is alive. 
 * Also, it can help suspend the thread for migration.
 */

package distsys.promigr.manager;

import distsys.promigr.process.MigratableProcess;

public class ThreadObject
{
    public Thread thread;
    public MigratableProcess process;
    
    /**
     * Returns the thread in which the current process is running in.
     * @return Thread running current process.
     */
    public Thread getThread() {
        return this.thread;
    }
    
    /**
     * Returns the current process (object).
     * @return Object representing the user process.
     */
    public MigratableProcess getProcess() {
        return this.process;
    }
    
    /**
     * Set the thread that the current process is executing in.
     * @param thread The thread executing the current process.
     */
    public void setThread(Thread thread) {
        this.thread = thread;
    }
    
    /**
     * Set the object that represents user process.
     * @param process The object (instance) of the user class. 
     */
    public void setProcess(MigratableProcess process) {
        this.process = process;
    }
}
