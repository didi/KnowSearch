package com.didiglobal.logi.op.manager.domain.task.entity;

import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * @author didi
 * @date 2022-07-12 9:05 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
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
    private String associationId;
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

    /**
     * 任务详情
     */
    private List<TaskDetail> detailList;

    /**
     * 关联模板id
     */
    private String templateId;

    public Task create(String content, Integer type, String describe, String associationId, Map<String, List<String>> groupToHostList) {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.content = content;
        this.type = type;
        this.isFinish = 0;
        this.describe = describe;
        this.associationId = associationId;
        this.status = TaskStatusEnum.WAITING.getStatus();
        groupToHostList.forEach((groupName, hosts) -> {
            hosts.forEach(host -> {
                TaskDetail taskDetail = new TaskDetail();
                taskDetail.create(groupName, host);
                detailList.add(taskDetail);
            });
        });
        return this;
    }

    public Task updateStatus(int status) {
        this.status = status;
        return this;

    }

}
