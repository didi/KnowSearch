package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "配置包查询DTO")
public class ESZeusConfigDTO extends BaseDTO  {

    @ApiModelProperty("集群Id")
    private Long  clusterId;

    @ApiModelProperty("集群名称")
    private String clusterName;

    @ApiModelProperty("组件名称")
    private String  enginName;

    @ApiModelProperty("配置文件名称")
    private String  typeName;

    @ApiModelProperty("配置文件内容")
    private String  content;
}
