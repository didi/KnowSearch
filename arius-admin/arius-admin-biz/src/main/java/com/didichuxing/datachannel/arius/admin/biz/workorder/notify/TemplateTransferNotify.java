package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateTransferNotify implements NotifyInfo {
    private Integer appId;

    private Integer tgtAppId;

    private String  templateName;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台索引权限转让申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder content = new StringBuilder();
        content.append("您在Arius平台上使用APPID：");
        content.append(appId);
        content.append("，将索引：");
        content.append(templateName);
        content.append("的管理权限转让给目标APPID：");
        content.append(tgtAppId);
        content.append("工单已经审批通过！");
        return content.toString();
    }
}
