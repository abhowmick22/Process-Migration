package distsys.promigr.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class MessageWrap implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String sourceAddr;
    private int command;
    private String dest;
    private MigratableProcess process;    
    private String procId;
    private Map<String, Boolean> procStatus;
    private boolean ack;
    
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
    
    public Map<String, Boolean> getProcStatus() {
        return this.procStatus;
    }
    
    public String getSourceAddr() {
        return this.sourceAddr;
    }
    
    public boolean getAck() {
        return this.ack;
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
    
    public void setProcStatus(Map<String, Boolean> procStatus) {
        this.procStatus = procStatus;
    }
    
    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }
    
    public void setAck(boolean ack) {
        this.ack = ack;
    }


}
