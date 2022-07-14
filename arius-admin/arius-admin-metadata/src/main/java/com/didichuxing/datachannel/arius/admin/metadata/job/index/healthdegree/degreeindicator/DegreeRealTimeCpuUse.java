package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeindicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeCpuUse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
import org.apache.commons.lang3.StringUtils;

@Deprecated
public class DegreeRealTimeCpuUse extends AbstractDegreeIndicator {
    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double avgCpuUse;
        double totalCpuUse = 0.0;
        for (ESIndexToNodeStats esESIndexToNodeStats : degreeParam.getEsIndexToNodeStats()) {
            if (StringUtils.isNotBlank(esESIndexToNodeStats.getMetrics().get("os-cpu-percent"))) {
                totalCpuUse += Double.parseDouble(esESIndexToNodeStats.getMetrics().get("os-cpu-percent"));
            }
        }

        RealTimeCpuUse realTimeCpuUsePO = (RealTimeCpuUse) t;

        if (degreeParam.getEsIndexToNodeStats().size() == 0) {
            realTimeCpuUsePO.setScore(100.0);
            realTimeCpuUsePO.setDesc("暂无实时cpu使用率信息.");
        } else {
            avgCpuUse = Math.floor(totalCpuUse / degreeParam.getEsIndexToNodeStats().size() * 100) / 100;
            realTimeCpuUsePO.setAvgCpuAvgUse(avgCpuUse);
            realTimeCpuUsePO.setScore(calc1(avgCpuUse));
        }

        return (T) realTimeCpuUsePO;
    }

    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_CPU_USE;
    }

    @Override
    public RealTimeCpuUse getRealTimePO() {
        return new RealTimeCpuUse();
    }
}