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

    @ApiModelProperty("集群名称")
    private String  cluster;

    @ApiModelProperty("分区健康")
    private String  health;

    @ApiModelProperty("分区状态")
    private String  status;

    @ApiModelProperty("分区名字")
    private String  index;

    @ApiModelProperty("分区shard个数")
    private String  pri;

    @ApiModelProperty("分区副本个数")
    private String  rep;

    @ApiModelProperty("分区文档个数")
    private String  docsCount;

    @ApiModelProperty("分区文档删除个数")
    private String  docsDeleted;

    @ApiModelProperty("分区主分片存储大小")
    private String  storeSize;

    @ApiModelProperty("分区存储大小")
    private String  priStoreSize;

    @ApiModelProperty("可读标志位")
    private Boolean readFlag;

    @ApiModelProperty("可写标志位")
    private Boolean writeFlag;
}
