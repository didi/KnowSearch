package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.CLUSTER_REGION_UNSUPPORTED_DIVIDE_TYPE;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.EXCLUSIVE;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.PRIVATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.PUBLIC;
import static com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum.UNKNOWN;
import static com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.common.constant.result.ResultType.FAIL;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterRegionWithNodeInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESLogicClusterWithRegionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterRegionWithNodeInfoVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperateTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
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
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClusterRegionManagerImpl implements ClusterRegionManager {

    private static final ILog      LOGGER = LogFactory.getLog(ClusterRegionManagerImpl.class);

    @Autowired
    private ClusterRegionService   clusterRegionService;

 

    @Autowired
    private ClusterLogicService    clusterLogicService;

    @Autowired
    private ClusterPhyService      clusterPhyService;


    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Autowired
    private ClusterNodeManager     clusterNodeManager;

    @Autowired
    private OperateRecordService   operateRecordService;
    @Autowired
    private ProjectService         projectService;

    @Autowired
    private AriusConfigInfoService  ariusConfigInfoService;

    private static final String COLD = "cold";
    private static final Integer LOGIC_ASSOCIATED_PHY_MAX_NUMBER = 2 << 9;
    private static final Integer PHY_ASSOCIATED_LOGIC_MAX_NUMBER = 2 << 9;
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
     * 逻辑集群绑定同一个物理集群的region的时候需要根据类型进行过滤，返回的region不包含cold region
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

        List<ClusterRegion> clusterRegions = clusterRegionService.listPhyClusterRegions(phyCluster).stream().filter(notColdTruePreByClusterRegion)
                                                                 .filter(clusterRegion -> clusterRegionService.isRegionCanBeBound(clusterRegion,clusterLogicType)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(clusterRegions)) {
            return Result.buildFail(String.format("物理集群[%s]无可用region, 请前往物理集群-region划分进行region创建", phyCluster));
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
                                                      boolean isAddClusterLogicFlag) throws AdminOperateException {
        //1. 前置校验
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }
        if (CollectionUtils.isEmpty(param.getClusterRegionDTOS())) {
            return Result.buildParamIllegal("逻辑集群关联region信息为空");
        }
       
        //2. 集群合法关联性校验
        for (int i = 0; i < param.getClusterRegionDTOS().size(); i++) {
            checkCanBeBound(param.getId(), param.getClusterRegionDTOS().get(i), param.getType());
        }
      

        //3. 逻辑集群绑定的物理集群版本一致性校验
        Result<Void> phyClusterVersionsResult = boundPhyClusterVersionsCheck(param);
        if (phyClusterVersionsResult.failed()) {
            return Result.buildFrom(phyClusterVersionsResult);
        }

        //4. 是否要创建逻辑集群
        if (isAddClusterLogicFlag) {
            param.setDataCenter(EnvUtil.getDC().getCode());
            Result<Long> createLogicClusterResult = clusterLogicService.createClusterLogic(param);
            if (createLogicClusterResult.failed()) {
                return Result.buildFrom(createLogicClusterResult);
            }
            param.setId(createLogicClusterResult.getData());
        }
    

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

    /**
     * 获取当前集群支持的所有attribute划分方式
     * @param clusterId 物理集群id
     * @return
     */
    @Override
    public Result<Set<String>> getClusterAttributeDivideType(Long clusterId) {
        // 获取当前集群所有attribute属性集合
        List<ClusterRoleHost> clusterRoleHostList = clusterRoleHostService.getByRoleAndClusterId(clusterId, DATA_NODE.getDesc());
        if(AriusObjUtils.isEmptyList(clusterRoleHostList)){
            return Result.buildSucc();
        }
        List<String> attributesList = clusterRoleHostList.stream().map(ClusterRoleHost::getAttributes)
                .distinct().collect(Collectors.toList());
        Set<String> attributeKeySet = Sets.newHashSet();
        attributesList.forEach((attributes) -> {
            Map<String, String> attributeMap = ConvertUtil.str2Map(attributes);
            attributeKeySet.addAll(attributeMap.keySet());
        });

        if(!attributeKeySet.isEmpty()) {
            // 获取平台不支持的划分方式
            Set<String> unsupportedTypeSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
                    CLUSTER_REGION_UNSUPPORTED_DIVIDE_TYPE, "", ",");
            // 过滤掉平台不支持的划分方式
            Set<String> clusterSupportedTypes = attributeKeySet.stream()
                    .filter(attributeKey -> !unsupportedTypeSet.contains(attributeKey)).collect(Collectors.toSet());
            return Result.buildSucc(clusterSupportedTypes);
        }

        return Result.buildSucc(attributeKeySet);
    }

    /**
     * 根据物理集群名称和划分方式获region信息，包含region中的数据节点信息
     * @param clusterName  物理集群名称
     * @param divideType region划分方式
     * @return
     */
    @Override
    public Result<List<ClusterRegionWithNodeInfoVO>> listClusterRegionInfoWithDivideType(String clusterName, String divideType){
        Result<Void> checkResult = divideTypeCheck(clusterName, divideType);
        if(checkResult.failed()){
            return Result.buildFail(checkResult.getMessage());
        }

        List<ClusterRegion> clusterRegions = clusterRegionService.listRegionsByClusterName(clusterName);
        if (CollectionUtils.isEmpty(clusterRegions)) {
            return Result.buildSucc();
        }

        // 构建region中的节点和attribute信息
        List<ClusterRegionWithNodeInfoVO> clusterRegionWithNodeInfoVOS = ConvertUtil.list2List(clusterRegions,
                ClusterRegionWithNodeInfoVO.class, region -> region.setClusterName(clusterName));

        for (ClusterRegionWithNodeInfoVO clusterRegionWithNodeInfoVO : clusterRegionWithNodeInfoVOS) {
            Result<List<ClusterRoleHost>> ret = clusterRoleHostService
                    .listByRegionId(clusterRegionWithNodeInfoVO.getId().intValue());
            if (ret.success() && CollectionUtils.isNotEmpty(ret.getData())) {
                List<ClusterRoleHost> data = ret.getData();
                // 构建节点信息
                List<String> nodeNameList = data.stream().filter(Objects::nonNull).map(ClusterRoleHost::getNodeSet)
                        .distinct().collect(Collectors.toList());
                String nodeNames = ListUtils.strList2String(nodeNameList);
                clusterRegionWithNodeInfoVO.setNodeNames(nodeNames);
                // 构建attribute属性信息
                Set<String> attributeValueSet = Sets.newHashSet();
                List<String> attributesList = data.stream().filter(Objects::nonNull)
                        .map(ClusterRoleHost::getAttributes).collect(Collectors.toList());
                for (String attributes : attributesList) {
                    Map<String, String> attributeMap = ConvertUtil.str2Map(attributes);
                    attributeValueSet.add(attributeMap.get(divideType));
                }
                String attributeValues = ListUtils.strSet2String(attributeValueSet);
                clusterRegionWithNodeInfoVO.setAttributeValues(attributeValues);
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
                    .editMultiNode2Region(Lists.newArrayList(clusterRegionWithNodeInfoDTO), operator, projectId, OperationEnum.DELETE);
                if (editMultiNode2RegionRet.failed()) {
                    throw new AdminOperateException(
                        String.format("删除region失败, msg:%s", editMultiNode2RegionRet.getMessage()));
                }
            }

            //CLUSTER_REGION, DELETE, regionId, "", operator
            operateRecordService.saveOperateRecordWithManualTrigger(
                    String.format("集群: %s, region 删除：%s, 删除的 regionId：%s", region.getPhyClusterName(),
                            region.getName(), regionId), operator, AuthConstant.SUPER_PROJECT_ID, regionId,
                    OperateTypeEnum.PHYSICAL_CLUSTER_REGION_CHANGE);
        }

        return deletResult;
    }

    
    

    /**
     * @param phyCluster
     * @return
     */
    @Override
    public List<ClusterRegion> getColdRegionByPhyCluster(String phyCluster) {
        List<ClusterRegion> clusterRegions = clusterRegionService.listPhyClusterRegions(phyCluster);
        //冷region是不会保存在逻辑集群侧的，所以这里关联的region肯定是大于1的，如果是小于1，那么是一定不会具备的
        if (clusterRegions.size()<=1){
            return Collections.emptyList();
        }
          return clusterRegions.stream().filter(coldTruePreByClusterRegion).collect(Collectors.toList());
    }
    
    /**
     * @param phyCluster
     * @return
     */
    @Override
    public List<ClusterRegion> listRegionByPhyCluster(String phyCluster) {
        return clusterRegionService.listPhyClusterRegions(phyCluster);
    }
    
    
    
    /**
     * > 通过逻辑集群 id 构建逻辑集群region vo
     *
     * @param logicClusterId 逻辑集群 ID
     * @return 列表<ClusterRegionVO>
     */
    @Override
    public Result<List<ClusterRegionVO>> buildLogicClusterRegionVOByLogicClusterId(Long logicClusterId) {
        final ClusterRegion region = clusterRegionService.getRegionByLogicClusterId(logicClusterId);
    
        return Result.buildSucc(buildLogicClusterRegionVO(Collections.singletonList(region)));
    }
    
    
    /***************************************** private method ****************************************************/
    private Result<Void> divideTypeCheck(String clusterName, String divideType){
        if(divideType.isEmpty()){
            return Result.buildFail("参数有误");
        }
        // 如果当前存在region，则只能使用该region的划分方法
        List<ClusterRegion> clusterRegions = clusterRegionService.listRegionsByClusterName(clusterName);
        ClusterRegion clusterRegion = clusterRegions.stream().findFirst().orElse(null);
        if(clusterRegion != null && !divideType.equals(clusterRegion.getDivideAttributeKey())){
            return Result.buildParamIllegal("当前集群region已存在划分方法，不支持其他划分方法");
        }
        return Result.buildSucc();
    }

    private final static Predicate<ClusterRegion> coldTruePreByClusterRegion = clusterRegion -> {
        if (StringUtils.isBlank(clusterRegion.getConfig())) {
            return Boolean.FALSE;
        }
        try {
            return JSON.parseObject(clusterRegion.getConfig()).getBoolean(COLD);
        
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    
    };

    private final static Predicate<ClusterRegion> notColdTruePreByClusterRegion = clusterRegion -> {
        if (StringUtils.isBlank(clusterRegion.getConfig())) {
            return Boolean.TRUE;
        }
        try {
            return !JSON.parseObject(clusterRegion.getConfig()).getBoolean(COLD);
        } catch (Exception e) {
            return Boolean.TRUE;
        }

    };
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
        Result<Boolean> validResult = canClusterLogicAssociatedPhyCluster(clusterLogicId,
            clusterRegionDTO.getPhyClusterName(), clusterRegionDTO.getId(), clusterLogicType);
        if (validResult.failed()) {
            throw new AdminOperateException(validResult.getMessage(), FAIL);
        }
    }
    public Result<Boolean> canClusterLogicAssociatedPhyCluster(Long clusterLogicId, String clusterPhyName,
                                                               Long regionId, Integer clusterLogicType) {
        //新建时clusterLogicId为空, 防止NPE
        if (AriusObjUtils.isNull(clusterLogicId)) {
            clusterLogicId = -1L;
        }
        /** todo 后续下线
         ClusterLogicContext clusterLogicContext = getClusterLogicContext(clusterLogicId);
         ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
         int associatedPhyNum = 0;
         int associatedLogicNum = 0;
         if (null != clusterLogicContext) {
         associatedPhyNum = clusterLogicContext.getAssociatedPhyNum();
         }
         if (null != clusterPhyContext) {
         associatedLogicNum = clusterPhyContext.getAssociatedLogicNum();
         }**/
        int associatedPhyNum = getAssociatedPhyNumByLogicId(clusterLogicId);
       
        int associatedLogicNum =getAssociatedLogicNumByClusterPhy(clusterPhyName);
       

        return doValid(associatedPhyNum, associatedLogicNum, clusterLogicId, clusterPhyName, regionId,
            clusterLogicType);
    }
    
    private int getAssociatedLogicNumByClusterPhy(String clusterPhyName) {
         // 1. set region
        List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(clusterPhyName);

        // 2. set ClusterLogicInfo
        Set<Long> associatedClusterLogicIds = Sets.newHashSet();
        for (ClusterRegion clusterRegion : regions) {
            // 添加每一个物理集群下每一个region所被绑定的逻辑集群
            List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
            if (!CollectionUtils.isEmpty(logicClusterIds)
                && !logicClusterIds.contains(Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))) {
                associatedClusterLogicIds.addAll(logicClusterIds);
            }
        }
        return associatedClusterLogicIds.size();
    }
    
    private int getAssociatedPhyNumByLogicId(Long clusterLogicId) {
        if (Objects.equals(clusterLogicId,-1L)){
            return 0;
        }
         // 获取逻辑集群已关联的 Region 信息
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
                Collections.singletonList(clusterLogicId));
         // 获取逻辑集群关联 region 下的 rack 节点信息
        List<ClusterRoleHost> associatedRackClusterHosts = Lists.newArrayList();
        for (ClusterRegion region : regions) {
            Result<List<ClusterRoleHost>> regionDataNodeListRet = clusterRoleHostService.listByRegionId(
                    region.getId().intValue());
            if (regionDataNodeListRet.failed()) {
                LOGGER.warn(
                        "class=ClusterContextManagerImpl||method=setRegionAndAssociatedClusterPhyDataNodeInfo||regionId={}||msg=failed to get regionDataNodeList:{}",
                        region.getId(), regionDataNodeListRet.getMessage());
                continue;
            }
            associatedRackClusterHosts.addAll(regionDataNodeListRet.getData());
        }
        
        // 设置数据节点总数
       
        return associatedRackClusterHosts.size();
    }
    
    /**
     * 具体校验逻辑
     * @param associatedPhyNumber    逻辑集群关联物理集群个数
     * @param associatedLogicNumber  物理集群关联逻辑集群个数
     * @param regionId               需要绑定的regionId
     * @param clusterLogicType       逻辑集群类型
     */
    private Result<Boolean> doValid(int associatedPhyNumber, int associatedLogicNumber, Long clusterLogicId,
                                    String clusterPhyName, Long regionId, Integer clusterLogicType) {

        if (AriusObjUtils.isNull(clusterLogicType)) {
            return Result.buildParamIllegal("逻辑集群类型为空");
        }

        if (UNKNOWN.getCode() == ClusterResourceTypeEnum.valueOf(clusterLogicType).getCode()) {
            return Result.buildParamIllegal("无法识别逻辑集群类型");
        }

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (AriusObjUtils.isNull(clusterPhy)) {
            return Result.buildFail("物理集群不存在");
        }

        if (PRIVATE.getCode() == clusterLogicType
            && !canClusterLogicBoundRegion(regionId, clusterPhyName, clusterLogicId)) {
            //先判断logic -> phy, 二次关联region需要先校验逻辑集群对应的物理集群数据是否合理
            if (associatedPhyNumber > 0) {
                return Result.buildParamIllegal(String.format("集群间关联失败 ,该独立逻辑集群%s已有关联物理集群", clusterLogicId));
            }
            //在判断phy -> logic
            if (associatedLogicNumber > 0) {
                return Result.buildFail(String.format("集群间关联失败, 物理集群%s已有关联逻辑集群", clusterPhyName));
            }
        }

        return Result.buildSucc(Boolean.TRUE);
    }
        /**
     * 判断指定的物理集群下的region是否被对应的逻辑集群绑定
     * @param regionId regionId
     * @param clusterPhyName 物理集群名称
     * @param clusterLogicId 逻辑集群id
     * @return  true of false
     */
    private boolean canClusterLogicBoundRegion(Long regionId, String clusterPhyName, Long clusterLogicId) {
        ClusterRegion region = clusterRegionService.getRegionById(regionId);
        ClusterPhyContext clusterPhyContext = getClusterPhyContext(clusterPhyName);
        List<Long> clusterLogicIds = Optional.ofNullable(clusterPhyContext)
                .map(ClusterPhyContext::getAssociatedClusterLogicIds).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(clusterLogicIds) && !clusterLogicIds.contains(clusterLogicId)) {
            return false;
        }

        if (!region.getLogicClusterIds().equals(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID)) {
            return false;
        }

        return region.getPhyClusterName().equals(clusterPhyName);
    }
    private ClusterLogicContext getClusterLogicContext(Long clusterLogicId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicByIdThatNotContainsProjectId(clusterLogicId);
        if (null == clusterLogic) {
            LOGGER.error(
                    "class=ClusterContextManagerImpl||method=flushClusterLogicContext||clusterLogicId={}||msg=clusterLogic is empty",
                    clusterLogicId);
            return null;
        }
        
        ClusterLogicContext build = buildInitESClusterLogicContextByType(clusterLogic);
        setAssociatedClusterPhyInfo(build);
        setRegionAndAssociatedClusterPhyDataNodeInfo(build);
        return build;
    }
    /**
     *   定义规则:
     *   1. Type为独立, LP = 1, PL = 1
     *   2. Type为共享, LP = n, PL = 1 ,  1 <= n <= 1024   多个逻辑集群共享一个物理集群, 每个逻辑集群关联物理集群一部分region,不可跨其他物理集群，
     *                                                    多个逻辑集群可关联同一部分region。
     *   3. Type为独占, LP = 1, PL = n ,  1 <= n <= 1024   一个逻辑集群独占一个或者多个物理集群
     */
    private ClusterLogicContext buildInitESClusterLogicContextByType(ClusterLogic clusterLogic) {

        if (PRIVATE.getCode() == clusterLogic.getType() || EXCLUSIVE.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType()).associatedPhyNumMax(1)
                .build();
        } else if (PUBLIC.getCode() == clusterLogic.getType()) {
            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType())
                .associatedPhyNumMax(LOGIC_ASSOCIATED_PHY_MAX_NUMBER).build();
        } else {
            LOGGER.error(
                "class=ClusterContextManagerImpl||method=buildInitESClusterLogicContextByType||esClusterLogicId={}||msg={}",
                clusterLogic.getId(), String.format("请确认逻辑集群%s类型是否存在", clusterLogic.getType()));

            return ClusterLogicContext.builder().clusterLogicName(clusterLogic.getName())
                .clusterLogicId(clusterLogic.getId()).logicClusterType(clusterLogic.getType()).associatedPhyNumMax(-1)
                .build();
        }
    }
    
    private void setAssociatedClusterPhyInfo(ClusterLogicContext build) {
        List<String> clusterPhyNames = clusterRegionService.listPhysicClusterNames(build.getClusterLogicId());
        if (build.getAssociatedPhyNumMax() < clusterPhyNames.size()) {
            LOGGER.error("class=ClusterContextManagerImpl||method=setAssociatedClusterPhyInfo"
                         + "||logicClusterType={}||esClusterLogicId={}||msg= 集群间关联超过最大限制数 {}, 请纠正",
                    build.getLogicClusterType(), build.getClusterLogicId(), build.getAssociatedPhyNumMax());
            return;
        }
        
        build.setAssociatedClusterPhyNames(clusterPhyNames);
        build.setAssociatedPhyNum(clusterPhyNames.size());
    }
    
    private void setRegionAndAssociatedClusterPhyDataNodeInfo(ClusterLogicContext build) {
        // 获取逻辑集群已关联的 Region 信息
        List<ClusterRegion> regions = clusterRegionService.getClusterRegionsByLogicIds(
                Collections.singletonList(build.getClusterLogicId()));
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));
        
        // 获取逻辑集群关联 region 下的 rack 节点信息
        List<ClusterRoleHost> associatedRackClusterHosts = Lists.newArrayList();
        for (ClusterRegion region : regions) {
            Result<List<ClusterRoleHost>> regionDataNodeListRet = clusterRoleHostService.listByRegionId(
                    region.getId().intValue());
            if (regionDataNodeListRet.failed()) {
                LOGGER.warn(
                        "class=ClusterContextManagerImpl||method=setRegionAndAssociatedClusterPhyDataNodeInfo||regionId={}||msg=failed to get regionDataNodeList:{}",
                        region.getId(), regionDataNodeListRet.getMessage());
                continue;
            }
            associatedRackClusterHosts.addAll(regionDataNodeListRet.getData());
        }
        
        // 设置数据节点总数
        build.setAssociatedDataNodeNum(associatedRackClusterHosts.size());
        
        // 设置数据节点 Ip 地址
        build.setAssociatedDataNodeIps(
                associatedRackClusterHosts.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
    }
    
    private ClusterPhyContext getClusterPhyContext(String clusterPhyName) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterPhyName);
        if (null == clusterPhy) {
            LOGGER.error(
                    "class=ClusterContextManagerImpl||method=flushClusterPhyContext||clusterPhyName={}||msg=clusterPhy is empty",
                    clusterPhyName);
            return null;
        }
        
        ClusterPhyContext build = ClusterPhyContext.builder().clusterPhyId(clusterPhy.getId().longValue())
                .clusterName(clusterPhy.getCluster()).associatedLogicNumMax(PHY_ASSOCIATED_LOGIC_MAX_NUMBER).build();
        
        setClusterPhyNodeInfo(build);
        setRegionAndClusterLogicInfoAndProjectId(build);
        return build;
    }
     private void setRegionAndClusterLogicInfoAndProjectId(ClusterPhyContext build) {
        // 1. set region
        List<ClusterRegion> regions = clusterRegionService.listPhyClusterRegions(build.getClusterName());
        build.setAssociatedRegionIds(regions.stream().map(ClusterRegion::getId).collect(Collectors.toList()));

        // 2. set ClusterLogicInfo
        Set<Long> associatedClusterLogicIds = Sets.newHashSet();
        for (ClusterRegion clusterRegion : regions) {
            // 添加每一个物理集群下每一个region所被绑定的逻辑集群
            List<Long> logicClusterIds = ListUtils.string2LongList(clusterRegion.getLogicClusterIds());
            if (!CollectionUtils.isEmpty(logicClusterIds)
                && !logicClusterIds.contains(Long.parseLong(AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID))) {
                associatedClusterLogicIds.addAll(logicClusterIds);
            }
        }

        build.setAssociatedClusterLogicIds(Lists.newArrayList(associatedClusterLogicIds));
        build.setAssociatedLogicNum(associatedClusterLogicIds.size());

        // 3. set projectId
        Set<Integer> projectIdSet = new HashSet<>();
        Set<String> clusterLogicSet = new HashSet<>();
        if (!CollectionUtils.isEmpty(associatedClusterLogicIds)) {
            for (Long associatedClusterLogicId : associatedClusterLogicIds) {
                clusterLogicService.listClusterLogicByIdThatProjectIdStrConvertProjectIdList(associatedClusterLogicId).stream().filter(Objects::nonNull)
                        .filter(clusterLogic -> null != clusterLogic.getProjectId() && null != clusterLogic.getName())
                        .forEach(clusterLogic -> {
                            projectIdSet.add(clusterLogic.getProjectId());
                            clusterLogicSet.add(clusterLogic.getName());
                        });
                
            }
        }

        build.setAssociatedProjectIds(Lists.newArrayList(projectIdSet));
        build.setAssociatedProjectNames(Lists.newArrayList(clusterLogicSet));
    }
    private void setClusterPhyNodeInfo(ClusterPhyContext build) {
        List<ClusterRoleHost> nodes = clusterRoleHostService.getNodesByCluster(build.getClusterName());
        List<ClusterRoleHost> dataNodes = nodes.stream().filter(r -> DATA_NODE.getCode() == r.getRole())
            .collect(Collectors.toList());

        build.setAssociatedDataNodeNum(dataNodes.size());
        build.setAssociatedDataNodeIps(dataNodes.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
        build.setAssociatedNodeIps(nodes.stream().map(ClusterRoleHost::getIp).collect(Collectors.toList()));
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






}