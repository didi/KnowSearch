package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-08-08 4:15 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralUpgradeComponentDTO extends GeneralBaseOperationComponentDTO {
    /**
     * 关联的安装包id
     */
    private Integer packageId;
}
