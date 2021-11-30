package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor.n9e;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {
    private Long id;
    /**
     * 必填，需要提前建一个组。
     */
    @JSONField(name = "group_id")
    private int groupId;

    private String name;

    /**
     * 告警模式 0 n9e，1 prometheus
     */
    private int type = 0;
    /**
     * 0 启动，1禁用
     */
    private int status;

    /**
     * 告警统计周期，单位为秒
     */
    @JSONField(name = "alert_duration")
    private int alertDuration = 60;

    private Expression expression;

    /**
     * 告警策略起始生效时间，格式为 00:00 - 23:59
     */
    @JSONField(name = "enable_stime")
    private String enableStime;

    /**
     * 告警策略结束生效时间，格式为 00:00 - 23:59
     */
    @JSONField(name = "enable_etime")
    private String enableEtime;

    /**
     * "1 2 3" 生效的天数，多个用空格分隔
     */
    @JSONField(name = "enable_days_of_week")
    private String enableDaysOfWeek;

    /**
     * 告警级别 一级1 二级2 三级3
     */
    private int priority;

    /**
     * "qq dinging", //发送通道
     */
    @JSONField(name = "notify_channels")
    private String notifyChannels;

    /**
     * 告警接收组id 多个用空格分隔
     */
    @JSONField(name = "notify_groups")
    private String notifyGroups;

    /**
     * 告警接收人id 多个用空格分隔
     */
    @JSONField(name = "notify_users")
    private String notifyUsers;

    /**
     * 回调地址，多个用空格分隔
     */
    private String callbacks;

    /**
     * 查看的时候返回
     */
    @JSONField(name = "notify_users_detail")
    List<UserInfo> notifyUsersDetail;

    @JSONField(name = "notify_groups_detail")
    List<UserGroup> notifyGroupsDetail;
}
