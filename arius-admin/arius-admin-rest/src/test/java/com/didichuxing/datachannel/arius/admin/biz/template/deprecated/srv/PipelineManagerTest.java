package com.didichuxing.datachannel.arius.admin.biz.template.deprecated.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chengxiang
 * @date 2022/06/09
 */
public class PipelineManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private PipelineManager pipelineManager;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Test
    public void createPipelineTest() {
        Result<Void> result = pipelineManager.createPipeline(46797);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void syncPipelineTest() {
        Result<Void> result = pipelineManager.syncPipeline(46797);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void deletePipelineTest() {
        Result<Void> result = pipelineManager.deletePipeline(46797);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void editFromTemplateLogicTest() {
        IndexTemplate oldTemplate = indexTemplateService.getLogicTemplateById(37519);
        IndexTemplate newTemplate = indexTemplateService.getLogicTemplateById(37519);
        newTemplate.setWriteRateLimit(400);
        Result<Void> result = pipelineManager.editFromTemplateLogic(oldTemplate, newTemplate);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void editFromTemplatePhysicalTest() {
        IndexTemplatePhy oldTemplatePhy = indexTemplatePhyService.getTemplateById(46797L);
        IndexTemplatePhy newTemplatePhy = indexTemplatePhyService.getTemplateById(46797L);
        newTemplatePhy.setVersion(3);
        Result<Void> result = pipelineManager.editFromTemplatePhysical(oldTemplatePhy, newTemplatePhy);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getRateLimitTest() {
        Integer rateLimit = pipelineManager.getRateLimit(46797);
        Assertions.assertNotNull(rateLimit);
    }

}