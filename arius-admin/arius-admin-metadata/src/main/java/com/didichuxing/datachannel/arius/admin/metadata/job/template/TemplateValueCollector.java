package com.didichuxing.datachannel.arius.admin.metadata.job.template;

import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValuePO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValueRecordPO;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import com.didichuxing.datachannel.arius.admin.common.util.DateTimeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateValueESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateValueRecordESDAO;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_FAILED;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;
import static com.didichuxing.datachannel.arius.admin.common.util.CommonUtils.formatDouble;
import static org.apache.lucene.util.RamUsageEstimator.ONE_GB;

@Component
public class TemplateValueCollector extends AbstractMetaDataJob {

    @Autowired
    protected TemplateLogicService templateLogicService;

    @Autowired
    private TemplateAccessESDAO templateAccessESDAO;

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired
    private TemplateValueESDAO templateValueEsDao;

    @Autowired
    private TemplateValueRecordESDAO templateValueRecordESDAO;

    private static final String DESC = "模板价值统计任务";

    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=StatisticsTemplateValueJobHandler||method=handleJobTask||msg={} task start", getTaskDesc());

        // 获取所有索引模板信息
        List<IndexTemplateLogicWithClusterAndMasterTemplate> indexTemplates = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplate();
        if (CollectionUtils.isEmpty(indexTemplates)) {
            return JOB_FAILED;
        }

        List<TemplateValuePO> templateValuePOS = Lists.newArrayList();

        // 遍历对每一个索引模板进行打标
        for (IndexTemplateLogicWithClusterAndMasterTemplate template : indexTemplates) {
            try {
                LOGGER.info("class=StatisticsTemplateValueJobHandler||method=handleJobTask||msg={} begin process template: {}",
                        getTaskDesc(), template.getName());

                templateValuePOS.add(statisticTemplateValue(template));

            } catch (Exception e) {
                LOGGER.error("class=StatisticsTemplateValueJobHandler||method=handleJobTask||errMsg={}||template={}",
                        getTaskDesc(), template.getName(), e);
            }
        }

        if (templateValueEsDao.batchInsert(templateValuePOS)) {
            long now = System.currentTimeMillis();
            List<TemplateValueRecordPO> recordPOS = templateValuePOS.stream().map( value -> {
                TemplateValueRecordPO recordPO = new TemplateValueRecordPO();
                BeanUtils.copyProperties(value, recordPO);
                recordPO.setTimestamp(now);
                return recordPO;
            }).collect( Collectors.toList());
            templateValueRecordESDAO.batchInsert(recordPOS);

            LOGGER.info("class=TemplateValueCollector||method=handleJobTask||msg=save template value succ||size={}", templateValuePOS.size());
        } else {
            LOGGER.warn("class=TemplateValueCollector||method=handleJobTask||msg=save template value fail||size={}", templateValuePOS.size());
        }

        return JOB_SUCCESS;
    }

    /**
     * 统计模板的价值分  索引价值(满分100) = 基数 + 索引访问量得分 + 索引存储得分
     * <p>
     *
     * @param template 模板信息
     * @return po
     */
    private TemplateValuePO statisticTemplateValue(IndexTemplateLogicWithClusterAndMasterTemplate template) {

        TemplateValuePO valuePO = new TemplateValuePO();
        valuePO.setLogicTemplateId(template.getId());

        int scoreByQuery    = getQueryScore(template, valuePO);
        int scoreBySize     = getSizeScore(template, valuePO);

        valuePO.setValue(scoreByQuery + scoreBySize);

        if (valuePO.getValue() < 0) {
            valuePO.setValue(0);
        }

        if (valuePO.getValue() > 100) {
            valuePO.setValue(100);
        }

        return valuePO;
    }

    /**
     * 小于50G  25分
     * 50G-250G 20分
     * 250G-1T 15分
     * 1T-10T 5分
     * 大于10T 0分
     *
     * @param template
     * @param valuePO
     * @return
     */
    private int getSizeScore(IndexTemplateLogicWithClusterAndMasterTemplate template, TemplateValuePO valuePO) {
        double totalSizeInBytes = ariusStatsIndexInfoEsDao.getLogicTemplateTotalSize(template.getId().longValue());
        valuePO.setSizeG(formatDouble(totalSizeInBytes / ONE_GB, 2));

        long sizeG = (long) (totalSizeInBytes / ONE_GB);

        if (sizeG > 10 * 1024) {
            return 0;
        }

        if (sizeG > 1024) {
            return 5;
        }

        if (sizeG > 250) {
            return 15;
        }

        if (sizeG > 50) {
            return 20;
        }

        return 25;
    }

    /**
     * 1万以上 100分
     * 5000-1万 80分
     * 1000-5000 60分
     * 100-1000 40分
     * 0-100 20分
     * 0 0分
     *
     * @param template
     * @param valuePO
     * @return
     */
    private int getQueryScore(IndexTemplateLogicWithClusterAndMasterTemplate template, TemplateValuePO valuePO) {
        // 索引模板创建时间和当前时间相差1天内，说明索引新创建的，是没有访问记录的
        if (DateTimeUtil.isAfterDateTime(template.getCreateTime(), 1)) {
            LOGGER.warn("class=StatisticsTemplateValueJobHandler||method=getQueryScore||msg={} process template {}, create time {}",
                    getTaskDesc(), template.getName(), template.getCreateTime());
            return 0;
        }

        // 得到这个索引模板近7天的访问记录
        List<TemplateAccessCountPO> list = templateAccessESDAO.getTemplateAccessLastNDayByLogicTemplateId(template.getId(), 7);

        if (CollectionUtils.isEmpty(list)) {
            throw new AriusRunTimeException("get template access count fail, " + template.getName(), ResultType.FAIL);
        }

        long sum = 0L;
        for (TemplateAccessCountPO po : list) {
            sum = sum + po.getCount();
        }

        // 近7天的平均值
        long avg = sum / list.size();

        valuePO.setAccessCount(avg);

        if(avg > 10000){return 100;}
        if(avg > 5000) {return 80;}
        if(avg > 1000) {return 60;}
        if(avg > 100)  {return 40;}
        if(avg > 0)    {return 20;}

        return 0;
    }

    private String getTaskDesc() {
        return DESC;
    }
}
