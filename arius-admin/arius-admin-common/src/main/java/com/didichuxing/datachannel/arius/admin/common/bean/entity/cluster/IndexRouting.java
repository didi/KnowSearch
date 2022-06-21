package com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chengxiang
 * @date 2022/06/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexRouting {

    private String index;

    private List<ShardInfo> shards;
}
