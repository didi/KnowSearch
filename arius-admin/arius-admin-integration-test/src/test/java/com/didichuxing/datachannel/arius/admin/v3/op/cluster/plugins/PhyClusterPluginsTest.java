package com.didichuxing.datachannel.arius.admin.v3.op.cluster.plugins;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.PluginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.PluginVO;
import com.didichuxing.datachannel.arius.admin.common.constant.PluginTypeEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.plugins.PhyClusterPluginsControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class PhyClusterPluginsTest extends BasePhyClusterInfoTest {

    private static Long pluginId;

    /**
     * 在当前类的所有测试方法之前执行
     * 由于本类中的方法都依赖物理集群，所以需要接入一个物理集群
     */
    @BeforeAll
    public static void preHandle() throws IOException {
        BasePhyClusterInfoTest.preHandle();
        // 上传插件
        File file = new File("/Users/didi/Desktop/test_upload.tar.gz");
        pluginId = upload(file);
    }

    @Test
    public void addTest() throws IOException {
        Assertions.assertNotNull(pluginId);
    }

    @Test
    public void pluginListTest() throws IOException {
        Result<List<PluginVO>> result = PhyClusterPluginsControllerMethod.pluginList(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
        Set<Long> idSet = result.getData().stream().map(PluginVO::getId).collect(Collectors.toSet());
        Assertions.assertTrue(idSet.contains(pluginId));
    }

    @Test
    public void deleteEsClusterConfigTest() throws IOException {
        File file = new File("/Users/didi/Desktop/test_upload.tar.gz");
        Long pluginId = upload(file);
        // 再删除
        Result<Long> result = PhyClusterPluginsControllerMethod.deleteEsClusterConfig(pluginId);
        Assertions.assertTrue(result.success());
        Result<List<PluginVO>> result2 = PhyClusterPluginsControllerMethod.pluginList(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
        Set<Long> idSet = result2.getData().stream().map(PluginVO::getId).collect(Collectors.toSet());
        Assertions.assertFalse(idSet.contains(pluginId));
    }

    @Test
    public void editTest() throws IOException {
        PluginDTO dto = new PluginDTO();
        dto.setId(pluginId);
        dto.setDesc("newDescTest");
        Result<Long> result = PhyClusterPluginsControllerMethod.edit(dto);
        Assertions.assertTrue(result.success());
        Result<List<PluginVO>> result2 = PhyClusterPluginsControllerMethod.pluginList(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
        for(PluginVO pluginVO : result2.getData()) {
            if(pluginVO.getId().equals(dto.getId())) {
                Assertions.assertEquals(pluginVO.getDesc(), dto.getDesc());
            }
        }
    }

    private static Long upload(File file) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("name", file.getName() + CustomDataSource.getRandomString(5));
        map.put("physicClusterId", phyClusterInfo.getPhyClusterId());
        map.put("pDefault", PluginTypeEnum.ADMIN_PLUGIN.getCode());
        map.put("md5", CustomDataSource.getRandomString(10));
        map.put("desc", "testtesttest");
        map.put("creator", CustomDataSource.operator);
        Result<Long> result = PhyClusterPluginsControllerMethod.add(map, "uploadFile", file);
        Assertions.assertTrue(result.success());
        return result.getData();
    }
}
