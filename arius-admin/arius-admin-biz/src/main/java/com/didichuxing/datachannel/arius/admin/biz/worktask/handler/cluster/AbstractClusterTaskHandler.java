package com.didichuxing.datachannel.arius.admin.biz.worktask.handler.cluster;

import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum.*;

import java.util.Optional;

import com.didichuxing.datachannel.arius.admin.biz.worktask.handler.ECMOpTaskHandler;
import com.didichuxing.datachannel.arius.admin.common.constant.task.OpTaskTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.worktask.content.ClusterBaseContent;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.task.ecm.EcmTaskDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.task.OpTask;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.ESPackageService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm.EcmHandleService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * 集群处理器
 * 统一集群任务处理流程
 *
 * @author ohushenglin_v
 * @date 2022-05-20
 */
public abstract class AbstractClusterTaskHandler extends ECMOpTaskHandler {
    public static final Result<Void> CLUSTER_TYPE_NOT_SUPPORT = Result.buildFail("集群类型暂时不支持！");
    protected final ILog             LOGGER                   = LogFactory.getLog(this.getClass());
    @Autowired
    protected ESPackageService       esPackageService;
    @Autowired
    protected EcmHandleService       ecmHandleService;
    @Autowired
    protected ClusterRoleHostService clusterRoleHostService;

    /**
     * 统一处理集群任务
     *
     * @param opTask op任务
     * @return {@link Result}<{@link OpTask}>
     */
    @Override
    public Result<OpTask> addTask(OpTask opTask) {
        Result<Void> initResult = initParam(opTask);
        if (initResult.failed()) {
            return Result.buildFail(initResult.getMessage());
        }
        Result<Void> validateResult = validateParam(opTask.getExpandData());
        if (validateResult.failed()) {
            return Result.buildFail(validateResult.getMessage());
        }
        Result<OpTask> buildResult = buildOpTask(opTask);
        if (buildResult.failed()) {
            return Result.buildFail(buildResult.getMessage());
        }
        return super.addTask(buildResult.getData());

    }

    /**
     * 初始化参数
     *
     * @param opTask op任务
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> initParam(OpTask opTask) {
        //这里目前默认为Host，后续如果支持了其他的类型，修改默认为unknown
        ESClusterTypeEnum type = Optional.ofNullable(opTask).map(OpTask::getExpandData).map(JSON::parseObject)
            .map(jsonObject -> jsonObject.getInteger("type")).map(ESClusterTypeEnum::valueOf).orElse(ES_HOST);

        Result<Void> initResult = Result.buildSucc();
        if (ES_HOST == type) {
            Optional.ofNullable(opTask).map(OpTask::getExpandData).map(JSON::parseObject).ifPresent(jsonObject -> {
                jsonObject.put("type", ES_HOST.getCode());
                opTask.setExpandData(jsonObject.toJSONString());
            });
            initResult = initHostParam(opTask);
        } else if (ES_DOCKER == type) {
            Optional.ofNullable(opTask).map(OpTask::getExpandData).map(JSON::parseObject).ifPresent(jsonObject -> {
                jsonObject.put("type", ES_DOCKER.getCode());
                opTask.setExpandData(jsonObject.toJSONString());
            });

            initResult = initDockerParam(opTask);
        }
        return initResult;
    }

    /**
     * 验证集群任务参数
     *
     * @param param 参数
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> validateParam(String param) {
        ESClusterTypeEnum type = Optional.ofNullable(param).map(JSON::parseObject)
            .map(jsonObject -> jsonObject.getInteger("type")).map(ESClusterTypeEnum::valueOf).orElse(UNKNOWN);

        if (ES_HOST == type) {
            return validateHostParam(param);
        } else {
            return CLUSTER_TYPE_NOT_SUPPORT;
        }
    }

    /**
     * 构建op任务参数
     *
     * @param opTask op任务
     * @return {@link Result}<{@link OpTask}>
     */
    Result<OpTask> buildOpTask(OpTask opTask) {
        ClusterBaseContent content = ConvertUtil.str2ObjByJson(opTask.getExpandData(), ClusterBaseContent.class);
        EcmTaskDTO ecmTaskDTO = new EcmTaskDTO();
        ecmTaskDTO
            .setTitle(content.getPhyClusterName() + OpTaskTypeEnum.valueOfType(opTask.getTaskType()).getMessage());
        ecmTaskDTO.setCreator(opTask.getCreator());
        ecmTaskDTO.setType(content.getType());
        ecmTaskDTO.setPhysicClusterId(ClusterConstant.INVALID_VALUE);

        Result<Void> result = CLUSTER_TYPE_NOT_SUPPORT;
        if (ES_DOCKER.getCode() == content.getType()) {
            result = buildDockerEcmTaskDTO(ecmTaskDTO, opTask.getExpandData(), opTask.getCreator());
        } else if (ES_HOST.getCode() == content.getType()) {
            result = buildHostEcmTaskDTO(ecmTaskDTO, opTask.getExpandData(), opTask.getCreator());
        }
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }
        opTask.setExpandData(JSON.toJSONString(ecmTaskDTO));
        opTask.setTaskType(opTask.getTaskType());
        return Result.buildSucc(opTask);
    }

    /**
     * 初始化 ESHost集群任务参数
     *
     * @param opTask op任务
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> initHostParam(OpTask opTask) {
        return Result.buildSucc();
    }

    /**
     * 初始化 ESDocker集群任务参数
     *
     * @param opTask op任务
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> initDockerParam(OpTask opTask) {
        return Result.buildSucc();
    }

    /**
     * 验证 ESHost 集群任务参数
     *
     * @param param 参数
     * @return {@link Result}<{@link Void}>
     */
    abstract Result<Void> validateHostParam(String param);

    /**
     * 构建ESHost的ecm任务信息
     *
     * @param ecmTaskDTO    ecm任务dto
     * @param param    任务参数
     * @param creator       任务创建人
     * @return {@link Result}<{@link Void}>
     */
    abstract Result<Void> buildHostEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator);

    /**
     * 构建ESDocker的ecm任务信息
     *
     * @param ecmTaskDTO    ecm任务dto
     * @param param    任务参数
     * @param creator       任务创建人
     * @return {@link Result}<{@link Void}>
     */
    Result<Void> buildDockerEcmTaskDTO(EcmTaskDTO ecmTaskDTO, String param, String creator) {
        return Result.buildSucc();
    }

}
