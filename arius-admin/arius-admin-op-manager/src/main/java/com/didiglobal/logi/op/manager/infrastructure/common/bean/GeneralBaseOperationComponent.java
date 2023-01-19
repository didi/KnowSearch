package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-26 3:03 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralBaseOperationComponent {
    protected List<GeneralGroupConfig> groupConfigList;

    protected String templateId;

    protected Integer batch;

    protected Result<Void> checkParam() {
        if (null == groupConfigList || groupConfigList.isEmpty()) {
            return Result.fail(ResultCode.PARAM_ERROR.getCode(), "配置组缺失");
        }
        return Result.success();
    }

    public void setGroupConfigList(List<GeneralGroupConfig> groupConfigList) {
        this.groupConfigList = ConvertUtil.list2List(groupConfigList, GeneralGroupConfig.class);
    }
}
