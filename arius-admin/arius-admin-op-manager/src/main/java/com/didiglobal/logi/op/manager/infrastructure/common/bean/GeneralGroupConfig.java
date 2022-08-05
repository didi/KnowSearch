package com.didiglobal.logi.op.manager.infrastructure.common.bean;

import lombok.Data;

/**
 * @author didi
 * @date 2022-07-13 8:10 下午
 */
@Data
public class GeneralGroupConfig {
    /**
     * 分组id
     */
    private Integer id;
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
     * 节点列表
     */
    private String hosts;
}
