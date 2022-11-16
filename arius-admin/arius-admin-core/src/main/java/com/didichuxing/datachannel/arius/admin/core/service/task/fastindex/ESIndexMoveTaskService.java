package com.didichuxing.datachannel.arius.admin.core.service.task.fastindex;

import java.util.List;

import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.fastindex.FastIndexDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskAdjustReadRateContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.fastindex.ESIndexMoveTaskStats;

/**
 * @author didi
 */
public interface ESIndexMoveTaskService {

    /**
     * 提交任务
     *
     * @param fastIndexDTO  数据迁移任务信息
     * @param context 请求体
     * @return {@link Result}<{@link String}>
     */
    Tuple<String, Result<String>> submitTask(FastIndexDTO fastIndexDTO, ESIndexMoveTaskContext context);

    /**
     * 获取任务统计数据
     *
     * @param fastIndexDTO   数据迁移任务信息
     * @param fastDumpTaskId 任务ID
     * @return {@link Result}<{@link ESIndexMoveTaskStats}>
     */
    Tuple<String, Result<ESIndexMoveTaskStats>> getTaskStats(FastIndexDTO fastIndexDTO, String fastDumpTaskId);

    /**
     * 获取所有任务统计数据
     *
     * @param fastIndexDTO 数据迁移任务信息
     * @return {@link Result}<{@link List}<{@link ESIndexMoveTaskStats}>>
     */
    Result<List<ESIndexMoveTaskStats>> getAllTaskStats(FastIndexDTO fastIndexDTO);

    /**
     * 调整任务读取写入限流值
     *
     * @param fastIndexDTO 数据迁移任务信息
     * @param context      请求体
     * @return {@link Result}<{@link Void}>
     */
    Tuple<String, Result<Void>> adjustReadRate(FastIndexDTO fastIndexDTO, ESIndexMoveTaskAdjustReadRateContext context);

    /**
     * 停止任务
     *
     * @param fastIndexDTO   数据迁移任务信息
     * @param fastDumpTaskId 任务ID
     * @return {@link Result}<{@link Void}>
     */
    Tuple<String, Result<Void>> stopTask(FastIndexDTO fastIndexDTO, String fastDumpTaskId);

    /**
     * 检查卫生
     *
     * @param fastIndexDTO 快速指数dto
     * @return {@link Result}<{@link Void}>
     */
    Result<Boolean> checkHealth(FastIndexDTO fastIndexDTO);
}
