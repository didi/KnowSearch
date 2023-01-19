package com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author didi
 * @date 2022/10/18
 */
@ApiModel("数据迁移实体")
@Data
public class FastIndexDTO {

    @ApiModelProperty("资源类型：1.template 2.index")
    private Integer                dataType;
    // 源资源
    @ApiModelProperty("原集群类型：1.es")
    private Integer                sourceClusterType;
    @ApiModelProperty("原物理集群名称")
    private String                 sourceCluster;
    @ApiModelProperty("任务提交地址（一对一代理）")
    private String                 taskSubmitAddress;
    @ApiModelProperty("原物理集群认证信息")
    private String                 sourceClusterPassword;
    @ApiModelProperty("原逻辑集群ID")
    private Long                   sourceLogicClusterId;
    @ApiModelProperty("原项目ID")
    private Integer                sourceProjectId;

    @ApiModelProperty("目标集群类型")
    private Integer                targetClusterType;
    @ApiModelProperty("目标物理集群名称")
    private String                 targetCluster;
    @ApiModelProperty("目标逻辑集群ID")
    private Long                   targetLogicClusterId;
    @ApiModelProperty("目标项目ID")
    private Integer                targetProjectId;

    @ApiModelProperty("对应关系：1.all to one 2.one to one")
    private Integer                relationType;
    @ApiModelProperty("写入方式：1.index_with_id 2.index 3.create")
    private Integer                writeType;
    @ApiModelProperty("任务读取限流速率（条/S）")
    private Long                   taskReadRate;

    @ApiModelProperty("任务开始时间")
    private Date                   taskStartTime;

    @ApiModelProperty("目标写入索引类型")
    private String                 targetIndexType;
    @ApiModelProperty("是否转让")
    private Boolean                transfer;
    @ApiModelProperty("转让状态：-1.无需转让 0.未转让 1.已转让 2.已回切")
    private Integer                transferStatus;
    @ApiModelProperty("最后一次转让结果（转让状态为已转让时为转让结果，为已回切时为回切）")
    private String                transferResult;

    @ApiModelProperty("主任务列表：资源列表")
    private List<FastIndexTaskDTO> taskList;
}
