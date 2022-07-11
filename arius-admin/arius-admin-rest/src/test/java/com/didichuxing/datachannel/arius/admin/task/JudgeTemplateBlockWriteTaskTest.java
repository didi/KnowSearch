package com.didichuxing.datachannel.arius.admin.task;

import com.didichuxing.datachannel.arius.admin.biz.indices.IndicesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @Authoer: zyl
 * @Date: 2022/07/11
 * @Version: 1.0
 */


public class JudgeTemplateBlockWriteTaskTest {

    @Mock
    private IndicesManager indicesManager;

    @Mock
    private IndexTemplateService indexTemplateService;

    @Test
    public void executeTest(){
        initMocks(this);
        List<Integer> templateIds = new ArrayList<>();
        templateIds.add(1);
        when(indexTemplateService.listAllTemplateIds()).thenReturn(templateIds);

        Result<Void> result = new Result<>();
        result.setCode(1);
        for (Integer templateId : templateIds) {
            when(indexTemplateService.getLogicTemplateById(templateId)).thenReturn(CustomDataSource.getIndexTemplate());
            IndexTemplate template = indexTemplateService.getLogicTemplateById(templateId);
            Long templateDiskSize = (long)(template.getDiskSize() * 10);

            when(indicesManager.getTemplateIndicesDiskSum(templateId)).thenReturn(400L);
            Long templateIndicesDiskSum = indicesManager.getTemplateIndicesDiskSum(templateId);

            if (templateIndicesDiskSum >= templateDiskSize){
                when(indexTemplateService.updateBlockWriteState(templateId, true, "admin")).thenReturn(CustomDataSource.getResult());
                result = indexTemplateService.updateBlockWriteState(templateId, true, "admin");
            }
        }

        Assertions.assertEquals(result.getCode(), 0);
    }

}
