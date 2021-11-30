package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;

public interface MetricsComputer {
    String compute(ESDataTempBean esDataTempBean);
}
