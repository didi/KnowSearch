package com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.base.impl.BaseTemplateSrvImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear.ClearManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.BaseTemplateSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.template.NewTemplateSrvEnum;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengxiang
 * @date 2022/5/16
 */
@Service
public class ClearManagerImpl extends BaseTemplateSrvImpl implements ClearManager {

    private final Integer RETRY_TIMES = 3;

    @Autowired
    private ESIndexService esIndexService;

    @Override
    public NewTemplateSrvEnum templateSrv() {
        return NewTemplateSrvEnum.TEMPLATE_CLEAR;
    }

    @Override
    public Result<Void> isTemplateSrvAvailable(Integer logicTemplateId) {
        return Result.buildSucc();
    }

    @Override
    protected Result<Void> openSrvImpl(List<Integer> templateIdList, BaseTemplateSrvOpenDTO openParam) {
        if (!(openParam instanceof TemplateClearDTO)) {
            return Result.buildParamIllegal("参数类型错误");
        }
        TemplateClearDTO templateClearDTO = (TemplateClearDTO) openParam;
        return clearIndices(templateClearDTO);
    }

    @Override
    protected Result<Void> closeSrvImpl(List<Integer> templateIdList) {
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

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(clearDTO.getTemplateId());
        Result<Void> deleteIndicesResult = batchDeletePhysicalTemplateIndices(templateLogicWithPhysical.getPhysicals(), clearDTO.getDelIndices());
        if (deleteIndicesResult.failed()) {
            return deleteIndicesResult;
        }

        return Result.buildSucc();
    }



    ///////////////////////private method//////////////////////////////////////

    /**
     * 批量删除物理模板对应分区索引
     * @param physicals 物理模板列表
     * @param delIndices 待删除分区索引列表
     * @return
     */
    private Result<Void> batchDeletePhysicalTemplateIndices(List<IndexTemplatePhy> physicals, List<String> delIndices) {
        for (IndexTemplatePhy templatePhysical : physicals) {
            if (CollectionUtils.isNotEmpty(delIndices)) {
                esIndexService.syncBatchDeleteIndices(templatePhysical.getCluster(), delIndices, RETRY_TIMES);
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
