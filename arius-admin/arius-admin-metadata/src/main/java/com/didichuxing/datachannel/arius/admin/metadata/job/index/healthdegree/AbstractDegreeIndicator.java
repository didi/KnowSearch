package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree;

import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndicatorChild;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeindicator.DegreeParam;
import com.didichuxing.datachannel.arius.admin.metadata.utils.ReadExprValueUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public abstract class AbstractDegreeIndicator implements IDegreeIndicator {
    protected static final ILog LOGGER = LogFactory.getLog(AbstractDegreeIndicator.class);

    private DegreeParam         degreeParam;

    protected AbstractDegreeIndicator() {
    }

    @Override
    public <T extends BaseDegree> T exec(DegreeParam degreeParam) {
        this.degreeParam = degreeParam;

        T baseRealTimePO = getRealTimePO();
        baseRealTimePO.setIndicatorsType(getType());
        baseRealTimePO.setWeight(getType().getWeight());
        baseRealTimePO.setWeightRate(IndicatorsType.getWeightRate(getType()));

        if (isZero()) {
            baseRealTimePO.setPunishment("*0%");
            baseRealTimePO.setWeightScore(0d);
            baseRealTimePO.setScore(0d);
            baseRealTimePO.setDesc("索引文档数为0, " + getType().getName() + "得分为0!");
        } else {
            try {
                baseRealTimePO = execInner(degreeParam, baseRealTimePO);
                baseRealTimePO.setWeightScore(baseRealTimePO.getScore() * baseRealTimePO.getWeightRate());
            } catch (Exception e) {
                LOGGER.info("class=AbstractDegreeIndicator||method=exec||index={}||type={}",
                    degreeParam.getIndexTemplate().getName(), getType().getName(), e);
            }
        }

        if (EnvUtil.isPre()) {
            LOGGER.info("class=AbstractDegreeIndicator||method=exec||index={}||type={}||score={}||weightScore=={}",
                degreeParam.getIndexTemplate().getName(), getType().getName(), baseRealTimePO.getScore(),
                baseRealTimePO.getWeightScore());
        }

        return baseRealTimePO;
    }

    public abstract <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t);

    protected boolean isZero() {
        return degreeParam.getTemplateDocNu() == 0;
    }

    protected double calc(double ratio) {
        double score = 0.0;

        for (IndicatorChild indicatorChildPO : degreeParam.getIndicatorChilds()) {
            if (getType().getCode() != indicatorChildPO.getCode()) {
                continue;
            }

            if (indicatorChildPO.getUpper() <= Math.floor(ratio)
                && (indicatorChildPO.getLower() == -1 || indicatorChildPO.getLower() >= Math.floor(ratio))) {
                String expr = indicatorChildPO.getScoreExpr().replace("k", "" + ratio);
                double value = ReadExprValueUtil.readExprValue(expr);

                if (value > 100) {
                    value = 100;
                }

                score = Math.floor(value * 100) / 100;
                break;
            }
        }

        return score;
    }

    protected double calc1(double ratio) {
        double score = 0.0;

        for (IndicatorChild indicatorChildPO : degreeParam.getIndicatorChilds()) {
            if (getType().getCode() != indicatorChildPO.getCode()) {
                continue;
            }

            if (indicatorChildPO.getUpper() <= Math.floor(ratio)
                && (indicatorChildPO.getLower() == -1 || indicatorChildPO.getLower() >= Math.floor(ratio))) {
                String expr = indicatorChildPO.getScoreExpr();
                score = ReadExprValueUtil.readExprValue(expr);

                if (score > 100) {
                    score = 100;
                }

                break;
            }
        }

        return score;
    }
}