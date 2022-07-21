package com.didiglobal.logi.op.manager.interfaces.dto;

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
public class GeneraInstallComponentDTO {
    /**
     * 组件名
     */
    private String name;
    /**
     * 关联的安装包id
     */
    private Integer packageId;
    /**
     * 值对象，关联group配置
     */
    private List<GeneralGroupConfigDTO> groupConfigList;

    /**
     * 依赖的组件id
     */
    private Integer dependComponentId;

    /**
     * 模板id
     */
    private String templateId;
}
