package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import java.util.List;

import javax.annotation.PostConstruct;

import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Service("ecmDockerHandler")
public class EcmDockerHandler extends AbstractEcmBaseHandle {

    @PostConstruct
    public void init() {
        esClusterTypeEnum = ESClusterTypeEnum.ES_DOCKER;
    }

    @Override
    public Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmOperateAppBase> startESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmOperateAppBase> scaleESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmOperateAppBase> upgradeESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmOperateAppBase> restartESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmOperateAppBase> removeESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum, String hostname) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<String> infoESCluster(EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<EcmSubTaskLog> getSubTaskLog(Long taskId, String hostname, EcmParamBase actionParamBase) {
        return Result.buildFail("暂不支持");
    }

    @Override
    public Result<List<EcmTaskStatus>> getTaskStatus(EcmParamBase actionParamBase, Integer orderType) {
        return Result.buildFail("暂不支持");
    }
}
