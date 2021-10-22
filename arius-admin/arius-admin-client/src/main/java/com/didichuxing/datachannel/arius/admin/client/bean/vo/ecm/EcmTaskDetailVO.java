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
    /**
     * 执行进度
     */
    @ApiModelProperty("执行进度")
    private Long percent;

    /**
     * 成功的节点个数
     */
    @ApiModelProperty("成功的节点个数")
    private Long success;

    /**
     * 失败的节点个数
     */
    @ApiModelProperty("失败的节点个数")
    private Long failed;

    /**
     * 执行中的节点个数
     */
    @ApiModelProperty("执行中的节点个数")
    private Long creating;

    /**
     * 等待中的节点个数
     */
    @ApiModelProperty("等待中的节点个数")
    private Long waiting;

    /**
     * 忽略的节点个数
     */
    @ApiModelProperty("忽略的节点个数")
    private Long ignore;

    /**
     * 取消的节点个数
     */
    @ApiModelProperty("取消的节点个数")
    private Long cancel;

    /**
     * 节点总个数
     */
    @ApiModelProperty("节点总个数")
    private Long sum;

    /**
     * 任务状态
     */
    @ApiModelProperty("任务状态")
    private String status;

    /**
     * 工单类型
     */
    @ApiModelProperty("工单类型  1 集群新增  2 集群扩容 3 集群缩容 4 集群重启 5 集群升级")
    private Integer orderType;

    /**
     * 具体详情
     */
    @ApiModelProperty("各角色任务详情<角色名称, 详情>")
    private Map<String, List<EcmTaskDetail>> roleNameTaskDetailMap;
}
