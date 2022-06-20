package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作类型枚举
 *
 * @author shizeying
 * @date 2022/06/16
 */
public enum OperateTypeEnum {
        /**
     * 物理集群：集群接入
     */
    PHYSICAL_CLUSTER_JOIN(NewModuleEnum.PHYSICAL_CLUSTER, "集群接入",0),
    /**
     * 物理集群：新建
     */
    PHYSICAL_CLUSTER_NEW(NewModuleEnum.PHYSICAL_CLUSTER, "集群新建",1),
    /**
     * 物理集群: 下线
     */
    PHYSICAL_CLUSTER_OFFLINE(NewModuleEnum.PHYSICAL_CLUSTER, "集群下线",2),
    /**
     * 物理集群:扩容
     */
    PHYSICAL_CLUSTER_CAPACITY(NewModuleEnum.PHYSICAL_CLUSTER, "集群扩容",3),
    /**
     * 物理集群：缩容
     */
    PHYSICAL_CLUSTER_SHRINKAGE_CAPACITY(NewModuleEnum.PHYSICAL_CLUSTER, "集群缩容",4),
    /**
     * 物理集群：重启
     */
    PHYSICAL_CLUSTER_RESTART(NewModuleEnum.PHYSICAL_CLUSTER, "重启",5),
    /**
     * 物理集群： 信息修改
     */
    PHYSICAL_CLUSTER_INFO_MODIFY(NewModuleEnum.PHYSICAL_CLUSTER, "物理集群信息修改",6),
    /**
     * 物理集群:升级
     */
    PHYSICAL_CLUSTER_UPGRADE(NewModuleEnum.PHYSICAL_CLUSTER, "集群升级",    7),
    /**
     * 物理集群:REGION变更
     */
    PHYSICAL_CLUSTER_REGION_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "REGION变更",8),
    /**
     * 物理集群：GATEWAY变更
     */
    PHYSICAL_CLUSTER_GATEWAY_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "GATEWAY变更",9),
    /**
     * 物理集群：配置文件变更
     */
    PHYSICAL_CLUSTER_CONF_FILE_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "配置文件变更", 10),
    /**
     * 物理集群动：动态配置变更
     */
    PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE(NewModuleEnum.PHYSICAL_CLUSTER, "动态配置变更", 11),
    /**
     * 我的集群：信息修改
     */
    MY_CLUSTER_INFO_MODIFY(NewModuleEnum.MY_CLUSTER, "我的集群信息修改",12),
    /**
     * 我的集群：申请集群
     */
    MY_CLUSTER_APPLY(NewModuleEnum.MY_CLUSTER, "申请集群",13),
    /**
     * 我的集群：集群扩容
     */
    MY_CLUSTER_CAPACITY(NewModuleEnum.MY_CLUSTER, "集群扩容",14),
    /**
     * 我的集群：集群缩容
     */
    MY_CLUSTER_SHRINKAGE_CAPACITY(NewModuleEnum.MY_CLUSTER, "集群缩容", 15),
    /**
     * 我的集群：集群下线
     */
    MY_CLUSTER_OFFLINE(NewModuleEnum.MY_CLUSTER, "集群下线",16),
    /**
     * 应用：新建应用
     */
    APPLICATION_CREATE(NewModuleEnum.APPLICATION, "新建应用",17),
    /**
     * 应用：删除应用
     */
    APPLICATION_DELETE(NewModuleEnum.APPLICATION, "删除应用",18),
    
    /**
     * 应用：访问模式
     */
    APPLICATION_ACCESS_MODE(NewModuleEnum.APPLICATION, "访问模式",19),
    /**
     * 应用：负责人变更
     */
    APPLICATION_OWNER_CHANGE(NewModuleEnum.APPLICATION, "负责人变更",20),
    /**
     * 应用程序：用户
     */
    APPLICATION_USER_CHANGE(NewModuleEnum.APPLICATION, "成员变更",45),
    /**
     * 租户：新增租户
     */
    TENANT_ADD(NewModuleEnum.TENANT, "新增租户",21),
    /**
     * 租户信息修改
     */
    TENANT_INFO_MODIFY(NewModuleEnum.TENANT, "租户信息修改",22),
    /**
     * 索引模板管理：模板创建
     */
    INDEX_TEMPLATE_MANAGEMENT_CREATE(NewModuleEnum.INDEX_MANAGEMENT, "模板创建",23),
    /**
     * 索引模板管理:编辑MAPPING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_MAPPING(NewModuleEnum.INDEX_MANAGEMENT, "编辑MAPPING",24),
    /**
     * 索引模板管理:编辑SETTING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_SETTING(NewModuleEnum.INDEX_MANAGEMENT, "编辑SETTING",25),
    /**
     * 索引模板管理:模板下线
     */
    INDEX_TEMPLATE_MANAGEMENT_OFFLINE(NewModuleEnum.INDEX_MANAGEMENT, "模板下线",26),
    /**
     * 索引模板管理:索引模板信息修改
     */
    INDEX_TEMPLATE_MANAGEMENT_INFO_MODIFY(NewModuleEnum.INDEX_MANAGEMENT, "索引模板信息修改",27),
    /**
     * 索引模板管理:升级版本
     */
    INDEX_TEMPLATE_MANAGEMENT_UPGRADED_VERSION(NewModuleEnum.INDEX_MANAGEMENT, "升版本",28),
    /**
     * 模板服务:DCDR设置
     */
    TEMPLATE_SERVICE_DCDR_SETTING(NewModuleEnum.TEMPLATE_SERVICE, "DCDR设置",29),
    /**
     * 模板服务:索引清理
     */
    TEMPLATE_SERVICE_CLEAN(NewModuleEnum.TEMPLATE_SERVICE, "索引清理",30),
    /**
     * 模板服务:模板扩容
     */
    TEMPLATE_SERVICE_CAPACITY(NewModuleEnum.TEMPLATE_SERVICE, "模板扩容",31),
    /**
     * 模板服务:模板缩容
     */
    TEMPLATE_SERVICE_SHRINKAGE_CAPACITY(NewModuleEnum.TEMPLATE_SERVICE, "模板缩容",32),
    /**
     * 模板服务
     */
    TEMPLATE_SERVICE(NewModuleEnum.TEMPLATE_SERVICE, "模板服务",33),
    /**
     * 索引管理:创建
     */
    INDEX_MANAGEMENT_CREATE(NewModuleEnum.INDEX_MANAGEMENT, "索引创建",34),
    /**
     * 索引管理:删除索引
     */
    INDEX_MANAGEMENT_DELETE(NewModuleEnum.INDEX_MANAGEMENT, "删除索引", 35),
    /**
     * 索引管理:别名调整
     */
    INDEX_MANAGEMENT_ALIAS_MODIFY(NewModuleEnum.INDEX_MANAGEMENT, "别名调整", 36),
    /**
     * 索引管理:索引读写变更
     */
    INDEX_MANAGEMENT_READ_WRITE_CHANGE(NewModuleEnum.INDEX_MANAGEMENT, "索引读写变更", 37),
    /**
     * 索引管理:操作索引
     */
    INDEX_MANAGEMENT_OP_INDEX(NewModuleEnum.INDEX_MANAGEMENT, "操作索引", 38),
    /**
     * 索引服务:执行索引服务
     */
    INDEXING_SERVICE_RUN(NewModuleEnum.INDEX_SERVICE,"执行索引服务",39),
    /**
     * 查询模板:DSL限流调整
     */
    QUERY_TEMPLATE_DSL_CURRENT_LIMIT_ADJUSTMENT(NewModuleEnum.QUERY_TEMPLATE, "DSL限流调整",40),
    /**
     * 查询模板:DSL查询模板禁用
     */
    QUERY_TEMPLATE_DISABLE(NewModuleEnum.QUERY_TEMPLATE, "DSL查询模板禁用",41),
    /**
     * 配置修改
     */
    SETTING_MODIFY(NewModuleEnum.SETTING, "配置修改",42),
    /**
     * 配置添加
     */
    SETTING_ADD(NewModuleEnum.SETTING, "新增配置",43),
    /**
     * 配置删除
     */
    SETTING_DELETE(NewModuleEnum.SETTING, "删除配置",44);
    /**
     * 模块
     */
    private final NewModuleEnum module;
    /**
     * 操作类型
     */
    private final String operationType;
    private final Integer code;
    
    OperateTypeEnum(NewModuleEnum module, String operationType, Integer code) {
        this.module = module;
        this.operationType = operationType;
        this.code = code;
    }
    
    public NewModuleEnum getModule() {
        return module;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public static Map<Integer, String> getOperationTypeByModule(Integer module) {
        final NewModuleEnum moduleEnum = NewModuleEnum.getModuleEnum(module);
        if (Objects.isNull(moduleEnum)) {
            return Arrays.stream(OperateTypeEnum.values())
                    .collect(Collectors.toMap(OperateTypeEnum::getCode, OperateTypeEnum::getOperationType));
        }
        return Arrays.stream(OperateTypeEnum.values())
                .filter(operationTypeEnum -> operationTypeEnum.getModule().equals(moduleEnum))
                .collect(Collectors.toMap(OperateTypeEnum::getCode, OperateTypeEnum::getOperationType));
    }
     public static OperateTypeEnum getOperationTypeEnum(Integer code) {
        return Arrays.stream(OperateTypeEnum.values()).filter(operationTypeEnum -> operationTypeEnum.getCode().equals(code))
                .findFirst().orElse(null);
    }
    
    
}