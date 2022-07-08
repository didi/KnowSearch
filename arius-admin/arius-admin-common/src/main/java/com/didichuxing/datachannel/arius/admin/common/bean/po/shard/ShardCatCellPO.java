package com.didichuxing.datachannel.arius.admin.common.bean.po.shard;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shard分布.
 *
 * @ClassName ShardDistributionVO
 * @Author gyp
 * @Date 2022/6/7
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardCatCellPO {
    @ApiModelProperty("主副")
    private String prirep;
    @ApiModelProperty("节点名称")
    private String node;
    @ApiModelProperty("文档")
    private String docs;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("索引")
    private String index;
    @ApiModelProperty("分片")
    private String shard;
    @ApiModelProperty("state")
    private String state;
    @ApiModelProperty("store大小")
    private String store;
}