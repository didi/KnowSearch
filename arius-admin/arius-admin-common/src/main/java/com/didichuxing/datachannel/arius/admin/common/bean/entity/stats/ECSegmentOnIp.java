package com.didichuxing.datachannel.arius.admin.common.bean.entity.stats;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ECSegmentOnIp {

    /**
     * segment数目
     */
    @JSONField(name = "sc")
    String segment;

    /**
     * es集群上实例的ip
     */
    @JSONField(name = "ip")
    String ip;
}
