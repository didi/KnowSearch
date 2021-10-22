package com.didichuxing.datachannel.arius.admin.core.notify.info.cluster;

import java.util.Date;

import com.didichuxing.datachannel.arius.admin.common.util.AriusDateUtils;
import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

/**
 * @author zengqiao
 * @date 20/10/13
 */
public class ClusterTemplatePhysicalMetaErrorNotifyInfo implements NotifyInfo {
    private String cluster;

    private String errMsg;

    public ClusterTemplatePhysicalMetaErrorNotifyInfo(String cluster, String errMsg) {
        this.cluster = cluster;
        this.errMsg = errMsg;
    }

    @Override
    public String getBizId() {
        return this.cluster;
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        StringBuilder msgBuilder = new StringBuilder();
        msgBuilder.append("【物理集群索引异常】\n");
        msgBuilder.append("集群名字：").append(cluster).append("\n");
        msgBuilder.append("异常信息：").append(errMsg).append("\n");
        msgBuilder.append("发送时间：").append(AriusDateUtils.date2Str(new Date(), null)).append("\n");
        return msgBuilder.toString();
    }
}