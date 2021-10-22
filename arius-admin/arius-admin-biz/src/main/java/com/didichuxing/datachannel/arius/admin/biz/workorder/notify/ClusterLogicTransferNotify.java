package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyConstant;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.Builder;

/**
 * Created by linyunan on 2021-06-17
 */
@Builder
public class ClusterLogicTransferNotify implements NotifyInfo {

    private String  clusterLogicName;

    private Integer currentAppId;

    private Integer targetAppId;

    private Integer sourceAppId;

    private String  targetResponsible;

    @Override
    public String getBizId() {
        return String.valueOf(targetAppId);
    }

    @Override
    public String getTitle() {
        return NotifyConstant.ARIUS_MAIL_NOTIFY + "搜索平台逻辑集群权限转让申请成功";
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    @Override
    public String getSmsContent() {
        return null;
    }

    @Override
    public String getVoiceContent() {
        return null;
    }

    private String getContent() {
        StringBuilder content = new StringBuilder();
        content.append("您在Arius平台上使用APPID：");
        content.append(currentAppId);
        content.append("，将逻辑集群：");
        content.append(clusterLogicName);
        content.append(String.format("的管理权限由项目Id：%s转让给目标项目ID：", sourceAppId));
        content.append(targetAppId);
        content.append("工单已经审批通过！");
        return content.toString();
    }
}
