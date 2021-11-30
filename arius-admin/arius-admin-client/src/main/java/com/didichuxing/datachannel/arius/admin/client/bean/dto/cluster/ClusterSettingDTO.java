package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiOperation("动态配置项信息")
public class ClusterSettingDTO extends BaseDTO {

    @ApiModelProperty("集群名称")
    private String clusterName;

    @ApiModelProperty("动态配置项修改的字段名称")
    private String key;

    @ApiModelProperty("动态修改配置项字段对应的值")
    private Object value;
}
