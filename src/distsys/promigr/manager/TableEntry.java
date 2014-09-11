package distsys.promigr.manager;

public class TableEntry {
    
    private String procId;
    private String processName;
    private String[] arguments;    
    private String nodeName;
    private boolean status;
    
    /**
     * Returns process ID.
     * @return process ID.
     */
    public String getProcId() {
        return this.procId;
    }
    
    /**
     * Returns process name.
     * @return process name.
     */
    public String getProcessName() {
        return this.processName;
    }
    
    /**
     * Returns constructor arguments of the process.
     * @return constructor arguments of the process..
     */
    public String[] getArguments() {
        return this.arguments;
    }
    
    /**
     * Returns name of node this process is running on.
     * @return Node name.
     */
    public String getNodeName() {
        return this.nodeName;
    }
    
    /**
     * Returns status (alive or not) of this process.
     * @return Status.
     */
    public boolean getStatus () {
        return this.status;
    }
    
    /**
     * Sets process ID.
     * @param procId Process ID.
     */
    public void setProcId (String procId) {
        this.procId = procId;
    }
    
    /**
     * Sets process arguments.
     * @param arguments Process args.
     */
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    /**
     * Sets process name.
     * @param processName Process name.
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    /**
     * Sets the status for this process.
     * @param status Status (alive or not).
     */
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    /**
     * Sets the node name that this process is executing on.
     * @param nodeName Name of the node.
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}

