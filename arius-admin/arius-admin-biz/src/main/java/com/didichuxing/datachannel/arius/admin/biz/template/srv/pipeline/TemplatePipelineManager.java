package com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;

/**
 * @author d06679
 * @date 2019-09-03
 */
public interface TemplatePipelineManager {

    /**
     * 修复模板的pipeline
     * @param logicId
     * @return
     */
    Result<Void> repairPipeline(Integer logicId) throws ESOperateException;

    /**
     * 同步pipeline
     * @param indexTemplatePhysical  物理模板
     * @param logicWithPhysical 逻辑模板
     */
    void syncPipeline(IndexTemplatePhy indexTemplatePhysical, IndexTemplateLogicWithPhyTemplates logicWithPhysical);

    /**
     * 创建
     * @param indexTemplatePhysical 物理模板
     * @param logicWithPhysical 逻辑模板
     * @return true/false
     */
    boolean createPipeline(IndexTemplatePhy indexTemplatePhysical,
                           IndexTemplateLogicWithPhyTemplates logicWithPhysical) throws ESOperateException;

    /**
     * 删除
     * @param indexTemplatePhysical 物理模板
     * @return true/false
     */
    boolean deletePipeline(IndexTemplatePhy indexTemplatePhysical) throws ESOperateException;

    /**
     * 修改逻辑字段
     * @param oldTemplate 逻辑模板
     * @param newTemplate 逻辑模板
     * @return true/false
     */
    boolean editFromTemplateLogic(IndexTemplateLogic oldTemplate, IndexTemplateLogic newTemplate);

    /**
     * 修改物理字段
     * @param oldTemplate 物理模板
     * @return true/false
     */
    boolean editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate,
                                     IndexTemplateLogicWithPhyTemplates logicWithPhysical) throws ESOperateException;

    /**
     * 调整限流值
     *
     * @param indexTemplatePhysical 名字
     * @param percent 百分比 [-99, 1000]
     * @return true/false
     */
    boolean editRateLimitByPercent(IndexTemplatePhy indexTemplatePhysical, Integer percent) throws ESOperateException;

    Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMaster);
}
