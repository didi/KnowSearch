package com.didichuxing.datachannel.arius.admin.common.bean.entity.region;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chengxiang
 * @date 2022/06/06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterRegionConfig {

    /**
     * 冷节点标志
     */
    private Boolean cold = Boolean.FALSE;
}
