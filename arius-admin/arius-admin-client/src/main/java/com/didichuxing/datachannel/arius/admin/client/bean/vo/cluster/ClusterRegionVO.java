package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Author: lanxinzheng
 * @Date: 2021/1/6
 * @Comment: 逻辑集群region VO
 */
@Data
@ApiModel(description = "集群Region信息")
public class ClusterRegionVO extends BaseVO {

    /**
     * region ID
     */
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 逻辑集群ID
     */
    @ApiModelProperty("逻辑集群ID")
    private Long logicClusterId;

    /**
     * 物理集群名称
     */
    @ApiModelProperty("物理集群名称")
    private String clusterName;

    /**
     * Rack列表
     */
    @ApiModelProperty("Rack列表")
    private String racks;
}
