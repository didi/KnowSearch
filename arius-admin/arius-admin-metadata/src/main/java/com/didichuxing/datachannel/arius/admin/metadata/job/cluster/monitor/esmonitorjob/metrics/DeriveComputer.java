package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class DeriveComputer implements MetricsComputer {
    private static final ILog  LOGGER = LogFactory.getLog(DeriveComputer.class);

    private MetricsRegister    metricsRegister;

    private MetricsComputeType computeType;

    public DeriveComputer(MetricsComputeType computeType, MetricsRegister metricsRegister) {
        this.computeType = computeType;
        this.metricsRegister = metricsRegister;
    }

    @Override
    public String compute(ESDataTempBean esDataTempBean) {
        try {
            String keyPre = esDataTempBean.getKeyPre();
            String dividendKey = keyPre + esDataTempBean.getDeriverParamByKey(ESDataTempBean.DIVIDEND);
            String divisorKey = keyPre + esDataTempBean.getDeriverParamByKey(ESDataTempBean.DIVISOR);

            Double first = metricsRegister.getBeforeComputeData(dividendKey);
            Double second = metricsRegister.getBeforeComputeData(divisorKey);

            if (first == null || second == null) {
                //防止AMS启动时误报
                esDataTempBean.setSendToN9e(false);

                return null;
            }

            Object value = computeType.compute(new ESDataTempBean(first), new ESDataTempBean(second));
            return String.valueOf(value);
        } catch (Exception e) {
            LOGGER.error("class=DeriveComputer||method=compute||msg=exception", e);
        }
        return null;
    }
}
