package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static javax.management.timer.Timer.ONE_DAY;

/**
 * 标记没有写入的索引
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkNoWriteLabelJob extends BaseTemplateMarkLabelJob {

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记没有写入的索引";
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
                                            List<TemplateLabelPO> expireLabels) {

        TemplateLabelPO noWrite = new TemplateLabelPO();
        noWrite.setIndexTemplateId(indexTemplate.getId());
        noWrite.setLabelId( TemplateLabelEnum.NO_WRITE.getId());
        noWrite.setLabelName(TemplateLabelEnum.NO_WRITE.getName());


        long current = System.currentTimeMillis();
        double maxTps = ariusStatsIndexInfoEsDao.getTemplateMaxTpsByTimeRangeNoPercent(indexTemplate.getId().longValue(), current - (7 * ONE_DAY), current);

        if (maxTps <= 0) {
            newLabels.add(noWrite);
        } else {
            expireLabels.add(noWrite);
        }

    }

}
