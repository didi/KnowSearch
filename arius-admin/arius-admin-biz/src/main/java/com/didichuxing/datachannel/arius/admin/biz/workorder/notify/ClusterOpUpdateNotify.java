package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterOpUpdateNotify implements NotifyInfo {

    private Integer appId;

    private String  clusterName;

    private String  approver;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台集群升级申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return String.format("您在Arius平台上使用APPID:%s, 对集群%s, 进行升级的工单申请已经审批通过！但还需进行集群下线的具体操作，后续具体进度可以咨询: %s", appId,
            clusterName, approver);
    }
}
