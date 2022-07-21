package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-13 10:40 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskPO {
    /**
     * 任务id
     */
    private Integer id;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 关联的任务id
     */
    private Integer associationId;
    /**
     * 类型
     */
    private Integer type;
    /**
     * 描述
     */
    private String describe;
    /**
     * 是否结束，0未结束，1结束
     */
    private Integer isFinish;
    /**
     * 任务内容
     */
    private String content;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
