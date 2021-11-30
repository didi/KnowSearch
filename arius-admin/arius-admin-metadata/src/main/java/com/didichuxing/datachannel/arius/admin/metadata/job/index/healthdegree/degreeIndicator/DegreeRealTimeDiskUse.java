package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeDiskUse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
import org.apache.commons.lang3.StringUtils;

public class DegreeRealTimeDiskUse extends AbstractDegreeIndicator {
    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double avgDiskUse;
        double totalDiskUse = 0.0;

        for (ESIndexToNodeStats esESIndexToNodeStats : degreeParam.getEsIndexToNodeStats()) {
            if(StringUtils.isNotBlank(esESIndexToNodeStats.getMetrics().get("fs-total-disk_free_percent"))){
                totalDiskUse += (1 - Double.parseDouble(esESIndexToNodeStats.getMetrics().get("fs-total-disk_free_percent")));
            }
        }

        RealTimeDiskUse realTimeDiskUsePO = (RealTimeDiskUse)t;

        if (degreeParam.getEsIndexToNodeStats().size() == 0) {
            realTimeDiskUsePO.setScore(100.0);
            realTimeDiskUsePO.setProcess("100*" + realTimeDiskUsePO.getWeight() + "%");
            realTimeDiskUsePO.setDesc("暂无实时磁使用率信息.");
        } else {
            // 保留两位小数
            avgDiskUse = Math.floor(totalDiskUse * 10000 / degreeParam.getEsIndexToNodeStats().size()) / 100;
            realTimeDiskUsePO.setAvgDiskUse(avgDiskUse);
            realTimeDiskUsePO.setScore(calc1(avgDiskUse));
        }

        return (T)realTimeDiskUsePO;
    }

    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_DISK_RATE;
    }

    @Override
    public RealTimeDiskUse getRealTimePO() {
        return new RealTimeDiskUse();
    }
}
