package com.didichuxing.datachannel.arius.admin.biz.task;

import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexLogsConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexRateLimitDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastDumpTaskLogVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexBriefVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.fastindex.FastIndexDetailVO;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;

import java.util.List;
import java.util.Map;

/**
 * 任务 Service
 *
 * @author d06679
 * @date 2020/12/21
 */
public interface FastIndexManager {

    /**
     * 提交任务
     *
     * @param fastIndexDTO 任务数据
     * @param operator     操作人
     * @param projectId    项目id
     * @return Result
     * @throws NotFindSubclassException 没有发现异常子类
     */
    Result<WorkTaskVO> submitTask(FastIndexDTO fastIndexDTO, String operator,
                                  Integer projectId);

    Result<Void> refreshTask(Integer taskId);

    Result<Void> cancelTask(Integer taskId, Integer projectId);

    Result<Void> restartTask(Integer taskId, Integer projectId);

    Result<Void> modifyTaskRateLimit(Integer taskId, FastIndexRateLimitDTO fastIndexRateLimitDTO);

    Result<FastIndexDetailVO> getTaskDetail(Integer taskId);

    Result<Void> transferTemplate(Integer taskId);

    Result<Void> rollbackTemplate(Integer taskId);

    PaginationResult<FastDumpTaskLogVO> pageGetTaskLogs(Integer projectId,
                                                        FastIndexLogsConditionDTO queryDTO) throws NotFindSubclassException;

    /**
     * 获取模板和索引的下拉框值
     * @param taskId
     * @return
     */
    Result<List<FastIndexBriefVO>> getFastIndexBrief(Integer taskId);
}