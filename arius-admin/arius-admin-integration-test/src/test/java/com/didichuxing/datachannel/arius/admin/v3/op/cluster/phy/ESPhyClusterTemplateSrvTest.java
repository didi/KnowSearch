package com.didichuxing.datachannel.arius.admin.v3.op.cluster.phy;

import com.didichuxing.datachannel.arius.admin.base.BasePhyClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.phy.ESPhyClusterTemplateSrvControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class ESPhyClusterTemplateSrvTest extends BasePhyClusterInfoTest {

    /**
     * 测试获取集群当前已经开启的索引服务
     */
    @Test
    public void listTest() throws IOException {
        Result<List<ESClusterTemplateSrvVO>> result = ESPhyClusterTemplateSrvControllerMethod.list(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
        Set<Integer> idSet = result.getData().stream().map(ESClusterTemplateSrvVO::getServiceId).collect(Collectors.toSet());
        // 获取创建物理集群时，设置的索引服务 id
        String[] ids = phyClusterInfo.getClusterJoinDTO().getTemplateSrvs().split(",");
        if(ids.length > 0) {
            Assertions.assertTrue(idSet.contains(Integer.parseInt(ids[0])));
        }
    }

    /**
     * 测试获取集群可供选择的索引服务
     */
    @Test
    public void listSelectTest() throws IOException {
        Result<List<ESClusterTemplateSrvVO>> result = ESPhyClusterTemplateSrvControllerMethod.listSelect(phyClusterInfo.getPhyClusterName());
        Assertions.assertTrue(result.success());
    }

    /**
     * 打开指定集群的指定索引服务
     */
    @Test
    public void addTemplateSrvIdTest() throws IOException {
        int templateSrvId = TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode();
        Result<Boolean> result = ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateSrvId));
        ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result.success());
        if(result.success()) {
            Result<List<ESClusterTemplateSrvVO>> result2 = ESPhyClusterTemplateSrvControllerMethod.listSelect(phyClusterInfo.getPhyClusterName());
            Set<Integer> idSet = result2.getData().stream().map(ESClusterTemplateSrvVO::getServiceId).collect(Collectors.toSet());
            // 判断是否有刚设置的索引服务id
            Assertions.assertTrue(idSet.contains(templateSrvId));
        }
    }

    /**
     * 关闭指定集群的指定索引服务
     */
    @Test
    public void delTemplateSrvIdTest() throws IOException {
        // 开启某个索引服务
        int templateSrvId = TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode();
        Result<Boolean> result = ESPhyClusterTemplateSrvControllerMethod.addTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result.success());
        if(result.success()) {
            Result<Boolean> result1 = ESPhyClusterTemplateSrvControllerMethod.delTemplateSrvId(phyClusterInfo.getPhyClusterName(), String.valueOf(templateSrvId));
            Assertions.assertTrue(result1.success());
            // 再获取看下是否已经关闭了
            Result<List<ESClusterTemplateSrvVO>> result2 = ESPhyClusterTemplateSrvControllerMethod.listSelect(phyClusterInfo.getPhyClusterName());
            if(result.success()) {
                Set<Integer> idSet = result2.getData().stream().map(ESClusterTemplateSrvVO::getServiceId).collect(Collectors.toSet());
                Assertions.assertFalse(idSet.contains(templateSrvId));
            }
        }
    }
}
