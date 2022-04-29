package com.didichuxing.datachannel.arius.admin.remote.zeus.bean;

import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.ecm.EcmHostStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZeusTaskStatus {
    private List<String> success;

    private List<String> failed;

    private List<String> running;

    private List<String> waiting;

    private List<String> timeout;

    private List<String> killing;

    private List<String> kill_failed;

    private List<String> cancelled;

    private List<String> ignored;

    public List<EcmTaskStatus> convert2EcmHostStatusEnumList(Integer taskId) {
        List<EcmTaskStatus> statusList = new ArrayList<>();
        if (!AriusObjUtils.isEmptyList(success)) {
            handleSuccess(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(failed)) {
            handleFailed(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(running)) {
            handleRunning(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(waiting)) {
            handleWaiting(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(timeout)) {
            handleTimeout(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(killing)) {
            handleKilling(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(kill_failed)) {
            handleKillFailed(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(cancelled)) {
            handleCancelled(taskId, statusList);
        }
        if (!AriusObjUtils.isEmptyList(ignored)) {
            handleIgnored(taskId, statusList);
        }
        return statusList;
    }

    private void handleCancelled(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : cancelled) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.CANCELLED);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleIgnored(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : ignored) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.IGNORE);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleKillFailed(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : kill_failed) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.KILL_FAILED);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleKilling(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : killing) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.KILLING);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleTimeout(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : timeout) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.TIMEOUT);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleWaiting(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : waiting) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.WAITING);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleRunning(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : running) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.RUNNING);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleFailed(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : failed) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.FAILED);
            statusList.add(ecmTaskStatus);
        }
    }

    private void handleSuccess(Integer taskId, List<EcmTaskStatus> statusList) {
        for (String hostname : success) {
            EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
            ecmTaskStatus.setTaskId(taskId);
            ecmTaskStatus.setHostname(hostname);
            ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.SUCCESS);
            statusList.add(ecmTaskStatus);
        }
    }
}