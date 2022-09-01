package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-09-01 10:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralRollbackComponentDTO extends GeneralBaseOperationComponentDTO{
    /**
     * 组件id
     */
    private Integer componentId;
    /**
     * rollback的类型，比如是配置变更回滚还是其他的
     */
    private Integer type;
    /**
     * 回滚任务id
     */
    private Integer taskId;
}
