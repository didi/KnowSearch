package com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response;

import lombok.Data;

import java.util.List;

@Data
public class EcmCreateApp extends EcmOperateAppBase {
    private List<String> hostList;

    public EcmCreateApp(Integer taskId, List<String> hostList) {
        this.taskId = taskId;
        this.hostList = hostList;
    }
}