package com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linyunan
 * @date 2021-03-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterJoinDTO extends BaseDTO {

    /**
     * @see ESClusterTypeEnum
     */
    @ApiModelProperty("集群类型(-1 未知 3 docker集群 4 host集群)")
    private Integer                    type;

    @ApiModelProperty("所属项目id")
    private Integer                    appId;

    @ApiModelProperty("集群名字")
    private String                     cluster;

    @ApiModelProperty("责任人")
    private String                     responsible;

    @ApiModelProperty("es版本")
    private String                     esVersion;

    @ApiModelProperty("集群角色 对应主机列表")
    private List<ESRoleClusterHostDTO> roleClusterHosts;

    @ApiModelProperty("描述")
    private String                     phyClusterDesc;

    @ApiModelProperty("是否划分集群")
    private Boolean                    divide;

    @ApiModelProperty("Region Racks列表, value 为r1, r2、r3")
    private List<String>               regionRacks;

    @ApiModelProperty("逻辑集群名称")
    private String                     logicCluster;
}
