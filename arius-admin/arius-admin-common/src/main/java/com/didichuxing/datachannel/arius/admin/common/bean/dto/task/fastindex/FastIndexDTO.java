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

    @ApiModelProperty("数据类型：1.index 2.template")
    private String                 dataType;
    // 源资源
    @ApiModelProperty("原资源类型：1.es")
    private String                 sourceType;
    @ApiModelProperty("原物理集群名称")
    private String                 sourceCluster;
    @ApiModelProperty("原物理集群代理地址（一对一代理）")
    private String                 sourceClusterAddress;
    @ApiModelProperty("原逻辑集群ID")
    private String                 sourceLogicClusterId;
    @ApiModelProperty("原项目ID")
    private String                 sourceProjectId;

    @ApiModelProperty("目标资源类型")
    private String                 targetType;
    @ApiModelProperty("目标物理集群名称")
    private String                 targetCluster;
    @ApiModelProperty("目标物理集群代理地址（一对一代理）")
    private String                 targetClusterAddress;
    @ApiModelProperty("目标逻辑集群ID")
    private String                 targetLogicClusterId;
    @ApiModelProperty("目标项目ID")
    private String                 targetProjectId;

    @ApiModelProperty("对应关系：1.all to one 2.one to one")
    private String                 relationType;
    @ApiModelProperty("写入类型：1.index_with_id 2.index 3.create")
    private String                 writeType;
    @ApiModelProperty("任务读取速率（条/S）")
    private Long                   taskReadRate;

    @ApiModelProperty("任务开始时间")
    private Date                   taskStartTime;

    @ApiModelProperty("是否转让")
    private Boolean                transfer = false;
    @ApiModelProperty("主任务列表")
    private List<FastIndexTaskDTO> taskList;

    @Data
    @ApiModel("前端提交任务的实体")
    private class FastIndexTaskDTO {
        @ApiModelProperty("原索引或模版名称（多个）")
        private List<String> sourceNames;
        @ApiModelProperty("索引的type")
        private List<String> indexTypes;
        @ApiModelProperty("目标索引或模版")
        private String targetName;
        @ApiModelProperty("mappings")
        private String       mappings;
        @ApiModelProperty("settings")
        private String       settings;
    }
}
