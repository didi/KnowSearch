package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class StrategyAction {
    private String notifyGroup;

    private String converge;

    private String callback;

    public boolean paramLegal() {
        if (notifyGroup == null || converge == null) {
            return false;
        }
        callback = (callback == null? "": callback);
        return true;
    }
}
