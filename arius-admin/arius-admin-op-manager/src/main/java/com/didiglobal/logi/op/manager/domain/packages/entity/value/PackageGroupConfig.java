package com.didiglobal.logi.op.manager.domain.packages.entity.value;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-11 2:17 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageGroupConfig {
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
    private JSONObject systemConfig;
    /**
     * 运行时配置
     */
    private JSONObject runningConfig;
    /**
     * 文件配置
     */
    private JSONObject fileConfig;
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

    public PackageGroupConfig create() {
        createTime = new Timestamp(System.currentTimeMillis());
        updateTime = new Timestamp(System.currentTimeMillis());
        return this;
    }

}
