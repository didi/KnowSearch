package com.didichuxing.datachannel.arius.admin.client.bean.dto.task.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.BaseDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmTaskTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ECM任务信息")
public class EcmTaskDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("ID主键自增")
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("工单ID")
    private Long workOrderId;

    @ApiModelProperty("物理集群ID")
    private Long physicClusterId;

    @ApiModelProperty("集群节点角色")
    private String clusterNodeRole;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("工单数据")
    private List<EcmParamBase> ecmParamBaseList;

    @ApiModelProperty("docker容器云/host 物理机")
    private Integer type;

    /**
     * @see EcmTaskTypeEnum
     */
    @ApiModelProperty("任务类型")
    private Integer orderType;

    @ApiModelProperty("插件创建人")
    private String creator;

}
