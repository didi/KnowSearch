package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-09-01 10:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralRestartComponentDTO extends GeneralBaseOperationComponentDTO {
    private Integer componentId;
}
