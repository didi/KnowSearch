package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-19 3:21 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentGroupConfigPO {
    /**
     * 配置id
     */
    private Integer id;
    /**
     * 组件id
     */
    private Integer componentId;
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
     * 进程数配置
     */
    private String processNumConfig;
    /**
     * 节点列表
     */
    private String hosts;
    /**
     * 配置版本
     */
    private String version;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
