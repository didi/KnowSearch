package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-07-20 3:47 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralScaleComponent extends GeneralBaseOperationComponent {
    private Integer type;

    private Integer componentId;

    public Result<Void> checkScaleParam() {
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
