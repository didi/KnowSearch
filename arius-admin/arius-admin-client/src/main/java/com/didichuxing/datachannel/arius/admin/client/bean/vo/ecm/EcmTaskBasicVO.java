package com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm;


import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description ="工单任务基本情况")
public class EcmTaskBasicVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("工单ID")
    private Long workOrderId;

    @ApiModelProperty("es版本")
    private String  esVersion;

    @ApiModelProperty("镜像名")
    private String imageName;

    @ApiModelProperty("集群名称")
    private String clusterName;

    @ApiModelProperty("集群描述")
    private String desc;

    @ApiModelProperty("服务节点")
    private String nsTree;

    /**
     * @deprecated
     */
    @Deprecated
    @ApiModelProperty("机房")
    private String idc;

    @ApiModelProperty("成本部门")
    private String dept;

    @ApiModelProperty("集群节点角色")
    private String clusterNodeRole;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("类型  docker容器云 3 /host 物理机 4")
    private Integer type;

    @ApiModelProperty("类型 1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级")
    private Integer orderType;

    @ApiModelProperty("创建人")
    private String creator;
}
