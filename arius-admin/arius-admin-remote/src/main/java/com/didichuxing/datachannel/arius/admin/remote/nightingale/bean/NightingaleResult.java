package com.didichuxing.datachannel.arius.admin.remote.nightingale.bean;

import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import lombok.Data;

@Data
public class NightingaleResult<T> {
    private T      dat;

    private String err;

    public boolean failed() {
        if (ValidateUtils.isBlank(err)) {
            return false;
        }
        return true;
    }
}