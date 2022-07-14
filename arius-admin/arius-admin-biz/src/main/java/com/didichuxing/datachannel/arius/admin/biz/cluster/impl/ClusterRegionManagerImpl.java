package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.OperateRecord;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.TriggerWayEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClusterRegionManagerImpl implements ClusterRegionManager {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterRegionManagerImpl.class);

    @Autowired
    private ClusterRegionService   clusterRegionService;

    @Autowired
    private ClusterContextManager  clusterContextManager;

    @Autowired
    private ClusterLogicService    clusterLogicService;

    @Autowired
    private ClusterPhyService      clusterPhyService;

    @Autowired
    private TemplateSrvManager     templateSrvManager;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterNodeManager     clusterNodeManager;

    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private ProjectService         projectService;

    /**
     * 构建regionVO
     * @param regions region列表
     * @return
     */
    @Override
    public List<ClusterRegionVO> buildLogicClusterRegionVO(List<ClusterRegion> regions) {
        if (CollectionUtils.isEmpty(regions)) {
            return new ArrayList<>();
        }

        return regions.stream().filter(Objects::nonNull).map(this::buildLogicClusterRegionVO)
            .collect(Collectors.toList());
    }

    /**
     * 逻辑集群绑定同一个物理集群的region的时候需要根据类型进行过滤
     * @param phyCluster 物理集群名称
     * @param clusterLogicType 逻辑集群类型
     * @return
     */
    @Override
    public Result<List<ClusterRegionVO>> listPhyClusterRegionsByLogicClusterTypeAndCluster(String phyCluster,
                                                                                           Integer clusterLogicType) {
        if (!ClusterResourceTypeEnum.isExist(clusterLogicType)) {
            return Result.buildFail("逻辑集群类型不存在");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyCluster);
        if (null == clusterPhy) {
            return Result.buildFail(String.format("物理集群[%s]不存在", phyCluster));
        }

        int resourceType = clusterPhy.getResourceType();
        if (clusterLogicType != resourceType) {
            return Result.buildFail(
                String.format("物理集群[%s]类型为[%s], 不满足逻辑集群类型[%s], 请调整类型一致", phyCluster, resourceType, clusterLogicType));
        }

        List<ClusterRegion> clusterRegions = clusterRegionService.listPhyClusterRegions(phyCluster);
        if (CollectionUtils.isEmpty(clusterRegions)) {
            return Result.buildFail(String.format("物理集群[%s]无划分region, 请先进行region划分", phyCluster));
        }
        return Result.buildSucc(ConvertUtil.list2List(clusterRegions, ClusterRegionVO.class,
            regionVO -> regionVO.setClusterName(phyCluster)));
    }

    /**
     * 构建regionVO
     * @param region region
     * @return
     */
    @Override
    public ClusterRegionVO buildLogicClusterRegionVO(ClusterRegion region) {
        if (region == null) {
            return null;
        }

        ClusterRegionVO logicClusterRegionVO = ConvertUtil.obj2Obj(region, ClusterRegionVO.class, regionVO -> {
            regionVO.setClusterName(region.getPhyClusterName());
        });
        logicClusterRegionVO.setId(region.getId());
        logicClusterRegionVO.setLogicClusterIds(region.getLogicClusterIds());
        logicClusterRegionVO.setClusterName(region.getPhyClusterName());
        return logicClusterRegionVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> batchBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param, String operator,
                                                      boolean isWorkOrder) throws AdminOperateException {
        //1. 前置校验
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }
        if (CollectionUtils.isEmpty(param.getClusterRegionDTOS())) {
            return Result.buildParamIllegal("逻辑集群关联region信息为空");
        }

        //2. 集群合法关联性校验
        param.getClusterRegionDTOS().stream().distinct().forEach(clusterRegionDTO -> {
            try {
                checkCanBeBound(param.getId(), clusterRegionDTO, param.getType());
            } catch (AdminOperateException e) {
                e.printStackTrace();
            }
        });

        //3. 逻辑集群绑定的物理集群版本一致性校验
        Result<Void> phyClusterVersionsResult = boundPhyClusterVersionsCheck(param);
        if (phyClusterVersionsResult.failed()) {
            return Result.buildFrom(phyClusterVersionsResult);
        }

        //4. 是否要创建逻辑集群
        if (isWorkOrder) {
            param.setDataCenter(EnvUtil.getDC().getCode());
            Result<Long> createLogicClusterResult = clusterLogicService.createClusterLogic(param);
            if (createLogicClusterResult.failed()) {
                return Result.buildFrom(createLogicClusterResult);
            }
            param.setId(createLogicClusterResult.getData());
        }
        //校验项目的合法性
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(ESLogicClusterWithRegionDTO::getProjectId, param,
            param.getProjectId());
        if (result.failed()) {
            return result;
        }

        //5. 初始化物理集群索引服务
        initTemplateSrvOfClusterPhy(param, operator);

        //6. 为逻辑集群绑定region
        return doBindRegionToClusterLogic(param, operator);
    }

    @Override
    public Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionWithNodeInfoByClusterName(String clusterName) {
        List<ClusterRegion> clusterRegions = clusterRegionService.listRegionsByClusterName(clusterName);
        if (CollectionUtils.isEmpty(clusterRegions)) {
            return Result.buildSucc();
        }

        // 构建region中的节点信息
        List<ClusterRegionWithNodeInfoVO> clusterRegionWithNodeInfoVOS = ConvertUtil.list2List(clusterRegions,
            ClusterRegionWithNodeInfoVO.class, region -> region.setClusterName(clusterName));
        for (ClusterRegionWithNodeInfoVO clusterRegionWithNodeInfoVO : clusterRegionWithNodeInfoVOS) {
            Result<List<ClusterRoleHost>> ret = clusterRoleHostService
                .listByRegionId(clusterRegionWithNodeInfoVO.getId().intValue());
            if (ret.success() && CollectionUtils.isNotEmpty(ret.getData())) {
                List<ClusterRoleHost> data = ret.getData();
                List<String> nodeNameList = data.stream().filter(Objects::nonNull).map(ClusterRoleHost::getNodeSet)
                    .distinct().collect(Collectors.toList());
                String nodeNames = ListUtils.strList2String(nodeNameList);
                clusterRegionWithNodeInfoVO.setNodeNames(nodeNames);
            }
        }

        return Result.buildSucc(clusterRegionWithNodeInfoVOS.stream().filter(r -> !AriusObjUtils.isBlank(r.getName()))
            .distinct().collect(Collectors.toList()));
    }

    @Override
    public Result<List<ClusterRegionVO>> listNotEmptyClusterRegionByClusterName(String clusterName) {
        Result<List<ClusterRegionWithNodeInfoVO>> ret = listClusterRegionWithNodeInfoByClusterName(clusterName);
        if (ret.failed()) {
            return Result.buildFrom(ret);
        }

        List<ClusterRegionWithNodeInfoVO> data = ret.getData();
        if (CollectionUtils.isEmpty(data)) {
            return Result.buildSucc();
        }

        // 过滤空region
        List<ClusterRegionVO> validClusterRegionVOList = data.stream()
            .filter(r -> Objects.nonNull(r) && !AriusObjUtils.isBlank(r.getNodeNames())).collect(Collectors.toList());

        return Result.buildSucc(validClusterRegionVOList);
    }

    /**
     * @param regionId
     * @param operator
     * @param projectId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deletePhyClusterRegion(Long regionId, String operator,
                                               Integer projectId) throws AdminOperateException {
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return result;
        }
        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        if (null == region) {
            return Result.buildFail(String.format("region[%s]不存在", regionId));
        }

        Result<Void> deletResult = clusterRegionService.deletePhyClusterRegion(regionId, operator);
        if (deletResult.success()) {
            // 释放region中的节点
            Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(regionId.intValue());
            if (ret.failed()) {
                throw new AdminOperateException(String.format("删除region失败, msg:%s", ret.getMessage()));
            }
            List<ClusterRoleHost> nodeList = ret.getData();
            if (CollectionUtils.isNotEmpty(nodeList)) {
                List<Integer> unBindingNodeIds = nodeList.stream().map(ClusterRoleHost::getId).map(Long::intValue)
                    .collect(Collectors.toList());
                ClusterRegionWithNodeInfoDTO clusterRegionWithNodeInfoDTO = new ClusterRegionWithNodeInfoDTO();
                clusterRegionWithNodeInfoDTO.setId(regionId);
                clusterRegionWithNodeInfoDTO.setUnBindingNodeIds(unBindingNodeIds);
                clusterRegionWithNodeInfoDTO.setPhyClusterName(region.getPhyClusterName());
                clusterRegionWithNodeInfoDTO.setName(region.getName());

                Result<Boolean> editMultiNode2RegionRet = clusterNodeManager
                    .editMultiNode2Region(Lists.newArrayList(clusterRegionWithNodeInfoDTO), operator, projectId);
                if (editMultiNode2RegionRet.failed()) {
                    throw new AdminOperateException(
                        String.format("删除region失败, msg:%s", editMultiNode2RegionRet.getMessage()));
                }
            }

            //CLUSTER_REGION, DELETE, regionId, "", operator
            operateRecordService
                .save(new OperateRecord.Builder().operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE)
                    .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                    .project(projectService.getProjectBriefByProjectId(AuthConstant.SUPER_PROJECT_ID))
                    .content(String.format("cluster:%s,region删除：%s,删除的regionId：%s", region.getPhyClusterName(),
                        region.getName(), regionId))
                    .userOperation(operator).bizId(clusterPhyService.getClusterByName(region.getPhyClusterName()))
                    .build());
        }

        return deletResult;
    }

    @Override
    public Result<Void> unbindRegion(Long regionId, Long logicClusterId, String operator, Integer projectId) {
        //校验操作合法性
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return result;
        }
        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        Result<Void> voidResult = clusterRegionService.unbindRegion(regionId, logicClusterId, operator);
        if (voidResult.success()) {
            // 操作记录
            operateRecordService.save(new OperateRecord.Builder()
                .operationTypeEnum(OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE)
                .triggerWayEnum(TriggerWayEnum.MANUAL_TRIGGER)
                .content(String.format("region解绑:%s", region.getName()))
                .project(projectService.getProjectBriefByProjectId(projectId)).userOperation(operator)
                .bizId(clusterPhyService.getClusterByName(region.getPhyClusterName())).build());
        }

        return voidResult;
    }

    /***************************************** private method ****************************************************/
    /**
     * 对于逻辑集群绑定的物理集群的版本进行一致性校验
     *
     * @param param     逻辑集群Region
     * @return
     */
    private Result<Void> boundPhyClusterVersionsCheck(ESLogicClusterWithRegionDTO param) {
        Set<String> boundPhyClusterVersions = Sets.newHashSet();
        for (ClusterRegionDTO clusterRegionDTO : param.getClusterRegionDTOS()) {
            ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterRegionDTO.getPhyClusterName());
            if (clusterPhy == null) {
                return Result.buildFail("region对应的物理集群信息为空");
            }

            if (clusterPhy.getEsVersion() == null) {
                return Result.buildFail("region对应的物理集群信息对应的版本号不不存在");
            }
            boundPhyClusterVersions.add(clusterPhy.getEsVersion());
        }

        if (boundPhyClusterVersions.size() != 1) {
            return Result.buildFail("逻辑集群绑定的物理集群的版本号应该一致");
        }

        return Result.buildSucc();
    }

    /**
     * 校验region是否可以被逻辑集群绑定
     * @param clusterLogicId         逻辑集群Id
     * @param clusterRegionDTO       region信息
     * @param clusterLogicType       逻辑集群类型
     */
    private void checkCanBeBound(Long clusterLogicId, ClusterRegionDTO clusterRegionDTO,
                                 Integer clusterLogicType) throws AdminOperateException {
        Result<Boolean> validResult = clusterContextManager.canClusterLogicAssociatedPhyCluster(clusterLogicId,
            clusterRegionDTO.getPhyClusterName(), clusterRegionDTO.getId(), clusterLogicType);
        if (validResult.failed()) {
            throw new AdminOperateException(validResult.getMessage(), FAIL);
        }
    }

    private Result<Void> doBindRegionToClusterLogic(ESLogicClusterWithRegionDTO param,
                                                    String operator) throws AdminOperateException {
        List<ClusterRegionDTO> clusterRegionDTOS = param.getClusterRegionDTOS();
        if (CollectionUtils.isEmpty(clusterRegionDTOS)) {
            return Result.buildParamIllegal("region相关参数非法");
        }

        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            Result<Void> bindRegionResult = clusterRegionService.bindRegion(clusterRegionDTO.getId(), param.getId(),
                null, operator);
            if (bindRegionResult.failed()) {
                throw new AdminOperateException(bindRegionResult.getMessage(), FAIL);
            }
        }

        return Result.buildSucc();
    }

    /**
     * 1. 逻辑集群无关联物理集群, 直接清理
     * 2. (共享类型)逻辑集群已关联物理集群, 新关联的物理集群添加逻辑集群已有索引服务
     * @param param             region实体
     * @param operator          操作者
     * @return
     */
    private void initTemplateSrvOfClusterPhy(ESLogicClusterWithRegionDTO param, String operator) {

        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(param.getId());
        if (null == clusterLogicContext) {
            LOGGER.error(
                "class=ClusterRegionManagerImpl||method=initTemplateSrvOfClusterPhy||clusterLogicId={}||errMsg=clusterLogicContext is empty",
                param.getId());
            return;
        }

        List<ClusterRegionDTO> clusterRegionDTOS = param.getClusterRegionDTOS();
        List<String> associatedClusterPhyNames = clusterLogicContext.getAssociatedClusterPhyNames();
        if (CollectionUtils.isEmpty(associatedClusterPhyNames)) {
            clearTemplateSrvOfClusterPhy(param.getId(), associatedClusterPhyNames, clusterRegionDTOS, operator);
        } else {
            addTemplateSrvToNewClusterPhy(param.getId(), associatedClusterPhyNames, clusterRegionDTOS, operator);
        }
    }

    /**
     * (共享类型)逻辑集群已关联物理集群, 新关联的物理集群默开启逻辑集群已有索引服务
     * @param clusterLogicId               逻辑集群ID
     * @param associatedClusterPhyNames    已关联物理集群名称
     * @param clusterRegionDTOS            region信息
     * @param operator                     操作者
     */
    private void addTemplateSrvToNewClusterPhy(Long clusterLogicId, List<String> associatedClusterPhyNames,
                                               List<ClusterRegionDTO> clusterRegionDTOS, String operator) {
        //获取已有逻辑集群索引服务
        List<Integer> clusterTemplateSrvIdList = templateSrvManager
            .getPhyClusterTemplateSrvIds(associatedClusterPhyNames.get(0));

        //更新已有新绑定物理集群中的索引服务
        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            if (associatedClusterPhyNames.contains(clusterRegionDTO.getPhyClusterName())) {
                continue;
            }

            try {
                String phyClusterName = clusterRegionDTO.getPhyClusterName();
                templateSrvManager.replaceTemplateServes(phyClusterName, clusterTemplateSrvIdList, operator);
            } catch (Exception e) {
                LOGGER.error(
                    "class=ClusterRegionManagerImpl||method=addTemplateSrvToNewClusterPhy||clusterLogicId={}||clusterPhy={}||errMsg={}",
                    clusterLogicId, clusterRegionDTO.getPhyClusterName(), e.getMessage());
            }
        }
    }

    /**
     * 逻辑集群无关联物理集群, 清理绑定物理集群索引服务
     * @param clusterLogicId               逻辑集群Id
     * @param associatedClusterPhyNames    已关联物理集群名称
     * @param clusterRegionDTOS             region信息
     * @param operator                      操作者
     */
    private void clearTemplateSrvOfClusterPhy(Long clusterLogicId, List<String> associatedClusterPhyNames,
                                              List<ClusterRegionDTO> clusterRegionDTOS, String operator) {
        for (ClusterRegionDTO clusterRegionDTO : clusterRegionDTOS) {
            if (associatedClusterPhyNames.contains(clusterRegionDTO.getPhyClusterName())) {
                continue;
            }

            try {
                templateSrvManager.delAllTemplateSrvByClusterPhy(clusterRegionDTO.getPhyClusterName(), operator);
            } catch (Exception e) {
                LOGGER.error(
                    "class=ClusterRegionManagerImpl||method=clearTemplateSrvOfClusterPhy||clusterLogicId={}||errMsg={}",
                    clusterLogicId, e.getMessage());
            }
        }
    }
}