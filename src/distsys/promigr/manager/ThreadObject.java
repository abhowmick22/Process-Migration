package distsys.promigr.manager;

import distsys.promigr.process.MigratableProcess;

public class ThreadObject
{
    public Thread thread;
    public MigratableProcess process;
    
    public Thread getThread() {
        return this.thread;
    }
    
    public MigratableProcess getProcess() {
        return this.process;
    }
    
    public void setThread(Thread thread) {
        this.thread = thread;
    }
    
    public void setProcess(MigratableProcess process) {
        this.process = process;
    }
}
