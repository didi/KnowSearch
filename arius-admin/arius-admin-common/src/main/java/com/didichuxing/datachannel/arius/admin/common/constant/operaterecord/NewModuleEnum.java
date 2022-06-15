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
    physical_cluster("physical_cluster","集群接入"),
    physical_cluster("physical_cluster","集群新建"),
    physical_cluster("physical_cluster","集群下线"),
    physical_cluster("physical_cluster","集群扩容"),
    physical_cluster("physical_cluster","集群缩容"),
    physical_cluster("physical_cluster","重启"),
    physical_cluster("physical_cluster","物理集群信息修改"),
    physical_cluster("physical_cluster",    "集群升级"),
    physical_cluster("physical_cluster","Region变更"),
    physical_cluster("physical_cluster","Gateway变更"),
    physical_cluster("physical_cluster","配置文件变更"),
    physical_cluster("physical_cluster","动态配置变更"),
    MY_CLUSTER("my_cluster","我的集群信息修改"),
    MY_CLUSTER("my_cluster","申请集群"),
    MY_CLUSTER("my_cluster","集群扩容"),
    MY_CLUSTER("my_cluster","集群缩容"),
    MY_CLUSTER("my_cluster","集群下线"),
    /**
     * 应用程序
     */
    APPLICATION_create(OplogConstant.PM_P),
    APPLICATION_delete(OplogConstant.PM_P),
    APPLICATION_access(OplogConstant.PM_P),
    APPLICATION_user(OplogConstant.RPM_U,"访问模式"),
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