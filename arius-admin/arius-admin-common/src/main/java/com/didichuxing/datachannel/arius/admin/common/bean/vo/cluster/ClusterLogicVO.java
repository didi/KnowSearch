package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 集群基本类
 *
 * @author wangshu
 * @date 2020/09/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("逻辑集群")
public class ClusterLogicVO extends BaseVO implements Comparable<ClusterLogicVO> {

    @ApiModelProperty("集群ID")
    private Long                         id;

    @ApiModelProperty("集群名字")
    private String                       name;

    @ApiModelProperty("数据中心")
    private String                       dataCenter;

    /** @see ClusterResourceTypeEnum */
    @ApiModelProperty("集群类型（1：公共；2：独立）")
    private Integer                      type;

    @ApiModelProperty("所属应用ID")
    private Integer                      projectId;

    @ApiModelProperty("所属应用名")
    private String                       projectName;

    @ApiModelProperty("是否关联物理集群")
    private Boolean                      phyClusterAssociated;

    @ApiModelProperty("关联物理集群列表")
    private List<String>                 associatedPhyClusterName;

    @ApiModelProperty("数据节点数")
    private Integer                      dataNodesNumber;

    @ApiModelProperty("gateway地址")
    private String                       gatewayAddress;

    @ApiModelProperty("责任人：后续进行下线")
    @Deprecated
    private String                       responsible;

    @ApiModelProperty("备注")
    private String                       memo;

    @ApiModelProperty("成本部门ID")
    private String                       libraDepartmentId;

    @ApiModelProperty("成本部门名称")
    private String                       libraDepartment;

    @ApiModelProperty("服务等级")
    private Integer                      level;

    @ApiModelProperty("配额")
    private Double                       quota;

    @ApiModelProperty("权限记录ID")
    private Long                         authId;

    /** @see ProjectClusterLogicAuthEnum */
    @ApiModelProperty("权限,1:配置管理,2:访问,-1:无权限")
    private Integer                      authType;

    @ApiModelProperty("权限,1:配置管理,2:访问,-1:无权限")
    private String                       permissions;

    @ApiModelProperty("ES集群版本")
    private String                       esClusterVersion;

    @ApiModelProperty("集群状态信息")
    private ConsoleClusterStatusVO       clusterStatus;

    @ApiModelProperty("配置")
    private String                       configJson;

    @ApiModelProperty("集群所开放的索引服务")
    private List<ESClusterTemplateSrvVO> esClusterTemplateSrvVOS;

    @ApiModelProperty("集群角色信息")
    private List<ESClusterRoleVO>        esClusterRoleVOS;

    @ApiModelProperty("集群健康状态")
    private Integer                      health;

    @ApiModelProperty("磁盘使用率")
    private Double                       diskUsagePercent;

    @ApiModelProperty("磁盘总量")
    private Long                         diskTotal;

    @ApiModelProperty("磁盘使用量")
    private Long                         diskUsage;

    @Override
    public int compareTo(ClusterLogicVO o) {
        if (null == o) {
            return -1;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}