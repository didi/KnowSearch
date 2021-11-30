package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterTemplateSrv {
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
