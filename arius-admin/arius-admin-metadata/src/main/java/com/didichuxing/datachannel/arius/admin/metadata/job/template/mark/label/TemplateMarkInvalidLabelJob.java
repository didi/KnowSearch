package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static javax.management.timer.Timer.ONE_DAY;

/**
 * 标记没有写入的索引
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkInvalidLabelJob extends BaseTemplateMarkLabelJob {

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    @Autowired
    private TemplateAccessESDAO templateAccessCountEsDao;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记废弃的索引";
    }

    /**
     * 生成一个索引模板需要新增和删除的标签
     *
     * @param indexTemplate
     * @param newLabels
     * @param expireLabels
     * @return
     */
    @Override
    protected void genShouldHasAndDelLabels(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate,
                                            List<TemplateLabelPO> newLabels,
                                            List<TemplateLabelPO> expireLabels) throws Exception {
        Date now = new Date();
        long current = now.getTime();
        long start = current - (30 * ONE_DAY);

        TemplateLabelPO invalid = new TemplateLabelPO();
        invalid.setIndexTemplateId(indexTemplate.getId());
        invalid.setLabelId( TemplateLabelEnum.INVALID.getId());
        invalid.setLabelName(TemplateLabelEnum.INVALID.getName());

        double maxTps = ariusStatsIndexInfoEsDao.getTemplateMaxTpsByTimeRangeNoPercent(indexTemplate.getId().longValue(), start, current);

        List<TemplateAccessCountPO> accessCountPos = templateAccessCountEsDao.getTemplateAccessLastNDayByLogicTemplateId(indexTemplate.getId(), 30);
        long accessCount = 0;
        if (CollectionUtils.isNotEmpty(accessCountPos)) {
            for (TemplateAccessCountPO countPo : accessCountPos) {
                accessCount = accessCount + countPo.getCount();
            }
        }

        LOGGER.info("method=MarkInvalidLabelJobHandler.genShouldHasAndDelLabels||templateId={}||templateName={}||accessCount={}||maxTps={}",
                indexTemplate.getId(), indexTemplate.getName(), accessCount, maxTps);

        if (maxTps < 0.0001 && accessCount < 1L) {
            newLabels.add(invalid);
        } else {
            expireLabels.add(invalid);
        }
    }
}