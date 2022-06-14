package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

/**
 * 操作方法枚举
 *
 * @author shizeying
 * @date 2022/06/14
 */
public enum OperationMethodEnum {
    /**
     * 手动触发
     */
    MANUAL_TRIGGER("手动触发"),
    /**
     * 系统触发
     */
    SYSTEM_TRIGGER("系统触发");

    private String operationMethod;
    
    OperationMethodEnum(String operationMethod) {
        this.operationMethod = operationMethod;
    }
    
    public String getOperationMethod() {
        return operationMethod;
    }
}