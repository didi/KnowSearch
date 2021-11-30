package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-10-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "物理集群条件查询信息")
public class ClusterPhyConditionDTO extends ESClusterDTO {
    /**
     * @see AppClusterPhyAuthEnum
     */
    @ApiModelProperty("权限类型 1:配置管理, 2:访问, -1:无权限")
    private Integer authType;
}
