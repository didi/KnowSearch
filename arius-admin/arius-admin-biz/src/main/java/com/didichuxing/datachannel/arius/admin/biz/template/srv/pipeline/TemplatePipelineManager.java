package com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateInfoWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyInfo;
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
     * @param indexTemplatePhysicalInfo  物理模板
     * @param logicWithPhysical 逻辑模板
     */
    void syncPipeline(IndexTemplatePhyInfo indexTemplatePhysicalInfo, IndexTemplateInfoWithPhyTemplates logicWithPhysical);

    /**
     * 创建
     * @param indexTemplatePhysicalInfo 物理模板
     * @param logicWithPhysical 逻辑模板
     * @return true/false
     */
    boolean createPipeline(IndexTemplatePhyInfo indexTemplatePhysicalInfo,
                           IndexTemplateInfoWithPhyTemplates logicWithPhysical) throws ESOperateException;

    /**
     * 删除
     * @param indexTemplatePhysicalInfo 物理模板
     * @return true/false
     */
    boolean deletePipeline(IndexTemplatePhyInfo indexTemplatePhysicalInfo) throws ESOperateException;

    /**
     * 修改逻辑字段
     * @param oldTemplate 逻辑模板
     * @param newTemplate 逻辑模板
     * @return true/false
     */
    boolean editFromTemplateLogic(IndexTemplateInfo oldTemplate, IndexTemplateInfo newTemplate);

    /**
     * 修改物理字段
     * @param oldTemplate 物理模板
     * @return true/false
     */
    boolean editFromTemplatePhysical(IndexTemplatePhyInfo oldTemplate, IndexTemplatePhyInfo newTemplate,
                                     IndexTemplateInfoWithPhyTemplates logicWithPhysical) throws ESOperateException;

    /**
     * 调整限流值
     *
     * @param indexTemplatePhysicalInfo 名字
     * @param percent 百分比 [-99, 1000]
     * @return true/false
     */
    boolean editRateLimitByPercent(IndexTemplatePhyInfo indexTemplatePhysicalInfo, Integer percent) throws ESOperateException;

    Integer getRateLimit(IndexTemplatePhyInfo indexTemplatePhysicalMasterInfo);
}
