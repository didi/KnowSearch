package com.didiglobal.logi.op.manager.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-07-15 11:31 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralGroupConfigHostVO {
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
     * 安装目录配置，多个用逗号隔开
     */
    private String installDirector;
    /**
     * 进程数配置
     */
    private String processNum;
    /**
     * 安装包url
     */
    private String url;
    /**
     * 操作类型
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
    /**
     * 配置依赖组件
     */
    private Integer dependConfigComponentId;

}
