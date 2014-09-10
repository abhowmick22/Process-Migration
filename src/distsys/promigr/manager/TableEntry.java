package distsys.promigr.manager;
import distsys.promigr.process.MigratableProcess;


public class TableEntry {
private static final long serialVersionUID = 1L;
    
    private String procId;
    private String processName;
    private String[] arguments;    
    private String nodeName;
    private boolean status;
    
    public String getProcId() {
        return this.procId;
    }
    
    public String getProcessName() {
        return this.processName;
    }
    
    public String[] getArguments() {
        return this.arguments;
    }
    
    public String getNodeName() {
        return this.nodeName;
    }
    
    public boolean getStatus () {
        return this.status;
    }
    
    public void setProcId (String procId) {
        this.procId = procId;
    }
    
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
}

