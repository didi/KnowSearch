package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.Data;

/**
 * @author didi
 * @date 2022-08-12 3:24 下午
 */
@Data
public class GeneralExecuteComponentFunction extends GeneralBaseOperationComponent {
    private Object param;

    private Integer componentId;

    public Result<Void> checkExecuteFunctionParam() {
        Result result = super.checkParam();

        if (result.failed()) {
            return result;
        }

        if (null == componentId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id缺失");
        }

        return Result.success();
    }
}
