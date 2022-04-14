package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator;


import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.BaseDegree;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.OffLine;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.AbstractDegreeIndicator;
import com.didichuxing.datachannel.arius.admin.metadata.utils.ReadExprValueUtil;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;

public class DegreeOffline extends AbstractDegreeIndicator {
    private static final Long GB_IN_BYTE = 1024 * 1024 * 1024L;

    @Override
    public <T extends BaseDegree> T execInner(DegreeParam degreeParam, T t) {
        OffLine offLinePO = (OffLine)t;

        IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate = degreeParam.getIndexTemplate();
        long templateAccessCount    = degreeParam.getTemplateAccessCount();

        offLinePO.setCluster(indexTemplate.getMasterTemplate().getCluster());
        offLinePO.setDepartment(indexTemplate.getLibraDepartment());
        offLinePO.setTemplate(indexTemplate.getName());
        offLinePO.setTemplateId(indexTemplate.getId());
        offLinePO.setZeroCount(0 == degreeParam.getTemplateDocNu());
        offLinePO.setCostByGb(degreeParam.getTemplateSizeInBytes()/GB_IN_BYTE);
        offLinePO.setYesterdayAccessNum(templateAccessCount);

        if(offLinePO.getCostByGb() > 0){
            offLinePO.setSingleGbAccess( ReadExprValueUtil.getDouble2(templateAccessCount / offLinePO.getCostByGb()));
        }

        //开始计算离线健康分
        computeOfflineScore(offLinePO, templateAccessCount);

        return (T)offLinePO;
    }

    private void computeOfflineScore(OffLine offLinePO, long templateAccessCount) {
        StringBuilder offlineDesc = new StringBuilder();
        double offLineScore   = 0.0;
        double singleGbAccess = offLinePO.getSingleGbAccess();

        if(!offLinePO.isZeroCount()){
            if (singleGbAccess > 1) {
                offLineScore = Math.log(singleGbAccess) / Math.log(2.0) * 10;
                offlineDesc.append("Math.log(").append(singleGbAccess).append(")/Math.log(2.0)*10");
            } else {
                offLineScore = singleGbAccess * 10;
                offlineDesc.append(singleGbAccess).append("*10");
            }

            // 按访问次数调整(仅对分数低于60分的)
            offLineScore = adjustByAccessCount(templateAccessCount, offlineDesc, offLineScore);

            // 最高分为100分
            if (offLineScore > 100) {
                offLineScore = 100;
                String temp= offlineDesc.toString();
                offlineDesc.delete(0,offlineDesc.length());
                offlineDesc.append("Math.min(").append(temp).append("),100)");
            }
        }

        offLinePO.setProcess(offlineDesc.toString());
        offLinePO.setScore(Math.floor(offLineScore));
        offLinePO.setPunishment(null);
    }

    private double adjustByAccessCount(long templateAccessCount, StringBuilder offlineDesc, double offLineScore) {
        if (offLineScore < 60) {
            int addScore = 0;

            if (templateAccessCount >= 1000000) {
                addScore = 30;
            } else if (1000000 > templateAccessCount && templateAccessCount >= 100000) {
                addScore = 20;
            } else if (100000 > templateAccessCount && templateAccessCount >= 10000) {
                addScore = 10;
            }

            if(addScore > 0){
                offLineScore += addScore;
                offlineDesc.append(" (+").append(addScore).append(")");
            }
        }
        return offLineScore;
    }

    @Override
    public IndicatorsType getType() {
        return IndicatorsType.BASE_OF_HEALTH_DEGREES;
    }

    @Override
    public OffLine getRealTimePO() {
        return new OffLine();
    }
}
