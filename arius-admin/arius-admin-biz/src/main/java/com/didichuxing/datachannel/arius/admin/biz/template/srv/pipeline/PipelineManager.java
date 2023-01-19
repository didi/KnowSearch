package com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline;

import java.util.Map;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didiglobal.knowframework.elasticsearch.client.request.ingest.Pipeline;

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
     * 修改逻辑字段
     * @param newTemplate 新逻辑模板
     * @param oldTemplate 旧逻辑模板
     * @return true/false
     */
    Result<Void> editFromTemplateLogic(IndexTemplate oldTemplate, IndexTemplate newTemplate);

    /**
     * 创建
     * @param indexTemplatePhysicalInfo 物理模板
     * @param logicWithPhysical 逻辑模板
     * @return true/false
     */
    boolean createPipeline(IndexTemplatePhy indexTemplatePhysicalInfo,
                           IndexTemplateWithPhyTemplates logicWithPhysical) throws ESOperateException;

    /**
     * 修改物理字段
     * @param oldTemplate 物理模板
     * @return true/false
     */
    boolean editFromTemplatePhysical(IndexTemplatePhy oldTemplate, IndexTemplatePhy newTemplate,
                                     IndexTemplateWithPhyTemplates logicWithPhysical) throws ESOperateException;

    Integer getRateLimit(IndexTemplatePhy indexTemplatePhysicalMasterInfo);

    /**
     * 同步pipeline
     * @param logicTemplateId 物理模板id
     * @param pipelineMap 集群全量pipeline信息
     * @return
     */
    Result<Void> syncPipeline(Integer logicTemplateId, Map<String, Pipeline> pipelineMap);

    /**
     * 删除
     * @param templatePhyId 物理模板id
     * @return true/false
     */
    Result<Void> deletePipeline(Integer templatePhyId);

    /**
     * 调整限流值
     *
     * @param indexTemplatePhysicalInfo 名字
     * @param percent 百分比 [-99, 1000]
     * @return true/false
     */
    boolean editRateLimitByPercent(IndexTemplatePhy indexTemplatePhysicalInfo,
                                   Integer percent) throws ESOperateException;

    /**
     * 修复模板的pipeline
     * @param logicId
     * @return
     */
    Result<Void> repairPipeline(Integer logicId) throws ESOperateException;

}