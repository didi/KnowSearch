package com.didichuxing.datachannel.arius.admin.metadata.job.template.mark;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.template.TemplateLabelPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.metadata.job.AbstractMetaDataJob;
import com.didichuxing.datachannel.arius.admin.metadata.service.TemplateLabelService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_FAILED;
import static com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant.JOB_SUCCESS;

/**
 * 对索引模板进行打标基础执行器
 *
 * @author d06679
 * @date 2018/3/29
 */
public abstract class BaseTemplateMarkLabelJob extends AbstractMetaDataJob {

    /**
     * 操作索引标签数据
     */
    @Autowired
    protected TemplateLabelService templateLabelService;

    @Autowired
    protected TemplateLogicService templateLogicService;

    /**
     * 处理任务
     *
     * @param params 参数
     * @return
     */
    @Override
    public Object handleJobTask(String params) {
        LOGGER.info("class=BaseTemplateMarkLabelJob||method=handleJobTask||msg=Mark {} task start", getTaskDesc());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 获取所有索引模板信息
        List<IndexTemplateLogicWithClusterAndMasterTemplate> indexTemplates = templateLogicService.getLogicTemplatesWithClusterAndMasterTemplate();
        if (CollectionUtils.isEmpty(indexTemplates)) {
            return JOB_FAILED;
        }

        // 过滤索引模板
        List<IndexTemplateLogicWithClusterAndMasterTemplate> lastTemplates = filterTemplate(indexTemplates);

        long changeCount = 0;

        // 遍历对每一个索引模板进行打标
        for (IndexTemplateLogicWithClusterAndMasterTemplate template : lastTemplates) {
            try {
                LOGGER.debug("class=BaseTemplateMarkLabelJob||method=handleJobTask||msg=mark {}  begin process template: {}",
                        getTaskDesc(), template.getName());

                // 对一个索引模板执行具体的打标任务
                List<TemplateLabelPO> shouldAdds = Lists.newArrayList();
                List<TemplateLabelPO> shouldDels = Lists.newArrayList();
                markTemplateLabel(template, shouldAdds, shouldDels);

                if (CollectionUtils.isNotEmpty(shouldAdds)) {
                    boolean insertResult = templateLabelService.batchInsert(shouldAdds);
                    changeCount += shouldAdds.size();
                    LOGGER.debug("class=BaseTemplateMarkLabelJob||method=handleJobTask||msg=template={}||label={}||changeCount={}||result={}",
                            template.getName(), getJoinLabelName(shouldAdds), changeCount, insertResult);
                }

                if (CollectionUtils.isNotEmpty(shouldDels)) {
                    boolean deleteResult = templateLabelService.batchDelete(shouldDels.stream().map( TemplateLabelPO::getId).collect(Collectors.toList()));
                    changeCount += shouldDels.size();
                    LOGGER.debug("class=BaseTemplateMarkLabelJob||method=handleJobTask||msg=template: {} delete label {} result: {}",
                            template.getName(), getJoinLabelName(shouldDels), deleteResult);

                }

            } catch (Exception e) {
                LOGGER.error("class=BaseTemplateMarkLabelJob||method=handleJobTask||errMsg={}||template={}||stack={}",
                        getTaskDesc(), template.getName(), e);
            }
        }


        stopWatch.stop();
        LOGGER.info("class=BaseTemplateMarkLabelJob||method=handleJobTask||msg=mark {} task finish, cost : {}",
                getTaskDesc(), stopWatch.getTime());

        return JOB_SUCCESS;
    }

    /**
     * 任务描述
     *
     * @return
     */
    protected abstract String getTaskDesc();

    /**
     * 生成一个索引模板需要新增和删除的标签
     *
     * @return
     */
    protected abstract void genShouldHasAndDelLabels(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, List<TemplateLabelPO> newLabels, List<TemplateLabelPO> expireLabels);


    /**
     * 过滤索引模板
     *
     * @param indexTemplatePOs
     * @return
     */
    protected List<IndexTemplateLogicWithClusterAndMasterTemplate> filterTemplate(List<IndexTemplateLogicWithClusterAndMasterTemplate> indexTemplatePOs) {
        return indexTemplatePOs;
    }


    /**
     * 对一个索引模板执行具体的打标任务
     */
    private void markTemplateLabel(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, List<TemplateLabelPO> shouldAdds, List<TemplateLabelPO> shouldDels) {
        if (indexTemplate == null) {
            LOGGER.error("class=BaseTemplateMarkLabelJob||method=markOneTemplateLabel||errMsg=mark template is null.");
            return;
        }

        // 根据索引模板Id和标签维度进行查找
        Map<String, TemplateLabelPO> existLabelMap = getExistLabelList(indexTemplate);

        // 回调子类具体实现
        // 识别出需要新增和删除的标签
        List<TemplateLabelPO> newLabels = Lists.newArrayList();
        List<TemplateLabelPO> expireLabels = Lists.newArrayList();
        genShouldHasAndDelLabels(indexTemplate, newLabels, expireLabels);


        // 对这个索引模板应该有的标签进行处理
        if (CollectionUtils.isNotEmpty(newLabels)) {
            handleNewLabels(indexTemplate, shouldAdds, existLabelMap, newLabels);
        }

        // 对这个索引模板应该删除的标签进行处理
        if (CollectionUtils.isNotEmpty(expireLabels)) {
            handleExpireLabels(indexTemplate, shouldDels, existLabelMap, expireLabels);
        }

    }

    private void handleExpireLabels(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, List<TemplateLabelPO> shouldDels, Map<String, TemplateLabelPO> existLabelMap, List<TemplateLabelPO> expireLabels) {
        for (TemplateLabelPO labelPO : expireLabels) {
            if (existLabelMap.containsKey(labelPO.getLabelId())) {
                shouldDels.add(existLabelMap.get(labelPO.getLabelId()));
                LOGGER.debug("class=BaseTemplateMarkLabelJob||method=markOneTemplateLabel||msg=template {} label {} should deleted",
                        indexTemplate.getName(), labelPO.getLabelId());
            } else {
                LOGGER.debug("class=BaseTemplateMarkLabelJob||method=markOneTemplateLabel||msg=template {} label {} not exist",
                        indexTemplate.getName(), labelPO.getLabelId());
            }
        }
    }

    private void handleNewLabels(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate, List<TemplateLabelPO> shouldAdds, Map<String, TemplateLabelPO> existLabelMap, List<TemplateLabelPO> newLabels) {
        for (TemplateLabelPO labelPO : newLabels) {
            if (existLabelMap.containsKey(labelPO.getId())) {
                LOGGER.debug("class=BaseTemplateMarkLabelJob||method=markOneTemplateLabel||msg=template {} label {} has exist",
                        indexTemplate.getName(), labelPO.getLabelId());
            } else {
                shouldAdds.add(labelPO);
                LOGGER.debug("class=BaseTemplateMarkLabelJob||method=markOneTemplateLabel||msg=template {} label {} new label",
                        indexTemplate.getName(), labelPO.getLabelId());
            }
        }
    }

    private Map<String, TemplateLabelPO> getExistLabelList(IndexTemplateLogicWithClusterAndMasterTemplate indexTemplate) {
        List<TemplateLabelPO> existLabelList = templateLabelService.listIndexTemplateLabelPO(indexTemplate.getId());
        Map<String, TemplateLabelPO> existLabelMap = Maps.newHashMap();
        for (TemplateLabelPO labelPO : existLabelList) {
            existLabelMap.put(labelPO.getLabelId(), labelPO);
        }
        return existLabelMap;
    }

    /**
     * 获取标签名称集合
     *
     * @param labelPOs
     * @return
     */
    private String getJoinLabelName(List<TemplateLabelPO> labelPOs) {
        List<String> labelNameList = Lists.newArrayList();
        labelNameList.addAll(labelPOs.stream().map( TemplateLabelPO::getLabelName).collect(Collectors.toList()));
        return String.join(",", labelNameList);
    }

}
