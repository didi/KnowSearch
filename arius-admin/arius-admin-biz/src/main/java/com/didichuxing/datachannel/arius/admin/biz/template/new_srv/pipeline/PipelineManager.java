package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.pipeline;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;

/**
 * @author chengxiang, d06679
 * @date 2022/5/13
 */
public interface PipelineManager {

    /**
     * 创建
     * @param templatePhyId 物理模板id
     * @return true/false
     */
    Result<Void> createPipeline(Integer templatePhyId);

    /**
     * 同步pipeline
     * @param templatePhyId 物理模板id
     * @return
     */
    Result<Void>syncPipeline(Integer templatePhyId);

    /**
     * 删除
     * @param templatePhyId 物理模板id
     * @return true/false
     */
    Result<Void> deletePipeline(Integer templatePhyId);

    /**
     * 修改逻辑字段
     * @param newTemplate 新逻辑模板
     * @param oldTemplate 旧逻辑模板
     * @return true/false
     */
    Result<Void> editFromTemplateLogic(IndexTemplate oldTemplate, IndexTemplate newTemplate);

    /**
     * 修改物理字段
     * @param oldTemplate 物理模板
     * @return true/false
     */
    Result<Void> editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate, IndexTemplateWithPhyTemplates logicWithPhysical);

    Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMasterInfo);

}
