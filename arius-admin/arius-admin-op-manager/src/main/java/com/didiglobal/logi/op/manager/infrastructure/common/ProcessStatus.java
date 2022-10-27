package com.didiglobal.logi.op.manager.infrastructure.common;

import lombok.Data;

/**
 * @author didi
 * @date 2022-08-12 3:41 下午
 */
@Data
public class ProcessStatus {
    /**
     * 完成百分比
     */
    private int rate;

    /**
     * 成功以及失败
     */
    private boolean isSuccess;
}
