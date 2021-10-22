package com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author d06679
 * @date 2019/3/19
 */
@Data
@ApiModel(description = "集群信息")
public class ESClusterPhyVO extends BaseVO implements Comparable {

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

    @ApiModelProperty("集群所开放的索引服务")
    private List<ESClusterTemplateSrvVO> esClusterTemplateSrvVOS;

    @ApiModelProperty("集群角色信息")
    private List<ESRoleClusterVO>        esRoleClusterVOS;

    @ApiModelProperty("物理集群状态")
    private String                       clusterStatus;

    @ApiModelProperty("磁盘使用率")
    private Double                       diskUsage;

    @ApiModelProperty("集群密码")
    private String                       password;

    @ApiModelProperty("机房")
    private String                       idc;

    @Override
    public int compareTo(Object o) {
        if(null == o){return 0;}

        ESClusterPhyVO e = (ESClusterPhyVO)o;
        return e.getId().intValue() - this.getId().intValue();
    }
}
