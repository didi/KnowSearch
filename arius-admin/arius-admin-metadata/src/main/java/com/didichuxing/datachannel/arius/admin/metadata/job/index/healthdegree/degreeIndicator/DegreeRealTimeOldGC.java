package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeOldGC;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
import org.apache.commons.lang3.StringUtils;

public class DegreeRealTimeOldGC extends AbstractDegreeIndicator {
    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double gcCount = 0;
        for (ESIndexToNodeStats esESIndexToNodeStats : degreeParam.getEsIndexToNodeStats()) {
            if(StringUtils.isNotBlank(esESIndexToNodeStats.getMetrics().get("jvm-gc-old-collection_count"))){
                gcCount += Math.floor(Double.parseDouble(esESIndexToNodeStats.getMetrics().get("jvm-gc-old-collection_count")) * 100) / 100;
            }
        }

        RealTimeOldGC realTimeOldGCPO = (RealTimeOldGC)t;

        realTimeOldGCPO.setAvgJvmOldGc(gcCount);
        realTimeOldGCPO.setScore(calc1(gcCount));
        return (T)realTimeOldGCPO;
    }

    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_JVM;
    }

    @Override
    public RealTimeOldGC getRealTimePO() {
        return new RealTimeOldGC();
    }
}
