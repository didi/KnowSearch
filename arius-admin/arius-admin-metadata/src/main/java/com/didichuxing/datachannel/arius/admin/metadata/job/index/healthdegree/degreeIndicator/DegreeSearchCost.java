package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.RealTimeSearchCost;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
@Deprecated
public class DegreeSearchCost extends AbstractDegreeIndicator {
    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        double searchCostTime = Math.floor(degreeParam.getTodayReaTimelInfo().getAvgIndicesSearchQueryTime() * 10000) / 10000;

        RealTimeSearchCost realTimeSearchCostPO = (RealTimeSearchCost)t;

        realTimeSearchCostPO.setAvgSearchCostTime(searchCostTime);

        double score;

        if (searchCostTime <= 0) {
            score = 100.0;
            realTimeSearchCostPO.setDesc("近期无查询,无查询时长信息.");
        } else {
            score = calc(searchCostTime);
        }

        realTimeSearchCostPO.setScore(score);
        return (T)realTimeSearchCostPO;
    }

    @Override
    public IndicatorsType getType() {
        return IndicatorsType.REAL_TIME_SEARCH_COST;
    }

    @Override
    public RealTimeSearchCost getRealTimePO() {
        return new RealTimeSearchCost();
    }
}