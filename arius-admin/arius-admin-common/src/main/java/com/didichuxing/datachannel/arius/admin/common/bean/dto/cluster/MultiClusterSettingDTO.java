package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Authoer: zyl
 * @Date: 2022/10/26
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiOperation("动态配置项信息")
public class MultiClusterSettingDTO{

    @ApiModelProperty("集群名称列表")
    private List<String> clusterNameList;

    @ApiModelProperty("动态配置项修改的字段名称")
    private String key;

    @ApiModelProperty("动态修改配置项字段对应的值")
    private Object value;
}

