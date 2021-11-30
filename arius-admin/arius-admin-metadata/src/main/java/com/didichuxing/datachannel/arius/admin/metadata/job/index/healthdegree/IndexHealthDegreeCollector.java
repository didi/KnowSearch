package com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndexRealTimeInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.index.IndicatorChild;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.stats.ESIndexToNodeStats;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.index.healthdegree.HealthDegreesPO;
import com.didichuxing.datachannel.arius.admin.common.constant.IndicatorsType;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.job.index.healthdegree.degreeIndicator.*;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.index.IndexHealthDegreeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexNodeInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 *  @author: zhaoqingrong
 *  @description:索引健康分
 *  @date: Create on 2019/2/25 下午5:20
 */
@Component
public class IndexHealthDegreeCollector extends AbstractMetaDataJob {

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired
    private AriusStatsIndexNodeInfoESDAO ariusStatsIndexNodeInfoEsDao;

    @Autowired
    private TemplateAccessESDAO templateAccessCountEsDao;

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private IndexHealthDegreeDAO indexHealthDegreeDao;

    private int monitorReportTime = 3;

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=IndexHealthDegreeJobHandler||method=handleJobTask||params={}", params);

        Map<String, Boolean>     healthDegreesMap   = new HashMap<>();
        List<IndicatorChild>   indicatorChildPOS  = genIndicatorChilds();

        Long todayEnd    = System.currentTimeMillis() - 2 * 60 * 1000;
        Long todayStart  = todayEnd - monitorReportTime * 60 * 1000;

        //1、获取到admin中所有模板
        List<IndexTemplateLogicWithClusterAndMasterTemplate>  indexTemplates =templateLogicService.getLogicTemplatesWithClusterAndMasterTemplate();

        for (IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate : indexTemplates){
            String templateName = indexTemplate.getName();
            String cluster      = indexTemplate.getMasterTemplate().getCluster();

            //2、获取索引的离线统计信息(容量、访问量、文档数)
            double templateSizeInBytes = ariusStatsIndexInfoEsDao.getTemplateTotalSize(templateName, cluster);
            long   templateDocNu       = ariusStatsIndexInfoEsDao.getTemplateTotalDocNu(templateName, cluster);
            long   templateAccessCount = templateAccessCountEsDao.getYesterDayTemplateAccessCount(templateName);

            //3、获取索引的实时/昨天查询统计信息
            IndexRealTimeInfo todayReaTimelInfo  = getIndexRealTimeInfo(templateName, cluster, 0);
            IndexRealTimeInfo yesdayReaTimelInfo = getIndexRealTimeInfo(templateName, cluster, 1);

            //4、获取索引的实时
            List<ESIndexToNodeStats> indexToNodeStats = ariusStatsIndexNodeInfoEsDao.getIndexToNodeStats(templateName, cluster, todayStart, todayEnd);

            DegreeParam degreeParam = genDegreeParam(indexTemplate, templateDocNu, templateSizeInBytes, templateAccessCount, todayReaTimelInfo, yesdayReaTimelInfo, indexToNodeStats, indicatorChildPOS);

            //5、开始计算健康分
            HealthDegreesPO healthDegreesPO = calcHealthDegrees(degreeParam);
            healthDegreesMap.put(templateName, indexHealthDegreeDao.batchInsertHealthDegress(Arrays.asList(healthDegreesPO)));
        }

        if(logOpen()){
            LOGGER.info("class=IndexHealthDegreeJobHandler||method=handleJobTask||healthDegreesMap={}", JSON.toJSONString(healthDegreesMap));
        }

        return JOB_SUCCESS;
    }

    /********************************************* private methods *********************************************/
    private IndexRealTimeInfo getIndexRealTimeInfo(String template, String cluster, int offset){
        Long todayEnd    = System.currentTimeMillis() - 2 * 60 * 1000 - offset * 24 * 60 * 60 * 1000;
        Long todayStart  = todayEnd - monitorReportTime * 60 * 1000;
        return ariusStatsIndexInfoEsDao.getIndexRealTimeInfoByTemplateAndCluster(offset, todayStart, todayEnd, template, cluster);
    }

    private DegreeParam genDegreeParam(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, long templateDocNu,
                                       double templateSizeInBytes, long templateAccessCount,
                                       IndexRealTimeInfo todayReaTimelInfo,
                                       IndexRealTimeInfo yesdayReaTimelInfo,
                                       List<ESIndexToNodeStats> esIndexToNodeStats,
                                       List<IndicatorChild> indicatorChildPOS){
        DegreeParam degreeParam = new DegreeParam();
        degreeParam.setIndexTemplate(indexTemplate);
        degreeParam.setTemplateDocNu(templateDocNu);
        degreeParam.setTemplateAccessCount(templateAccessCount);
        degreeParam.setTemplateSizeInBytes(templateSizeInBytes);
        degreeParam.setEsIndexToNodeStats(esIndexToNodeStats);
        degreeParam.setTodayReaTimelInfo(todayReaTimelInfo);
        degreeParam.setYesdayReaTimelInfo(yesdayReaTimelInfo);
        degreeParam.setIndicatorChilds(indicatorChildPOS);
        return degreeParam;
    }

    /**
     * 计算索引健康度
     * @param degreeParam
     * @return
     */
    private HealthDegreesPO calcHealthDegrees(DegreeParam degreeParam){
        IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate = degreeParam.getIndexTemplate();

        HealthDegreesPO healthDegreesPO = new HealthDegreesPO();
        healthDegreesPO.setTemplate(indexTemplate.getName());
        healthDegreesPO.setCluster(indexTemplate.getMasterTemplate().getCluster());
        healthDegreesPO.setTemplateId(indexTemplate.getId());
        healthDegreesPO.setLogicTemplateId(indexTemplate.getId());
        healthDegreesPO.setDepartment(indexTemplate.getLibraDepartment());
        healthDegreesPO.setTimestamp(System.currentTimeMillis());

        healthDegreesPO.setOffLine(new DegreeOffline().exec(degreeParam));
        healthDegreesPO.setRealTimeCpuUse(new DegreeRealTimeCpuUse().exec(degreeParam));
        healthDegreesPO.setRealTimeDiskUse(new DegreeRealTimeDiskUse().exec(degreeParam));
        healthDegreesPO.setRealTimeOldGC(new DegreeRealTimeOldGC().exec(degreeParam));
        healthDegreesPO.setRealTimeSearch(new DegreeRealTimeSearch().exec(degreeParam));
        healthDegreesPO.setRealTimeWrite(new DegreeRealTimeWriter().exec(degreeParam));
        healthDegreesPO.setRealTimeSearchCost(new DegreeSearchCost().exec(degreeParam));

        return calcIndexToalHealthDegress(healthDegreesPO);
    }

    private HealthDegreesPO calcIndexToalHealthDegress(HealthDegreesPO healthDegreesPO){
        try {
            Double onLineScore = healthDegreesPO.getRealTimeWrite().getWeightScore()
                    + healthDegreesPO.getRealTimeDiskUse().getWeightScore()
                    + healthDegreesPO.getRealTimeOldGC().getWeightScore()
                    + healthDegreesPO.getRealTimeCpuUse().getWeightScore()
                    + healthDegreesPO.getRealTimeSearch().getWeightScore()
                    + healthDegreesPO.getRealTimeSearchCost().getWeightScore();

            Double offLineScore = healthDegreesPO.getOffLine().getWeightScore();

            healthDegreesPO.setOnLineScore(onLineScore);
            healthDegreesPO.setOffLineScore(offLineScore);

            Double total = offLineScore + onLineScore;

            if(total <= 0){total = 0.0;}

            healthDegreesPO.setTotalScore(Math.floor(total * 100) / 100);

            healthDegreesPO.setDesc("索引健康分计算=离线模块得分(权重:" + IndicatorsType.BASE_OF_HEALTH_DEGREES + "%):"
                    + healthDegreesPO.getOffLineScore() + "+实时写入指标(权重:"
                    + healthDegreesPO.getRealTimeWrite().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeWrite().getWeightScore() + "+磁盘使用指标(权重:"
                    + healthDegreesPO.getRealTimeDiskUse().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeDiskUse().getWeightScore() + "+实时jvmGc指标(权重:"
                    + healthDegreesPO.getRealTimeOldGC().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeOldGC().getWeightScore() + "+实时cpu使用率指标(权重:"
                    + healthDegreesPO.getRealTimeCpuUse().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeCpuUse().getWeightScore() + "+实时查询量(权重:"
                    + healthDegreesPO.getRealTimeSearch().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeSearch().getWeightScore() + "+实时实时查询时长(权重:"
                    + healthDegreesPO.getRealTimeSearchCost().getWeight() + "%):"
                    + healthDegreesPO.getRealTimeSearchCost().getWeightScore() + "\r\n");

            if(logOpen()){
                LOGGER.info("class=IndexHealthDegreeJobHandler||method=calcIndexToalHealthDegress||healthDegreesPO={}", healthDegreesPO.getDesc());
            }
            return healthDegreesPO;
        }catch (Exception e){
            LOGGER.error("class=IndexHealthDegreeJobHandler||method=handleJobTask||params={}", JSON.toJSONString(healthDegreesPO), e);
        }
        return healthDegreesPO;
    }

    //从紫阳代码翻译而来
    private List<IndicatorChild> genIndicatorChilds(){
        List<IndicatorChild> indicatorChildPOS = new ArrayList<>();
        indicatorChildPOS.add(new IndicatorChild(1, 2, 0, 99, "k*100", 1));
        indicatorChildPOS.add(new IndicatorChild(2, 2, 100, -1, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(3, 3, 0, 99, "k*100", 1));
        indicatorChildPOS.add(new IndicatorChild(4, 3, 100, -1, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(5, 5, 0, 50, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(6, 5, 51, 200, "80", 1));
        indicatorChildPOS.add(new IndicatorChild(7, 5, 201, 1000, "60", 1));
        indicatorChildPOS.add(new IndicatorChild(8, 5, 1001, 5000, "40", 1));
        indicatorChildPOS.add(new IndicatorChild(9, 5, 5001, -1, "0", 1));
        indicatorChildPOS.add(new IndicatorChild(10, 6, 0, 30, "40", 1));
        indicatorChildPOS.add(new IndicatorChild(11, 6, 31, 50, "60", 1));
        indicatorChildPOS.add(new IndicatorChild(12, 6, 51, 80, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(13, 6, 81, 90, "20", 1));
        indicatorChildPOS.add(new IndicatorChild(14, 6, 91, 100, "0", 1));
        indicatorChildPOS.add(new IndicatorChild(15, 7, 0, 10, "40", 1));
        indicatorChildPOS.add(new IndicatorChild(16, 7, 11, 20, "60", 1));
        indicatorChildPOS.add(new IndicatorChild(17, 7, 21, 60, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(18, 7, 61, 80, "40", 1));
        indicatorChildPOS.add(new IndicatorChild(19, 7, 81, 100, "0", 1));
        indicatorChildPOS.add(new IndicatorChild(20, 4, 0, 2, "100", 1));
        indicatorChildPOS.add(new IndicatorChild(21, 4, 3, 4, "80", 1));
        indicatorChildPOS.add(new IndicatorChild(22, 4, 5, 6, "40", 1));
        indicatorChildPOS.add(new IndicatorChild(23, 4, 7, 8, "20", 1));
        indicatorChildPOS.add(new IndicatorChild(24, 4, 9, -1, "0", 1));

        return indicatorChildPOS;
    }

    private boolean logOpen(){
        return !EnvUtil.isOnline();
    }
}