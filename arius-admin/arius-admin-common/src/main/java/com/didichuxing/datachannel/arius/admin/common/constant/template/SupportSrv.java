package com.didichuxing.datachannel.arius.admin.common.constant.template;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支持srv
 *
 * @author shizeying
 * @date 2022/07/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public   class SupportSrv {
    private Boolean dcdrModuleExists     = false;
    private Boolean coldRegionExists     = false;
    private Boolean pipelineModuleExists = false;
    private Boolean enableWriteRateLimit = false;
    private Boolean isPartition          = false;
}