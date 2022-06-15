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
    physical_cluster("物理集群","集群接入"),
    physical_cluster("物理集群","集群新建"),
    physical_cluster("物理集群","集群下线"),
    physical_cluster("物理集群","集群扩容"),
    physical_cluster("物理集群","集群缩容"),
    physical_cluster("物理集群","重启"),
    physical_cluster("物理集群","物理集群信息修改"),
    physical_cluster("物理集群",    "集群升级"),
    physical_cluster("物理集群","Region变更"),
    physical_cluster("物理集群","Gateway变更"),
    physical_cluster("物理集群","配置文件变更"),
    physical_cluster("物理集群","动态配置变更"),
    MY_CLUSTER("我的集群","我的集群信息修改"),
    MY_CLUSTER("我的集群","申请集群"),
    MY_CLUSTER("我的集群","集群扩容"),
    MY_CLUSTER("我的集群","集群缩容"),
    MY_CLUSTER("我的集群","集群下线"),
    /**
     * 应用程序
     */
    APPLICATION_create(OplogConstant.PM_P, "新建应用"),
    APPLICATION_delete(OplogConstant.PM_P,"删除应用"),
   
    APPLICATION_user(OplogConstant.RPM_U,"访问模式"),
    APPLICATION_user(OplogConstant.RPM_U,"负责人变更"),
     APPLICATION_access(OplogConstant.UM_U,"新增租户"),
     APPLICATION_access(OplogConstant.UM_U,"租户信息修改"),
    a("索引模板管理","模板创建"),
    a("索引模板管理","编辑mapping"),
    a("索引模板管理","编辑setting"),
    a("索引模板管理","模板下线"),
    a("索引模板管理","索引模板信息修改"),
    a("索引模板管理","升版本"),
    b("模板服务","DCDR设置"),
    b("模板服务","索引清理"),
    b("模板服务","模板扩容"),
    b("模板服务","模板缩容"),
    b("模板服务","模板服务"),
    c("索引管理","索引创建"),
    c("索引管理","删除索引"),
    c("索引管理","别名调整"),
    c("索引管理","索引读写变更"),
    c("索引管理","操作索引"),
    d("索引服务","执行索引服务"),
    e("查询模板","DSL限流调整"),
    f("查询模板","DSL限流调整"),
    f("查询模板","DSL查询模板禁用"),
    f("配置","配置修改"),
    f("配置","新增配置"),
    f("配置","删除配置"),
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