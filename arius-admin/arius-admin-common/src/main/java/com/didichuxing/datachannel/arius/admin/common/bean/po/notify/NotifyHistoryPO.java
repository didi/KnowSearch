package com.didichuxing.datachannel.arius.admin.common.bean.po.notify;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyHistoryPO extends BasePO {

    /**
     * 主键
     */
    private Long   id;

    /**
     * 任务
     */
    private String taskType;

    /**
     * 业务主键
     */
    private String bizId;

    /**
     * 接收人
     */
    private String receiver;

    /**
     * 渠道
     */
    private String channel;

}
