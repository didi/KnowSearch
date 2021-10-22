package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * es集群索引服务信息
 * @author zhaoqingrong
 */
@Data
@ApiModel(description = "es集群索引服务信息")
public class ESClusterTemplateSrvVO {

    @ApiModelProperty("索引服务id")
    private Integer serviceId;

    @ApiModelProperty("索引服务名称")
    private String serviceName;

    @ApiModelProperty("索引服务所需的最低es版本号")
    private String esVersion;
}
