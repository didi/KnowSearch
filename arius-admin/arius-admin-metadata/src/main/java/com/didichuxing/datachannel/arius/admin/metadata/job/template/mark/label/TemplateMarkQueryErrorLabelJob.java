package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.persistence.es.index.dao.gateway.GatewayJoinESDAO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static javax.management.timer.Timer.ONE_DAY;

/**
 * 标记有异常查询的索引
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkQueryErrorLabelJob extends BaseTemplateMarkLabelJob {

    @Autowired
    private GatewayJoinESDAO gatewayJoinEsDao;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记有异常查询的索引";
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
                                            List<TemplateLabelPO> newLabels, List<TemplateLabelPO> expireLabels) {
        TemplateLabelPO errQuery = new TemplateLabelPO();
        errQuery.setIndexTemplateId(indexTemplate.getId());
        errQuery.setLabelId( TemplateLabelEnum.QUERY_ERROR.getId());
        errQuery.setLabelName(TemplateLabelEnum.QUERY_ERROR.getName());

        long current = System.currentTimeMillis();
        long errorCount = gatewayJoinEsDao.getErrorCntByTemplateName(indexTemplate.getName(), current - (7 * ONE_DAY), current);

        if (errorCount <= 0) {
            expireLabels.add(errQuery);
        } else {
            newLabels.add(errQuery);
        }

    }


}
