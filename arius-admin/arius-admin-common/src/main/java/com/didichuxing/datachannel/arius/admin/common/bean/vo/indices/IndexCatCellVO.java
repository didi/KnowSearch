package com.didichuxing.datachannel.arius.admin.common.bean.vo.indices;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.bean.vo.BaseVO;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lyn
 * @date 2021/09/30
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "IndexCatCellVO", description = "索引详情")
public class IndexCatCellVO extends BaseVO {
    @ApiModelProperty("主键")
    private String       key;

    @ApiModelProperty("物理集群名称")
    private String       cluster;

    @ApiModelProperty("逻辑集群名称")
    private String       clusterLogic;

    @ApiModelProperty("逻辑集群ID")
    private Long         resourceId;

    @ApiModelProperty("projectId")
    private Integer      projectId;

    @ApiModelProperty("分区健康")
    private String       health;

    @ApiModelProperty("分区状态")
    private String       status;

    @ApiModelProperty("索引名字")
    private String       index;

    @ApiModelProperty("分区shard个数")
    private Long         pri;

    @ApiModelProperty("分区副本个数")
    private Long         rep;

    @ApiModelProperty("分区文档个数")
    private Long         docsCount;

    @ApiModelProperty("分区文档删除个数")
    private Long         docsDeleted;

    @ApiModelProperty("分区主分片存储大小")
    private String       storeSize;

    @ApiModelProperty("分区存储大小")
    private String       priStoreSize;

    @ApiModelProperty("可读标志位")
    private Boolean      readFlag;

    @ApiModelProperty("可写标志位")
    private Boolean      writeFlag;

    @ApiModelProperty("segment数")
    private Long         totalSegmentCount;

    @ApiModelProperty("模板ID")
    private Integer      templateId;

    @ApiModelProperty("通过平台索引创建标识 true 通过平台创建，false不是通过平台创建")
    private Boolean      platformCreateFlag;

    @ApiModelProperty("别名")
    private List<String> aliases;

    @ApiModelProperty("异步translog")
    private Boolean translogAsync;

    @ApiModelProperty("恢复优先级")
    private Integer priorityLevel;

    @ApiModelProperty("索引type")
    private List<String> indexTypeList;
}