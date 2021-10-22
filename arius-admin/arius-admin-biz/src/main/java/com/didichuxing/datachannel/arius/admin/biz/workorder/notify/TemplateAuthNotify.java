package com.didichuxing.datachannel.arius.admin.biz.workorder.notify;

import com.didichuxing.datachannel.arius.admin.core.notify.NotifyInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateAuthNotify implements NotifyInfo {

    private Integer appId;

    private String  templateName;

    private boolean addClusterAuth;

    private String  clusterName;

    @Override
    public String getBizId() {
        return String.valueOf(appId);
    }

    @Override
    public String getMailContent() {
        return getContent();
    }

    private String getContent() {
        if (addClusterAuth) {
            return String.format(
                "您在Arius平台上使用APPID：%s，申请索引：%s 的权限工单已经审批通过，" + "同时已为您添加索引所在集群 %s 的访问权限。现在可以使用该appid来查询或写入该索引了！", appId,
                templateName, clusterName);
        } else {
            return String.format("您在Arius平台上使用APPID：%s，申请索引：%s 的权限工单已经审批通过！" + "现在可以使用该appid来查询或写入该索引了！", appId,
                templateName);
        }
    }
}
