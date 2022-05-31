package com.didichuxing.datachannel.arius.admin.biz.template.manage.create;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.util.RandomGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TemplateCreateManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateCreateManager templateCreateManager;

    @Test
    public void createTest() {
        IndexTemplateWithCreateInfoDTO createInfoDTO = new IndexTemplateWithCreateInfoDTO();
        createInfoDTO.setName(RandomGenerator.randomString(10));
        createInfoDTO.setDataCenter("cn");
        createInfoDTO.setDataType(1);
        createInfoDTO.setResponsible("admin");
        createInfoDTO.setExpireTime(3);
        createInfoDTO.setCyclicalRoll(Boolean.TRUE);
        createInfoDTO.setDiskSize(30.0);
        createInfoDTO.setMapping("{\"date\":{\"type\":\"date\",\"doc_values\":true,\"index\":true,\"format\":\"yyyy-MM-dd HH:mm:ss.SSS\"}}");
        createInfoDTO.setResourceId(985L);
        createInfoDTO.setDateField("date");
        createInfoDTO.setDateFieldFormat("yyyy-MM-dd HH:mm:ss.SSS");
        createInfoDTO.setLevel(1);

        Result<Void> createResult = templateCreateManager.create(createInfoDTO, "admin", 1);
        Assertions.assertTrue(createResult.success());
    }
}
