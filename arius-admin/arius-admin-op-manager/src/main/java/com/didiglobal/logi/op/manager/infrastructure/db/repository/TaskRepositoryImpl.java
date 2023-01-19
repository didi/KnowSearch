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
public class TaskRepositoryImpl implements TaskRepository {

    @Autowired
    private TaskDao taskDao;

    @Override
    public int insertTask(Task task) {
        TaskPO po = TaskConverter.convertTaskDO2PO(task);
        taskDao.insert(po);
        return po.getId();
    }

    @Override
    public Task getTaskById(int id) {
        return TaskConverter.convertTaskPO2DO(taskDao.getById(id));
    }
    
    @Override
    public List<Task> getTaskListByIds(List<Integer> taskIds) {
        return TaskConverter.convertTaskPO2DOList(taskDao.getTaskListByIds(taskIds));
    }
    
    @Override
    public int updateTaskStatus(int id, int status) {
        return taskDao.updateStatus(id, status);
    }

    @Override
    public void updateTaskStatusAndIsFinish(int id, int status, int isFinish) {
        taskDao.updateStatusAndIsFinish(id, status, isFinish);
    }

    @Override
    public List<Task> getUnFinishTaskList() {
        return TaskConverter.convertTaskPO2DOList(taskDao.getUnFinishTaskList());
    }

    @Override
    public List<Task> getUnFinalStatusTaskList() {
        return TaskConverter.convertTaskPO2DOList(taskDao.getUnFinalStatusTaskList());
    }


}