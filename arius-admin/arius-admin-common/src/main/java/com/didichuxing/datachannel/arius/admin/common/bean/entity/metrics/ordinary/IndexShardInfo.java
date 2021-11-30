package com.didichuxing.datachannel.arius.admin.common.bean.entity.metrics.ordinary;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by linyunan on 2021-08-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexShardInfo implements Serializable {

    private String index;
    private int    shard;
    private String prirep;
    private String state;
    private long   docs;
    private String store;
    private String ip;
    private String node;
}
