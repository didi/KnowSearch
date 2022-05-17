package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear.ClearManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
public class ClearManagerImpl extends BaseTemplateSrvImpl implements ClearManager {

    private final Integer RETRY_TIMES = 3;

    @Autowired
    private ESIndexService esIndexService;

    @Override
    public TemplateServiceEnum templateSrv() {
        return TemplateServiceEnum.TEMPLATE_CLEAR;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    public Result<Void> clearIndices(TemplateClearDTO clearDTO) {
        Result<Void> srvAvailableResult = isTemplateSrvAvailable(clearDTO.getTemplateId());
        if (srvAvailableResult.failed()) {
            return srvAvailableResult;
        }

        if (CollectionUtils.isEmpty(clearDTO.getDelIndices())) {
            return Result.buildParamIllegal("清理索引不能为空");
        }

        Result<Void> checkResult = checkIndicesDeletable(clearDTO.getDelIndices(), clearDTO.getTemplateId());
        if (checkResult.failed()) {
            return checkResult;
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(clearDTO.getTemplateId());
        Result<Void> deleteIndicesResult = batchDeletePhysicalTemplateIndices(templateLogicWithPhysical.getPhysicals(), clearDTO.getDelIndices());
        if (deleteIndicesResult.failed()) {
            return deleteIndicesResult;
        }

        return Result.buildSucc();
    }



    ///////////////////////private method//////////////////////////////////////

    private Result<Void> checkIndicesDeletable(List<String> delIndices, Integer logicTemplateId) {
        for (String index : delIndices) {
            if (index.endsWith("*")) {
                return Result.buildParamIllegal("索引名字不能以*结尾");
            }
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(logicTemplateId);
        IndexTemplatePhy templatePhy = templateLogicWithPhysical.getAnyOne();
        List<String> matchIndices = indexTemplatePhyService.getMatchNoVersionIndexNames(templatePhy.getId());
        for (String index : delIndices) {
            if (!matchIndices.contains(index)) {
                return Result.buildParamIllegal(index + "不属于该索引模板");
            }
        }
        return Result.buildSucc();
    }


    /**
     * 批量删除物理模板对应分区索引
     * @param physicals 物理模板列表
     * @param delIndices 待删除分区索引列表
     * @return
     */
    private Result<Void> batchDeletePhysicalTemplateIndices(List<IndexTemplatePhy> physicals, List<String> delIndices) {
        for (IndexTemplatePhy templatePhysical : physicals) {
            if (templatePhysical.getVersion() > 0) {
                List<String> delIndicesWithVersion = genDeleteIndicesWithVersion(delIndices);
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(delIndicesWithVersion)) {
                    esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndicesWithVersion, RETRY_TIMES);
                }
            }

            if (delIndices.size() != esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndices, RETRY_TIMES)) {
                return Result.buildFail("删除索引失败，请重试");
            }
        }

        return Result.buildSucc();
    }


    /**
     * 生成带有版本模式的待删除索引列表
     * @param delIndices 待删除索引列表
     * @return
     */
    private List<String> genDeleteIndicesWithVersion(List<String> delIndices) {
        List<String> indicesWithVersion = new ArrayList<>();
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(delIndices)) {
            for (String delIndex : delIndices) {
                indicesWithVersion.add(delIndex + "_v*");
            }
        }

        return indicesWithVersion;
    }

}
