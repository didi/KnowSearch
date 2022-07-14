package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor.esmonitorjob.metrics;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESDataTempBean;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESNodeToIndexTempBean;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

public class ESNodeToIndexComputer {
    private static final ILog  LOGGER = LogFactory.getLog(ESNodeToIndexComputer.class);

    private MetricsRegister    metricsRegister;

    private MetricsComputeType computeType;

    public ESNodeToIndexComputer(MetricsComputeType computeType, MetricsRegister metricsRegister) {
        this.computeType = computeType;
        this.metricsRegister = metricsRegister;
    }

    public String compute(ESNodeToIndexTempBean dataBean) {
        try {
            //无需计算
            if (computeType == MetricsComputeType.NONE) {
                if (EnvUtil.isTest()) {
                    LOGGER.warn("class=ESNodeToIndexComputer||method=compute||valueName={}", dataBean.getValueName());
                }

                return String.valueOf(dataBean.getValue());
            }

            if (computeType == MetricsComputeType.DERIVE_DIVISION) {
                return devireCompute(dataBean);
            }

            return simpleCompute(dataBean);
        } catch (Exception e) {
            LOGGER.error("class=ESNodeToIndexComputer||method=compute||msg=exception", e);
        }
        return null;
    }

    private String devireCompute(ESNodeToIndexTempBean dataBean) {
        String keyPre = dataBean.getKeyPre();

        String dividendKey = keyPre + dataBean.getDeriveParam(ESDataTempBean.DIVIDEND);
        String divisorKey = keyPre + dataBean.getDeriveParam(ESDataTempBean.DIVISOR);

        Double first = metricsRegister.getBeforeComputeData(dividendKey);
        Double second = metricsRegister.getBeforeComputeData(divisorKey);

        if (first == null || second == null) {
            if (EnvUtil.isTest()) {
                LOGGER.warn("class=ESNodeToIndexComputer||method=devireCompute||keyPre={}||value={}", keyPre,
                    dataBean.getValueName());
            }
            return null;
        }

        Object value = computeType.compute(new ESNodeToIndexTempBean(first), new ESNodeToIndexTempBean(second));
        if (value == null) {
            if (EnvUtil.isTest()) {
                LOGGER.warn("class=ESNodeToIndexComputer||method=devireCompute||dataBean={}", dataBean);
            }
            return null;
        }

        return String.valueOf(value);
    }

    private String simpleCompute(ESNodeToIndexTempBean dataBean) {
        String key = dataBean.getKey();
        ESNodeToIndexTempBean beforeData = metricsRegister.getBeforeNodeToIndexData(key);
        metricsRegister.putBeforeNodeToIndexData(key, dataBean);
        if (beforeData == null) {
            if (EnvUtil.isTest()) {
                LOGGER.warn("class=ESNodeToIndexComputer||method=simpleCompute||key={}", key);
            }
            return null;
        } else {

            if (dataBean.getValue() == null) {
                if (EnvUtil.isTest()) {
                    LOGGER.warn(
                        "class=ESNodeToIndexComputer||method=simpleCompute||dataBean={}||msg=dataValue is null!",
                        dataBean);
                }
                return null;
            }

            Double value = computeType.compute(beforeData, dataBean);
            if (value == null) {
                if (EnvUtil.isTest()) {
                    LOGGER.warn(
                        "class=ESNodeToIndexComputer||method=simpleCompute||dataBean={}||msg=computer value is null!",
                        dataBean);
                }
                return null;
            }

            metricsRegister.putBeforeComputeData(key, value);
            return String.valueOf(value);
        }
    }
}
