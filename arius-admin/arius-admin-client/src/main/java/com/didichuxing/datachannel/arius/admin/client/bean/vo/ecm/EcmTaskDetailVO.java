package com.didichuxing.datachannel.arius.admin.client.bean.vo.ecm;

import io.swagger.annotations.ApiModel;
import java.util.List;
import java.util.Map;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmTaskDetail;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.BaseVO;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "ECM任务执行详情")
public class EcmTaskDetailVO extends BaseVO {

    @ApiModelProperty("执行进度")
    private Long percent;

    @ApiModelProperty("成功的节点个数")
    private Long success;

    @ApiModelProperty("失败的节点个数")
    private Long failed;

    @ApiModelProperty("执行中的节点个数")
    private Long creating;

    @ApiModelProperty("等待中的节点个数")
    private Long waiting;

    @ApiModelProperty("忽略的节点个数")
    private Long ignore;

    @ApiModelProperty("取消的节点个数")
    private Long cancel;

    @ApiModelProperty("节点总个数")
    private Long sum;

    @ApiModelProperty("任务状态")
    private String status;

    @ApiModelProperty("工单类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级")
    private Integer orderType;

    @ApiModelProperty("各角色任务详情<角色名称, 详情>")
    private Map<String, List<EcmTaskDetail>> roleNameTaskDetailMap;
}
