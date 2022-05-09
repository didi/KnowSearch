package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.PluginPO;
import com.didichuxing.datachannel.arius.admin.common.constant.PluginTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.storage.FileStorageService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class PluginServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESPluginService esPluginService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESPluginDAO esPluginDAO;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    public void listESPluginTest() {
        //mock数据
        PluginPO pluginPO = addESPluginWithoutCheck();
        Long pluginId = pluginPO.getId();
        Assertions.assertTrue(esPluginService.listESPlugin().stream().anyMatch(esPluginPO1 ->
                esPluginPO1.getId().equals(pluginId) && esPluginPO1.getPDefault().equals(pluginPO.getPDefault())));
    }

    @Test
    /**
     * 这里由于涉及到在文件系统中上传文件的操作，所以这里这里直接进行数据表的mock
     */
    public void addESPluginTest() {
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("插件为空", esPluginService.addESPlugin(null).getMessage());
        Assertions.assertEquals("物理集群id为空", esPluginService.addESPlugin(pluginDTO).getMessage());
        pluginDTO.setPhysicClusterId("0");
        Assertions.assertEquals("物理集群id不存在", esPluginService.addESPlugin(pluginDTO).getMessage());
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(CustomDataSource.PHY_CLUSTER_NAME);
        pluginDTO.setPhysicClusterId(clusterPhy.getId().toString());
        // mock文件管理系统
        Mockito.when(fileStorageService.upload(Mockito.anyString(),
                Mockito.anyString(), Mockito.any())).thenReturn(Result.buildSucc("test", ""));
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());
        // ES能力插件
        pluginDTO.setPDefault(PluginTypeEnum.ES_PLUGIN.getCode());
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).failed());
        // 平台能力插件
        pluginDTO.setPDefault(PluginTypeEnum.ADMIN_PLUGIN.getCode());
        Assertions.assertTrue(esPluginService.addESPlugin(pluginDTO).success());
    }

    @Test
    public void updateESPluginDescTest() {
        //mock数据
        PluginPO pluginPO = addESPluginWithoutCheck();
        Long pluginId = pluginPO.getId();
        String desc = "update now";
        //对于数据的描述信息进行修改
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("当前插件不存在", esPluginService.updateESPluginDesc(pluginDTO, CustomDataSource.OPERATOR).getMessage());
        pluginDTO.setDesc(desc);
        pluginDTO.setId(pluginId);
        Assertions.assertTrue(esPluginService.updateESPluginDesc(pluginDTO, CustomDataSource.OPERATOR).success());
        //确认插件的描述信息被修改
        Assertions.assertEquals(desc, esPluginDAO.getById(pluginId).getDesc());
    }

    @Test
    public void deletePluginByIdTest() {
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("非运维人员不能删除插件", esPluginService.deletePluginById(null, CustomDataSource.OPERATOR).getMessage());
        Assertions.assertEquals("插件id为空", esPluginService.deletePluginById(null, "admin").getMessage());
    }

    @Test
    public void getAllSysDefaultPluginsTest() {
        //目前不存储系统默认插件的信息
        Assertions.assertTrue(esPluginService.getAllSysDefaultPluginIds().isEmpty());
        //mock数据
        PluginPO pluginPO = addESPluginWithoutCheck();
        Long pluginId = pluginPO.getId();
        Assertions.assertTrue(ListUtils.string2StrList(esPluginService.getAllSysDefaultPluginIds()).stream()
                .anyMatch(s -> s.equals(pluginId.toString())));
    }

    @Test
    public void getPluginsByClusterNameTest() {
        Assertions.assertTrue(esPluginService.getPluginsByClusterName(null).isEmpty());
        //mock数据
        PluginPO pluginPO = addESPluginWithoutCheck();
        ClusterPhy clusterById = clusterPhyService.getClusterById(Integer.valueOf(pluginPO.getPhysicClusterId()));
        Assertions.assertTrue(clusterById != null);
        Long pluginId = pluginPO.getId();
        Assertions.assertTrue(esPluginService.getPluginsByClusterName(clusterById.getCluster()).stream()
                .anyMatch(esPlugin -> esPlugin.getId().equals(pluginId)));
    }

    /**
     * 这里直接进行数据的mock，屏蔽掉文件系统的具体实现
     */
    private PluginPO addESPluginWithoutCheck() {
        PluginDTO pluginDTO = CustomDataSource.esPluginDTOFactory();
        pluginDTO.setPhysicClusterId("157");
        pluginDTO.setFileName("wpk-test");
        pluginDTO.setMd5("123456789");
        pluginDTO.setUrl("test");
        pluginDTO.setPDefault(1);
        PluginPO pluginPO = ConvertUtil.obj2Obj(pluginDTO, PluginPO.class);
        esPluginDAO.insert(pluginPO);
        return pluginPO;
    }
}
