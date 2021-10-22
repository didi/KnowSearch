package com.didichuxing.datachannel.arius.admin.core.notify.info.template;

import static com.didichuxing.datachannel.arius.admin.client.bean.common.LogicResourceConfig.QUOTA_CTL_ALL;

import com.didichuxing.datachannel.arius.admin.core.notify.MailTool;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.LogicTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.PercentUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import com.google.common.collect.Lists;

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

    /**
     * 解析格式化的字符串为字符串列表
     * @param formattedData 格式化的字符串
     * @return
     */
    private List<String> parseArrays(String formattedData) {
        return Lists.newArrayList(formattedData.split(","));
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
