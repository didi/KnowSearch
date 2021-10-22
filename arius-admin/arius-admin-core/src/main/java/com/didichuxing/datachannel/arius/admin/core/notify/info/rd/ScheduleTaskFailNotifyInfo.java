package com.didichuxing.datachannel.arius.admin.core.notify.info.rd;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;

import lombok.Data;

/**
 * @author zengqiao
 * @date 20/10/12
 */
@Data
public class ScheduleTaskFailNotifyInfo implements NotifyInfo {

    private String taskName;

    private String bizId;

    private String retryUrl;

    private String errorMsg;

    public ScheduleTaskFailNotifyInfo(String taskName, String bizId, String retryUrl) {
        this.taskName = taskName;
        this.bizId = bizId;
        this.retryUrl = retryUrl;
    }

    public ScheduleTaskFailNotifyInfo(String taskName, String bizId, String retryUrl, String errorMsg) {
        this.taskName = taskName;
        this.bizId = bizId;
        this.retryUrl = retryUrl;
        this.errorMsg = errorMsg;
    }

    @Override
    public String getBizId() {
        return bizId;
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("【调度任务执行失败】\n");
        msgBuilder.append("任务名字：").append(taskName).append("\n");
        msgBuilder.append("任务标示：").append(bizId).append("\n");
        msgBuilder.append("重试链接：").append(retryUrl).append("\n");
        if (StringUtils.isNotBlank(errorMsg)) {
            msgBuilder.append("异常信息：").append(errorMsg).append("\n");
        }
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");
        return msgBuilder.toString();
    }
}