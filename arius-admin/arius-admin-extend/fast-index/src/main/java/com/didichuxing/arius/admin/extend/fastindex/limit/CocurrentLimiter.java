package com.didichuxing.arius.admin.extend.fastindex.limit;

import java.util.HashMap;
import java.util.Map;

public class CocurrentLimiter {
    private int allCocurrent = 0;
    private Map<String, Integer> nodeCocurrent = new HashMap<>();

    private int maxCocurrent;
    private int nodeMaxCocurrent;


    public CocurrentLimiter(int maxCocurrent, int nodeMaxCocurrent) {
        this.maxCocurrent = maxCocurrent;
        this.nodeMaxCocurrent = nodeMaxCocurrent;
    }


    public void add(String hostName) {
        allCocurrent++;
        if(!nodeCocurrent.containsKey(hostName)) {
            nodeCocurrent.put(hostName, 0);
        }

        int old = nodeCocurrent.get(hostName);
        nodeCocurrent.put(hostName, old+1);
    }


    public boolean tryOne(String hostName) {
        if(allCocurrent>=maxCocurrent) {
            return false;
        }

        if(nodeCocurrent.containsKey(hostName) &&
                nodeCocurrent.get(hostName)>=nodeMaxCocurrent) {
            return false;
        }

        return true;
    }

    public void clear() {
        allCocurrent=0;
        nodeCocurrent.clear();
    }
}
