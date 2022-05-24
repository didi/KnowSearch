package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.clear.ClearManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateClearDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class ClearManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private ClearManager clearManager;

    @Test
    public void clearIndicesTest() {
        TemplateClearDTO clearDTO = new TemplateClearDTO();
        clearDTO.setTemplateId(37519);
        clearDTO.setDelIndices(Arrays.asList("lcx_template_0411_2022-05-25_v1"));
        Result<Void> result = clearManager.clearIndices(clearDTO);
        Assertions.assertTrue(result.success());
    }
}
