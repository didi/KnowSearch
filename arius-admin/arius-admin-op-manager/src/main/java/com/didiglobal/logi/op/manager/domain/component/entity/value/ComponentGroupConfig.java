package com.didiglobal.logi.op.manager.domain.component.entity.value;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-12 10:49 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentGroupConfig {
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

    public ComponentGroupConfig create(){
        this.version = "1";
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }
}
