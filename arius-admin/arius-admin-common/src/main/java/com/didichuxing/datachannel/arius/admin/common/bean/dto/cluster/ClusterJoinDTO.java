package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
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
    private Integer                    projectId;

    @ApiModelProperty("集群名字")
    private String                     cluster;

    

    @ApiModelProperty("es版本")
    private String                     esVersion;

    @ApiModelProperty("集群角色 对应主机列表")
    private List<ESClusterRoleHostDTO> roleClusterHosts;

    @ApiModelProperty("描述")
    private String                     phyClusterDesc;

    @ApiModelProperty("集群认证信息：'user:password'")
    private String                     password;

    @ApiModelProperty("录入规则 0 自动获取 1 全量录入")
    private Integer                    importRule;

    @ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
    private String                     tags;

    @ApiModelProperty("数据中心")
    private String                     dataCenter;

    @ApiModelProperty("IaaS平台类型")
    private String                     platformType;

    @ApiModelProperty("集群资源类型(-1 未知 1 共享 2 独立 3 独享)")
    private Integer                    resourceType;

    @ApiModelProperty("kibana外链地址")
    private String                    kibanaAddress;

    @ApiModelProperty("cerebro外链地址")
    private String                    cerebroAddress;
}