package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 物理集群Rack信息
 * @author wangshu
 * @date 2020/10/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理集群Rack信息")
public class PhyClusterRackVO extends BaseVO {

    @ApiModelProperty("物理集群名称")
    private String  cluster;

    @ApiModelProperty("rack")
    private String  rack;

    @ApiModelProperty("是否已经划分到region中，0：没有，1：已经被占用")
    private Integer usageFlags;
}
