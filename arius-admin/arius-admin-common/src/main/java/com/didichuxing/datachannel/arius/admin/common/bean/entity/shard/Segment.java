package com.didichuxing.datachannel.arius.admin.common.bean.entity.shard;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Segment extends BaseEntity {

    private String index;

    private String shard;

    private String ip;

    private String segment;

    private Double memoSize;
}
