package com.didi.cloud.fastdump.core.service.sinker;

import com.didi.cloud.fastdump.common.bean.sinker.BaseDataSinker;
import com.didi.cloud.fastdump.common.bean.stats.BaseMoveTaskStats;

import java.util.List;

/**
 * Created by linyunan on 2022/8/10
 */
public interface DataSinkerService<Sinker extends BaseDataSinker, TaskStats extends BaseMoveTaskStats, Data> {
    void beforeSink(Sinker sinker, TaskStats taskStats, Data data) throws Exception;

    Integer doSink(Sinker sinker, TaskStats taskStats, Data data) throws Exception;

    void afterSink(Sinker sinker, TaskStats taskStats, Data data) throws Exception;

    void commit(Sinker sinker, TaskStats taskStats, Data data) throws Exception;
}
