package com.didichuxing.datachannel.arius.admin.biz.template.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.NO_CAPACITY_PLAN;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import java.util.List;
import java.util.UUID;

import com.didichuxing.datachannel.arius.admin.biz.template.TemplateAction;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.biz.component.DistributorUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateDistributedRack;
import com.didichuxing.datachannel.arius.admin.common.bean.common.TemplateResourceConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplateDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.IndexTemplatePhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.ExtendServiceFactory;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.TemplateClusterConfigProvider;
import com.didichuxing.datachannel.arius.admin.biz.extend.foctory.TemplateClusterDistributor;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
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
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ExtendServiceFactory       extendServiceFactory;

    @Autowired
    private ClusterPhyService clusterPhyService;

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
    public Result<Integer> createWithAutoDistributeResource(IndexTemplateDTO logicDTO,
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
     * @param expectHotTime    期望热数据保存天数
     * @param expectExpireTime 期望保存周期
     * @param expectQuota      期望quota
     * @param submitor         操作人
     * @return result
     */
    @Override
    public Result<Void> indecreaseWithAutoDistributeResource(Integer logicId, Integer expectHotTime, Integer expectExpireTime, Double expectQuota,
                                                       String submitor) throws AdminOperateException {
        IndexTemplate templateLogic = indexTemplateService.getLogicTemplateById(logicId);

        if (templateLogic == null) {
            return Result.buildParamIllegal("模板不存在");
        }

        IndexTemplateDTO logicDTO = new IndexTemplateDTO();
        logicDTO.setId(logicId);
        logicDTO.setExpireTime(expectExpireTime);
        logicDTO.setQuota(expectQuota);

        //校验是否可以修改为指定的热数据保存天数
        Result<Void> validOpenColdAndHotServiceResult = validOpenColdAndHotServiceResult(logicId, expectHotTime, expectExpireTime);
        if (validOpenColdAndHotServiceResult.success()) {
            LOGGER.info("class=TemplateActionImpl||method=indecreaseWithAutoDistributeResource" +
                    "||msg={}", validOpenColdAndHotServiceResult.getMessage());
            logicDTO.setHotTime(expectHotTime);
        }

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService.getTemplateByLogicId(logicId);
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
        return indexTemplateService.editTemplate(logicDTO, submitor);
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

    private Result<Integer> handleCreateWithAutoDistributeResource(IndexTemplateDTO logicDTO, String operator) throws AdminOperateException {
        int indexDefaultWriterSetFlags = -1;
        for (IndexTemplatePhyDTO physicalDTO : logicDTO.getPhysicalInfos()) {
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

    private void handleIndexTemplatePhysical(IndexTemplatePhyDTO physicalDTO) {
        if (physicalDTO.getDefaultWriterFlags() == null) {
            physicalDTO.setDefaultWriterFlags(true);
        }
        if (StringUtils.isBlank(physicalDTO.getGroupId())) {
            physicalDTO.setGroupId(UUID.randomUUID().toString());
        }
    }

    /**
     * 校验是否可以修改指定逻辑模板的热数据保存天数
     *
     * @param logicId          逻辑模板的id
     * @param expectHotTime    期待的保存天数
     * @param expectExpireTime 期待的热数据保存天数
     * @return 校验结果
     */
    private Result<Void> validOpenColdAndHotServiceResult(Integer logicId, Integer expectHotTime, Integer expectExpireTime) {
        IndexTemplateWithPhyTemplates logicTemplateWithPhysicalsById = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);
        if (AriusObjUtils.isNull(logicTemplateWithPhysicalsById) ||
                CollectionUtils.isEmpty(logicTemplateWithPhysicalsById.getPhysicals())) {
            return Result.buildFail("逻辑集群指定信息为空");
        }

        //获取逻辑模板对应的一个物理模板
        IndexTemplatePhy indexTemplatePhy = logicTemplateWithPhysicalsById.getPhysicals().get(0);
        if (AriusObjUtils.isNull(indexTemplatePhy)) {
            return Result.buildFail("物理集群信息为空");
        }

        //获取对应的物理集群的信息
        ClusterPhy clusterPhyByName = clusterPhyService.getClusterByName(indexTemplatePhy.getCluster());
        if (AriusObjUtils.isNull(clusterPhyByName)) {
            return Result.buildFail("物理集群信息为空");
        }

        //获取物理集群下已经开启的索引服务
        String templateSrvs = clusterPhyByName.getTemplateSrvs();
        List<String> templateSrvIds = ListUtils.string2StrList(templateSrvs);
        if (CollectionUtils.isEmpty(templateSrvIds) ||
                !templateSrvIds.contains(TemplateServiceEnum.TEMPLATE_COLD.getCode().toString())) {
            return Result.buildFail("指定物理集群没有开启索引服务");
        }

        //只要当热数据保存天数小于等于总的模板保存天数才有意义
        if (expectExpireTime != -1 || expectExpireTime < expectHotTime) {
            return Result.buildFail("热数据天数的设置不应该超过期待的保存天数");
        }

        return Result.buildSucc();
    }
}
