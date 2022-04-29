package com.didichuxing.datachannel.arius.admin.base;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.DCDRMasterSlaveSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.task.OpTaskDCDRControllerMethod;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.util.List;

public class BaseDCDRTaskTest extends BaseContextTest {

    protected static WorkTaskVO dcdrWorkTaskVO;
    protected static Long dcdrTemplateId = 19615L;

    @BeforeAll
    public static void preHandle() throws IOException {
        // 开启DCDR服务
        Result<WorkTaskVO> result =  startDCDRMasterSlaveSwitch(dcdrTemplateId);
        Assertions.assertTrue(result.success());
        Assertions.assertNotNull(result.getData());
        dcdrWorkTaskVO = result.getData();
    }

    @AfterAll
    public static void afterCompletion() throws IOException {
        // 关闭DCDR服务
        Result<Void> partialCancelResult = cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(dcdrWorkTaskVO.getId(), Lists.newArrayList(dcdrTemplateId));
        Assertions.assertTrue(partialCancelResult.success());
        Result<Void> fullCancelResult = cancelDCDRMasterSlaveSwitch(dcdrWorkTaskVO.getId());
        Assertions.assertTrue(fullCancelResult.success());
    }



    public static Result<WorkTaskVO> startDCDRMasterSlaveSwitch(Long templateId) throws IOException {
        DCDRMasterSlaveSwitchDTO dto = new DCDRMasterSlaveSwitchDTO();
        dto.setType(1);
        dto.setTemplateIds(Lists.newArrayList(templateId));
        return OpTaskDCDRControllerMethod.dcdrSwitchMasterSlave(dto);
    }

    public static Result<Void> cancelDCDRMasterSlaveSwitch(Integer taskId) throws IOException {
        return OpTaskDCDRControllerMethod.cancelDcdrSwitchMasterSlaveByTaskId(taskId);
    }

    public static Result<Void> cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateIds) throws IOException {
        return OpTaskDCDRControllerMethod.cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(taskId, templateIds);
    }
}
