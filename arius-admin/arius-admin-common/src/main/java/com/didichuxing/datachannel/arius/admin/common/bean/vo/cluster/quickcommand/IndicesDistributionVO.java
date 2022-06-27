package com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.quickcommand;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * indices分布.
 *
 * @ClassName indicesVO
 * @Author gyp
 * @Date 2022/6/1
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicesDistributionVO {
    @ApiModelProperty("健康检查")
    private String health;
    @ApiModelProperty("状态")
    private String status;
    @ApiModelProperty("索引名")
    private String index;
    @ApiModelProperty("主分片数量")
    private String pri;
    @ApiModelProperty("副分片数量")
    private String rep;
    @ApiModelProperty("文档数量")
    private String docsCount;
    @ApiModelProperty("文档删数量")
    private String docsDeleted;
    @ApiModelProperty("存储主副本和副本的大小")
    private String storeSize;
    @ApiModelProperty("主分片大小")
    private String priStoreSize;
    @ApiModelProperty("uuid")
    private String uuid;
}