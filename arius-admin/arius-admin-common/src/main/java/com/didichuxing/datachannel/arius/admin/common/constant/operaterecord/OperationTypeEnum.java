package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作类型枚举
 *
 * @author shizeying
 * @date 2022/06/16
 */
public enum OperationTypeEnum {
        /**
     * 物理集群：集群接入
     */
    PHYSICAL_CLUSTER_JOIN(NewModuleEnum.PHYSICAL_CLUSTER, "集群接入"),
    /**
     * 物理集群：新建
     */
    PHYSICAL_CLUSTER_NEW(NewModuleEnum.PHYSICAL_CLUSTER, "集群新建"),
    /**
     * 物理集群: 下线
     */
    PHYSICAL_CLUSTER_OFFLINE(NewModuleEnum.PHYSICAL_CLUSTER, "集群下线"),
    /**
     * 物理集群:扩容
     */
    PHYSICAL_CLUSTER_CAPACITY(NewModuleEnum.PHYSICAL_CLUSTER, "集群扩容"),
    /**
     * 物理集群：缩容
     */
    PHYSICAL_CLUSTER_SHRINKAGE_CAPACITY(NewModuleEnum.PHYSICAL_CLUSTER, "集群缩容"),
    /**
     * 物理集群：重启
     */
    PHYSICAL_CLUSTER_RESTART(NewModuleEnum.PHYSICAL_CLUSTER, "重启"),
    /**
     * 物理集群： 信息修改
     */
    PHYSICAL_CLUSTER_INFO_MODIFY(NewModuleEnum.PHYSICAL_CLUSTER, "物理集群信息修改"),
    /**
     * 物理集群:升级
     */
    PHYSICAL_CLUSTER_UPGRADE(NewModuleEnum.PHYSICAL_CLUSTER, "集群升级"),
    /**
     * 物理集群:REGION变更
     */
    PHYSICAL_CLUSTER_REGION_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "REGION变更"),
    /**
     * 物理集群：GATEWAY变更
     */
    PHYSICAL_CLUSTER_GATEWAY_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "GATEWAY变更"),
    /**
     * 物理集群：配置文件变更
     */
    PHYSICAL_CLUSTER_CONF_FILE_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "配置文件变更"),
    /**
     * 物理集群动：动态配置变更
     */
    PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "动态配置变更"),
    /**
     * 我的集群：信息修改
     */
    MY_CLUSTER_INFO_MODIFY(NewModuleEnum.MY_CLUSTER, "我的集群信息修改"),
    /**
     * 我的集群：申请集群
     */
    MY_CLUSTER_APPLY(NewModuleEnum.MY_CLUSTER, "申请集群"),
    /**
     * 我的集群：集群扩容
     */
    MY_CLUSTER_CAPACITY(NewModuleEnum.MY_CLUSTER, "集群扩容"),
    /**
     * 我的集群：集群缩容
     */
    MY_CLUSTER_SHRINKAGE_CAPACITY(NewModuleEnum.MY_CLUSTER, "集群缩容"),
    /**
     * 我的集群：集群下线
     */
    MY_CLUSTER_OFFLINE(NewModuleEnum.MY_CLUSTER, "集群下线"),
    /**
     * 应用：新建应用
     */
    APPLICATION_CREATE(NewModuleEnum.APPLICATION, "新建应用"),
    /**
     * 应用：删除应用
     */
    APPLICATION_DELETE_DELETE(NewModuleEnum.APPLICATION, "删除应用"),
    
    /**
     * 应用：访问模式
     */
    APPLICATION_ACCESS_MODE(NewModuleEnum.APPLICATION, "访问模式"),
    /**
     * 应用：负责人变更
     */
    APPLICATION_OWNER_CHANGE(NewModuleEnum.APPLICATION, "负责人变更"),
    /**
     * 租户：新增租户
     */
    TENANT_ADD(NewModuleEnum.TENANT, "新增租户"),
    /**
     * 租户信息修改
     */
    TENANT_INFO_MODIFY(NewModuleEnum.TENANT, "租户信息修改"),
    /**
     * 索引模板管理：模板创建
     */
    INDEX_TEMPLATE_MANAGEMENT_CREATE(NewModuleEnum.INDEX_MANAGEMENT, "模板创建"),
    /**
     * 索引模板管理:编辑MAPPING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_MAPPING(NewModuleEnum.INDEX_MANAGEMENT, "编辑MAPPING"),
    /**
     * 索引模板管理:编辑SETTING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_SETTING(NewModuleEnum.INDEX_MANAGEMENT, "编辑SETTING"),
    /**
     * 索引模板管理:模板下线
     */
    INDEX_TEMPLATE_MANAGEMENT_OFFLINE(NewModuleEnum.INDEX_MANAGEMENT, "模板下线"),
    /**
     * 索引模板管理:索引模板信息修改
     */
    INDEX_TEMPLATE_MANAGEMENT_INFO_MODIFY(NewModuleEnum.INDEX_MANAGEMENT, "索引模板信息修改"),
    /**
     * 索引模板管理:升级版本
     */
    INDEX_TEMPLATE_MANAGEMENT_UPGRADED_VERSION(NewModuleEnum.INDEX_MANAGEMENT, "升版本"),
    /**
     * 模板服务:DCDR设置
     */
    TEMPLATE_SERVICE_DCDR_SETTING(NewModuleEnum.TEMPLATE_SERVICE, "DCDR设置"),
    /**
     * 模板服务:索引清理
     */
    TEMPLATE_SERVICE_CLEAN(NewModuleEnum.TEMPLATE_SERVICE, "索引清理"),
    /**
     * 模板服务:模板扩容
     */
    TEMPLATE_SERVICE_CAPACITY(NewModuleEnum.TEMPLATE_SERVICE, "模板扩容"),
    /**
     * 模板服务:模板缩容
     */
    TEMPLATE_SERVICE_SHRINKAGE_CAPACITY(NewModuleEnum.TEMPLATE_SERVICE, "模板缩容"),
    /**
     * 模板服务
     */
    TEMPLATE_SERVICE(NewModuleEnum.TEMPLATE_SERVICE, "模板服务"),
    /**
     * 索引管理:创建
     */
    INDEX_MANAGEMENT_CREATE(NewModuleEnum.INDEX_MANAGEMENT, "索引创建"),
    /**
     * 索引管理:删除索引
     */
    INDEX_MANAGEMENT_DELETE(NewModuleEnum.INDEX_MANAGEMENT, "删除索引"),
    /**
     * 索引管理:别名调整
     */
    INDEX_MANAGEMENT_ALIAS_MODIFY(NewModuleEnum.INDEX_MANAGEMENT, "别名调整"),
    /**
     * 索引管理:索引读写变更
     */
    INDEX_MANAGEMENT_READ_WRITE_CHANGE(NewModuleEnum.INDEX_MANAGEMENT, "索引读写变更"),
    /**
     * 索引管理:操作索引
     */
    INDEX_MANAGEMENT_OP_INDEX(NewModuleEnum.INDEX_MANAGEMENT, "操作索引"),
    /**
     * 索引服务:执行索引服务
     */
    INDEXING_SERVICE_RUN(NewModuleEnum.INDEX_SERVICE,"执行索引服务"),
    /**
     * 查询模板:DSL限流调整
     */
    QUERY_TEMPLATE_DSL_CURRENT_LIMIT_ADJUSTMENT(NewModuleEnum.QUERY_TEMPLATE, "DSL限流调整"),
    /**
     * 查询模板:DSL查询模板禁用
     */
    QUERY_TEMPLATE_DISABLE(NewModuleEnum.QUERY_TEMPLATE, "DSL查询模板禁用"),
    /**
     * 配置修改
     */
    SETTING_MODIFY(NewModuleEnum.SETTING, "配置修改"),
    /**
     * 配置添加
     */
    SETTING_ADD(NewModuleEnum.SETTING, "新增配置"),
    /**
     * 配置删除
     */
    SETTING_DELETE(NewModuleEnum.SETTING, "删除配置");
    /**
     * 模块
     */
    private final NewModuleEnum module;
    /**
     * 操作类型
     */
    private final String operationType;
    
    OperationTypeEnum(NewModuleEnum module, String operationType) {
        this.module = module;
        this.operationType = operationType;
    }
    
    public NewModuleEnum getModule() {
        return module;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public static List<String> getOperationTypeByModule(Integer module) {
        final NewModuleEnum moduleEnum = NewModuleEnum.getNewModuleEnum(module);
        if (Objects.isNull(moduleEnum)) {
            return Arrays.stream(OperationTypeEnum.values()).map(OperationTypeEnum::getOperationType)
                    .collect(Collectors.toList());
        }
        return Arrays.stream(OperationTypeEnum.values())
                .filter(operationTypeEnum -> operationTypeEnum.getModule().equals(moduleEnum))
                .map(OperationTypeEnum::getOperationType).collect(Collectors.toList());
    }
    
    
}