package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.pipeline.PipelineManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
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

    @Test
    public void createPipelineTest() {
        Result<Void> result = pipelineManager.createPipeline(46797);
        Assertions.assertTrue(result.success());
    }

}
