package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-09-01 11:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralRollbackComponent extends GeneralBaseOperationComponent{
    private Integer componentId;
    /**
     * rollback的类型，比如是配置变更回滚还是其他的
     */
    private Integer type;

    /**
     * 回滚任务id
     */
    private Integer taskId;

    public Result<Void> checkRollbackParam() {
        Result result = super.checkParam();

        if (result.failed()) {
            return result;
        }

        if (null == componentId || null == taskId || null == type) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id或者任务id缺失");
        }

        return Result.success();
    }
}
