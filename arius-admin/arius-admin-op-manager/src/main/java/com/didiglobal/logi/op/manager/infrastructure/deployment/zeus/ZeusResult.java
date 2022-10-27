package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusResult {
    private Object data;

    private String msg;

    public boolean failed() {
        if (msg == null || msg.isEmpty()) {
            return false;
        }
        return true;
    }
}
