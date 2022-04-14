package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/6
 * @Comment: 逻辑集群region VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群Region信息")
public class ClusterRegionVO extends BaseVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("逻辑集群ID")
    private String logicClusterIds;

    @ApiModelProperty("物理集群名称")
    private String clusterName;

    @ApiModelProperty("Rack列表")
    private String racks;
}
