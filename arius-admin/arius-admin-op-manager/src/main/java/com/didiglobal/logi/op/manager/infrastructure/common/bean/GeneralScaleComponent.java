package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-20 3:47 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralScaleComponent {
    private Integer componentId;
    private List<GeneralGroupConfig> groupConfigList;

    public Result<Void> checkScaleParam(){
        if (null == componentId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "id缺失");
        }

        if (groupConfigList.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "配置组缺失");
        }
        return Result.success();
    }


}
