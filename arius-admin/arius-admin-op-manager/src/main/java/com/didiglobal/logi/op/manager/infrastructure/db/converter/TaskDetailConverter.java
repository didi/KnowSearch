package com.didiglobal.logi.op.manager.infrastructure.db.converter;

import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.db.TaskDetailPO;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 4:41 下午
 */
public class TaskDetailConverter {
    public static List<TaskDetailPO> convertTaskDO2POList(List<TaskDetail> list) {
        return ConvertUtil.list2List(list, TaskDetailPO.class);
    }

    public static List<TaskDetail>  convertTaskPO2DOList(List<TaskDetailPO> list) {
        return ConvertUtil.list2List(list, TaskDetail.class);
    }

}
