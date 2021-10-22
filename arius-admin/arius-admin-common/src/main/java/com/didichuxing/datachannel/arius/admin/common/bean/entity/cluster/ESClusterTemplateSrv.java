package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import lombok.Data;

@Data
public class ESClusterTemplateSrv {
    /**
     * 索引服务id
     */
    private Integer serviceId;

    /**
     * 索引服务名称
     */
    private String serviceName;

    /**
     * 索引服务所需的最低es版本号
     */
    private String esVersion;
}
