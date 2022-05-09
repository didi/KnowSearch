package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
public class ESClusterPhyVO extends BaseVO implements Comparable<ESClusterPhyVO> {

    @ApiModelProperty("集群ID")
    private Integer                      id;

    @ApiModelProperty("集群名字")
    private String                       cluster;

    @ApiModelProperty("描述")
    private String                       desc;

    @ApiModelProperty("tcp地址-读")
    private String                       readAddress;

    @ApiModelProperty("tcp地址-写")
    private String                       writeAddress;

    @ApiModelProperty("http地址-读")
    private String                       httpAddress;

    @ApiModelProperty("http地址-写")
    private String                       httpWriteAddress;

    /** @see ESClusterTypeEnum */
    @ApiModelProperty("集群类型(3 docker, 4 host)")
    private Integer                      type;

    @ApiModelProperty("集群展示用属性标签，如「集群所属资源类型」等等")
    private String tags;

    @ApiModelProperty("数据中心")
    private String                       dataCenter;

    @ApiModelProperty("单节点实例数 host物理集群必填")
    private Integer                      pidCount;

    @ApiModelProperty("机器规格  host物理集群必填")
    private String                       machineSpec;

    @ApiModelProperty("服务等级")
    private Integer                      level;

    @ApiModelProperty("es版本")
    private String                       esVersion;

    @ApiModelProperty("镜像地址")
    private String                       imageName;

    @ApiModelProperty("集群所开放的索引服务")
    private List<ESClusterTemplateSrvVO> esClusterTemplateSrvVOS;

    @ApiModelProperty("集群角色信息")
    private List<ESClusterRoleInfoVO> esClusterRoleInfoVOS;

    @ApiModelProperty("磁盘使用率")
    private Double                       diskUsagePercent;

    @ApiModelProperty("磁盘总量")
    private Long                         diskTotal;

    @ApiModelProperty("磁盘使用量")
    private Long                         diskUsage;

    @ApiModelProperty("集群密码")
    private String                       password;

    @ApiModelProperty("机房")
    private String                       idc;

    @ApiModelProperty("client运行模式（0：读写共享 1：读写分离）")
    private Integer                     runMode;

    @ApiModelProperty("指定用写client的action")
    private String                      writeAction;

    @ApiModelProperty("集群状态")
    private Integer                     health;

    @ApiModelProperty("活跃的分片数目")
    private Long                        activeShardNum;

    @Override
    public int compareTo(ESClusterPhyVO o) {
        if (null == o) {
            return 0;
        }

        return o.getId().intValue() - this.getId().intValue();
    }
}
