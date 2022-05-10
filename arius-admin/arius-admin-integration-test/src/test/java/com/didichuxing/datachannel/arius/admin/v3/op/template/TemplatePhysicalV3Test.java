package com.didichuxing.datachannel.arius.admin.v3.op.template;

import com.didichuxing.datachannel.arius.admin.base.BaseTemplateInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.TemplatePhysicalUpgradeDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.template.TemplatePhysicalV3ControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cjm
 */
public class TemplatePhysicalV3Test extends BaseTemplateInfoTest {

    @Test
    public void listTest() throws IOException {
        Result<List<IndexTemplatePhysicalVO>> result = TemplatePhysicalV3ControllerMethod.list(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void listTemplatePhyNamesTest() throws IOException {
        Result<List<String>> result = TemplatePhysicalV3ControllerMethod.listTemplatePhyNames();
        Assertions.assertTrue(result.success());
    }

    @Test
    public void getAppNodeNamesTest() throws IOException {
        Result<List<IndexTemplatePhysicalVO>> result = TemplatePhysicalV3ControllerMethod.list(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
        Assertions.assertFalse(result.getData().isEmpty());
        Result<List<String>> result2 = TemplatePhysicalV3ControllerMethod.getAppNodeNames(result.getData().get(0).getId());
        Assertions.assertTrue(result2.success());
    }

    @Test
    public void multipleEditTest() throws IOException {
        Result<List<IndexTemplatePhysicalVO>> result = TemplatePhysicalV3ControllerMethod.list(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
        Assertions.assertFalse(result.getData().isEmpty());
        IndexTemplatePhysicalVO indexTemplatePhysicalVO = result.getData().get(0);
        IndexTemplatePhyDTO dto = new IndexTemplatePhyDTO();
        dto.setId(indexTemplatePhysicalVO.getId());
        dto.setName("newNameTest");
        List<IndexTemplatePhyDTO> list = new ArrayList<>();
        list.add(dto);
        Result<Boolean> result2 = TemplatePhysicalV3ControllerMethod.multipleEdit(list);
        Assertions.assertTrue(result2.success());
        Result<List<IndexTemplatePhysicalVO>> result3 = TemplatePhysicalV3ControllerMethod.list(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result3.success());
        Assertions.assertFalse(result3.getData().isEmpty());
        for(IndexTemplatePhysicalVO vo : result3.getData()) {
            if(vo.getId().equals(dto.getId())) {
                Assertions.assertEquals(dto.getName(), vo.getName());
                break;
            }
        }
    }

    @Test
    public void multipleUpgradeTest() throws IOException {
        Result<List<IndexTemplatePhysicalVO>> result = TemplatePhysicalV3ControllerMethod.list(templateInfo.getLogicTemplateId());
        Assertions.assertTrue(result.success());
        Assertions.assertFalse(result.getData().isEmpty());
        IndexTemplatePhysicalVO indexTemplatePhysicalVO = result.getData().get(0);
        List<TemplatePhysicalUpgradeDTO> list = new ArrayList<>();
        TemplatePhysicalUpgradeDTO dto = new TemplatePhysicalUpgradeDTO();
        dto.setShard(indexTemplatePhysicalVO.getShard() + 1);
        dto.setPhysicalId(indexTemplatePhysicalVO.getId());
        dto.setVersion(dto.getVersion() == null ? 1 : dto.getVersion() + 1);
        list.add(dto);
        Result<Boolean> result2 = TemplatePhysicalV3ControllerMethod.multipleUpgrade(list);
        // 非分区模版
        Assertions.assertTrue(result2.failed());
    }
}
