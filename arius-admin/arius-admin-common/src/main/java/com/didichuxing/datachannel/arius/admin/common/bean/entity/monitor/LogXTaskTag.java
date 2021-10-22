package com.didichuxing.datachannel.arius.admin.common.bean.entity.monitor;

import lombok.Data;

@Data
public class LogXTaskTag extends BaseTag {

    private Long syncTaskId;

    private Long logXTaskId;

    private String machineRoom;
}
