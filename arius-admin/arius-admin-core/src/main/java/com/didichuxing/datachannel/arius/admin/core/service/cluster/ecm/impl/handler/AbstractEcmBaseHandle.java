package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractEcmBaseHandle {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractEcmBaseHandle.class);

    protected ESClusterTypeEnum   esClusterTypeEnum;

    public ESClusterTypeEnum getEsClusterTypeEnum() {
        return esClusterTypeEnum;
    }

    public abstract Result saveESCluster(List<EcmParamBase> ecmParamBaseList);

    public abstract Result<EcmOperateAppBase> startESCluster(EcmParamBase actionParamBase);

    public abstract Result scaleESCluster(EcmParamBase actionParamBase);

    public abstract Result upgradeESCluster(EcmParamBase actionParamBase);

    public abstract Result restartESCluster(EcmParamBase actionParamBase);

    public abstract Result removeESCluster(EcmParamBase actionParamBase);

    public abstract Result actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum,
                                                 String hostname);

    public abstract Result infoESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmSubTaskLog> getSubTaskLog(Long taskId, String hostname, EcmParamBase actionParamBase);

    public abstract Result<List<EcmTaskStatus>> getTaskStatus(EcmParamBase ecmParamBase, Integer orderType);
}
