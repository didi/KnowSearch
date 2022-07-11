package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.core.service.template.logic.impl.IndexTemplateServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.template.IndexTemplateDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @Authoer: zyl
 * @Date: 2022/07/11
 * @Version: 1.0
 */


public class IndexTemplateServiceTest {

    @Mock
    private IndexTemplateDAO indexTemplateDAO;

    @InjectMocks
    private IndexTemplateServiceImpl indexTemplateService;

    @Test
    public void listAllTemplateIdsTest(){
        initMocks(this);
        when(indexTemplateDAO.listAllIds()).thenReturn(CustomDataSource.getTemplateIds());
        List<Integer> templateIds = indexTemplateService.listAllTemplateIds();
        Assertions.assertFalse(templateIds.isEmpty());
    }
}
