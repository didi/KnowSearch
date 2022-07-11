package com.didichuxing.datachannel.arius.admin.biz.indices;

import com.didichuxing.datachannel.arius.admin.core.service.es.ESIndexService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @Authoer: zyl
 * @Date: 2022/07/11
 * @Version: 1.0
 */


public class IndicesManagerTest {

    @Mock
    private IndexTemplatePhyService indexTemplatePhyService;

    @Mock
    private ESIndexService esIndexService;

    @Mock
    private IndexTemplateService indexTemplateService;

    @InjectMocks
    private IndicesManagerImpl indicesManager;

    @Test
    public void getTemplateIndicesDiskSumTest(){
        initMocks(this);
        when(indexTemplateService.getLogicTemplateWithPhysicalsById(1)).thenReturn(CustomDataSource.getTemplateWithPhyTemplates());
        when(indexTemplatePhyService.getTemplateWithLogicById(1L)).thenReturn(CustomDataSource.getIndexTemplatePhyWithLogic());
        when(esIndexService.syncCatIndexByExpression("test", "test*")).thenReturn(CustomDataSource.getCatIndexResult());

        Long templateIndicesDiskSum = indicesManager.getTemplateIndicesDiskSum(1);
        Assertions.assertEquals(templateIndicesDiskSum, 3L);
    }
}
