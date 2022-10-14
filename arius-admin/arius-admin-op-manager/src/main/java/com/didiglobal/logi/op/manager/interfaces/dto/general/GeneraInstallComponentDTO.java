package com.didiglobal.logi.op.manager.interfaces.dto.general;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /**
     * 用户名密码
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 是否开启tsl认证（0未开启，1开启）
     */
    private Integer isOpenTSL;
}
