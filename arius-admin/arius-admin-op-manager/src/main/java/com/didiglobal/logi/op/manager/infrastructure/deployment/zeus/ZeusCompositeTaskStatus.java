package com.didiglobal.logi.op.manager.infrastructure.deployment.zeus;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author didi
 * @date 2022-10-31 16:16
 */
@Data
public class ZeusCompositeTaskStatus {
    private Map<Integer, ZeusTaskStatus> taskStatusMap = new LinkedHashMap<>();

    public void addTaskStatus(Integer executeId, ZeusTaskStatus status) {
        taskStatusMap.put(executeId, status);
    }

    public ZeusTaskStatus getTaskStatus(Integer executeId) {
        return taskStatusMap.get(executeId);
    }

    public boolean isExistNullTaskId() {
        return taskStatusMap.containsKey(null);
    }

    public ZeusTaskStatus getLeastTaskStatus() {
        ZeusTaskStatus zeusTaskStatus = null;
        for (Map.Entry<Integer, ZeusTaskStatus> entry : taskStatusMap.entrySet()) {
            if (null != entry.getKey()) {
                zeusTaskStatus = entry.getValue();
            }
        }
        return zeusTaskStatus;
    }
}
