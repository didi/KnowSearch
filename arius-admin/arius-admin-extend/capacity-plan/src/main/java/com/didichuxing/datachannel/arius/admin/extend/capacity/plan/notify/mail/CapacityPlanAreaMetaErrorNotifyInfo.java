package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.notify.mail;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import com.didichuxing.datachannel.arius.admin.extend.capacity.plan.bean.entity.CapacityPlanArea;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zengqiao
 * @date 20/10/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacityPlanAreaMetaErrorNotifyInfo implements NotifyInfo {

    private CapacityPlanArea area;

    private List<String>     errMsgs;

    @Override
    public String getBizId() {
        return String.valueOf(area.getId());
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("【容量规划area元数据异常】\n");
        msgBuilder.append("areaID：").append(area.getResourceId()).append("\n");
        msgBuilder.append("异常信息：").append(String.join(",", errMsgs)).append("\n");
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");
        return msgBuilder.toString();
    }
}