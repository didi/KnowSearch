package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.label;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateLabelEnum;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.metadata.job.template.mark.BaseTemplateMarkLabelJob;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.indices.stats.IndexNodes;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 索引删除文档个数标签
 *
 * @author d06679
 * @date 2018/4/3
 */
@Component
public class TemplateMarkHasDeletedDocLabelJob extends BaseTemplateMarkLabelJob {

    /**
     * 访问es集群客户端
     */
    @Autowired
    private ESIndexService esIndexService;

    /**
     * 任务描述
     *
     * @return
     */
    @Override
    protected String getTaskDesc() {
        return "标记有删除操作的索引";
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
        Long delCount = getDelCount(indexTemplate);
        if (delCount < 0) {
            return;
        }

        TemplateLabelPO hasDelLabel = new TemplateLabelPO();
        hasDelLabel.setIndexTemplateId(indexTemplate.getId());
        hasDelLabel.setLabelId(TemplateLabelEnum.HAS_DELETED_DOC.getId());
        hasDelLabel.setLabelName(TemplateLabelEnum.HAS_DELETED_DOC.getName());

        if (delCount <= 0) {
            expireLabels.add(hasDelLabel);
        } else {
            newLabels.add(hasDelLabel);
        }

    }

    private Long getDelCount(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate) {
        IndexTemplatePhy masterTemplate = indexTemplate.getMasterTemplate();

        Map<String, IndexNodes> indexNodesMap = esIndexService.getIndexNodes(masterTemplate.getCluster(), masterTemplate.getExpression());
        if (indexNodesMap == null || indexNodesMap.isEmpty()) {
            LOGGER.error("class=MarkHasDeletedDocLabelJobHandler||method=getDelCount||errMsg=template: {} get index fail", indexTemplate.getName());
            return -1L;
        }

        List<Long> delCountList = Lists.newArrayList();
        Long sum = 0L;

        // 每个索引删除文档个数进行累加
        for (Map.Entry<String, IndexNodes> entry : indexNodesMap.entrySet()) {
            Long count = entry.getValue().getPrimaries().getDocs().getDeleted();

            delCountList.add(count);
            sum = sum + count;
        }

        // 由于indexNodesMap以非空判断，delCountList肯定不为空
        return sum / delCountList.size();
    }


}
