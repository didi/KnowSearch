package com.didiglobal.logi.op.manager.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-07-14 6:38 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralGroupConfigDTO {
    /**
     * 分组名
     */
    private String groupName;
    /**
     * 系统配置
     */
    private String systemConfig;
    /**
     * 运行时配置
     */
    private String runningConfig;
    /**
     * 文件配置
     */
    private String fileConfig;
    /**
     * 安装目录配置
     */
    private String installDirectoryConfig;

    /**
     * ip对应的进程数量
     */
    private String processNumConfig;

    /**
     * 节点列表
     */
    private String hosts;
}
