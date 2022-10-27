package com.didiglobal.logi.op.manager.interfaces.dto.general;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author didi
 * @date 2022-07-25 5:36 下午
 */
@Data
@AllArgsConstructor
public class GeneralConfigChangeComponentDTO extends GeneralBaseOperationComponentDTO{
    private Integer componentId;
}
