package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;

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
    @ApiModelProperty("集群类型(-1 未知 3 docker集群 4 host集群 5 VM集群)")
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
    private List<ESClusterRoleHostDTO> roleClusterHosts;

    @ApiModelProperty("描述")
    private String                     phyClusterDesc;

    @ApiModelProperty("是否划分集群")
    private Boolean                    divide;

    @ApiModelProperty("Region Racks列表, value 为r1, r2、r3")
    private List<String>               regionRacks;

    @ApiModelProperty("逻辑集群名称")
    private String                     logicCluster;

    @ApiModelProperty("集群接入开启的索引服务")
    private String                     templateSrvs;

    @ApiModelProperty("集群认证信息：'user:password'")
    private String                     password;

    @ApiModelProperty("录入规则 0 自动获取 1 全量录入")
    private Integer importRule;

    @ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
    private String tags;
}
