package com.didichuxing.datachannel.arius.admin.task;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.util.SizeUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.elasticsearch.client.response.indices.catindices.CatIndexResult;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;

/**
 * @Authoer: zyl
 * @Date: 2022/07/11
 * @Version: 1.0
 */

public class JudgeTemplateBlockWriteTaskTest {

    @Mock
    private IndicesManager       indicesManager;

    @Mock
    private IndexTemplateService indexTemplateService;

    @Test
    public void executeTest() {
        initMocks(this);
        Result<Void> result = new Result<>();
        result.setCode(1);

        when(indexTemplateService.getLogicTemplateWithPhysicalsById(1))
            .thenReturn(CustomDataSource.getIndexTemplateWithPhyTemplates());
        IndexTemplateWithPhyTemplates indexTemplateWithPhyTemplates = indexTemplateService
            .getLogicTemplateWithPhysicalsById(1);
        long limitDiskSize = (long) (indexTemplateWithPhyTemplates.getDiskSize() * 1024 * 1024 * 1024);
        IndexTemplatePhy masterPhyTemplate = indexTemplateWithPhyTemplates.getMasterPhyTemplate();
        when(indicesManager.listIndexCatInfoByTemplatePhyId(masterPhyTemplate.getId()))
            .thenReturn(CustomDataSource.getCatIndexResult());

        List<CatIndexResult> catIndexResults = indicesManager
            .listIndexCatInfoByTemplatePhyId(masterPhyTemplate.getId());
        long templateIndicesDiskSum = 0;
        if (CollectionUtils.isNotEmpty(catIndexResults)) {
            templateIndicesDiskSum = catIndexResults.stream().mapToLong(r -> SizeUtil.getUnitSize(r.getStoreSize()))
                .sum();
        }

        if (templateIndicesDiskSum >= templateIndicesDiskSum) {
            when(indexTemplateService.updateBlockWriteState(1, true)).thenReturn(CustomDataSource.getResult());
            result = indexTemplateService.updateBlockWriteState(1, true);
        }

        Assertions.assertEquals(result.getCode(), 0);
    }
}