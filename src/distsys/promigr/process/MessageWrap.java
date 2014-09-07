package distsys.promigr.process;

import java.io.Serializable;

public class MessageWrap implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private int command;
    private String dest;
    private MigratableProcess process;    
    private String procId;
    
    public int getCommand() {
        return this.command;
    }
    
    public String getDest() {
        return this.dest;
    }
    
    public MigratableProcess getProcess() {
        return this.process;
    }
    
    public String getProcId() {
        return this.procId;
    }
    
    public void setCommand(int command) {
        this.command = command;
    }
    
    public void setDest(String dest) {
        this.dest = dest;
    }
    
    public void setMigratableProcess(MigratableProcess process) {
        this.process = process;
    }
    
    public void setProcId(String procId) {
        this.procId = procId;
    }

}
