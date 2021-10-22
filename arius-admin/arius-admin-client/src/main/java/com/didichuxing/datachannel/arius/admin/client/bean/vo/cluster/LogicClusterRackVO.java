package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/22
 */
@Data
@ApiModel(description = "逻辑集群映射信息")
public class LogicClusterRackVO extends BaseVO {

    /**
     * 主键
     */
    @ApiModelProperty("映射ID")
    private Long   id;

    /**
     * 逻辑资源id
     */
    @ApiModelProperty("逻辑集群ID")
    private Long   resourceId;

    /**
     * 集群
     */
    @ApiModelProperty("物理集群名称")
    private String cluster;

    /**
     * rack 当ownType是2时有值
     */
    @ApiModelProperty("rack")
    private String rack;

}
