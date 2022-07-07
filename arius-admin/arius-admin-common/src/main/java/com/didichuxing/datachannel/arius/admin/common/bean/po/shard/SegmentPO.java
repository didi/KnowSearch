package com.didichuxing.datachannel.arius.admin.common.bean.po.shard;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SegmentPO extends BasePO {

    private String index;

    private String shard;

    private String ip;

    private String segment;

    @JSONField(name = "size.memory")
    private Double memoSize;
}
