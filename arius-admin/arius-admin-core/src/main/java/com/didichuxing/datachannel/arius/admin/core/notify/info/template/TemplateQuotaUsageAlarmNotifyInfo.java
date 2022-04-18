package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import static com.didichuxing.datachannel.arius.admin.common.bean.common.LogicResourceConfig.QUOTA_CTL_ALL;

import com.didichuxing.datachannel.arius.admin.core.notify.MailTool;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.PercentUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.Builder;
import lombok.Data;

/**
 * @author linyunan
 * @date 2021-05-14
 */
@Data
@Builder
public class TemplateQuotaUsageAlarmNotifyInfo implements NotifyInfo {

    private IndexTemplateLogic      templateLogic;

    private App                     app;

    private LogicTemplateQuotaUsage templateQuotaUsage;

    private String                  ariusConsole;

    private String                  ctlRange;

    @Override
    public String getBizId() {
        return String.valueOf(templateLogic.getId());
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
        return MailTool
            .readMailHtmlFileInJarFile(QUOTA_CTL_ALL.equals(ctlRange) ? "TemplateQuotaDiskCpuUsageErrorContent.html"
                : "TemplateQuotaDiskUsageErrorContent.html")
            .replace("{templateName}", templateLogic.getName())
            .replace("{templateId}", String.valueOf(templateLogic.getId())).replace("{appName}", app.getName())
            .replace("{appId}", String.valueOf(app.getId())).replace("{appResponsible}", app.getResponsible())
            .replace("{responsible}", templateLogic.getResponsible())
            .replace("{quota}", String.valueOf(templateLogic.getQuota()))
            .replace("{diskUsage}", getDiskUsePercent(templateQuotaUsage))
            .replace("{costDisk}", String.valueOf(templateQuotaUsage.getActualDiskG()))
            .replace("{quotaDisk}", String.valueOf(templateQuotaUsage.getQuotaDiskG()))
            .replace("{cpuUsage}", getCpuUsePercent(templateQuotaUsage))
            .replace("{costCpu}", String.valueOf(templateQuotaUsage.getActualCpuCount()))
            .replace("{quotaCpu}", String.valueOf(templateQuotaUsage.getQuotaCpuCount()))
            .replace("{ariusConsole}", ariusConsole);
    }

    private String getDiskUsePercent(LogicTemplateQuotaUsage templateQuotaUsage) {
        double usage = templateQuotaUsage.getActualDiskG() / templateQuotaUsage.getQuotaDiskG();
        return PercentUtils.getStrWithLimit(usage);
    }

    private String getCpuUsePercent(LogicTemplateQuotaUsage templateQuotaUsage) {
        double usage = templateQuotaUsage.getActualCpuCount() / templateQuotaUsage.getQuotaCpuCount();
        return PercentUtils.getStrWithLimit(usage);
    }
}
