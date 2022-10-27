package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点信息.
 *
 * @ClassName ClusterNodeInfoVO
 * @Author gyp
 * @Date 2022/10/21
 * @Version 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("节点信息")
public class ClusterNodeInfoVO {

    @ApiModelProperty("节点名称")
    private String                                       nodeName;

    @ApiModelProperty("节点类型")
    private String                                       nodeType;
}