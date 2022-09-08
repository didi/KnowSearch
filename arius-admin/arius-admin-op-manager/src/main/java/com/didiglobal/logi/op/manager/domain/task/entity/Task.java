package com.didiglobal.logi.op.manager.domain.task.entity;

import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.Tuple;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
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
     * 状态，0待执行，1执行，2失败，4成功，5取消，6杀死，7超时，8忽略
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

    public Task create(String content, Integer type, String describe, String associationId, Map<String, List<Tuple<String, Integer>>>  groupToHostList) {
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.content = content;
        this.type = type;
        this.isFinish = 0;
        this.describe = describe;
        this.associationId = associationId;
        this.status = TaskStatusEnum.WAITING.getStatus();
        this.detailList = new ArrayList<>();
        groupToHostList.forEach((groupName, hosts) -> {
            hosts.forEach(hostAndProcessNum -> {
                TaskDetail taskDetail = new TaskDetail();
                taskDetail.create(groupName, hostAndProcessNum.v1(), hostAndProcessNum.v2());
                this.detailList.add(taskDetail);
            });
        });
        return this;
    }

    public Task updateStatus(int status) {
        this.status = status;
        return this;

    }

    public Result<Void> checkExecuteTaskStatus() {
        if (status == TaskStatusEnum.RUNNING.getStatus()) {
            return Result.fail(ResultCode.TASK_IS_RUNNING);
        }
        if (status == TaskStatusEnum.FAILED.getStatus() ||
                status == TaskStatusEnum.SUCCESS.getStatus() ||
                status == TaskStatusEnum.CANCELLED.getStatus() ||
                status == TaskStatusEnum.KILLED.getStatus()) {
            return Result.fail(ResultCode.TASK_IS_FINISH);
        }
        return Result.success();
    }

    public Result<Void> checkTaskActionStatus(TaskActionEnum action) {
        switch (action) {
            case PAUSE:
                if (status != TaskStatusEnum.RUNNING.getStatus()) {
                    return Result.fail(ResultCode.TASK_IS_NOT_RUNNING);
                }
                break;
            case START:
                if (status != TaskStatusEnum.WAITING.getStatus() &&
                        status != TaskStatusEnum.PAUSE.getStatus()) {
                    return Result.fail(ResultCode.TASK_IS_RUNNING_OR_FINISH);
                }
                break;
            case CANCEL:
                //失败的任务也允许取消
                if (status != TaskStatusEnum.WAITING.getStatus() &&
                        status != TaskStatusEnum.RUNNING.getStatus() &&
                        status != TaskStatusEnum.PAUSE.getStatus() &&
                        status != TaskStatusEnum.FAILED.getStatus()) {
                    return Result.fail(ResultCode.TASK_IS_FINISH);
                }
                break;
            case KILL:
                if (status != TaskStatusEnum.WAITING.getStatus() &&
                        status != TaskStatusEnum.RUNNING.getStatus() &&
                        status != TaskStatusEnum.PAUSE.getStatus()) {
                    return Result.fail(ResultCode.TASK_IS_FINISH);
                }
                break;
            default:
                return Result.fail(ResultCode.COMMON_FAIL);
        }
        return Result.success();
    }

    public Result<Void> checkRetryActionStatus() {
        if (status != TaskStatusEnum.FAILED.getStatus()) {
            return Result.fail(ResultCode.TASK_IS_NOT_FAILED);
        }
        return Result.success();
    }

    public Result<Void> checkHostActionStatus() {
        boolean finalStatus = isFinalStatus();
        if (finalStatus) {
            return Result.fail(ResultCode.TASK_IS_FINISH);
        }
        return Result.success();
    }

    public boolean isFinalStatus() {
        return status == TaskStatusEnum.KILLED.getStatus() ||
                status== TaskStatusEnum.CANCELLED.getStatus();
    }

    public boolean isUserActionStatus() {
        return status == TaskStatusEnum.PAUSE.getStatus() ||
                status == TaskStatusEnum.KILLED.getStatus() ||
                status == TaskStatusEnum.CANCELLED.getStatus();
    }

}
