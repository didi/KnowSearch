package com.didichuxing.datachannel.arius.admin.remote.nightingale.bean;

import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.constant.ecm.EcmHostStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ValidateUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NightingaleTaskStatus {
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
        if (!ValidateUtils.isEmptyList(success)) {
            for (String hostname : success) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.SUCCESS);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(failed)) {
            for (String hostname : failed) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.FAILED);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(running)) {
            for (String hostname : running) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.RUNNING);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(waiting)) {
            for (String hostname : waiting) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.WAITING);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(timeout)) {
            for (String hostname : timeout) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.TIMEOUT);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(killing)) {
            for (String hostname : killing) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.KILLING);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(kill_failed)) {
            for (String hostname : kill_failed) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.KILL_FAILED);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(cancelled)) {
            for (String hostname : cancelled) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.CANCELLED);
                statusList.add(ecmTaskStatus);
            }
        }
        if (!ValidateUtils.isEmptyList(ignored)) {
            for (String hostname : ignored) {
                EcmTaskStatus ecmTaskStatus = new EcmTaskStatus();
                ecmTaskStatus.setTaskId(taskId);
                ecmTaskStatus.setHostname(hostname);
                ecmTaskStatus.setStatusEnum(EcmHostStatusEnum.IGNORE);
                statusList.add(ecmTaskStatus);
            }
        }
        return statusList;
    }
}