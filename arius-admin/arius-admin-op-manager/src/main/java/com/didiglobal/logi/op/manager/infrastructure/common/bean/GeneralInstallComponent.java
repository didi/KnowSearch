package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.domain.component.entity.value.ComponentGroupConfig;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 8:01 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralInstallComponent extends GeneralBaseOperationComponent{
    /**
     * 组件名
     */
    private String name;
    /**
     * 关联的安装包id
     */
    private Integer packageId;

    /**
     * 依赖的组件id
     */
    private Integer dependComponentId;

    /**
     * 依赖配置组件id
     */
    private Integer dependConfigComponentId;

    public Result<Void> checkInstallParam(){
        Result result = super.checkParam();
        if (result.failed()) {
            return result;
        }

        if (name.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "name缺失");
        }

        if (null == packageId) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "安装包缺失");
        }
        return Result.success();
    }
}
