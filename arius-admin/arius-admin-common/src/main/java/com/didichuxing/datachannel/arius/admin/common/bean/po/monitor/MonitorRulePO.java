package com.didichuxing.datachannel.arius.admin.common.bean.po.monitor;

import com.baomidou.mybatisplus.annotation.TableName;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("monitor_rule")
public class MonitorRulePO extends BasePO {
    private Long id;

    /**
     * 告警策略名称
     */
    private String name;
    /**
     * 夜莺告警策略id
     */
    private Long alertRuleId;
    /**
     * 项目ID
     */
    private Long appId;
    /**
     * 告警等级. 1 2 3
     */
    private Integer priority;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 监控类型 cluster.物理集群; template.物理模版; node.节点
     */
    private String category;
    /**
     * 监控对象名称,多个用逗号隔开
     */
    private String objectNames;
    /**
     * 监控指标
     */
    private String metrics;
    /**
     * 告警组id，多个用逗号隔开
     */
    private String notifyGroups;
    /**
     * 告警组成员，多个用逗号隔开
     */
    private String notifyUsers;
    /**
     * 状态  -1 删除, 0 启用, 1 停用.
     */
    private Integer status;
}