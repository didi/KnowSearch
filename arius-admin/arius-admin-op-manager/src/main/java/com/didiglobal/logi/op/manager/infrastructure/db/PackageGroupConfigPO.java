package com.didiglobal.logi.op.manager.infrastructure.db;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-11 2:25 下午
 */
public class PackageGroupConfigPO {
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
     * 关联安装包id
     */
    private Integer packageId;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
