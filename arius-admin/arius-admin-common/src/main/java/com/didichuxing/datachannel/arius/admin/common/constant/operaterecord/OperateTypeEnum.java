package com.didichuxing.datachannel.arius.admin.common.constant.operaterecord;

import com.didichuxing.datachannel.arius.admin.common.tuple.TupleTwo;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuples;
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
    PHYSICAL_CLUSTER_JOIN(ModuleEnum.PHYSICAL_CLUSTER, "集群接入", 0),
    /**
     * 物理集群：新建
     */
    PHYSICAL_CLUSTER_NEW(ModuleEnum.PHYSICAL_CLUSTER, "集群新建", 1),
    /**
     * 物理集群: 下线
     */
    PHYSICAL_CLUSTER_OFFLINE(ModuleEnum.PHYSICAL_CLUSTER, "集群下线", 2),
    /**
     * 物理集群:扩容
     */
    PHYSICAL_CLUSTER_CAPACITY(ModuleEnum.PHYSICAL_CLUSTER, "集群扩缩容", 3),
    
    /**
     * 物理集群：重启
     */
    PHYSICAL_CLUSTER_RESTART(ModuleEnum.PHYSICAL_CLUSTER, "重启", 5),
    
    /**
     * 物理集群： 信息修改
     */
    PHYSICAL_CLUSTER_INFO_MODIFY(ModuleEnum.PHYSICAL_CLUSTER, "物理集群信息修改", 6),
    //PHYSICAL_CLUSTER_START(NewModuleEnum.PHYSICAL_CLUSTER, "启动",57),
    /**
     * 物理集群:升级
     */
    PHYSICAL_CLUSTER_UPGRADE(ModuleEnum.PHYSICAL_CLUSTER, "集群升级", 7),
    /**
     * 物理集群:REGION变更
     */
    PHYSICAL_CLUSTER_REGION_CHANGE(ModuleEnum.PHYSICAL_CLUSTER, "REGION变更", 8),
    /**
     * 物理集群：GATEWAY变更
     */
    PHYSICAL_CLUSTER_GATEWAY_CHANGE(ModuleEnum.PHYSICAL_CLUSTER, "GATEWAY变更", 9),
    /**
     * 物理集群：配置文件变更 todo op-task/cluster-config-add
     */
    PHYSICAL_CLUSTER_CONF_FILE_CHANGE(ModuleEnum.PHYSICAL_CLUSTER, "配置文件变更", 10),
    /**
     * 物理集群动：动态配置变更
     */
    PHYSICAL_CLUSTER_DYNAMIC_CONF_CHANGE(ModuleEnum.PHYSICAL_CLUSTER, "动态配置变更", 11),
    /**
     * 我的集群：信息修改
     */
    MY_CLUSTER_INFO_MODIFY(ModuleEnum.MY_CLUSTER, "我的集群信息修改", 12),
    /**
     * 我的集群：申请集群
     */
    MY_CLUSTER_APPLY(ModuleEnum.MY_CLUSTER, "申请集群", 13),
    /**
     * 我的集群：集群扩容
     */
    MY_CLUSTER_CAPACITY(ModuleEnum.MY_CLUSTER, "集群扩缩容", 3),
    
    /**
     * 我的集群：集群下线
     */
    MY_CLUSTER_OFFLINE(ModuleEnum.MY_CLUSTER, "集群下线", 2),
    /**
     * 应用：新建应用
     */
    APPLICATION_CREATE(ModuleEnum.APPLICATION, "新建应用", 17),
    /**
     * 应用：删除应用
     */
    APPLICATION_DELETE(ModuleEnum.APPLICATION, "删除应用", 18),
    
    /**
     * 应用：访问模式
     */
    APPLICATION_ACCESS_MODE(ModuleEnum.APPLICATION, "访问模式", 19),
    /**
     * 应用：访问设置
     */
    APPLICATION_ACCESS_CHANGE(ModuleEnum.APPLICATION,"访问设置变更",69),

    /**
     * 应用：负责人变更
     */
    APPLICATION_OWNER_CHANGE(ModuleEnum.APPLICATION, "负责人变更", 20),
    /**
     * 应用程序：用户
     */
    APPLICATION_USER_CHANGE(ModuleEnum.APPLICATION, "成员变更", 45),
    /**
     * 租户：新增租户
     */
    TENANT_ADD(ModuleEnum.TENANT, "新增租户", 21),
    /**
     * 租户信息修改
     */
    TENANT_INFO_MODIFY(ModuleEnum.TENANT, "租户信息修改", 22),
    /**
     * 索引模板管理：模板创建 todo 新建模版
     */
    TEMPLATE_MANAGEMENT_CREATE(ModuleEnum.TEMPLATE_MANAGEMENT, "模板创建", 23),

    /**
     * 模板管理:模板下线
     */
    TEMPLATE_MANAGEMENT_OFFLINE(ModuleEnum.TEMPLATE_MANAGEMENT, "模板下线", 26),
     TEMPLATE_MANAGEMENT_EDIT_MAPPING(ModuleEnum.TEMPLATE_MANAGEMENT, "编辑MAPPING", 60),
    TEMPLATE_MANAGEMENT_EDIT_SETTING(ModuleEnum.TEMPLATE_MANAGEMENT, "编辑SETTING", 59),
    /**
     * 索引模板管理:索引模板信息修改
     */
    TEMPLATE_MANAGEMENT_INFO_MODIFY(ModuleEnum.TEMPLATE_MANAGEMENT, "索引模板信息修改", 27),
    /**
     * 索引模板管理:升级版本
     */
    TEMPLATE_SERVICE_UPGRADED_VERSION(ModuleEnum.TEMPLATE_SERVICE, "升版本", 28),
    /**
     * 模板服务:DCDR设置
     */
    TEMPLATE_SERVICE_DCDR_SETTING(ModuleEnum.TEMPLATE_SERVICE, "DCDR设置", 29),
    /**
     * 模板服务:索引清理
     */
    TEMPLATE_SERVICE_CLEAN(ModuleEnum.TEMPLATE_SERVICE, "索引清理", 30),
    /**
     * 模板服务:模板扩缩容
     */
    TEMPLATE_SERVICE_CAPACITY(ModuleEnum.TEMPLATE_SERVICE, "模板扩缩容", 31),
   
    
    /**
     * 模板服务
     */
    TEMPLATE_SERVICE(ModuleEnum.TEMPLATE_SERVICE, "模板服务", 33),
    /**
     * 索引管理:创建
     */
    INDEX_MANAGEMENT_CREATE(ModuleEnum.INDEX_MANAGEMENT, "索引创建", 34),
    /**
     * 索引管理:删除索引
     */
    INDEX_MANAGEMENT_DELETE(ModuleEnum.INDEX_MANAGEMENT, "删除索引", 35),
    /**
     * 索引管理:别名调整
     */
    INDEX_MANAGEMENT_ALIAS_MODIFY(ModuleEnum.INDEX_MANAGEMENT, "别名调整", 36),
    INDEX_MANAGEMENT_LABEL_MODIFY(ModuleEnum.INDEX_MANAGEMENT, "标签调整", 56),
        /**
     * 索引模板管理:编辑MAPPING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_MAPPING(ModuleEnum.INDEX_MANAGEMENT, "编辑MAPPING", 24),
    /**
     * 索引模板管理:编辑SETTING
     */
    INDEX_TEMPLATE_MANAGEMENT_EDIT_SETTING(ModuleEnum.INDEX_MANAGEMENT, "编辑SETTING", 25),

    /**
     * 索引管理:操作索引
     */
    INDEX_SERVICE_OP_INDEX(ModuleEnum.INDEX_SERVICE, "操作索引", 38),
    /**
     * 索引服务:执行索引服务
     */
    INDEXING_SERVICE_RUN(ModuleEnum.INDEX_SERVICE, "执行索引服务", 39),
        /**
     * 索引管理:索引读写变更
     */
    INDEX_SERVICE_READ_WRITE_CHANGE(ModuleEnum.INDEX_SERVICE, "索引读写变更", 37),
    /**
     * 查询模板:DSL限流调整
     */
    QUERY_TEMPLATE_DSL_CURRENT_LIMIT_ADJUSTMENT(ModuleEnum.QUERY_TEMPLATE, "DSL限流调整", 40),
    /**
     * 查询模板:DSL查询模板禁用
     */
    QUERY_TEMPLATE_DISABLE(ModuleEnum.QUERY_TEMPLATE, "DSL查询模板禁用", 41),
    /**
     * DSL查询:查询记录
     */
    DSL_QUERY_RECORD(ModuleEnum.DSL_QUERY, "DSL查询记录", 66),
    /**
     * 配置修改
     */
    SETTING_MODIFY(ModuleEnum.SETTING, "配置修改", 42),
    /**
     * 配置添加
     */
    SETTING_ADD(ModuleEnum.SETTING, "新增配置", 43),
    /**
     * 配置删除
     */
    SETTING_DELETE(ModuleEnum.SETTING, "删除配置", 44),
    
    ES_CLUSTER_PLUGINS_ADD(ModuleEnum.ES_CLUSTER_PLUGINS, "新增插件", 47),
    ES_CLUSTER_PLUGINS_EDIT(ModuleEnum.ES_CLUSTER_PLUGINS, "编辑插件", 48),
    ES_CLUSTER_PLUGINS_DELETE(ModuleEnum.ES_CLUSTER_PLUGINS, "删除配置", 49),
    ES_CLUSTER_CONFIG_DELETE(ModuleEnum.ES_CLUSTER_CONFIG, "删除集群配置", 50),
    
    ES_CLUSTER_CONFIG_ADD(ModuleEnum.ES_CLUSTER_CONFIG, "新增集群配置", 51),
    
    ROLE_MANAGER_DELETE(ModuleEnum.ROLE_MANAGER, "删除角色", 58),
    ROLE_MANAGER_CREATE(ModuleEnum.ROLE_MANAGER, "创建角色", 53),
    ROLE_MANAGER_UNBIND_USER(ModuleEnum.ROLE_MANAGER, "角色解绑用户", 54),
    ROLE_MANAGER_BIND_USER(ModuleEnum.ROLE_MANAGER, "角色绑定用户", 55),
    PHYSICAL_CLUSTER_NODE_CHANGE(ModuleEnum.PHYSICAL_CLUSTER, "节点变更", 62);

    /**
     * 模块
     */
    private final ModuleEnum module;
    /**
     * 操作类型
     */
    private final String     operationType;
    private final Integer    code;

    OperateTypeEnum(ModuleEnum module, String operationType, Integer code) {
        this.module = module;
        this.operationType = operationType;
        this.code = code;
    }

    public ModuleEnum getModule() {
        return module;
    }

    public String getOperationType() {
        return operationType;
    }

    public Integer getCode() {
        return code;
    }

    public static Map<Integer, String> getOperationTypeByModule(Integer module) {
        final ModuleEnum moduleEnum = ModuleEnum.getModuleEnum(module);
        if (Objects.isNull(moduleEnum)) {
            return Arrays.stream(OperateTypeEnum.values())
                .map(operateTypeEnum -> Tuples.of(operateTypeEnum.getOperationType(), operateTypeEnum.getCode()))
                .distinct().collect(Collectors.toMap(TupleTwo::v2, TupleTwo::v1));
        }
        return Arrays.stream(OperateTypeEnum.values())
            .filter(operationTypeEnum -> operationTypeEnum.getModule().equals(moduleEnum))
            .map(operateTypeEnum -> Tuples.of(operateTypeEnum.getOperationType(), operateTypeEnum.getCode())).distinct()
            .collect(Collectors.toMap(TupleTwo::v2, TupleTwo::v1));
    }

    public static OperateTypeEnum getOperationTypeEnum(Integer code) {
        return Arrays.stream(OperateTypeEnum.values())
            .filter(operationTypeEnum -> operationTypeEnum.getCode().equals(code)).findFirst().orElse(null);
    }

}