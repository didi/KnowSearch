package com.didichuxing.datachannel.arius.admin.common.bean.vo.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "TemplateCyclicalRollInfoVO", description = "索引分区信息")
public class TemplateCyclicalRollInfoVO {

    @ApiModelProperty("分区健康")
    private String health;

    @ApiModelProperty("分区状态")
    private String status;

    @ApiModelProperty("分区名字")
    private String index;

    @ApiModelProperty("分区shard个数")
    private String pri;

    @ApiModelProperty("分区副本个数")
    private String rep;

    @ApiModelProperty("分区文档个数")
    private String docsCount;

    @ApiModelProperty("分区文档删除个数")
    private String docsDeleted;

    @ApiModelProperty("分区主分片存储大小")
    private String storeSize;

    @ApiModelProperty("分区存储大小")
    private String priStoreSize;
}
