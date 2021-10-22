package com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "ECM任务信息")
public class EcmTaskDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键自增
     */
    @ApiModelProperty("ID主键自增")
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
     * 物理集群ID
     */
    @ApiModelProperty("物理集群ID")
    private Long physicClusterId;

    /**
     * 集群节点角色
     */
    @ApiModelProperty("集群节点角色")
    private String clusterNodeRole;

    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;

    /**
     *  工单数据
     */
    @ApiModelProperty("工单数据")
    private List<EcmParamBase> ecmParamBaseList;

    /**
     * 类型  docker容器云/host 物理机
     */
    @ApiModelProperty("docker容器云/host 物理机")
    private Integer type;

    /**
     * 任务类型
     * @see EcmTaskTypeEnum
     */
    @ApiModelProperty("任务类型")
    private Integer orderType;

    /**
     * 插件创建人
     */
    @ApiModelProperty("插件创建人")
    private String creator;

}
