package com.didichuxing.datachannel.arius.admin.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.base.BaseLogicClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic.ClusterLogicTemplateSrvControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class ClusterLogicTemplateSrvTest extends BaseLogicClusterInfoTest {

    @Test
    public void listTest() throws IOException {
        Result<List<ESClusterTemplateSrvVO>> result = ClusterLogicTemplateSrvControllerMethod.list(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
        String[] templateSrvs = phyClusterInfo.getClusterJoinDTO().getTemplateSrvs().split(",");
        // 已开启的，应该和创建时设置的一致
        Assertions.assertEquals(result.getData().size(), templateSrvs.length);
    }

    @Test
    public void listSelectTest() throws IOException {
        Result<List<ESClusterTemplateSrvVO>> result = ClusterLogicTemplateSrvControllerMethod.listSelect(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void addTemplateSrvIdTest() throws IOException {
        int templateSrvId = TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode();
        // 先关闭
        Result<Boolean> result = ClusterLogicTemplateSrvControllerMethod.delTemplateSrvId(logicClusterInfo.getLogicClusterId(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result.success());
        // 再打开
        Result<Boolean> result2 = ClusterLogicTemplateSrvControllerMethod.addTemplateSrvId(logicClusterInfo.getLogicClusterId(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result2.success());
        // 再判断是否真的打开了
        Result<List<ESClusterTemplateSrvVO>> result3 = ClusterLogicTemplateSrvControllerMethod.list(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result3.success());
        Set<Integer> idSet = result3.getData().stream().map(ESClusterTemplateSrvVO::getServiceId).collect(Collectors.toSet());
        Assertions.assertTrue(idSet.contains(templateSrvId));
    }

    @Test
    public void delTemplateSrvIdTest() throws IOException {
        int templateSrvId = TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode();
        // 先打开
        Result<Boolean> result = ClusterLogicTemplateSrvControllerMethod.addTemplateSrvId(logicClusterInfo.getLogicClusterId(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result.success());
        // 再关闭
        Result<Boolean> result2 = ClusterLogicTemplateSrvControllerMethod.delTemplateSrvId(logicClusterInfo.getLogicClusterId(), String.valueOf(templateSrvId));
        Assertions.assertTrue(result2.success());
        // 再判断是否真的关闭了
        Result<List<ESClusterTemplateSrvVO>> result3 = ClusterLogicTemplateSrvControllerMethod.list(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result3.success());
        Set<Integer> idSet = result3.getData().stream().map(ESClusterTemplateSrvVO::getServiceId).collect(Collectors.toSet());
        Assertions.assertFalse(idSet.contains(templateSrvId));
    }
}
