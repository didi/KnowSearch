package com.didichuxing.datachannel.arius.admin.biz.template.new_srv;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplateAdjustShardDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class AdjustShardManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateLogicManager templateLogicManager;

    @Test
    public void adjustShardTest() {
        TemplateAdjustShardDTO templateAdjustShardDTO = new TemplateAdjustShardDTO();
        templateAdjustShardDTO.setTemplateId(37519);
        templateAdjustShardDTO.setShardNum(2);
        Result<Void> result = templateLogicManager.adjustShard(templateAdjustShardDTO.getTemplateId(), templateAdjustShardDTO.getShardNum());
        Assertions.assertTrue(result.success());
    }

}
