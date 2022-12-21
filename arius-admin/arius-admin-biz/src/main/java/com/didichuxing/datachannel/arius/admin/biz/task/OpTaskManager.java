package com.didichuxing.datachannel.arius.admin.biz.task;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskProcessDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager.ESClusterExpandWithPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.op.manager.ESClusterShrinkWithPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didiglobal.logi.op.manager.interfaces.vo.TaskDetailVO;
import java.util.List;

/**
 * 任务 Service
 *
 * @author d06679
 * @date 2020/12/21
 */
public interface OpTaskManager {

    /**
     * 提交一个任务
     *
     * @param opTaskDTO 任务数据
     * @param projectId
     * @return Result
     * @throws AdminOperateException 异常
     */
    Result<OpTask> addTask(OpTaskDTO opTaskDTO, Integer projectId) throws NotFindSubclassException;

    /**
     * 判断一个任务是否存在
     * @param key 关键值
     * @param type 任务类型
     * @return
     */
    boolean existUnClosedTask(Integer key, Integer type) throws NotFindSubclassException;

    /**
     * 插入一条任务
     *
     * @param task task
     * @return int
     */
    boolean insert(OpTask task);

    /**
     * 通过id更新任务
     * @param task task
     * @return int
     */
    Boolean updateTask(OpTask task);

    /**
     * 通过id获取任务
     *
     * @param id 任务id
     * @return TaskPO
     */
    Result<OpTask> getById(Integer id);

    /**
     * 获取所有的任务
     *
     * @return List<TaskPO>
     */
    Result<List<OpTask>> list();
    
    /**
     * 按任务类型获取待处理任务
     *
     * @param taskTypes 任务类型列表。
     * @return OpTask 对象列表。
     */
    Result<List<OpTask>> getPendingTaskByTypes(List<Integer> taskTypes);

    /**
     * 处理任务任务
     *
     * @param processDTO 任务
     * @return Result
     */
    Result<Void> processTask(OpTaskProcessDTO processDTO) throws NotFindSubclassException;

    /**获取最新任务
     * 通过businessKey获取最新的任务
     *
     * @param businessKey 业务id
     * @param taskType 任务类型
     * @return {@link Result}<{@link OpTask}>
     */
    Result<OpTask> getLatestTask(String businessKey, Integer taskType);

    /**
     * 通过taskType获取待处理任务
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTask}>
     */
    List<OpTask> getPendingTaskByType(Integer taskType);

    /**
     * 根据类型获取失败任务
     * @param taskType
     * @return
     */
    List<OpTask> getSuccessTaskByType(Integer taskType);

    /**
     * 任务中心分页查询
     * @param projectId
     * @param queryDTO
     * @return
     */
    PaginationResult<OpTaskVO> pageGetTasks(Integer projectId, OpTaskQueryDTO queryDTO) throws NotFindSubclassException;
		
		/**
		 * 使用给定的 id 执行命令。
		 *
		 * @param id 要删除的用户的 ID。
		 * @return 一个 Result<Void> 对象。
		 */
		Result<Void> execute(Integer id);
    
    /**
     * “执行任务。”
     *
     *
     * @param id 任务标识。
     * @param action 要对任务执行的操作。可能的值为：
     * @return 结果对象。
     */
    Result<Void> operateTask(Integer id, String action);
    
    /**
     * 重试任务
     *
     * @param id 要重试的任务的任务 ID。
     * @return 包含 Void 对象的 Result 对象。
     */
    Result<Void> retryTask(Integer id);
    
    /**
     *
     * 对任务子节点进行相应的操作
     *
     * @param id createTask 方法返回的任务 ID。
     * @param action 要在主机上执行的操作。该值可以是“添加”或“删除”。
     * @param host 要操作的主机的主机名。
     * @param groupName 主机所属组的名称。
     * @return 返回类型是一个 Result 对象。
     */
    Result<Void> operateHost(Integer id, String action, String host, String groupName);
    
    /**
     * 获取任务日志
     *
     * @param id        要获取日志的任务的任务 ID。
     * @param hostname  运行任务的机器的主机名。
     * @param groupName
     * @param type      0-stdout，1-stderr
     * @return 包含任务日志的 Result 对象。
     */
    Result<String> getTaskLog(Integer id, String hostname,String groupName, int type);
    
    /**
     * > 通过id获取任务详情
     *
     * @param id 任务编号
     * @return TaskDetailVO 对象的列表。
     */
    Result<List<TaskDetailVO>> getTaskDetail(Integer id);
    
    /**
     * 添加任务esexpand
     *
     * @param data      数据
     * @param operator  操作人或角色
     * @param projectId 项目id
     * @return {@link Result}<{@link WorkTaskVO}>
     */
    Result<WorkTaskVO> addTaskESExpand(ESClusterExpandWithPluginDTO data, String operator, Integer projectId)
        throws NotFindSubclassException;
    
    /**
     * 添加任务esshrink
     *
     * @param data      数据
     * @param operator  操作人或角色
     * @param projectId 项目id
     * @return {@link Result}<{@link WorkTaskVO}>
     */
    Result<WorkTaskVO> addTaskESShrink(ESClusterShrinkWithPluginDTO data, String operator, Integer projectId)
        throws NotFindSubclassException;
}