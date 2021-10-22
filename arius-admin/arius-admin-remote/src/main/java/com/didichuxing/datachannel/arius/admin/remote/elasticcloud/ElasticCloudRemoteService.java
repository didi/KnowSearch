package com.didichuxing.datachannel.arius.admin.remote.elasticcloud;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.EcmParamBase;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudCommonActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.elasticcloud.ElasticCloudScaleActionParam;
import com.didichuxing.datachannel.arius.admin.client.bean.common.ecm.response.EcmSubTaskLog;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.bizenum.EcmActionEnum;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.request.ElasticCloudCreateParamDTO;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudAppStatus;
import com.didichuxing.datachannel.arius.admin.remote.elasticcloud.bean.response.ElasticCloudStatus;


public interface ElasticCloudRemoteService {
    /**
     * 创建弹性云集群并全部启动
     */
    Result<ElasticCloudAppStatus> createAndStartAll(ElasticCloudCreateParamDTO elasticCloudCreateParamDTO, String namespace, String machineRoom);

    /**
     * 按组升级或重启集群
     */
    Result upgradeOrRestartByGroup(ElasticCloudCommonActionParam elasticCloudCommonActionParam);

    /**
     * 扩缩容弹性云集群
     */
    Result scaleAndExecuteAll(ElasticCloudScaleActionParam elasticCloudScaleActionParam);

    /**
     * 删除弹性云集群
     */
    Result delete(ElasticCloudCommonActionParam elasticCloudActionParam);

    /**
     * 操作未完成的任务进行
     * 动作: pause|continue|redoFailed|skipFailed
     */
    Result actionNotFinishedTask(ElasticCloudCommonActionParam elasticCloudCommonActionParam, EcmActionEnum actionEnum);

    /**
     * 根据taskId 获取弹性云部署日志
     * @return Result
     */
    Result<EcmSubTaskLog> getTaskLog(Long taskId, String podName, EcmParamBase actionParamBase);

    /**
     * 获取弹性云集群的状态
     * @param actionParamBase
     * @return Result
     */
    Result<ElasticCloudStatus> getTaskStatus(EcmParamBase actionParamBase);

    /**
     * 获取集群信息
     * param elasticCloudCommonActionParam
     * @return Result
     */
    Result getClusterInfo(ElasticCloudCommonActionParam elasticCloudCommonActionParam);
}
