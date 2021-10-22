package com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm;


import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "ECM任务详情")
public class EcmTaskVO extends BaseVO {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键自增
     */
    @ApiModelProperty("ID")
    private Long id;

    /**
     * 标题
     */
    @ApiModelProperty("标题")
    private String title;

    /**
     * 工单ID
     */
    @ApiModelProperty("工单ID")
    private Long workOrderId;

    /**
     * es版本
     */
    @ApiModelProperty("es版本")
    private String  esVersion;

    /**
     * 镜像名
     */
    @ApiModelProperty("镜像名")
    private String imageName;

    /**
     * 物理集群ID
     */
    @ApiModelProperty("物理集群ID")
    private Long physicClusterId;

    /**
     * 集群节点角色 执行顺序
     */
    @ApiModelProperty("集群节点角色 执行顺序")
    private String clusterNodeRole;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     * 类型  docker容器云/host 物理机
     */
    @ApiModelProperty("类型  docker容器云 3 /host 物理机 4")
    private Integer type;
    /**
     * 类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级
     */
    @ApiModelProperty("类型 1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级")
    private Integer orderType;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String creator;


}
