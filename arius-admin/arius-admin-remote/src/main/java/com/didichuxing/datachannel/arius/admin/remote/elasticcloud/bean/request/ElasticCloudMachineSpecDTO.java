package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElasticCloudMachineSpecDTO {

    /**
     * 容器CPU
     */
    private String cpu;

    /**
     * 容器Memory
     */
    private String memory;
}
