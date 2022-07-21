package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-20 3:36 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralScaleComponentDTO {

    private Integer componentId;

    private List<GeneralGroupConfigDTO> groupConfigList;

}
