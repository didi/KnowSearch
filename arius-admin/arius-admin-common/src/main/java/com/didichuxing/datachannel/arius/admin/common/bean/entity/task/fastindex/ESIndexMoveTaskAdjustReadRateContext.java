package com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author didi
 */
@Data
@AllArgsConstructor
public class ESIndexMoveTaskAdjustReadRateContext {
    private String taskId;
    /**
     * 写入文档流控
     */
    private Long readFileRateLimit;
}
