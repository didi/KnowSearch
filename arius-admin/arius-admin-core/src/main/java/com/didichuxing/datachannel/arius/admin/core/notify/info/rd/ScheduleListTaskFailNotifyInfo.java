package com.didichuxing.datachannel.arius.admin.core.notify.info.rd;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.Data;

/**
 * @author zengqiao
 * @date 20/10/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleListTaskFailNotifyInfo implements NotifyInfo {

    private List<ScheduleTaskFailNotifyInfo> scheduleTaskFailNotifyInfoList = new ArrayList<>();

    public void addScheduleTaskFailMsg(ScheduleTaskFailNotifyInfo scheduleTaskFailNotifyInfo) {
        scheduleTaskFailNotifyInfoList.add(scheduleTaskFailNotifyInfo);
    }

    @Override
    public String getBizId() {
        return scheduleTaskFailNotifyInfoList.stream().map(ScheduleTaskFailNotifyInfo::getBizId)
            .collect(Collectors.joining(","));
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        if (CollectionUtils.isEmpty(scheduleTaskFailNotifyInfoList)) {
            return "";
        }

        StringBuilder msgBuilder = new StringBuilder("");
        msgBuilder.append("【调度任务执行失败】\n");

        msgBuilder.append("任务名字：").append(scheduleTaskFailNotifyInfoList.get(0).getTaskName()).append("\n");
        msgBuilder.append("重试链接：").append(scheduleTaskFailNotifyInfoList.get(0).getRetryUrl()).append("\n");
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");

        for (ScheduleTaskFailNotifyInfo scheduleTaskFailNotifyInfo : scheduleTaskFailNotifyInfoList) {
            msgBuilder.append("任务标示：").append(scheduleTaskFailNotifyInfo.getBizId()).append("\n");
            if (StringUtils.isNotBlank(scheduleTaskFailNotifyInfo.getErrorMsg())) {
                msgBuilder.append("异常信息：").append(scheduleTaskFailNotifyInfo.getErrorMsg()).append("\n");
            }
        }

        return msgBuilder.toString();
    }
}