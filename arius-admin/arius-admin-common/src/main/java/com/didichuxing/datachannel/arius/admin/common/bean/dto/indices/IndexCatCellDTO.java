package com.didichuxing.datachannel.arius.admin.common.bean.dto.indices;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/5/31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "索引详情")
public class IndexCatCellDTO {

    @ApiModelProperty("主键")
    private String  key;

    @ApiModelProperty("物理集群名称")
    private String  cluster;

    @ApiModelProperty("逻辑集群名称")
    private String  clusterLogic;

    @ApiModelProperty("逻辑集群Id")
    private Long    resourceId;

    @ApiModelProperty("项目Id")
    private Integer projectId;

    @ApiModelProperty("分区健康")
    private String  health;

    @ApiModelProperty("分区状态")
    private String  status;

    @ApiModelProperty("分区名字")
    private String  index;

    @ApiModelProperty("分区shard个数")
    private Long    pri;

    @ApiModelProperty("分区副本个数")
    private Long    rep;

    @ApiModelProperty("分区文档个数")
    private Long    docsCount;

    @ApiModelProperty("分区文档删除个数")
    private Long    docsDeleted;

    @ApiModelProperty("分区主分片存储大小")
    private String  storeSize;

    @ApiModelProperty("分区存储大小")
    private String  priStoreSize;

    @ApiModelProperty("可读标志位")
    private Boolean readFlag;

    @ApiModelProperty("可写标志位")
    private Boolean writeFlag;

    @ApiModelProperty("删除标识")
    private Boolean deleteFlag;

    @ApiModelProperty("时间戳")
    private Long    timestamp;

    @ApiModelProperty("primaries segment count")
    private Long    primariesSegmentCount;

    @ApiModelProperty("total segment count")
    private Long    totalSegmentCount;

    @ApiModelProperty("平台模板Id")
    private Integer templateId;

    @ApiModelProperty("通过平台索引创建标识 true 通过平台创建，false不是通过平台创建")
    private Boolean platformCreateFlag;

    @ApiModelProperty("异步translog")
    private Boolean translogAsync;

    @ApiModelProperty("恢复优先级")
    private Integer priorityLevel;
}
