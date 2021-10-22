package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreateNotify implements NotifyInfo {

    private Integer appId;

    private String  templateName;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台模版创建申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return String.format("您在Arius平台上使用APPID:%s, 创建模版【%s】的工单审核通过!", appId, templateName);
    }
}
