package com.didichuxing.datachannel.arius.admin.v3.op.cluster.logic;

import com.didichuxing.datachannel.arius.admin.base.BaseLogicClusterInfoTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterVO;
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
        Result<List<ConsoleClusterVO>> result = ESLogicClusterOpV3ControllerMethod.getAppLogicClusterInfo();
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().stream().map(ConsoleClusterVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void getAppLogicClusterInfoByTypeTest() throws IOException {
        Result<List<ConsoleClusterVO>> result = ESLogicClusterOpV3ControllerMethod.getAppLogicClusterInfoByType(logicClusterInfo.getType());
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().stream().map(ConsoleClusterVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void pageGetConsoleClusterVOSTest() throws IOException {
        // 带上名字去查询
        ClusterLogicConditionDTO dto = CustomDataSource.getClusterLogicConditionDTO(logicClusterInfo.getLogicClusterName());
        PaginationResult<ConsoleClusterVO> result = ESLogicClusterOpV3ControllerMethod.pageGetConsoleClusterVOS(dto);
        Assertions.assertTrue(result.success());
        // 包含刚创建的逻辑集群名
        Set<String> nameSet = result.getData().getBizData().stream().map(ConsoleClusterVO::getName).collect(Collectors.toSet());
        Assertions.assertTrue(nameSet.contains(logicClusterInfo.getLogicClusterName()));
    }

    @Test
    public void getTest() throws IOException {
        Result<ConsoleClusterVO> result = ESLogicClusterOpV3ControllerMethod.get(logicClusterInfo.getLogicClusterId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void checkTemplateValidForCreateTest() throws IOException {
        Result<Void> result =
                ESLogicClusterOpV3ControllerMethod.checkTemplateValidForCreate(logicClusterInfo.getLogicClusterId(), "20");
        Assertions.assertTrue(result.success());
    }
}
