package com.didi.cloud.fastdump.core.action.movetask;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.didi.cloud.fastdump.common.bean.taskcontext.BaseTaskActionContext;
import com.didi.cloud.fastdump.common.threadpool.TaskThreadPool;
import com.didi.cloud.fastdump.core.action.Action;

/**
 * 基础TaskAction, 每条链路定义XXXTaskAction
 *
 * Created by linyunan on 2022/8/11
 */
public abstract class BaseMoveTaskAction<TaskActionContext extends BaseTaskActionContext> implements Action<BaseTaskActionContext, String> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseMoveTaskAction.class);

    /**
     * 任务线程池
     */
    protected TaskThreadPool      moveTaskThreadPool;

    @PostConstruct
    public void init() {
        initMoveTaskThreadPool();
    }

    protected abstract void initMoveTaskThreadPool();

    /**
     * 核心模板流水线处理, 可以覆盖重写自己的处理流水线
     * @param taskActionContext    TaskContext
     * @throws Exception          Exception
     * @return String             taskId
     */
    protected String submit(TaskActionContext taskActionContext) throws Exception {
        //TODO: 提交任务过慢, 需要加速

        // 1、参数检验
        check(taskActionContext);
        // 2、初始化
        init(taskActionContext);
        // 3、解析源索引文件位置（文件位置可能不在本fast-dump节点）
        source(taskActionContext);
        // 4、转化请求到其他节点（迁移数据的文件位置可能不在本fast-dump节点， 需要二次转化请求）
        transform(taskActionContext);

        return taskActionContext.getTaskId();
    }

    protected abstract void check(TaskActionContext taskActionContext) throws Exception;

    protected abstract void init(TaskActionContext taskActionContext) throws Exception;

    protected abstract void source(TaskActionContext taskActionContext) throws Exception;

    /**
     * 转化请求到其他节点（文件位置可能不在本fast-dump节点， 需要二次转化请求）
     * @param taskActionContext                      任务上下文
     */
    protected abstract void transform(TaskActionContext taskActionContext) throws Exception;
}