package com.didiglobal.logi.op.manager.interfaces.dto.general;

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

    /**
     * 分组配置
     */
    private List<GeneralGroupConfigDTO> groupConfigList;

    /**
     * 操作并发度
     */
    private Integer batch;
}
