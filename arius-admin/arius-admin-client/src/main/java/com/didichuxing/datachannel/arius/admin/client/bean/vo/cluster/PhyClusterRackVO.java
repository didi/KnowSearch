package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 物理集群Rack信息
 * @author wangshu
 * @date 2020/10/13
 */
@Data
@ApiModel(description = "物理集群Rack信息")
public class PhyClusterRackVO extends BaseVO {
    /**
     * 集群
     */
    @ApiModelProperty("物理集群名称")
    private String cluster;

    /**
     * rack
     */
    @ApiModelProperty("rack")
    private String rack;

    /**
     * rack
     */
    @ApiModelProperty("是否已经被占用，0：没有，1：已经被占用")
    private Integer usageFlags;
}
