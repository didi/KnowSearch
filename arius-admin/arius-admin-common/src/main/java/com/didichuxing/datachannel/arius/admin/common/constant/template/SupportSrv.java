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
    private boolean dcdrModuleExists     = false;
    private boolean coldRegionExists     = false;
    private boolean pipelineModuleExists = false;
    private boolean enableWriteRateLimit = false;
    private boolean isPartition          = false;
}