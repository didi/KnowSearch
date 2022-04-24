package com.didichuxing.datachannel.arius.admin.common.bean.po.stats;

import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BasePO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ESClusterThreadPO extends BasePO {

    @JSONField(name = "node_name")
    private String nodeName;

    @JSONField(name = "name")
    private String threadName;

    @JSONField(name = "active")
    private Long activeNum;

    @JSONField(name = "rejected")
    private Long rejectedNum;

    @JSONField(name = "queue")
    private Long queueNum;

}
