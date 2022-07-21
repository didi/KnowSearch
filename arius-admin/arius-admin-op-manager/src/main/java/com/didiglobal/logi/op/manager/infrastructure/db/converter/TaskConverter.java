package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.infrastructure.db.TaskPO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

/**
 * @author didi
 * @date 2022-07-13 1:47 下午
 */
public class TaskConverter {
    public static TaskPO convertTaskDO2PO(Task task) {
        return ConvertUtil.obj2Obj(task, TaskPO.class);
    }

    public static Task convertTaskPO2DO(TaskPO po) {
        return ConvertUtil.obj2Obj(po, Task.class);
    }
}
