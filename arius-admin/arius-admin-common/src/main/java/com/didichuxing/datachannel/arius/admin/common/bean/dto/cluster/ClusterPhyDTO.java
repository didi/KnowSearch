package com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "集群信息")
public class ClusterPhyDTO extends PageDTO {

    @ApiModelProperty("集群ID")
    private Integer                    id;

    @ApiModelProperty("集群名字")
    private String                     cluster;

    @ApiModelProperty("描述")
    private String                     desc;

    @ApiModelProperty("tcp地址-读")
    private String                     readAddress;

    @ApiModelProperty("tcp地址-写")
    private String                     writeAddress;

    @ApiModelProperty("http地址-读")
    private String                     httpAddress;

    @ApiModelProperty("http地址-写")
    private String                     httpWriteAddress;

    /**
     * @see ESClusterTypeEnum
     */
    @ApiModelProperty("集群类型(-1 未知 3 docker集群 4 host集群 5 vm集群)")
    private Integer                    type;

    @ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
    private String                     tags;

    @ApiModelProperty("数据中心")
    private String                     dataCenter;

    @ApiModelProperty("机房")
    private String                     idc;

    @ApiModelProperty("服务等级")
    private Integer                    level;

    @ApiModelProperty("es版本")
    private String                     esVersion;

    @ApiModelProperty("镜像名称")
    private String                     imageName;

    @ApiModelProperty("机器节点")
    private String                     nsTree;

    @ApiModelProperty("插件ID")
    private String                     plugIds;

    @ApiModelProperty("程序包Id")
    private Long                       packageId;

    @ApiModelProperty("配置包")
    private ESConfigDTO                eSConfigDTO;

    @ApiModelProperty("插件包列表")
    private List<PluginDTO>            plugs;

    @ApiModelProperty("集群角色列表")
    private List<ESClusterRoleDTO>     roleClusters;

    @ApiModelProperty("集群角色 对应主机列表")
    private List<ESClusterRoleHostDTO> roleClusterHosts;

    @ApiModelProperty("单节点实例数")
    private Integer                    pidCount;

    @ApiModelProperty("机器规格  host物理集群必填")
    private String                     machineSpec;

    @ApiModelProperty("集群创建人")
    private String                     creator;

    @ApiModelProperty("集群的索引服务id列表")
    private String                     templateSrvs;

    @ApiModelProperty("集群密码")
    private String                     password;

    @ApiModelProperty("client运行模式（0：读写共享 1：读写分离）")
    private Integer                    runMode;

    @ApiModelProperty("指定用写client的action")
    private String                     writeAction;

    @ApiModelProperty("集群状态 0 green 1 yellow 2 red -1 未知")
    private Integer                    health;

    @ApiModelProperty("活跃分片数目")
    private Long                       activeShardNum;

    @ApiModelProperty("磁盘总量(byte)")
    private Long                       diskTotal;

    @ApiModelProperty("磁盘使用大小(byte)")
    private Long                       diskUsage;

    @ApiModelProperty("磁盘使用率(byte)")
    private Double                     diskUsagePercent;

    @ApiModelProperty("IaaS平台类型")
    private String                     platformType;

    /**
     * @see ClusterResourceTypeEnum
     */
    @ApiModelProperty("集群资源类型(-1 未知 1 共享 2 独立 3 独享)")
    private Integer                    resourceType;

    @ApiModelProperty("gateway地址")
    private String                     gatewayUrl;
}
