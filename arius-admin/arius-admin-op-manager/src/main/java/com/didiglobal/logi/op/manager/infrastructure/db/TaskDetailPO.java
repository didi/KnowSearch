package com.didiglobal.logi.op.manager.infrastructure.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-13 10:43 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDetailPO {
    /**
     * 关联任务id
     */
    private Integer id;
    /**
     * 执行任务id
     */
    private Integer executeTaskId;
    /**
     * 状态
     */
    private Integer status;
    /**
     * host
     */
    private String host;
    /**
     * 分组id
     */
    private String groupName;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
