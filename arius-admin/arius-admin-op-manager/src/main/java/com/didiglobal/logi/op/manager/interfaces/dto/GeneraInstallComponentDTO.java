package com.didiglobal.logi.op.manager.interfaces.dto;

import com.didiglobal.logi.op.manager.infrastructure.common.bean.GeneralBaseOperationComponent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-14 6:37 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneraInstallComponentDTO extends GeneralBaseOperationComponentDTO {
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
}
