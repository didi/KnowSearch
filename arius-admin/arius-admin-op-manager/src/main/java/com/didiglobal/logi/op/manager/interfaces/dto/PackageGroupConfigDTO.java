package com.didiglobal.logi.op.manager.interfaces.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author didi
 * @date 2022-07-11 2:57 下午
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class PackageGroupConfigDTO {
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
}
