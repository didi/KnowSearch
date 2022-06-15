package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import static com.didiglobal.logi.security.common.constant.OplogConstant.PM_P;

import com.didiglobal.logi.security.common.constant.OplogConstant;

/**
 * 操作记录模块枚举
 *
 * @author shizeying
 * @date 2022/06/13
 */
public enum NewModuleEnum {
    /**
     * 应用程序
     */
    APPLICATION_create(OplogConstant.PM_P);
    APPLICATION_delete(OplogConstant.PM_P);
    APPLICATION_access(OplogConstant.PM_P);
    APPLICATION_user(OplogConstant.RPM_U,"访问模式");
    APPLICATION_user(OplogConstant.RPM_U,"访问模式");
    
    /**
     * 模块
     */
    private String module;
    /**
     * 操作类型
     */
    private String operationType;

    
    NewModuleEnum(String module, String operationType, String operatingContent) {
        this.module = module;
        this.operationType = operationType;
    }
    

    
    public String getModule() {
        return module;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
 
   
    
    
}