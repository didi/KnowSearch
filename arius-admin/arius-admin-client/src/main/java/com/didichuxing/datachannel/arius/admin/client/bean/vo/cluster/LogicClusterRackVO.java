package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "逻辑集群映射信息")
public class LogicClusterRackVO extends BaseVO {

    @ApiModelProperty("映射ID")
    private Long   id;

    @ApiModelProperty("逻辑集群ID,共享情况下rack可能会对应多个逻辑集群ID")
    private String resourceIds;

    @ApiModelProperty("逻辑集群ID,为兼容取rack对应的逻辑集群的首位")
    private Long resourceId;

    @ApiModelProperty("物理集群名称")
    private String cluster;

    @ApiModelProperty("rack")
    private String rack;

}
