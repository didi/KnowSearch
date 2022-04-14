package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateValuePO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 低价值索引标签
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkLowValueLabelJob extends BaseTemplateMarkLabelJob {


    @Autowired
    private TemplateValueService templateValueService;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记低价值索引";
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
        TemplateLabelPO valueLow = new TemplateLabelPO();
        valueLow.setIndexTemplateId(indexTemplate.getId());
        valueLow.setLabelId(TemplateLabelEnum.VALUE_LOW.getId());
        valueLow.setLabelName(TemplateLabelEnum.VALUE_LOW.getName());

        TemplateValuePO valuePO = templateValueService.getTemplateValueByLogicTemplateId(indexTemplate.getId());

        if (valuePO == null) {
            expireLabels.add(valueLow);
            return;
        }

        if (valuePO.getValue() < 20) {
            newLabels.add(valueLow);
        } else {
            expireLabels.add(valueLow);
        }
    }
}
