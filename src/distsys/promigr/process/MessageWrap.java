/**
 * MessageWrap provides the ability to wrap the object corresponding to the user process, along 
 * with additional metadata for effective communication between the master and slave. The master 
 * can use it to send appropriate commands to be performed on the user process (object) that is 
 * wrapped within a MessageWrap object, which is then sent over the network.  
 */

package distsys.promigr.process;

import java.io.Serializable;
import java.util.Map;

public class MessageWrap implements Serializable
{
    private String sourceAddr;
    private int command;
    private String dest;
    private MigratableProcess process;    
    private String procId;
    private Map<String, Boolean> procStatus;
    
    /**
     * Returns the command to be executed.
     * @return Command.
     */
    public int getCommand() {
        return this.command;
    }
    
    /**
     * Returns the destination where process is to be migrated/
     * @return Destination.
     */
    public String getDest() {
        return this.dest;
    }
    
    /**
     * Returns the object instance of user's class.
     * @return Instance of user class.
     */
    public MigratableProcess getProcess() {
        return this.process;
    }
    
    /**
     * Returns the process ID.
     * @return Process ID. 
     */
    public String getProcId() {
        return this.procId;
    }
    
    /**
     * Returns the process ID and status map for a local manager.
     * @return process ID and status map.
     */
    public Map<String, Boolean> getProcStatus() {
        return this.procStatus;
    }
    
    /**
     * Returns the source address from where the command was sent.
     * @return source address.
     */
    public String getSourceAddr() {
        return this.sourceAddr;
    }
    
    /**
     * Sets the command to be sent.
     * @param command
     */
    public void setCommand(int command) {
        this.command = command;
    }
    
    /**
     * Sets the destination address.
     * @param dest 
     */
    public void setDest(String dest) {
        this.dest = dest;
    }
    
    /**
     * Sets the object/instance of the user class.
     * @param process
     */
    public void setMigratableProcess(MigratableProcess process) {
        this.process = process;
    }
    
    /**
     * Sets the process ID.
     * @param procId
     */
    public void setProcId(String procId) {
        this.procId = procId;
    }
    
    /**
     * Sets each process' status (alive or not) running on a local manager.
     * @param procStatus
     */
    public void setProcStatus(Map<String, Boolean> procStatus) {
        this.procStatus = procStatus;
    }
    
    /**
     * Sets the source address from where the command was sent.
     * @param sourceAddr
     */
    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }
}
