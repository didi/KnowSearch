package com.didichuxing.datachannel.arius.admin.common.bean.po.shard;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
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
public class ShardCatCellPO extends BaseESPO {
    @ApiModelProperty("主副")
    private String prirep;
    @ApiModelProperty("节点名称")
    private String node;
    @ApiModelProperty("文档")
    private int docs;
    @ApiModelProperty("ip")
    private String ip;
    @ApiModelProperty("索引")
    private String index;
    @ApiModelProperty("分片")
    private long shard;
    @ApiModelProperty("state")
    private String state;
    @ApiModelProperty("store大小")
    private long store;
    @ApiModelProperty("物理集群名称")
    private String clusterPhy;

    private long    timestamp;

    @Override
    @JSONField(serialize = false)
    public String getKey() {
        return index+"@"+clusterPhy+"@"+prirep+"@"+ip+"@"+shard;
    }

    @Override
    public String getRoutingValue() {
        return null;
    }
}