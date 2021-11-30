package com.didichuxing.datachannel.arius.admin.remote.nightingale.bean;

import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NightingaleResult<T> {
    private T      dat;

    private String err;

    public boolean failed() {
        if (AriusObjUtils.isBlank(err)) {
            return false;
        }
        return true;
    }
}