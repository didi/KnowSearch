package com.didichuxing.datachannel.arius.admin.core.notify;

import static com.didichuxing.datachannel.arius.admin.core.notify.NotifyChannelEnum.EMAIL;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * 通知渠道
 *
 * @author didi
 */
public enum NotifyTaskTypeEnum {

                                /**
                                 * 责任人即将离职任务
                                 */
                                RESPONSIBLE_LEAVING("ResponsibleLeaving", Sets.newHashSet(EMAIL.getName()), 1, 60),

                                /**
                                 * 下线废弃索引
                                 */
                                DROP_INVALID_TEMPLATE("DropInvalidTemplate", Sets.newHashSet(EMAIL.getName()), 1, 60),

                                /**
                                 * 下线无数据无查询模板
                                 */
                                DROP_UNHEALTHY_TEMPLATE("DropUnhealthyTemplate", Sets.newHashSet(EMAIL.getName()), 1,
                                                        60),

                                /**
                                 * 周报发送
                                 */
                                REPORT_SERVE("reportServe", Sets.newHashSet(EMAIL.getName()), 7, 60),

                                /**
                                 * 模板settings发生变更
                                 */
                                TEMPLATE_SETTINGS_CHANGED("settingsChanged", Sets.newHashSet(), 1, 60),

                                /**
                                 * 模板别名发生变更
                                 */
                                TEMPLATE_ALIASES_CHANGED("aliasesChanged", Sets.newHashSet(), 1, 60),

                                /**
                                 * 模板types发生变更
                                 */
                                TEMPLATE_PROPERTIES_TYPES_CHANGED("typesChanged", Sets.newHashSet(), 1, 60),

                                /**
                                 * 工单通知：appCreate
                                 */
                                WORK_ORDER_APP_CREATE("workOrderAppCreate", Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：clusterOpIndecrease
                                 */
                                WORK_ORDER_CLUSTER_OP_INDECREASE("workOrderClusterOpIndecrease",
                                                                 Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：clusterOpNew
                                 */
                                WORK_ORDER_CLUSTER_OP_NEW("workOrderClusterOpNew", Sets.newHashSet(EMAIL.getName()),
                                                          1000, 60),
                                /**
                                 * 工单通知：clusterOpNew
                                 */
                                WORK_ORDER_CLUSTER_OP_PLUG_OPERATING("logicClusterPlugOperating",
                                                                     Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：clusterOpOffline
                                 */
                                WORK_ORDER_CLUSTER_OP_OFFLINE("workOrderClusterOpOffline",
                                                              Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：clusterOpRestart
                                 */
                                WORK_ORDER_CLUSTER_OP_RESTART("workOrderClusterOpRestart",
                                                              Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：clusterOpUpdate
                                 */
                                WORK_ORDER_CLUSTER_OP_UPDATE("workOrderClusterOpUpdate",
                                                             Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：logicClusterAuth
                                 */
                                WORK_ORDER_LOGIC_CLUSTER_AUTH("workOrderLogicClusterAuth",
                                                              Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：logicClusterCreate
                                 */
                                WORK_ORDER_LOGIC_CLUSTER_CREATE("workOrderLogicClusterCreate",
                                                                Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：logicClusterIndecrease
                                 */
                                WORK_ORDER_LOGIC_CLUSTER_INDECREASE("workOrderLogicClusterIndecreate",
                                                                    Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：logicClusterPlugin
                                 */
                                WORK_ORDER_LOGIC_CLUSTER_PLUGIN("workOrderLogicClusterPlugin",
                                                                Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：queryDSLLimit
                                 */
                                WORK_ORDER_QUERY_DSL_LIMIT("workOrderQueryDSLLimit", Sets.newHashSet(EMAIL.getName()),
                                                           1000, 60),
                                /**
                                 * 工单通知：phyClusterPlugin
                                 */
                                WORK_ORDER_PHY_CLUSTER_PLUGIN("workOrderPhyClusterPlugin",
                                                              Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：templateAuth
                                 */
                                WORK_ORDER_TEMPLATE_AUTH("workOrderTemplateAuth", Sets.newHashSet(EMAIL.getName()),
                                                         1000, 60),

                                /**
                                 * 工单通知：templateCreate
                                 */
                                WORK_ORDER_TEMPLATE_CREATE("workOrderTemplateCreate", Sets.newHashSet(EMAIL.getName()),
                                                           1000, 60),

                                /**
                                 * 工单通知：templateIndecrease
                                 */
                                WORK_ORDER_TEMPLATE_INDECREASE("workOrderTemplateIndecrease",
                                                               Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：templateQueryDsl
                                 */
                                WORK_ORDER_TEMPLATE_QUERY_DSL("workOrderTemplateQueryDsl",
                                                              Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：模板转让
                                 */
                                WORK_ORDER_TEMPLATE_TRANSFER("workOrderTemplateTransfer",
                                                             Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 工单通知：逻辑集群转让
                                 */
                                WORK_ORDER_CLUSTER_LOGIC_TRANSFER("clusterLogicTransfer",
                                                                  Sets.newHashSet(EMAIL.getName()), 1000, 60),

                                /**
                                 * 内部周期任务执行失败
                                 */
                                SCHEDULE_TASK_FAILED("scheduledTaskFailed", Sets.newHashSet(), -1, -1),

                                /**
                                 * OP通知：DCDR主从切换
                                 */
                                OP_DCDR_SWITCH_MASTER("opDcdrSwitchMaster", Sets.newHashSet(), -1, -1),

                                /**
                                 * 集群物理模版元信息错误通知
                                 */
                                CLUSTER_TEMPLATE_PHYSICAL_META_ERROR("clusterTemplatePhysicalMetaError",
                                                                     Sets.newHashSet(), -1, -1),

                                /**
                                 * 物理模版元信息错误通知
                                 */
                                TEMPLATE_PHYSICAL_META_ERROR("templatePhysicalMetaError", Sets.newHashSet(), -1, -1),

                                /**
                                 * 逻辑模版元信息错误通知
                                 */
                                TEMPLATE_LOGICAL_META_ERROR("templateLogicMetaError", Sets.newHashSet(), -1, -1),

                                /**
                                 * 模版字段长度超过限制导致同步模版失败通知
                                 */
                                TEMPLATE_MAPPING_FIELD_LIMIT_ERROR("templateMappingFieldLimitError", Sets.newHashSet(),
                                                                   -1, -1),

                                /**
                                 * 资源元信息错误通知
                                 */
                                RESOURCE_META_ERROR("resourceMetaError", Sets.newHashSet(), -1, -1),

                                /**
                                 * 容量规划任务通知
                                 */
                                CAPACITY_PLAN_TASK("capacityPlanTask", Sets.newHashSet(), -1, -1),

                                /**
                                 * 容量规划任务通知
                                 */
                                CAPACITY_PLAN_AREA_META_ERROR("capacityPlanAreaMetaError", Sets.newHashSet(), -1, -1),

                                /**
                                 * App资源移交
                                 */
                                ARIUS_USER_OFFLINE_TRANSFER_APP("ariusUserOfflineTransferApp",
                                                                Sets.newHashSet(EMAIL.getName()), -1, -1),

                                /**
                                 * 模板资源移交
                                 */
                                ARIUS_USER_OFFLINE_TRANSFER_TEMPLATE_AND_RESOURCE("ariusUserOfflineTransferTemplateAndResource",
                                                                                  Sets.newHashSet(EMAIL.getName()), -1,
                                                                                  -1),

                                /**
                                 * 重要模版确认
                                 */
                                IMPORTANT_TEMPLATE_AUTH("ImportantTemplateAuth", Sets.newHashSet(EMAIL.getName()), -1,
                                                        -1),
                                /**
                                 * 模板quota使用率告警通知
                                 */
                                TEMPLATE_QUOTA_USAGE_ALARM_ERROR("templateQuotaUsageAlarm",
                                                                 Sets.newHashSet(EMAIL.getName()), -1, -1)

    ;

    /**
     * 任务名称
     */
    private final String      name;

    /**
     * 任务发送渠道
     */
    private final Set<String> channels;

    /**
     * 每天最大发送次数
     * -1 不限制
     */
    private final Integer     maxSendCountPerDay;

    /**
     * 发送间隔 分钟
     * -1 不限制
     */
    private final Integer     sendIntervalMinutes;

    NotifyTaskTypeEnum(String name, Set<String> channels, Integer maxSendCountPerDay, Integer sendIntervalMinutes) {
        this.name = name;
        this.channels = channels;
        this.maxSendCountPerDay = maxSendCountPerDay;
        this.sendIntervalMinutes = sendIntervalMinutes;
    }

    public String getName() {
        return name;
    }

    public Set<String> getChannels() {
        return channels;
    }

    public Integer getMaxSendCountPerDay() {
        return maxSendCountPerDay;
    }

    public Integer getSendIntervalMinutes() {
        return sendIntervalMinutes;
    }
}
