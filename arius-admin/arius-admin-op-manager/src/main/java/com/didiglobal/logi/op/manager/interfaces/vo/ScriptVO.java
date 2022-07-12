package com.didiglobal.logi.op.manager.interfaces.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-06 2:46 下午
 */
@Data
@NoArgsConstructor
public class ScriptVO {
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
}
