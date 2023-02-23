package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class SimpleComputer implements MetricsComputer {
    private static final ILog  LOGGER = LogFactory.getLog(SimpleComputer.class);

    private MetricsRegister    metricsRegister;

    private MetricsComputeType computeType;

    public SimpleComputer(MetricsComputeType computeType, MetricsRegister metricsRegister) {
        this.computeType = computeType;
        this.metricsRegister = metricsRegister;
    }

    @Override
    public String compute(ESDataTempBean esDataTempBean) {
        try {
            String key = esDataTempBean.getKey();
            ESDataTempBean beforeData = metricsRegister.getBeforeEsData(key);
            metricsRegister.putBeforeEsData(key, esDataTempBean);
            if (beforeData == null) {
                LOGGER.debug("class=SimpleComputer||method=compute||key={}||msg=beforeData is null!", key);

                //防止启动AMS时误报
                esDataTempBean.setSendToN9e(false);
                return null;
            } else {
                if (esDataTempBean.getValue() == null) {
                    LOGGER.debug("class=SimpleComputer||method=compute||msg=collect data value is null. {}", esDataTempBean);

                    return null;
                }

                Double value = computeType.compute(beforeData, esDataTempBean);

                //为了衍生计算,缓存计算结果;每次周期开始之前会清空上个周期的缓存
                metricsRegister.putBeforeComputeData(key, value);
                return String.valueOf(value);
            }
        } catch (Exception e) {
            LOGGER.info("class=SimpleComputer||method=compute||msg=exception", e);
        }
        return null;
    }
}
