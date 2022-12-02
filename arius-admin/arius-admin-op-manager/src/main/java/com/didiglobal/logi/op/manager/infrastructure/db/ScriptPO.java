package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-04 7:22 下午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPO {
    /**
     * 脚本id
     */
    private Integer id;
    /**
     * 脚本名
     */
    private String name;
    /**
     * 模板id
     */
    private String templateId;
    /**
     * 内容地址
     */
    private String contentUrl;
    /**
     * 描述
     */
    private String describe;
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
