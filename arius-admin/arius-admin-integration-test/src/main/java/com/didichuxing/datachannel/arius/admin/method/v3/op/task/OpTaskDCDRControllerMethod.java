package com.didichuxing.datachannel.arius.admin.method.v3.op.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.didichuxing.datachannel.arius.admin.AriusClient;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.DCDRMasterSlaveSwitchDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.task.WorkTaskVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRSingleTemplateMasterSlaveSwitchDetailVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.DCDRTasksDetailVO;

import java.io.IOException;
import java.util.List;


public class OpTaskDCDRControllerMethod {

    public static final String DCDR_TASK = "/v3/op/dcdr/work-order/task";

    public static Result<DCDRTasksDetailVO> getDCDRMasterSlaveSwitchDetailVO(Integer taskId) throws IOException {
        String path = String.format("%s/%d/detail", DCDR_TASK, taskId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<DCDRTasksDetailVO>>(){});
    }

    public static Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO> getDCDRSingleTemplateMasterSlaveSwitchDetailVO(Integer taskId, Long templateId) throws IOException {
        String path = String.format("%s/%d/%d/detail", DCDR_TASK, taskId, templateId);
        return JSON.parseObject(AriusClient.get(path), new TypeReference<Result<DCDRSingleTemplateMasterSlaveSwitchDetailVO>>(){});
    }

    public static Result<WorkTaskVO> dcdrSwitchMasterSlave(DCDRMasterSlaveSwitchDTO dcdrMasterSlaveSwitchDTO) throws IOException {
        String path = String.format("%s/switchMasterSlave", DCDR_TASK);
        return JSON.parseObject(AriusClient.post(path, dcdrMasterSlaveSwitchDTO), new TypeReference<Result<WorkTaskVO>>(){});
    }

    public static Result<Void> cancelDcdrSwitchMasterSlaveByTaskIdAndTemplateIds(Integer taskId, List<Long> templateIds) throws IOException {
        String path = String.format("%s/%d/%s/cancel", DCDR_TASK, taskId, templateIds.toString().substring(1, templateIds.toString().length() - 1));
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> cancelDcdrSwitchMasterSlaveByTaskId(Integer taskId) throws IOException {
        String path = String.format("%s/%d/cancel", DCDR_TASK, taskId);
        return JSON.parseObject(AriusClient.delete(path), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> refreshDcdrChannelState(Integer taskId, Long templateId) throws IOException {
        String path = String.format("%s/%d/%d/refresh", DCDR_TASK, taskId, templateId);
        return JSON.parseObject(AriusClient.post(path, "dummyBody"), new TypeReference<Result<Void>>(){});
    }

    public static Result<Void> forceSwitchMasterSlave(Integer taskId, Long templateId) throws IOException {
        String path = String.format("%s/%d/%d/forceSwitch", DCDR_TASK, taskId, templateId);
        return JSON.parseObject(AriusClient.put(path), new TypeReference<Result<Void>>(){});
    }

}
