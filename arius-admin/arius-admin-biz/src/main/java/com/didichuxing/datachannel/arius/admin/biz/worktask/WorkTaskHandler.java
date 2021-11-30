package com.didichuxing.datachannel.arius.admin.biz.worktask;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.WorkTask;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.detail.AbstractTaskDetail;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author d06679
 * @date 2020/12/21
 */
public interface WorkTaskHandler extends BaseHandle {

    /**
     * 创建一个任务
     *
     * 1、校验任务内容是否合法
     * 2、提交任务
     * @param workTask 任务数据
     * @return result
     */
    Result<WorkTask> addTask(WorkTask workTask);

    /**
     * 判断一个任务是否存在，参数待定
     * @return
     */
    boolean existUnClosedTask(Integer key, Integer type);

    /**
     * 处理任务
     * @param workTask 任务
     * @return result
     */
    Result<Void> process(WorkTask workTask, Integer step, String status, String expandData);

    /**
     * 获取任务详细信息
     * @param extensions 扩展信息
     * @return AbstractTaskDetail
     */
    AbstractTaskDetail getTaskDetail(String extensions);

}
