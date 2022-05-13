package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.pipeline;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author chengxiang, d06679
 * @date 2022/5/13
 */
public interface PipelineManager {

    /**
     * 同步pipeline
     * @param indexTemplatePhy 物理模板
     */
    void syncPipeline(IndexTemplatePhy indexTemplatePhy);

    /**
     * 创建
     * @param indexTemplatePhy 物理模板
     * @return true/false
     */
    Boolean createPipeline(IndexTemplatePhy indexTemplatePhy);

    /**
     * 删除
     * @param indexTemplatePhysicalInfo 物理模板
     * @return true/false
     */
    Boolean deletePipeline(IndexTemplatePhy indexTemplatePhysicalInfo);

    /**
     * 修改逻辑字段
     * @param oldTemplate 逻辑模板
     * @param newTemplate 逻辑模板
     * @return true/false
     */
    Boolean editFromTemplateLogic(IndexTemplate oldTemplate, IndexTemplate newTemplate);

    /**
     * 修改物理字段
     * @param oldTemplate 物理模板
     * @return true/false
     */
    Boolean editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate,
                                     IndexTemplateWithPhyTemplates logicWithPhysical);

    /**
     * 调整限流值
     *
     * @param indexTemplatePhysicalInfo 名字
     * @param percent 百分比 [-99, 1000]
     * @return true/false
     */
    Boolean editRateLimitByPercent(IndexTemplatePhy indexTemplatePhysicalInfo, Integer percent);

    Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMasterInfo);

}
