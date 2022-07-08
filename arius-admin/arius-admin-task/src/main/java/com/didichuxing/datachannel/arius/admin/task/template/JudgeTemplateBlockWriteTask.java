package com.didichuxing.datachannel.arius.admin.task.template;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import com.didiglobal.logi.job.annotation.Task;
import com.didiglobal.logi.job.common.TaskResult;
import com.didiglobal.logi.job.core.job.Job;
import com.didiglobal.logi.job.core.job.JobContext;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 定时判断是否对模版禁写
 *    判断依据：模版下所有分区总存储大小 > 模版配额磁盘大小
 *    每五分钟执行一次
 *
 * @Authoer: zyl
 * @Date: 2022/07/07
 * @Version: 1.0
 */

@Task(name = "JudgeTemplateBlockWriteTask", description = "依据模版磁盘是否有余量来判断是否禁写模版",
        cron = "0 */5 * * * ?", autoRegister = true)
public class JudgeTemplateBlockWriteTask implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(JudgeTemplateBlockWriteTask.class);

    public static final String ADMIN = "admin";

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndicesManager indicesManager;

    @Override
    public TaskResult execute(JobContext jobContext) throws Exception {
        LOGGER.info("class=JudgeTemplateBlockWriteTask||method=execute||msg=start");

        // 获取所有模版id
        List<Integer> templateIds = indexTemplateService.listAllTemplateIds();

        // 对每个模版进行判断
        for (Integer templateId : templateIds) {
            // 获取该模版配额磁盘大小，转为mb
            IndexTemplate template = indexTemplateService.getLogicTemplateById(templateId);
            Double templateDiskSize = template.getDiskSize() * 1024;

            // 获取该模版所有索引/分区占用磁盘总和
            Double templateIndicesDiskSum = getTemplateIndicesDiskSum(templateId);

            // 判断是否禁写
            if (templateIndicesDiskSum >= templateDiskSize){
                indexTemplateService.updateBlockWriteState(templateId, true, ADMIN);
            }
        }

        return TaskResult.SUCCESS;
    }


    /**
     * 获取该模版所有索引/分区占用磁盘总和
     *
     * @param templateId 模版id
     * @return 该模版所有索引/分区占用磁盘总和
     */
    private Double getTemplateIndicesDiskSum(Integer templateId){
        // 根据逻辑模版id获取对应的物理模版详情   （一个逻辑模版可能涉及多个物理模版）
        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService
                .getLogicTemplateWithPhysicalsById(templateId);

        // 当前逻辑模版分区（索引）列表
        List<CatIndexResult> catIndexResults = Lists.newArrayList();

        // 获取逻辑索引模板所在master集群的模板列表
        List<IndexTemplatePhy> physicalMasters = templateLogicWithPhysical.fetchMasterPhysicalTemplates();
        for (IndexTemplatePhy physicalMaster : physicalMasters) {
            try {
                catIndexResults.addAll(indicesManager.listIndexCatInfoByTemplatePhyId(physicalMaster.getId()));
            } catch (Exception e) {
                LOGGER.warn("class=JudgeTemplateBlockWriteTask||method=getTemplateIndicesDiskSum||logicId={}||errMsg={}", templateId, e.getMessage(), e);
            }
        }

        Double templateIndicesDiskSum = 0.0;
        // 统计逻辑模版所有索引的占用磁盘大小
        for (CatIndexResult catIndexResult : catIndexResults) {
            // storeSize属性为string类型，因为单位不一样，有b、kb、mb、gb，统一转换为mb
            String storeSize = catIndexResult.getStoreSize();
            StringBuilder sb = new StringBuilder();
            int idx = storeSize.length() - 1;
            while (idx > 0){
                if (Character.isDigit(storeSize.charAt(idx))){
                    break;
                }
                sb.append(storeSize.charAt(idx));
                idx--;
            }
            Double num = Double.valueOf(storeSize.substring(0, idx + 1));
            String unit = sb.reverse().toString();
            Double size = 0.0;
            if ("mb".equals(unit)){
                size = num;
            } else if ("gb".equals(unit)){
                size = num * 1024;
            } else if ("kb".equals(unit)){
                size = num / 1024;
            } else if ("b".equals(unit)){
                size = num / 1024 / 1024;
            }

            templateIndicesDiskSum += size;
        }

        return templateIndicesDiskSum;
    }
}
