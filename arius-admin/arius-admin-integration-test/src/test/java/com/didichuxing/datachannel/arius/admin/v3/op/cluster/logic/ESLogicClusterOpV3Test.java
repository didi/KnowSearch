package com.didichuxing.datachannel.arius.admin.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.base.BaseLogicClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.cluster.logic.ESLogicClusterOpV3ControllerMethod;
import com.didichuxing.datachannel.arius.admin.source.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjm
 */
public class ESLogicClusterOpV3Test extends BaseLogicClusterInfoTest {

    @Test
    public void getAppLogicClusterNamesTest() throws IOException {
        Result<List<String>> result = ESLogicClusterOpV3ControllerMethod.getAppLogicClusterNames();
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Assertions.assertTrue(result.getData().contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void getAppLogicClusterInfoTest() throws IOException {
        Result<List<ClusterLogicVO>> result = ESLogicClusterOpV3ControllerMethod.getLogicClustersByProjectId();
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().stream().map(ClusterLogicVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void getAppLogicClusterInfoByTypeTest() throws IOException {
        Result<List<ClusterLogicVO>> result = ESLogicClusterOpV3ControllerMethod.getAppLogicClusterInfoByType(logicClusterInfo.getType());
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().stream().map(ClusterLogicVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void pageGetConsoleClusterVOSTest() throws IOException {
        // 带上名字去查询
        ClusterLogicConditionDTO dto = CustomDataSource.getClusterLogicConditionDTO(logicClusterInfo.getLogicClusterName());
        PaginationResult<ClusterLogicVO> result = ESLogicClusterOpV3ControllerMethod.pageGetConsoleClusterVOS(dto);
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().getBizData().stream().map(ClusterLogicVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void getTest() throws IOException {
        Result<ClusterLogicVO> result = ESLogicClusterOpV3ControllerMethod.get(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void checkTemplateValidForCreateTest() throws IOException {
        Result<Void> result =
                ESLogicClusterOpV3ControllerMethod.checkTemplateValidForCreate(logicClusterInfo.getLogicClusterId(), "20");
        Assertions.assertTrue(result.success());
    }
}
