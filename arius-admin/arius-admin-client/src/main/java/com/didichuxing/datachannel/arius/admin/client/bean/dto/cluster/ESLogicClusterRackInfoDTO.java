package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/22
 */
@Data
@ApiModel(description = "逻辑集群映射信息")
public class ESLogicClusterRackInfoDTO extends BaseDTO {

    /**
     * 主键
     */
    @ApiModelProperty("映射ID")
    private Long id;

    /**
     * 逻辑资源id
     */
    @ApiModelProperty("逻辑集群ID")
    @JSONField(alternateNames = {"resourceId", "logicClusterId"})
    private Long logicClusterId;

    /**
     * 集群
     */
    @ApiModelProperty("物理集群名称")
    @JSONField(alternateNames = {"cluster", "phyClusterName"})
    private String phyClusterName;

    /**
     * rack 当ownType是2时有值
     */
    @ApiModelProperty("rack")
    @JSONField(alternateNames = {"rack", "racks"})
    private String racks;

}
