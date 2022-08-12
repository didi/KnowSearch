package com.didiglobal.logi.op.manager.infrastructure.db.repository;

import com.didiglobal.logi.op.manager.domain.task.entity.Task;
import com.didiglobal.logi.op.manager.domain.task.repository.TaskRepository;
import com.didiglobal.logi.op.manager.infrastructure.db.TaskPO;
import com.didiglobal.logi.op.manager.infrastructure.db.converter.TaskConverter;
import com.didiglobal.logi.op.manager.infrastructure.db.mapper.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author didi
 * @date 2022-07-13 1:44 下午
 */
@Repository
public  class TaskRepositoryImpl implements TaskRepository {

    @Autowired
    private TaskDao taskDao;

    @Override
    public int insertTask(Task task) {
        TaskPO po = TaskConverter.convertTaskDO2PO(task);
        return taskDao.insert(po);
    }

    @Override
    public Task getTaskById(int id) {
        return TaskConverter.convertTaskPO2DO(taskDao.getById(id));
    }

    @Override
    public void updateTaskStatus(int id, int status) {
        taskDao.updateStatus(id, status);
    }

    @Override
    public void updateTaskStatusAndIsFinish(int id, int status, int isFinish) {
        //TODO
    }

    @Override
    public List<Task> getUnFinishTaskList() {
        return taskDao.getUnFinishTaskList();
    }


}
