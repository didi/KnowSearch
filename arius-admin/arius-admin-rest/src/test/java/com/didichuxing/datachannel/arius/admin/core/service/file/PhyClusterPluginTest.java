package com.didichuxing.datachannel.arius.admin.core.service.file;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PhyClusterPluginTest extends AriusAdminApplicationTests {
    @Autowired
    private ESPluginService esPluginService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESPluginDAO esPluginDAO;

    @Test
    public void uploadPluginTest() throws IOException {
        Result<Long> updateResult = esPluginService.addESPlugin(getESPluginDTO());
        Long id = updateResult.getData();
        Assertions.assertTrue(updateResult.success());
        Result deleteResult = esPluginService.deletePluginById(id,"admin");
        Assertions.assertTrue(deleteResult.success());
    }

    @Test
    public void pluginAddOrderTest() {
        // 获取插件的信息列表
        List<ESPluginPO> byNameAndVersionAndPhysicClusterId = esPluginDAO.getByNameAndVersionAndPhysicClusterId("analysis-ik", "6.6.1", "303");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(byNameAndVersionAndPhysicClusterId));
        // 插件的安装
        ESPluginPO esPluginPO = byNameAndVersionAndPhysicClusterId.get(0);
        Assertions.assertNotNull(esPluginPO);
    }

    @Test
    public void pluginTaskTest() {
        Assertions.assertNotNull(esPluginService);
    }

    private ESPluginDTO getESPluginDTO() throws IOException {
        ESPluginDTO esPluginDTO = new ESPluginDTO();
        String clusterName = "dc-cluster";
        esPluginDTO.setPhysicClusterId(clusterPhyService.getClusterByName(clusterName).getId().toString());
        String filePath = "/Users/didi/wpkShell/analysis-ik.tar.gz";
        String fileName = "analysis-ik.tar.gz";
        File file = new File(filePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileInputStream);
        esPluginDTO.setUploadFile(mockMultipartFile);
        esPluginDTO.setFileName(fileName);
        esPluginDTO.setCreator("wpk");
        esPluginDTO.setDesc("插件安装测试");
        esPluginDTO.setMd5("test-test-test");
        esPluginDTO.setPDefault(true);
        return esPluginDTO;
    }
}
