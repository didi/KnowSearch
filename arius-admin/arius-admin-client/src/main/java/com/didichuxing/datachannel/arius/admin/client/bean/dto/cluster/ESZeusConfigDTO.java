package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "配置包查询DTO")
public class ESZeusConfigDTO extends BaseDTO  {

    /**
     * 集群ID
     */
    @ApiModelProperty("集群Id")
    private Long  clusterId;

    /**
     * 集群ID
     */
    @ApiModelProperty("集群名称")
    private String clusterName;

    /**
     * 组件名称
     */
    @ApiModelProperty("组件名称")
    private String  enginName;

    /**
     * 配置文件名称
     */
    @ApiModelProperty("配置文件名称")
    private String  typeName;

    /**
     * 配置文件内容
     */
    @ApiModelProperty("配置文件内容")
    private String  content;
}
