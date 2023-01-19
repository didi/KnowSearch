package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-19 2:36 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentPO {
    /**
     * 组件id
     */
    private Integer id;
    /**
     * 状态(0 green,1 yellow,2 red,3 unKnow)
     */
    private Integer status;
    /**
     * 包含的组件id列表
     */
    private String containComponentIds;
    /**
     * 组件名
     */
    private String name;
    /**
     * 关联的安装包id
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
    /**
     * 是否卸载（0未卸载，1卸载）
     */
    private Integer isDeleted;
    /**
     * 依赖配置的组件id
     */
    private Integer dependConfigComponentId;
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
