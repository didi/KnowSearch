package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-26 3:01 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralBaseOperationComponentDTO {
    private List<GeneralGroupConfigDTO> groupConfigList;

    private String associationId;
}
