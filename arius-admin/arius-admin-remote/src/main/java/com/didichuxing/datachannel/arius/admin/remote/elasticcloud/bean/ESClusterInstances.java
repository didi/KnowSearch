package com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESClusterInstances {
    /**
     *  当前实例
     */
    private Integer current;
    /**
     *  期望实例
     */
    private Integer desired;
}
