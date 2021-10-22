package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.stats.AriusStatsIndexInfoESDAO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标记无数据索引
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkNoDataLabelJob extends BaseTemplateMarkLabelJob {

    @Autowired
    private AriusStatsIndexInfoESDAO ariusStatsIndexInfoEsDao;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记无数据索引";
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
        TemplateLabelPO noData = new TemplateLabelPO();
        noData.setIndexTemplateId(indexTemplate.getId());
        noData.setLabelId( TemplateLabelEnum.NO_DATA.getId());
        noData.setLabelName(TemplateLabelEnum.NO_DATA.getName());

        IndexTemplatePhy masterTemplate = indexTemplate.getMasterTemplate();
        long totalDocNum = ariusStatsIndexInfoEsDao.getTemplateTotalDocNu(masterTemplate.getName(), masterTemplate.getCluster());


        if (totalDocNum == 0) {
            newLabels.add(noData);
        } else {
            expireLabels.add(noData);
        }

    }

}
