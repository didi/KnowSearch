package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeWrite;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;

public class DegreeRealTimeWriter extends AbstractDegreeIndicator {
    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_WRITE;
    }

    @Override
    public RealTimeWrite getRealTimePO(){
        return new RealTimeWrite();
    }

    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double todayIndexRate  = Math.floor(degreeParam.getTodayReaTimelInfo().getAvgIndexingIndexTotalRate() * 10000) / 10000;
        double yesdayIndexRate = Math.floor(degreeParam.getYesdayReaTimelInfo().getAvgIndexingIndexTotalRate() * 10000) / 10000;

        RealTimeWrite realTimeWritePO = (RealTimeWrite)t;

        realTimeWritePO.setAvgIndexingRate(todayIndexRate);
        realTimeWritePO.setYesterdayAvgIndexingRate(yesdayIndexRate);

        StringBuilder descBuild = new StringBuilder();
        descBuild.append("实时写入率为:").append(todayIndexRate).append("条/s");
        descBuild.append(" ,历史写入率为:").append(yesdayIndexRate).append("条/s");
        realTimeWritePO.setDesc(descBuild.toString());

        double score;

        if (todayIndexRate <= 0) {
            score = 0.0;
        } else if (yesdayIndexRate <= 0) {
            score = 100.0;
        } else {
            score = calc(todayIndexRate / yesdayIndexRate);
        }

        realTimeWritePO.setScore(score);

        StringBuilder processBuild = new StringBuilder();
        processBuild.append(score).append("*").append(realTimeWritePO.getWeightRate()).append("%");
        realTimeWritePO.setProcess(processBuild.toString());

        return (T)realTimeWritePO;
    }
}
