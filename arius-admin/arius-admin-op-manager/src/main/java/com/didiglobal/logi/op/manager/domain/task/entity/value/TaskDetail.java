package com.didiglobal.logi.op.manager.domain.task.entity.value;

import com.didiglobal.logi.op.manager.infrastructure.common.Result;
import com.didiglobal.logi.op.manager.infrastructure.common.ResultCode;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.HostActionEnum;
import com.didiglobal.logi.op.manager.infrastructure.common.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.webresources.CachedResource;

import java.sql.Timestamp;

/**
 * @author didi
 * @date 2022-07-13 10:12 上午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDetail {
    /**
     * 关联任务id
     */
    private Integer id;
    /**
     * 执行任务id
     */
    private Integer executeTaskId;
    /**
     * 状态，0待执行，1执行，2失败，3暂停，4成功，5取消，6杀死
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
     * 进程数量
     */
    private Integer processNum;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

    public TaskDetail create(String groupName, String host, int processNum) {
        this.status = TaskStatusEnum.WAITING.getStatus();
        this.createTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
        this.groupName = groupName;
        this.host = host;
        this.processNum = processNum;
        return this;
    }

    /**
     * 检查节点action对应的节点状态是否符合
     * @param action
     * @return
     */
    public Result<Void> checkHostActionStatus(HostActionEnum action) {
        switch (action) {
            case IGNORE:
            case REDO:
                if (status != TaskStatusEnum.TIMEOUT.getStatus() &&
                        status != TaskStatusEnum.FAILED.getStatus()) {
                    return Result.fail(ResultCode.TASK_HOST_IS_NOT_ERROR);
                }
                break;
            case KILL:
                if (status != TaskStatusEnum.RUNNING.getStatus()) {
                    return Result.fail(ResultCode.TASK_IS_NOT_RUNNING);
                }
                break;
            default:
                return Result.fail(ResultCode.COMMON_FAIL);
        }
        return Result.success();

    }

    /**
     * 是否是正常状态，success以及ignored以及killed可以认为是正常成功状态
     *
     * @return true 正常，false 非正常
     */
    public boolean isNormalStatus() {
        return status == TaskStatusEnum.SUCCESS.getStatus() ||
                status == TaskStatusEnum.IGNORED.getStatus() ||
                status == TaskStatusEnum.KILLED.getStatus();
    }

}
