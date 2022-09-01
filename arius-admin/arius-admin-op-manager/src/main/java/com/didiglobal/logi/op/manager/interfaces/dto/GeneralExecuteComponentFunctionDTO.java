package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-08-15 11:43
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralExecuteComponentFunctionDTO extends GeneralBaseOperationComponentDTO{
    private Object param;
    private Integer componentId;
}
