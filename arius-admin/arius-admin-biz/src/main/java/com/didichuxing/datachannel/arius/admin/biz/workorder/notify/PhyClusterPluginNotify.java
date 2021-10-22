package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhyClusterPluginNotify implements NotifyInfo {
    private Integer appId;

    private String phyClusterName;

    private String approver;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "物理集群插件变更申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent(){
        StringBuilder content = new StringBuilder();
        content.append("您在Arius平台上使用APPID：");
        content.append(appId);
        content.append("，申请集群：");
        content.append(phyClusterName);
        content.append("插件变更的需求已经被【");
        content.append(approver);
        content.append("】审批通过！但还需进行集群的具体操作，后续具体进度可以咨询：");
        content.append(approver);
        return content.toString();
    }
}
