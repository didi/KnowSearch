package com.didi.cloud.fastdump.common.bean.es;

import lombok.Data;

/**
 * Created by linyunan on 2022/8/23
 */
@Data
public class ShardInfo {
    /**
     * shard所属索引
     */
    private String  index;
    /**
     * shard 所在节点的 uuid
     */
    private String nodeId;
    /**
     * shard 序号
     */
    private String  shard;
    private String  prirep;
    private String  state;
    private Integer docs;
    private String  store;
    private String  ip;
    private String  node;
}
