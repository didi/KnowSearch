package com.didichuxing.datachannel.arius.admin.core.notify;

import java.util.Set;

import lombok.Data;

@Data
public class NotifyTaskInfo {

    /**
     * 通知任务类型
     */
    private String      type;

    /**
     * 任务发送渠道
     */
    private Set<String> channels;

    /**
     * 每天最大发送次数
     * -1 不限制
     */
    private Integer     maxSendCountPerDay;

    /**
     * 发送间隔 分钟
     * -1 不限制
     */
    private Integer     sendIntervalMinutes;

    /**
     * mock的接收人
     */
    private String      mockReceiver;

}
