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

    //这里主要是给比如安装或者回滚之类的操作需要的一些参数返回
    /**
     * 包的url
     */
    private String url;
    /**
     * 针对哪种操作类型
     */
    private Integer type;
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
