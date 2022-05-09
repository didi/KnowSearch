package com.didichuxing.datachannel.arius.admin.v3.op.task;

import com.didichuxing.datachannel.arius.admin.base.BaseDCDRTaskTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRSingleTemplateMasterSlaveSwitchDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRTasksDetailVO;
import com.didichuxing.datachannel.arius.admin.method.v3.op.task.OpTaskDCDRControllerMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OpTaskDCDRControllerTest extends BaseDCDRTaskTest {


    @Test
    public void testGetDCDRMasterSlaveSwitchDetailVO() throws IOException {
        Result<DCDRTasksDetailVO> result = OpTaskDCDRControllerMethod.getDCDRMasterSlaveSwitchDetailVO(dcdrWorkTaskVO.getId());
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testGetDCDRSingleTemplateMasterSlaveSwitchDetailVO() throws IOException {
        Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> result = OpTaskDCDRControllerMethod.getDCDRSingleTemplateMasterSlaveSwitchDetailVO(dcdrWorkTaskVO.getId(), dcdrTemplateId);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testRefreshDcdrChannelState() throws IOException {
        Result<Void> result = OpTaskDCDRControllerMethod.refreshDcdrChannelState(dcdrWorkTaskVO.getId(), dcdrTemplateId);
        Assertions.assertTrue(result.success());
    }

    @Test
    public void testForceSwitchMasterSlave() throws IOException {
        Result<Void> result = OpTaskDCDRControllerMethod.forceSwitchMasterSlave(dcdrWorkTaskVO.getId(), dcdrTemplateId);
        Assertions.assertTrue(result.success());
    }

}
