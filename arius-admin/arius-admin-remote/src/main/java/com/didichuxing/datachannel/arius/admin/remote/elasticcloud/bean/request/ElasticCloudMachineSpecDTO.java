package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ElasticCloudMachineSpecDTO extends BaseDTO {

    private static final long serialVersionUID = -3594275317504067611L;
    /**
     * 容器CPU
     */
    private String cpu;

    /**
     * 容器Memory
     */
    private String memory;
}
