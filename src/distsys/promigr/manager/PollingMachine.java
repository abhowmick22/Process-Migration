package distsys.promigr.manager;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

public class PollingMachine implements Runnable
{
    private ConcurrentMap<String, Boolean> pollMap;
    
    public PollingMachine(ConcurrentMap<String, Boolean> pollMap)
    {
        this.pollMap = pollMap;
    }

    @Override
    public void run()
    {
        while(true) {
            for(String machineName: this.pollMap.keySet()) {
                
            }
        }
        
    }
    
}
