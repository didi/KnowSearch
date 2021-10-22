package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogicClusterAuthNotify implements NotifyInfo {

    private Integer appId;

    private String  logicClusterName;

    private String  approver;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台集群权限申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        return String.format("您在Arius平台上使用APPID:%s, 申请集群【%s】的权限已被【%s】审核通过!", appId, logicClusterName, approver);
    }

}
