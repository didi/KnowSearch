package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-08-08 4:20 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralUpgradeComponent {
    /**
     * 关联的安装包id
     */
    private Integer packageId;

    /**
     * 依赖的组件id
     */
    private Integer componentId;

    /**
     * 模板id
     */
    protected String templateId;

    protected String associationId;

    public Result<Void> checkParam() {
        if (null == componentId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "id缺失");
        }

        if (null == packageId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "升级版本缺失");
        }
        return Result.success();
    }
}
