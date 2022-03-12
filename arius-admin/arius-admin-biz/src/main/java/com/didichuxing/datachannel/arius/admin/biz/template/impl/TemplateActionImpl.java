package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType.NO_CAPACITY_PLAN;

import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import java.util.List;
import java.util.UUID;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.client.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplateLogicDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.template.IndexTemplatePhysicalDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.ExtendServiceFactory;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterConfigProvider;
import com.didichuxing.datachannel.arius.admin.biz.extend.intfc.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019-08-04
 */
@Service
public class TemplateActionImpl implements TemplateAction {

    private static final ILog          LOGGER = LogFactory.getLog(TemplateActionImpl.class);

    @Autowired
    private TemplateLogicService       templateLogicService;

    @Autowired
    private TemplatePhyService         templatePhyService;

    @Autowired
    private ExtendServiceFactory       extendServiceFactory;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private DistributorUtils           distributorUtils;

    @Autowired
    private AppClusterLogicAuthService logicClusterAuthService;

    @Autowired
    private TemplateLogicManager       templateLogicManager;

    /**
     * 自动获取资源
     *
     * @param logicDTO 模板
     * @param operator 操作人
     * @return result
     * @throws AdminOperateException exception
     */
    @Override
    public Result<Integer> createWithAutoDistributeResource(IndexTemplateLogicDTO logicDTO,
                                                            String operator) throws AdminOperateException {
        // 必须指定物理模板
        if (CollectionUtils.isEmpty(logicDTO.getPhysicalInfos())) {
            return Result.buildFail("未指定物理模板");
        }

        Long logicClusterId = logicDTO.getPhysicalInfos().get(0).getResourceId();
        if (!logicClusterAuthService.canCreateLogicTemplate(logicDTO.getAppId(), logicClusterId)) {
            return Result.buildFail(String.format("APP[%s]没有在逻辑集群[%s]下创建模板的权限", logicDTO.getAppId(), logicClusterId));
        }

        return handleCreateWithAutoDistributeResource(logicDTO, operator);

    }

    /**
     * 扩缩容
     *
     * @param logicId          逻辑id
     * @param expectExpireTime 期望保存周期
     * @param expectQuota      期望quota
     * @param submitor         操作人
     * @return result
     */
    @Override
    public Result<Void> indecreaseWithAutoDistributeResource(Integer logicId, Integer expectExpireTime, Double expectQuota,
                                                       String submitor) throws AdminOperateException {
        IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(logicId);

        if (templateLogic == null) {
            return Result.buildParamIllegal("模板不存在");
        }

        IndexTemplateLogicDTO logicDTO = new IndexTemplateLogicDTO();
        logicDTO.setId(logicId);
        logicDTO.setExpireTime(expectExpireTime);
        logicDTO.setQuota(expectQuota);

        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getTemplateByLogicId(logicId);
        if (!CollectionUtils.isEmpty(templatePhysicals) && (expectQuota > templateLogic.getQuota())) {
            double deltaQuota = (expectQuota - templateLogic.getQuota()) / templatePhysicals.size();
            if (deltaQuota > 0) {
                for (IndexTemplatePhy templatePhysical : templatePhysicals) {
                    ClusterLogic clusterLogic = clusterLogicService
                        .getClusterLogicByRack(templatePhysical.getCluster(), templatePhysical.getRack());
                    Result<TemplateDistributedRack> distributorResult = increaseTemplateDistributedRack(
                        clusterLogic.getId(), templatePhysical.getCluster(), templatePhysical.getRack(),
                        deltaQuota);
                    if (distributorResult.failed()) {
                        LOGGER.warn(
                            "class=TemplateActionImpl||method=indecreaseWithAutoDistributeResource||resourceId={}||quota={}||msg=acquire cluster fail: {}",
                            clusterLogic.getId(), deltaQuota, distributorResult.getMessage());
                        return Result.buildFrom(distributorResult);
                    }
                }
            } else {
                LOGGER.info(
                    "class=TemplateActionImpl||method=indecreaseWithAutoDistributeResource||logicId={}||deltaQuota={}||msg=deltaQuota < 0",
                    logicId, deltaQuota);
            }
        }

        // 修改模板quota及保存时长信息
        return templateLogicService.editTemplate(logicDTO, submitor);
    }

    /**
     * 获取物理模板资源配置
     *
     * @param physicalId 物理模板id
     * @return result
     */
    @Override
    public TemplateResourceConfig getPhysicalTemplateResourceConfig(Long physicalId) {
        TemplateClusterConfigProvider extendConfigProvider = null;
        Result<TemplateClusterConfigProvider> extendResult = extendServiceFactory
            .getExtend(TemplateClusterConfigProvider.class);
        if (extendResult.success()) {
            extendConfigProvider = extendResult.getData();
        } else {
            LOGGER.warn("class=TemplateActionImpl||method=createWithAutoDistributeResource||msg=extendConfigProvider not find");
        }

        TemplateClusterConfigProvider defaultConfigProvider = extendServiceFactory
            .getDefault(TemplateClusterConfigProvider.class);

        Result<TemplateResourceConfig> configResult = null;

        if (extendConfigProvider != null) {
            configResult = extendConfigProvider.getTemplateResourceConfig(physicalId);
        }

        if (configResult == null || configResult.getCode().equals(NO_CAPACITY_PLAN.getCode())) {
            configResult = defaultConfigProvider.getTemplateResourceConfig(physicalId);
        }

        if (configResult.failed()) {
            return new TemplateResourceConfig();
        }

        return configResult.getData();
    }

    /**************************************** private methods ****************************************/
    /**
     * 扩容使用
     * @param resourceId 资源
     * @param cluster 集群
     * @param quota rack
     * @return result
     */
    private Result<TemplateDistributedRack> increaseTemplateDistributedRack(Long resourceId, String cluster,
                                                                            String rack, double quota) {
        TemplateClusterDistributor extendDistributor = null;

        Result<TemplateClusterDistributor> extendResult = extendServiceFactory
            .getExtend(TemplateClusterDistributor.class);
        if (extendResult.success()) {
            extendDistributor = extendResult.getData();
        } else {
            LOGGER.warn("class=TemplateActionImpl||method=getTemplateResourceInner||msg=extendDistributor not find");
        }

        TemplateClusterDistributor defaultDistributor = extendServiceFactory
            .getDefault(TemplateClusterDistributor.class);

        Result<TemplateDistributedRack> distributedRackResult = null;
        if (extendDistributor != null) {
            distributedRackResult = extendDistributor.indecrease(resourceId, cluster, rack, quota);
        }

        if (distributedRackResult == null || distributedRackResult.getCode().equals(NO_CAPACITY_PLAN.getCode())) {
            distributedRackResult = defaultDistributor.indecrease(resourceId, cluster, rack, quota);
        }

        if (distributedRackResult.failed()) {
            return distributedRackResult;
        }

        return distributedRackResult;
    }

    private Result<Integer> handleCreateWithAutoDistributeResource(IndexTemplateLogicDTO logicDTO, String operator) throws AdminOperateException {
        int indexDefaultWriterSetFlags = -1;
        for (IndexTemplatePhysicalDTO physicalDTO : logicDTO.getPhysicalInfos()) {
            if (StringUtils.isNotBlank(physicalDTO.getCluster()) && physicalDTO.getRack() != null) {
                handleIndexTemplatePhysical(physicalDTO);
                continue;
            }
            if (indexDefaultWriterSetFlags == -1) {
                indexDefaultWriterSetFlags = 0;
            }
            Result<TemplateDistributedRack> distributedRackResult = distributorUtils.getTemplateDistributedRack(physicalDTO.getResourceId(), logicDTO.getQuota());
            if (distributedRackResult.failed()) {
                LOGGER.warn("class=TemplateActionImpl||method=createWithAutoDistributeResource||msg=distributedRackResult fail");
                return Result.buildFrom(distributedRackResult);
            }
            physicalDTO.setCluster(distributedRackResult.getData().getCluster());
            physicalDTO.setRack(distributedRackResult.getData().getRack());
            physicalDTO.setDefaultWriterFlags(false);
            if (indexDefaultWriterSetFlags <= 0 && distributedRackResult.getData().isResourceMatched()) {
                physicalDTO.setDefaultWriterFlags(true);
                indexDefaultWriterSetFlags = 1;
            }
            if (StringUtils.isBlank(physicalDTO.getGroupId())) {
                physicalDTO.setGroupId(UUID.randomUUID().toString());
            }
        }
        if (indexDefaultWriterSetFlags == 0) {
            return Result.buildFail("集群空闲资源不足");
        }

        return templateLogicManager.createLogicTemplate(logicDTO, operator);
    }

    private void handleIndexTemplatePhysical(IndexTemplatePhysicalDTO physicalDTO) {
        if (physicalDTO.getDefaultWriterFlags() == null) {
            physicalDTO.setDefaultWriterFlags(true);
        }
        if (StringUtils.isBlank(physicalDTO.getGroupId())) {
            physicalDTO.setGroupId(UUID.randomUUID().toString());
        }
    }

}
