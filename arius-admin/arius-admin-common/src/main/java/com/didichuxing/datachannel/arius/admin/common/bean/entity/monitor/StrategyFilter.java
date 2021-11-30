package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StrategyFilter {
    private String tkey;

    private String topt;

    private String tval;

    public boolean paramLegal() {
        if (tkey == null
                || topt == null
                || tval == null) {
            return false;
        }
        return true;
    }
}
