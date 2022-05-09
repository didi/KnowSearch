package com.didichuxing.datachannel.arius.admin.common.bean.vo.ecm;


import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ECM任务详情")
public class EcmTaskVO extends BaseVO {

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

    @ApiModelProperty("物理集群ID")
    private Long physicClusterId;

    @ApiModelProperty("集群节点角色 执行顺序")
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
