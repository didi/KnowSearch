package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.impl.handler;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmOperateAppBase;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.common.bean.common.ecm.response.EcmTaskStatus;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.remote.zeus.bean.constant.EcmActionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractEcmBaseHandle {
    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractEcmBaseHandle.class);

    protected ESClusterTypeEnum   esClusterTypeEnum;

    public ESClusterTypeEnum getEsClusterTypeEnum() {
        return esClusterTypeEnum;
    }

    /**
     * 保留集群信息、集群角色信息, 不保留集群节点信息, 集群节点信息由定时任务同步
     */
    public abstract Result<Long> saveESCluster(List<EcmParamBase> ecmParamBaseList);

    public abstract Result<EcmOperateAppBase> startESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmOperateAppBase> scaleESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmOperateAppBase> upgradeESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmOperateAppBase> restartESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmOperateAppBase> removeESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmOperateAppBase> actionNotFinishedTask(EcmParamBase actionParamBase, EcmActionEnum ecmActionEnum, String hostname);

    public abstract Result<String> infoESCluster(EcmParamBase actionParamBase);

    public abstract Result<EcmSubTaskLog> getSubTaskLog(Long taskId, String hostname, EcmParamBase actionParamBase);

    public abstract Result<List<EcmTaskStatus>> getTaskStatus(EcmParamBase ecmParamBase, Integer orderType);
}
