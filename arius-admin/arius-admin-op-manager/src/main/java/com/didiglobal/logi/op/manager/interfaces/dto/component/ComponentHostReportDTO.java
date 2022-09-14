package com.didiglobal.logi.op.manager.interfaces.dto.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-08-29 17:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentHostReportDTO {
    /**
     * 组件id
     */
    private Integer componentId;
    /**
     * 分组名
     */
    private String groupName;
    /**
     * 组件host
     */
    private String host;
    /**
     * 状态
     */
    private Integer status;
}
