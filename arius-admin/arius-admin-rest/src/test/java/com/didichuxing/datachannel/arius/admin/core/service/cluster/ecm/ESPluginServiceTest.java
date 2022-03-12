package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESPluginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.po.esplugin.ESPluginPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESPluginDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

@Transactional
@Rollback
public class ESPluginServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private ESPluginService esPluginService;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Autowired
    private ESPluginDAO esPluginDAO;

    private static final String OPERATOR = "wpk";

    @Test
    public void listESPluginTest() {
        //mock数据
        ESPluginPO esPluginPO = addESPluginWithoutCheck();
        Long pluginId = esPluginPO.getId();
        Assertions.assertTrue(esPluginService.listESPlugin().stream().anyMatch(esPluginPO1 ->
                esPluginPO1.getId().equals(pluginId) && esPluginPO1.getPDefault().equals(esPluginPO.getPDefault())));
    }

    @Test
    /**
     * 这里由于涉及到在文件系统中上传文件的操作，所以这里这里直接进行数据表的mock
     */
    public void addESPluginTest() {
        ESPluginDTO esPluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("插件为空", esPluginService.addESPlugin(null).getMessage());
        Assertions.assertEquals("物理集群id为空", esPluginService.addESPlugin(esPluginDTO).getMessage());
        esPluginDTO.setPhysicClusterId("0");
        Assertions.assertEquals("物理集群id不存在", esPluginService.addESPlugin(esPluginDTO).getMessage());
        esPluginDTO.setPhysicClusterId("873");
        Assertions.assertEquals("文件不存在", esPluginService.addESPlugin(esPluginDTO).getMessage());
    }

    @Test
    public void updateESPluginDescTest() {
        //mock数据
        ESPluginPO esPluginPO = addESPluginWithoutCheck();
        Long pluginId = esPluginPO.getId();
        String desc = "update now";
        //对于数据的描述信息进行修改
        ESPluginDTO esPluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("当前插件不存在", esPluginService.updateESPluginDesc(esPluginDTO, OPERATOR).getMessage());
        esPluginDTO.setDesc(desc);
        esPluginDTO.setId(pluginId);
        Assertions.assertTrue(esPluginService.updateESPluginDesc(esPluginDTO, OPERATOR).success());
        //确认插件的描述信息被修改
        Assertions.assertEquals(desc, esPluginDAO.getById(pluginId).getDesc());
    }

    @Test
    public void deletePluginByIdTest() {
        ESPluginDTO esPluginDTO = CustomDataSource.esPluginDTOFactory();
        Assertions.assertEquals("非运维人员不能删除插件", esPluginService.deletePluginById(null, OPERATOR).getMessage());
        Assertions.assertEquals("插件id为空", esPluginService.deletePluginById(null, "admin").getMessage());
    }

    @Test
    public void getAllSysDefaultPluginsTest() {
        //目前不存储系统默认插件的信息
        Assertions.assertTrue(esPluginService.getAllSysDefaultPluginIds().isEmpty());
        //mock数据
        ESPluginPO esPluginPO = addESPluginWithoutCheck();
        Long pluginId = esPluginPO.getId();
        Assertions.assertTrue(ListUtils.string2StrList(esPluginService.getAllSysDefaultPluginIds()).stream()
                .anyMatch(s -> s.equals(pluginId.toString())));
    }

    @Test
    public void getPluginsByClusterNameTest() {
        Assertions.assertTrue(esPluginService.getPluginsByClusterName(null).isEmpty());
        //mock数据
        ESPluginPO esPluginPO = addESPluginWithoutCheck();
        ClusterPhy clusterById = clusterPhyService.getClusterById(Integer.valueOf(esPluginPO.getPhysicClusterId()));
        Long pluginId = esPluginPO.getId();
        Assertions.assertTrue(esPluginService.getPluginsByClusterName(clusterById.getCluster()).stream()
                .anyMatch(esPlugin -> esPlugin.getId().equals(pluginId)));
    }

    /**
     * 这里直接进行数据的mock，屏蔽掉文件系统的具体实现
     */
    private ESPluginPO addESPluginWithoutCheck() {
        ESPluginDTO esPluginDTO = CustomDataSource.esPluginDTOFactory();
        esPluginDTO.setPhysicClusterId("873");
        esPluginDTO.setFileName("wpk-test");
        esPluginDTO.setMd5("123456789");
        esPluginDTO.setUrl("test");
        esPluginDTO.setPDefault(true);
        ESPluginPO esPluginPO = ConvertUtil.obj2Obj(esPluginDTO, ESPluginPO.class);
        esPluginDAO.insert(esPluginPO);
        return esPluginPO;
    }
}
