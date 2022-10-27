package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-10-24 16:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralUninstallComponent extends GeneralBaseOperationComponent {
    private Integer componentId;

    public Result<Void> checkConfigChangeParam() {

        if (null == componentId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "组件id缺失");
        }

        return Result.success();
    }
}
