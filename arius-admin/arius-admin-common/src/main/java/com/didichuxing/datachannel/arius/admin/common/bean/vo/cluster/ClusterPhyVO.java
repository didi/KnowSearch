package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author d06679
 * @date 2019/3/19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群信息")
public class ClusterPhyVO extends BaseVO implements Comparable<ClusterPhyVO> {

    @ApiModelProperty("集群ID")
    private Integer                                      id;

    @ApiModelProperty("集群名字")
    private String                                       cluster;

    @ApiModelProperty("描述")
    private String                                       desc;

    @ApiModelProperty("tcp地址-读")
    private String                                       readAddress;

    @ApiModelProperty("tcp地址-写")
    private String                                       writeAddress;

    @ApiModelProperty("http地址-读")
    private String                                       httpAddress;

    @ApiModelProperty("http地址-写")
    private String                                       httpWriteAddress;

    /** @see ESClusterTypeEnum */
    @ApiModelProperty("集群类型(3 docker, 4 host)")
    private Integer                                      type;

    @ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
    private String                                       tags;

    @ApiModelProperty("数据中心")
    private String                                       dataCenter;

    @ApiModelProperty("单节点实例数 host物理集群必填")
    private Integer                                      pidCount;

    @ApiModelProperty("机器规格  host物理集群必填")
    private String                                       machineSpec;

    @ApiModelProperty("服务等级")
    private Integer                                      level;

    @ApiModelProperty("es版本")
    private String                                       esVersion;

    @ApiModelProperty("镜像地址")
    private String                                       imageName;

    @ApiModelProperty("集群所开放的索引服务")
    private List<ESClusterTemplateSrvVO>                 esClusterTemplateSrvVOS;

    @ApiModelProperty("集群角色信息")
    private List<ESClusterRoleVO>                        esClusterRoleVOS;

    @ApiModelProperty("磁盘使用率")
    private Double                                       diskUsagePercent;

    @ApiModelProperty("磁盘总量")
    private Long                                         diskTotal;

    @ApiModelProperty("磁盘使用量")
    private Long                                         diskUsage;

    @ApiModelProperty("集群密码")
    private String                                       password;

    @ApiModelProperty("机房")
    private String                                       idc;

    @ApiModelProperty("client运行模式（0：读写共享 1：读写分离）")
    private Integer                                      runMode;

    @ApiModelProperty("指定用写client的action")
    private String                                       writeAction;

    @ApiModelProperty("集群状态")
    private Integer                                      health;

    @ApiModelProperty("活跃的分片数目")
    private Long                                         activeShardNum;

    @ApiModelProperty("IaaS平台类型")
    private String                                       platformType;

    @ApiModelProperty("SLB地址")
    private String                                       slbAddress;

    /**
     * @see ClusterResourceTypeEnum
     */
    @ApiModelProperty("集群资源类型(-1 未知 1 共享 2 独立 3 独享)")
    private Integer                                      resourceType;
    //TODO 0.3.2移除
    @Deprecated
    @ApiModelProperty("gateway地址")
    private String                                       gatewayUrl;
    @ApiModelProperty("是否支持 zeus:（扩缩容、升级、重启、配置变更）")
    private Boolean supportZeus = true;
    @ApiModelProperty("逻辑集群与对应region的列表")
    private List<Tuple<ClusterLogicVOWithProjects, ClusterRegionVO>> logicClusterAndRegionList;
    @ApiModelProperty("物理集群绑定的逻辑集群")
    private List<String> bindLogicCluster;
    //TODO 0.3.2新增
    @ApiModelProperty("代理地址")
    private String proxyAddress;
    @ApiModelProperty("绑定gateway")
    private Integer gatewayId;
    @ApiModelProperty("是否为ecm接入")
    private Boolean ecmAccess;

    /**
     * 添加逻辑集群
     *
     * @param clusterLogicVO 逻辑集群
     * @param regionVO    region
     */
    public void addLogicCluster(ClusterLogicVOWithProjects clusterLogicVO, ClusterRegionVO regionVO) {
        if (null == logicClusterAndRegionList) {
            logicClusterAndRegionList = Lists.newArrayList();
        }
        if (clusterLogicVO != null || regionVO != null) {
            Tuple<ClusterLogicVOWithProjects, ClusterRegionVO> tuple = new Tuple<>(clusterLogicVO, regionVO);
            logicClusterAndRegionList.add(tuple);
        }
    }

    @Override
    public int compareTo(ClusterPhyVO o) {
        if (null == o) {
            return 0;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}