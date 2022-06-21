package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/06/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShardInfo {

    private String state;

    private Boolean primary;

    private String node;

    private Integer shard;

    private String index;

}
