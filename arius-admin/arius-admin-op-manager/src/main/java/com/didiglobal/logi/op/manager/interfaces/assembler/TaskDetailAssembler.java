package com.didiglobal.logi.op.manager.interfaces.assembler;

import com.didiglobal.logi.op.manager.domain.task.entity.value.TaskDetail;
import com.didiglobal.logi.op.manager.infrastructure.util.ConvertUtil;
import com.didiglobal.logi.op.manager.interfaces.vo.TaskDetailVO;

import java.util.List;

public class TaskDetailAssembler {

    public static List<TaskDetailVO> toVOList(List<TaskDetail> taskDetailList) {
        return ConvertUtil.list2List(taskDetailList, TaskDetailVO.class);
    }
}
