package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.common.util.Getter;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppCreateNotify implements NotifyInfo {

    private Integer appId;

    private String  appName;

    private String  verifyCode;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台应用申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return Getter.getHtmlFileInJarFile("html/AppCreatedContent.html").replace("{appName}", appName)
            .replace("{appId}", String.valueOf(appId)).replace("{appVerifyCode}", verifyCode);
    }
}
