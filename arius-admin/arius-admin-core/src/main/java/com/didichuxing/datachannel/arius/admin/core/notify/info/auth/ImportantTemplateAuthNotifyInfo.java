package com.didichuxing.datachannel.arius.admin.core.notify.info.auth;

import com.didichuxing.datachannel.arius.admin.core.notify.MailTool;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019-07-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportantTemplateAuthNotifyInfo implements NotifyInfo {

    private Integer logicTemplateId;

    private String  templateName;

    private String  ariusConsole;

    @Override
    public String getBizId() {
        return String.valueOf(logicTemplateId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台索引权限申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return MailTool.readMailHtmlFileInJarFile("ImportantTemplateAuthContent.html")
            .replace("{templateName}", templateName).replace("{ariusConsole}", ariusConsole);
    }
}
