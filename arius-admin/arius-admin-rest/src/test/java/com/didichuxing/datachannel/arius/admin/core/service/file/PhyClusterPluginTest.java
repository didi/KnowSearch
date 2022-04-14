package com.didichuxing.datachannel.arius.admin.core.service.file;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPluginService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class PhyClusterPluginTest extends AriusAdminApplicationTest {

    @Autowired
    private ESPluginService esPluginService;

    @MockBean
    private ClusterPhyService clusterPhyService;

    @MockBean
    private ESPluginDAO esPluginDAO;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    public void updateESPluginDescTest() {
        // 不存在插件
        Mockito.when(esPluginDAO.getById(-1L)).thenReturn(CustomDataSource.getESPluginPO());
        Assertions.assertTrue(esPluginService.updateESPluginDesc(CustomDataSource.getESPluginDTO(), "admin").failed());
        // 存在的插件
        Mockito.when(esPluginDAO.updateDesc(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(esPluginDAO.updateDesc(Mockito.any(), Mockito.any())).thenReturn(1);
        Mockito.when(esPluginDAO.getById(Mockito.anyLong())).thenReturn(CustomDataSource.getESPluginPO());
        Assertions.assertTrue(esPluginService.updateESPluginDesc(CustomDataSource.getESPluginDTO(), "admin").success());
    }

    @Test
    public void addESPluginTest() {
        Mockito.when(clusterPhyService.getClusterById(-1)).thenReturn(null);
        Mockito.when(clusterPhyService.getClusterById(1)).thenReturn(CustomDataSource.esClusterPhyFactory());
        PluginDTO pluginDTO = CustomDataSource.getESPluginDTO();

        Assertions.assertTrue(esPluginService.addESPlugin(null).failed());
        pluginDTO.setUploadFile(null);
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());
        pluginDTO.setPhysicClusterId("-1");
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());
        pluginDTO.setPhysicClusterId(null);
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());

        Mockito.when(fileStorageService.upload(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Result.buildSucc());
        Mockito.when(esPluginDAO.insert(Mockito.any())).thenReturn(1);
        pluginDTO = CustomDataSource.getESPluginDTO();
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());
    }

    @Test
    public void deletePluginByIdTest() {
        Mockito.when(esPluginDAO.delete(Mockito.anyLong())).thenReturn(1);
        Mockito.when(esPluginDAO.getById(-1L)).thenReturn(null);
        Mockito.when(esPluginDAO.getById(1L)).thenReturn(CustomDataSource.getESPluginPO());
        Mockito.when(clusterPhyService.getClusterById(1)).thenReturn(CustomDataSource.esClusterPhyFactory());
        Assertions.assertTrue(esPluginService.deletePluginById(-1L, "admin").failed());
        Mockito.when(fileStorageService.remove(Mockito.anyString())).thenReturn(Result.buildFail());
        Assertions.assertTrue(esPluginService.deletePluginById(1L, "admin").failed());
        Mockito.when(fileStorageService.remove(Mockito.anyString())).thenReturn(Result.buildSucc());
        Mockito.when(esPluginDAO.delete(1L)).thenReturn(1);
        ClusterPhy clusterPhy = CustomDataSource.esClusterPhyFactory();
        clusterPhy.setPlugIds("");
        Mockito.when(clusterPhyService.getClusterById(1)).thenReturn(clusterPhy);
        Assertions.assertTrue(esPluginService.deletePluginById(1L, "admin").success());
    }

    @Test
    public void getPluginsByClusterNameTest() {
        // 不存在的物理集群名
        Mockito.when(clusterPhyService.getClusterByName("test")).thenReturn(null);
        Assertions.assertTrue(esPluginService.getPluginsByClusterName("test").isEmpty());
        // 存在的
        Mockito.when(clusterPhyService.getClusterByName(CustomDataSource.PHY_CLUSTER_NAME)).thenReturn(CustomDataSource.esClusterPhyFactory());
        Mockito.when(esPluginDAO.listByPlugIds(Mockito.anyList())).thenReturn(CustomDataSource.getESPluginPOList());
        Assertions.assertFalse(esPluginService.getPluginsByClusterName(CustomDataSource.PHY_CLUSTER_NAME).isEmpty());
    }

    @Test
    public void getESPluginByIdTest() {
        Mockito.when(esPluginDAO.getById(1L)).thenReturn(CustomDataSource.getESPluginPO());
        Assertions.assertNotNull(esPluginService.getESPluginById(1L));
    }

    @Test
    public void getAllSysDefaultPluginIdsTest() {
        Mockito.when(esPluginDAO.getAllSysDefaultPlugins()).thenReturn(CustomDataSource.getESPluginPOList());
        Assertions.assertFalse(esPluginService.getAllSysDefaultPluginIds().isEmpty());
    }
}
