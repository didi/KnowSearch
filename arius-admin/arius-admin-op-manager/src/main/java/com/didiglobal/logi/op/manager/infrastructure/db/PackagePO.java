package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-11 2:25 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackagePO {
    /**
     * 安装包id
     */
    private Integer id;
    /**
     * 安装包名字
     */
    private String name;
    /**
     * 地址
     */
    private String url;
    /**
     * 版本
     */
    private String version;
    /**
     * 描述
     */
    private String describe;
    /**
     * 类型，0是配置依赖，1是配置独立
     */
    private Integer type;
    /**
     * 脚本id
     */
    private Integer scriptId;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 创建者
     */
    private String creator;
}
