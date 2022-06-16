package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 触发方式枚举
 *
 * @author shizeying
 * @date 2022/06/14
 */
public enum TriggerWayEnum {
    /**
     * 手动触发
     */
    MANUAL_TRIGGER("手动触发"),
    /**
     * 系统触发
     */
    SYSTEM_TRIGGER("系统触发");

    private String operationMethod;
    
    TriggerWayEnum(String operationMethod) {
        this.operationMethod = operationMethod;
    }
    
    public String getOperationMethod() {
        return operationMethod;
    }
    
    public static List<String> getOperationList() {
        return Arrays.stream(TriggerWayEnum.values()).map(TriggerWayEnum::getOperationMethod)
                .collect(Collectors.toList());
        
    }
}