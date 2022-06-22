package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeSearch;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
@Deprecated
public class DegreeRealTimeSearch extends AbstractDegreeIndicator {
    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_SEARCH;
    }

    @Override
    public RealTimeSearch getRealTimePO(){
        return new RealTimeSearch();
    }

    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double todaySearchRate  = Math.floor(degreeParam.getTodayReaTimelInfo().getAvgSearchQueryTotalRate() * 10000) / 10000;
        double yesdaySearchRate = Math.floor(degreeParam.getYesdayReaTimelInfo().getAvgSearchQueryTotalRate() * 10000) / 10000;

        RealTimeSearch realTimeSearchPO = (RealTimeSearch)t;

        realTimeSearchPO.setAvgSearchRate(todaySearchRate);
        realTimeSearchPO.setYesterdayAvgSearchRate(yesdaySearchRate);

        StringBuilder descBuild = new StringBuilder();
        descBuild.append("当前实时查询率为:").append(todaySearchRate).append("次/s");
        descBuild.append(" ,历史查询率为:").append(yesdaySearchRate).append("次/s");
        realTimeSearchPO.setDesc(descBuild.toString());

        double score = (yesdaySearchRate <= 0) ? 100 : calc(todaySearchRate / yesdaySearchRate);

        realTimeSearchPO.setScore(score);

        StringBuilder processBuild = new StringBuilder();
        processBuild.append(score).append("*").append(realTimeSearchPO.getWeightRate()).append("%");
        realTimeSearchPO.setProcess(processBuild.toString());

        return (T)realTimeSearchPO;
    }
}