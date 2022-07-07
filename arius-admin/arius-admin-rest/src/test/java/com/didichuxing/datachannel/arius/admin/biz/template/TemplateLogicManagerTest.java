package com.didichuxing.datachannel.arius.admin.biz.template;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateWithCreateInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.util.RandomGenerator;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class TemplateLogicManagerTest extends AriusAdminApplicationTest {

    @Autowired
    private TemplateLogicManager templateLogicManager;

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

        Result<Void> createResult = null;
        try {
            createResult = templateLogicManager.create(createInfoDTO, "admin", 1);
        } catch (AdminOperateException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(createResult.success());
    }

    @Test
    public void adjustShardTest() {
         Integer projectId=1;
        Result<Void> result = null;
        try {
            result = templateLogicManager.adjustShard(37519, 2, 1, "admin");
        } catch (AdminOperateException e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result.success());
    }

    @Test
    public void clearIndicesTest() {
        Result<Void> result = templateLogicManager.clearIndices(37519, Arrays.asList("lcx_template_0411_2022-05-25_v1"), 1,
                "");
        Assertions.assertTrue(result.success());
    }

    @Test
    public void upgradeTemplateTest() {
        Integer templateId = 37519;
        Result<Void> result = null;
        try {
            result = templateLogicManager.upgrade(templateId, "admin", 1);
        } catch (AdminOperateException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assertions.assertTrue(result.success());
    }
}