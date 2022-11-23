package com.didichuxing.datachannel.arius.admin.core.service.task;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.OpTaskQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.bean.po.task.OpTaskPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.OpTaskVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.task.OpTaskDAO;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * op任务服务impl
 *
 * @author shizeying
 * @date 2022/08/12
 */
@Service
@NoArgsConstructor
public class OpTaskServiceImpl implements OpTaskService {
    private static final ILog      LOGGER = LogFactory.getLog(OpTaskServiceImpl.class);
    @Autowired
    private              OpTaskDAO opTaskDao;
    
    /**
     * 更新
     *
     * @param task 任务
     * @return boolean
     */
    @Override
    public boolean update(OpTask task) {
        return opTaskDao.update(ConvertUtil.obj2Obj(task, OpTaskPO.class)) > 0;
    }
    
    /**
     * 获取通过id
     *
     * @param id id
     * @return {@link OpTask}
     */
    @Override
    public OpTask getById(Integer id) {
        return ConvertUtil.obj2Obj(opTaskDao.getById(id), OpTask.class);
    }
    
    /**
     * 获取所有集合
     *
     * @return {@link List}<{@link OpTask}>
     */
    @Override
    public List<OpTask> listAll() {
        return Optional.ofNullable(opTaskDao.listAll()).map(opTasks -> ConvertUtil.list2List(opTasks, OpTask.class))
                .orElse(Collections.emptyList());
    }
    
    /**
     * 获取成功任务通过类型
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTask}>
     */
    @Override
    public List<OpTask> getSuccessTaskByType(Integer taskType) {
        return ConvertUtil.list2List(opTaskDao.getSuccessTaskByType(taskType), OpTask.class);
    }
    
    /**
     * 获取pending任务通过类型
     *
     * @param taskType 任务类型
     * @return {@link List}<{@link OpTask}>
     */
    @Override
    public List<OpTask> getPendingTaskByType(Integer taskType) {
        return ConvertUtil.list2List(opTaskDao.getPendingTaskByType(taskType), OpTask.class);
    }
    
    @Override
    public List<OpTaskPO> getPendingTaskByTypes(List<Integer> taskTypes) {
        return opTaskDao.getPendingTaskByTypes(taskTypes);
    }
    
    /**
     * 获取最新任务
     *
     * @param businessKey 业务key
     * @param taskType    任务类型
     * @return {@link OpTask}
     */
    @Override
    public OpTask getLatestTask(String businessKey, Integer taskType) {
        return ConvertUtil.obj2Obj(opTaskDao.getLatestTask(businessKey, taskType), OpTask.class);
    }
    
    /**
     * 新增
     *
     * @param task 任务
     * @return boolean
     */
    @Override
    public boolean insert(OpTask task) {
        OpTaskPO opTaskPO = ConvertUtil.obj2Obj(task, OpTaskPO.class);
        boolean success = opTaskDao.insert(opTaskPO) > 0;
        task.setId(opTaskPO.getId());
        return success;
    }

    @Override
    public Tuple<Long, List<OpTaskVO>> pagingGetTasksByCondition(OpTaskQueryDTO queryDTO) {
        String sortTerm = null == queryDTO.getSortTerm() ? SortConstant.ID : queryDTO.getSortTerm();
        String sortType = queryDTO.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        List<OpTaskPO> opTaskPOList = Lists.newArrayList();
        Long count = 0L;
        try {
            opTaskPOList = opTaskDao.pagingByCondition(queryDTO, (queryDTO.getPage() - 1) * queryDTO.getSize(),
                    queryDTO.getSize(), sortTerm, sortType);
            count = opTaskDao.countByCondition(queryDTO);
        } catch (Exception e) {
            LOGGER.error("class=OpTaskServiceImpl||method=pagingGetTasksByCondition||err={}",
                    e.getMessage(), e);
        }
        return new Tuple<>(count, ConvertUtil.list2List(opTaskPOList,OpTaskVO.class));
    }
}