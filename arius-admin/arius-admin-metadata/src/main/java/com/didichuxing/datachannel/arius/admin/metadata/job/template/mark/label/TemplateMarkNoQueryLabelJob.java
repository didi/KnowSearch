package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.query.TemplateAccessCountPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.template.TemplateAccessESDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标记没有查询的索引
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkNoQueryLabelJob extends BaseTemplateMarkLabelJob {

    @Autowired
    private TemplateAccessESDAO templateAccessEsDao;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记没有查询的索引";
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
    protected void genShouldHasAndDelLabels(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, List<TemplateLabelPO> newLabels, List<TemplateLabelPO> expireLabels) {
        TemplateLabelPO noQuery = new TemplateLabelPO();
        noQuery.setIndexTemplateId(indexTemplate.getId());
        noQuery.setLabelId(TemplateLabelEnum.NO_QUERY.getId());
        noQuery.setLabelName(TemplateLabelEnum.NO_QUERY.getName());

        int days = 30;
        List<TemplateAccessCountPO> list = templateAccessEsDao.getTemplateAccessLastNDayByLogicTemplateId(indexTemplate.getId(), days);

        if (CollectionUtils.isEmpty(list) || list.size() < (days * 8 / 10)) {
            expireLabels.add(noQuery);
            return;
        }

        long sum = 0L;
        for (TemplateAccessCountPO po : list) {
            sum = sum + po.getCount();
        }

        if (sum == 0) {
            newLabels.add(noQuery);
        } else {
            expireLabels.add(noQuery);
        }
    }
    
}
