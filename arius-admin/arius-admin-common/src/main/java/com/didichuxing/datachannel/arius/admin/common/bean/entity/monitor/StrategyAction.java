package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
