package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.MASTER_NODE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum.ONLINE;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum.ES_HOST;
import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicTypeEnum.PRIVATE;
import static com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum.CN;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.DEFAULT_CLUSTER_IDC;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.DEFAULT_CLUSTER_NODE_SPEC;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.DEFAULT_CLUSTER_PAID_COUNT;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.DEFAULT_CLUSTER_TEMPLATE_SRVS;
import static com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant.MASTER_NODE_MIN_NUMBER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterNodeManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplatePhyManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.pipeline.TemplatePipelineManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESLogicClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterPhyRegionInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESClusterTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterHostVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ESRoleClusterVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppLogicClusterAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterTypeEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ResourceLogicLevelEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterStatis;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ESClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUser;
import com.didichuxing.datachannel.arius.admin.common.event.resource.ClusterPhyEvent;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ClusterUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.common.util.RackUtils;
import com.didichuxing.datachannel.arius.admin.core.component.CacheSwitch;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicClusterAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ESClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ESRegionRackService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.ESClusterStatisService;
import com.didichuxing.datachannel.arius.admin.persistence.component.ESGatewayClient;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import sun.net.util.IPAddressUtil;

@Component
public class ClusterPhyManagerImpl implements ClusterPhyManager {

    private static final ILog          LOGGER                 = LogFactory.getLog(ClusterPhyManagerImpl.class);

    @Autowired
    private ESTemplateService          esTemplateService;

    @Autowired
    private ESClusterPhyService        esClusterPhyService;

    @Autowired
    private ESClusterLogicService      esClusterLogicService;

    @Autowired
    private ESRoleClusterService       esRoleClusterService;

    @Autowired
    private ESRoleClusterHostService   esRoleClusterHostService;

    @Autowired
    private ESClusterStatisService     esClusterStatisService;

    @Autowired
    private TemplatePhyService         templatePhyService;

    @Autowired
    private TemplateSrvManager         templateSrvManager;

    @Autowired
    private TemplatePhyMappingManager  templatePhyMappingManager;

    @Autowired
    private TemplatePipelineManager    templatePipelineManager;

    @Autowired
    private TemplateLogicService       templateLogicService;

    @Autowired
    private TemplatePhyManager         templatePhyManager;

    @Autowired
    private ESRegionRackService        esRegionRackService;

    @Autowired
    private AppLogicClusterAuthService appLogicClusterAuthService;

    @Autowired
    private ESGatewayClient            esGatewayClient;

    @Autowired
    private ClusterNodeManager         clusterNodeManager;

    @Autowired
    private ClusterContextManager      clusterContextManager;

    @Autowired
    private AppService                 appService;

    @Autowired
    private CacheSwitch                cacheSwitch;

    @Autowired
    private OperateRecordService       operateRecordService;

    private final static FutureUtil                        futureUtil           = FutureUtil.init("PhyClusterManager");

    private final Cache<Integer, List<ConsoleClusterPhyVO>> consoleClusterPhyVOSCache = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();

    @Override
    public boolean copyMapping(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = templatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info("class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||msg=copyMapping no template",
                    cluster);
            return true;
        }

        int succeedCount = 0;
        // 遍历物理模板，copy mapping
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 获取物理模板对应的逻辑模板
                IndexTemplateLogic templateLogic = templateLogicService.getLogicTemplateById(physical.getLogicId());
                // 同步索引的mapping到模板
                Result result = templatePhyMappingManager.syncMappingConfig(cluster, physical.getName(),
                    physical.getExpression(), templateLogic.getDateFormat());

                if (result.success()) {
                    succeedCount++;
                    if (!setTemplateSettingSingleType(cluster, physical.getName())) {
                        LOGGER.error(
                            "class=ESClusterPhyServiceImpl||method=copyMapping||errMsg=failedUpdateSingleType||cluster={}||template={}",
                            cluster, physical.getName());
                    }
                } else {
                    LOGGER.warn(
                        "class=ESClusterPhyServiceImpl||method=copyMapping||cluster={}||template={}||msg=copyMapping fail",
                        cluster, physical.getName());
                }
            } catch (Exception e) {
                LOGGER.error("class=ESClusterPhyServiceImpl||method=copyMapping||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }

        return succeedCount * 1.0 / physicals.size() > 0.7;
    }

    /**
     * 同步元数据
     * @param cluster    集群名称
     * @param retryCount 重试次数
     * @return
     */
    @Override
    public boolean syncTemplateMetaData(String cluster, int retryCount) {
        // 获取物理集群下的所有物理模板
        List<IndexTemplatePhy> physicals = templatePhyService.getNormalTemplateByCluster(cluster);
        if (CollectionUtils.isEmpty(physicals)) {
            LOGGER.info(
                "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||cluster={}||msg=syncTemplateMetaData no template",
                cluster);
            return true;
        }

        // 遍历物理模板
        for (IndexTemplatePhy physical : physicals) {
            try {
                // 同步模板元数据到ES集群（修改ES集群中的模板）
                templatePhyManager.syncMeta(physical.getId(), retryCount);
                // 同步最新元数据到ES集群pipeline
                templatePipelineManager.syncPipeline(physical,
                    templateLogicService.getLogicTemplateWithPhysicalsById(physical.getLogicId()));
            } catch (Exception e) {
                LOGGER.error(
                    "class=ESClusterPhyServiceImpl||method=syncTemplateMetaData||errMsg={}||cluster={}||template={}",
                    e.getMessage(), cluster, physical.getName(), e);
            }
        }

        return true;
    }

    /**
     * 物理集群资源使用率
     * @param cluster 物理集群
     * @return
     */
    @Override
    public ESClusterStatis getClusterResourceUsage(String cluster) {
        return getPhyClusterStatus(cluster);
    }

    /**
     * 获取物理集群状态信息
     * @param cluster 集群名称
     * @return
     */
    @Override
    public ESClusterStatis getClusterStatus(String cluster) {
        return getPhyClusterStatus(cluster);
    }

    /**
     * 根据指定物理集群和Racks获取对应状态信息
     * @param cluster 物理集群
     * @return
     */
    @Override
    public ESClusterStatis getPhyClusterStatus(String cluster) {
        return ConvertUtil.obj2Obj(esClusterStatisService.getPhyClusterStatisticsInfo(cluster), ESClusterStatis.class);
    }

    /**
     * 集群是否存在
     * @param clusterName 集群名字
     * @return true 存在
     */
    @Override
    public boolean isClusterExists(String clusterName) {
        return esClusterPhyService.isClusterExists(clusterName);
    }

    /**
     * 释放racks
     * @param cluster    集群名称
     * @param racks      要释放的racks，逗号分隔
     * @param retryCount 重试次数
     * @return result
     */
    @Override
    public Result releaseRacks(String cluster, String racks, int retryCount) {
        if (!isClusterExists(cluster)) {
            return Result.buildNotExist("集群不存在");
        }

        Set<String> racksToRelease = Sets.newHashSet(racks.split(AdminConstant.RACK_COMMA));

        // 获取分配到要释放的rack上的物理模板
        List<IndexTemplatePhy> templatePhysicals = templatePhyService.getNormalTemplateByClusterAndRack(cluster,
            racksToRelease);

        // 没有模板被分配在要释放的rack上
        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildSucc();
        }

        List<String> errMsgList = Lists.newArrayList();
        // 遍历模板，修改模板的rack设置
        for (IndexTemplatePhy templatePhysical : templatePhysicals) {
            // 去掉要释放的rack后的剩余racks
            String tgtRack = RackUtils.removeRacks(templatePhysical.getRack(), racksToRelease);

            LOGGER.info("method=releaseRack||template={}||srcRack={}||tgtRack={}", templatePhysical.getName(),
                templatePhysical.getRack(), tgtRack);

            try {
                // 修改模板
                Result result = templatePhyManager.editTemplateRackWithoutCheck(templatePhysical.getId(), tgtRack,
                    AriusUser.SYSTEM.getDesc(), retryCount);

                if (result.failed()) {
                    errMsgList.add(templatePhysical.getName() + "失败：" + result.getMessage() + ";");
                }

            } catch (Exception e) {
                errMsgList.add(templatePhysical.getName() + "失败：" + e.getMessage() + ";");
                LOGGER.warn("method=releaseRack||template={}||srcRack={}||tgtRack={}||errMsg={}",
                    templatePhysical.getName(), templatePhysical.getRack(), tgtRack, e.getMessage(), e);
            }
        }

        if (CollectionUtils.isEmpty(errMsgList)) {
            return Result.buildSucc();
        }

        return Result.buildFail(String.join(",", errMsgList));
    }

    @Override
    public List<ConsoleClusterPhyVO> getConsoleClusterPhyVOS(ESClusterDTO param, Integer currentAppId) {

        List<ESClusterPhy> esClusterPhies = esClusterPhyService.listClustersByCondt(param);

        if (cacheSwitch.clusterPhyCacheEnable()) {
            try {
                return consoleClusterPhyVOSCache.get(currentAppId,
                    () -> buildConsoleClusterPhy(esClusterPhies, currentAppId));
            } catch (ExecutionException e) {
                return buildConsoleClusterPhy(esClusterPhies, currentAppId);
            }
        }

        return buildConsoleClusterPhy(esClusterPhies, currentAppId);
    }

    @Override
    public ConsoleClusterPhyVO getConsoleClusterPhyVO(Integer clusterId, Integer currentAppId) {
        if (AriusObjUtils.isNull(clusterId)) {
            return null;
        }

        List<ConsoleClusterPhyVO> consoleClusterPhyVOS = consoleClusterPhyVOSCache.getIfPresent(currentAppId);

        if (CollectionUtils.isEmpty(consoleClusterPhyVOS)) {
            consoleClusterPhyVOS = getConsoleClusterPhyVOS(null, currentAppId);
        }
        
        return  Objects.requireNonNull(consoleClusterPhyVOS)
                .stream()
                .filter(r -> clusterId.equals(r.getId()))
                .findAny()
                .get();
    }

    @Override
    public Result<List<String>> listCanBeAssociatedClustersPhys(Integer clusterLogicType) {
        return Result.buildSucc(clusterContextManager.getCanBeAssociatedClustersPhys(clusterLogicType));
    }

    @Override
    public Result<List<ESClusterPhyRegionInfoVO>> getClusterPhyRegionInfos(Integer clusterId) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(esClusterPhy)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }

        List<ESRoleClusterHostVO> esRoleClusterHosts = clusterNodeManager
            .convertClusterNodes(esRoleClusterHostService.getNodesByCluster(esClusterPhy.getCluster()));

        return Result.buildSucc(buildESClusterPhyRegions(esRoleClusterHosts));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result clusterJoin(ESClusterJoinDTO param, String operator) {
        try {
            Result checkResult = validCheckForClusterJoin(param, operator);
            if (checkResult.failed()) {
                return checkResult;
            }

            return doClusterJoin(param, operator);
        } catch (Exception e) {
            LOGGER.error("method=clusterJoin||logicCluster{}||clusterPhy={}||errMsg={}", param.getLogicCluster(),
                param.getCluster(), e.getStackTrace());
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail("操作失败, 请联系管理员");
        }
    }

    @Override
	public Result checkValidForClusterNodes(List<String> ips) {
        List<String> invalidIps = Lists.newArrayList();
        for (String ipStr : ips) {
            boolean iPv4LiteralAddress = IPAddressUtil.isIPv4LiteralAddress(ipStr);
            boolean iPv6LiteralAddress = IPAddressUtil.isIPv6LiteralAddress(ipStr);
            if (!(iPv4LiteralAddress || iPv6LiteralAddress)) {
                invalidIps.add(ipStr);
            }
        }

        if (invalidIps.size() > 0) {
            return Result.buildFail(String.format("ips不合法:%s", ListUtils.strList2String(invalidIps)));
        }

        return Result.buildSucc();
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteClusterJoin(Integer clusterId, String operator) {
        ESClusterPhy esClusterPhy = esClusterPhyService.getClusterById(clusterId);
        if (AriusObjUtils.isNull(esClusterPhy)) {
            return Result.buildParamIllegal("物理集群不存在");
        }

        try {
            doDeleteClusterJoin(esClusterPhy, operator);
        } catch (AdminOperateException e) {
            LOGGER.error("class=ClusterPhyManagerImpl||method=deleteClusterJoin||errMsg={}||e={}||clusterId={}",
                e.getMessage(), e, clusterId);
            // 这里必须显示事务回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }

        return Result.buildSucc();
    }
    /**************************************** private method ***************************************************/
    /**
     * 更新物理模板setting single_type为true
     * @param cluster  集群
     * @param template 物理模板
     * @return
     */
    private boolean setTemplateSettingSingleType(String cluster, String template) {
        Map<String, String> setting = new HashMap<>();
        setting.put(AdminConstant.SINGLE_TYPE_KEY, AdminConstant.DEFAULT_SINGLE_TYPE);
        try {
            return esTemplateService.syncUpsertSetting(cluster, template, setting, 3);
        } catch (ESOperateException e) {
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=setTemplateSettingSingleType||errMsg={}||e={}||cluster={}||template={}",
                e.getMessage(), e, cluster, template);
        }

        return false;
    }

    /**
     * 构建物理集群详情
     * @param phyClusters 物理集群元数据信息
     * @param currentAppId 当前登录项目
     */
    private List<ConsoleClusterPhyVO> buildConsoleClusterPhy(List<ESClusterPhy> phyClusters, Integer currentAppId) {

        List<ConsoleClusterPhyVO> consoleClusterPhyVOS = ConvertUtil.list2List(phyClusters, ConsoleClusterPhyVO.class);

        consoleClusterPhyVOS.forEach(consoleClusterPhyVO -> buildPhyCluster(consoleClusterPhyVO, currentAppId));

        Collections.sort(consoleClusterPhyVOS);

        return consoleClusterPhyVOS;
    }

    /**
     * 构建物理集群详情
     * @param consoleClusterPhyVO 物理集群元数据信息
     * @return
     */
    private void buildPhyCluster(ConsoleClusterPhyVO consoleClusterPhyVO, Integer currentAppId) {

        if (!AriusObjUtils.isNull(consoleClusterPhyVO)) {
            futureUtil.runnableTask(() -> buildPhyClusterStatus(consoleClusterPhyVO))
                      .runnableTask(() -> buildPhyClusterResourceUsage(consoleClusterPhyVO))
                      .runnableTask(() -> buildPhyClusterTemplateSrv(consoleClusterPhyVO))
                      .runnableTask(() -> buildClusterRole(consoleClusterPhyVO))
                      .waitExecute();

            buildWithOtherInfo(consoleClusterPhyVO, currentAppId);
        }
    }

    private void buildClusterRole(ConsoleClusterPhyVO cluster) {
        try {
            List<ESRoleCluster> roleClusters = esRoleClusterService.getAllRoleClusterByClusterId(cluster.getId());
            List<ESRoleClusterVO> roleClusterVOS = ConvertUtil.list2List(roleClusters, ESRoleClusterVO.class);

            for (ESRoleClusterVO esRoleClusterVO : roleClusterVOS) {
                List<ESRoleClusterHost> roleClusterHosts = esRoleClusterHostService
                    .getByRoleClusterId(esRoleClusterVO.getId());
                List<ESRoleClusterHostVO> esRoleClusterHostVOS = ConvertUtil.list2List(roleClusterHosts,
                    ESRoleClusterHostVO.class);
                esRoleClusterVO.setEsRoleClusterHostVO(esRoleClusterHostVOS);
            }

            cluster.setEsRoleClusterVOS(roleClusterVOS);
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildClusterRole||logicClusterId={}", cluster.getId(), e);
        }
    }

    private void buildPhyClusterStatus(ConsoleClusterPhyVO cluster) {
        try {
            ESClusterStatis phyStatus = getClusterStatus(cluster.getCluster());
            if (phyStatus != null) {
                cluster.setClusterStatus(phyStatus.getStatus());
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterStatus||logicClusterId={}", cluster.getId(), e);
        }
    }

    private void buildPhyClusterResourceUsage(ConsoleClusterPhyVO cluster) {
        try {
            ESClusterStatis resourceUsage = getClusterResourceUsage(cluster.getCluster());
            if (resourceUsage != null) {
                if (resourceUsage.getTotalDisk() > 0) {
                    cluster.setDiskUsage(resourceUsage.getUsedDisk() / resourceUsage.getTotalDisk());
                } else {
                    cluster.setDiskUsage(0.0);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterResourceUsage||logicClusterId={}",
                cluster.getId(), e);
        }
    }

    private void buildPhyClusterTemplateSrv(ConsoleClusterPhyVO cluster) {
        try {
            Result<List<ESClusterTemplateSrv>> listResult = templateSrvManager
                .getPhyClusterTemplateSrv(cluster.getCluster());
            if (null != listResult && listResult.success()) {
                cluster.setEsClusterTemplateSrvVOS(
                    ConvertUtil.list2List(listResult.getData(), ESClusterTemplateSrvVO.class));
            }
        } catch (Exception e) {
            LOGGER.warn("class=ClusterPhyManagerImpl||method=buildPhyClusterTemplateSrv||logicClusterId={}",
                cluster.getId(), e);
        }
    }

    /**
     * 1. 获取gateway地址
     * 2. 关联App的权限信息
     * 3. 物理集群责任人
     */
    private void buildWithOtherInfo(ConsoleClusterPhyVO cluster, Integer currentAppId) {
        cluster.setGatewayAddress(esGatewayClient.getGatewayAddress());

        ESClusterPhyContext esClusterPhyContext = clusterContextManager.getESClusterPhyContext(cluster.getCluster());
        List<Long> clusterLogicIds = Lists.newArrayList();
        if (!AriusObjUtils.isNull(esClusterPhyContext) && !AriusObjUtils.isNull(esClusterPhyContext.getAssociatedClusterLogicIds())) {
            clusterLogicIds  = Lists.newArrayList(esClusterPhyContext.getAssociatedClusterLogicIds());
        }

        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(clusterLogicIds.get(0));
        if (AriusObjUtils.isNull(esClusterLogic)) {
            LOGGER.warn(
                "class=ClusterPhyManagerImpl||method=buildWithOtherInfo||clusterName={}||msg=the associated logical cluster is empty",
                cluster.getCluster());
            return;
        }
        
        //TODO:  一个物理集群对应多个逻辑集群的情况该归属哪个appId
        cluster.setBelongAppId(esClusterLogic.getAppId());

        cluster.setResponsible(esClusterLogic.getResponsible());

        App app = appService.getAppById(esClusterLogic.getAppId());
        if (!AriusObjUtils.isNull(app)) {
            cluster.setBelongAppName(app.getName());
        }

        //TODO: auth table中 加type字段标识是逻辑集群还是物理集群
        AppLogicClusterAuthEnum logicClusterAuthEnum = appLogicClusterAuthService
            .getLogicClusterAuthEnum(currentAppId, clusterLogicIds.get(0));
        cluster.setCurrentAppAuth(logicClusterAuthEnum.getCode());
    }

    private List<ESClusterPhyRegionInfoVO> buildESClusterPhyRegions(List<ESRoleClusterHostVO> esRoleClusterHostVOS) {

        List<ESClusterPhyRegionInfoVO> esClusterPhyRegions = Lists.newArrayList();

        List<ClusterRegion> regions = Lists.newArrayList();
        for (ESRoleClusterHostVO esRoleClusterHostVO : esRoleClusterHostVOS) {
            regions = esRegionRackService.listPhyClusterRegions(esRoleClusterHostVO.getCluster());
            if (CollectionUtils.isNotEmpty(regions)) {
                break;
            }
        }
        
        Map<String, Long> rack2regionIdMap = Maps.newHashMap();
        for (ClusterRegion region : regions) {
            String racksStr = region.getRacks();
            List<String> racks = ListUtils.string2StrList(racksStr);
            racks.forEach(r -> rack2regionIdMap.put(r, region.getId()));
        }

        for (ESRoleClusterHostVO esRoleClusterHostVO : esRoleClusterHostVOS) {
            ESClusterPhyRegionInfoVO esClusterPhyRegionInfoVO = ConvertUtil.obj2Obj(esRoleClusterHostVO,
                ESClusterPhyRegionInfoVO.class);

            //设置节点所在regionId
            Long regionId = -1L;
            if (esRoleClusterHostVO.getRole() == DATA_NODE.getCode()) {
                String rack = esRoleClusterHostVO.getRack();
                if (!AriusObjUtils.isNull(rack)) {
                    regionId = rack2regionIdMap.get(rack);
                    esClusterPhyRegionInfoVO.setRegionId(regionId);
                }
            }

            //设置region绑定的逻辑集群名称
            ClusterRegion region = esRegionRackService.getRegionById(regionId);
            if (!AriusObjUtils.isNull(region)) {
                ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterById(region.getLogicClusterId());
                if (!AriusObjUtils.isNull(esClusterLogic)) {
                    esClusterPhyRegionInfoVO.setLogicClusterName(esClusterLogic.getName());
                }
            }

            esClusterPhyRegions.add(esClusterPhyRegionInfoVO);
        }

        return esClusterPhyRegions;
    }

    private Result doClusterJoin(ESClusterJoinDTO param, String operator) {
        // 1.保存物理集群信息(集群、角色、节点)
        Result saveClusterResult = saveClusterPhy(param, operator);
        if (saveClusterResult.failed()) {
            return saveClusterResult;
        }

        // 2.创建region信息
        List<Long> regionIds = new ArrayList<>();
        for (String racks : param.getRegionRacks()) {
            if (StringUtils.isBlank(racks)) {
                continue;
            }
            Result<Long> result = esRegionRackService.createPhyClusterRegion(param.getCluster(), racks, null,
                    operator);
            if (result.success()) {
                regionIds.add(result.getData());
            }
        }

        // 3.保存逻辑集群信息
        Result<Long> saveClusterLogicResult = saveClusterLogic(param, operator);
        if (saveClusterLogicResult.failed()) {
            return saveClusterLogicResult;
        }

        // 4.绑定Region
        Long clusterLogicId = saveClusterLogicResult.getData();
        regionIds.forEach(regionId -> esRegionRackService.bindRegion(regionId, clusterLogicId, null, operator));

        // 刷新集群上下文
        SpringTool.publish(new ClusterPhyEvent(this));

        operateRecordService.save(ModuleEnum.ES_CLUSTER_JOIN, OperationEnum.ADD, param.getCluster(),
                param.getPhyClusterDesc(), operator);

        return Result.buildSucc();
    }

    private Result saveClusterPhy(ESClusterJoinDTO param, String operator) {

        Map<Integer, List<ESRoleClusterHostDTO>> hostMap = param
                    .getRoleClusterHosts()
                    .stream()
                    .collect(Collectors.groupingBy(ESRoleClusterHostDTO::getRole, Collectors.toList()));

        ESClusterDTO clusterDTO = buildClusterPhy(param, operator);
        Result addClusterRet = esClusterPhyService.createCluster(clusterDTO, operator);
        if (addClusterRet.failed()){
            return addClusterRet;
        }

        for (Map.Entry<Integer, List<ESRoleClusterHostDTO>> entry : hostMap.entrySet()) {
            // 保存集群角色信息
            ESRoleClusterDTO esRoleClusterDTO = buildESRoleCluster(entry, param, clusterDTO.getId());
            esRoleClusterService.save(esRoleClusterDTO);

            // 保存节点信息
            for (ESRoleClusterHostDTO hostDTO : entry.getValue()) {
                ESRoleClusterHost esRoleClusterHost = ConvertUtil.obj2Obj(hostDTO, ESRoleClusterHost.class);
                esRoleClusterHost.setRoleClusterId(esRoleClusterDTO.getId());
                //主机之前是通过ecm接口拿到status信息后拿到hostName 这里先用ip
                esRoleClusterHost.setHostname(hostDTO.getIp());
                esRoleClusterHost.setCluster(clusterDTO.getCluster());
                esRoleClusterHost.setRack(hostDTO.getRack());
                esRoleClusterHost.setStatus(ONLINE.getCode());
                esRoleClusterHost.setNodeSet("");
                esRoleClusterHostService.save(esRoleClusterHost);
            }
        }
        
        return Result.buildSucc();
    }

    private ESRoleClusterDTO buildESRoleCluster(Entry<Integer, List<ESRoleClusterHostDTO>> entry, ESClusterJoinDTO param,Integer clusterId) {
        ESRoleClusterDTO esRoleClusterDTO = new ESRoleClusterDTO();
        ESClusterNodeRoleEnum roleEnum = ESClusterNodeRoleEnum.valueOf(entry.getKey());
        esRoleClusterDTO.setRole(roleEnum.getDesc());
        esRoleClusterDTO.setElasticClusterId(clusterId.longValue());
        esRoleClusterDTO.setRoleClusterName(param.getCluster() + "-" + roleEnum.getDesc());
        esRoleClusterDTO.setEsVersion(param.getEsVersion());
        esRoleClusterDTO.setPidCount(DEFAULT_CLUSTER_PAID_COUNT);
        esRoleClusterDTO.setPodNumber(entry.getValue().size());
        esRoleClusterDTO.setCfgId(-1);
        esRoleClusterDTO.setMachineSpec(DEFAULT_CLUSTER_NODE_SPEC);
        return esRoleClusterDTO;
    }

    private ESClusterDTO buildClusterPhy(ESClusterJoinDTO param,String operator) {
        ESClusterDTO clusterDTO = ConvertUtil.obj2Obj(param, ESClusterDTO.class);
        List<ESRoleClusterHostDTO> clientNotes = param.getRoleClusterHosts()
                .stream()
                .filter(r -> CLIENT_NODE.getCode() == r.getRole())
                .collect(Collectors.toList());

        String clientAddress = getClientAddress(clientNotes);

        clusterDTO.setDesc(param.getPhyClusterDesc());
        clusterDTO.setDataCenter(CN.getCode());
        clusterDTO.setHttpAddress(clientAddress);
        clusterDTO.setHttpWriteAddress(clientAddress);
        clusterDTO.setTemplateSrvs(DEFAULT_CLUSTER_TEMPLATE_SRVS);
        clusterDTO.setIdc(DEFAULT_CLUSTER_IDC);
        clusterDTO.setLevel(ResourceLogicLevelEnum.NORMAL.getCode());
        clusterDTO.setImageName("");
        clusterDTO.setPackageId(-1L);
        clusterDTO.setNsTree("");
        clusterDTO.setPlugIds("");
        clusterDTO.setCreator(operator);
        return clusterDTO;
    }

    private Result<Long> saveClusterLogic(ESClusterJoinDTO param, String operator) {
        ESLogicClusterDTO esLogicClusterDTO = new ESLogicClusterDTO();
        esLogicClusterDTO.setAppId(param.getAppId());
        esLogicClusterDTO.setResponsible(param.getResponsible());
        esLogicClusterDTO.setName(param.getLogicCluster());
        esLogicClusterDTO.setDataCenter(CN.getCode());
        esLogicClusterDTO.setType(PRIVATE.getCode());

        Long dataNodeNumber = param.getRoleClusterHosts()
                .stream()
                .filter(hosts -> DATA_NODE.getCode() == hosts.getRole())
                .count();

        esLogicClusterDTO.setDataNodeNu(dataNodeNumber.intValue());
        esLogicClusterDTO.setLibraDepartmentId("");
        esLogicClusterDTO.setLibraDepartment("");
        esLogicClusterDTO.setMemo(param.getPhyClusterDesc());

        Result<Long> result = esClusterLogicService.createLogicCluster(esLogicClusterDTO, operator);
        if (result.failed()) {
            return Result.buildFail("逻辑集群创建失败");
        }

        return result;
    }

    private String getClientAddress(List<ESRoleClusterHostDTO> hostDTOList){
        if (CollectionUtils.isEmpty(hostDTOList)){
            return "";
        }

        List<String> clientAddresses = Lists.newArrayList();
        hostDTOList.forEach(host -> clientAddresses.add(host.getIp() + ":" + host.getPort()));
        return ListUtils.strList2String(clientAddresses);
    }

    private Result validCheckForClusterJoin(ESClusterJoinDTO param, String operator) {
        if (AriusObjUtils.isNull(param)) {
            return Result.buildParamIllegal("参数为空");
        }

        if (AriusObjUtils.isNull(operator)) {
            return Result.buildParamIllegal("操作人不存在");
        }

        if (ES_HOST != ESClusterTypeEnum.valueOf(param.getType())) {
            return Result.buildParamIllegal("仅支持host类型的集群, 请确认是否为host类型");
        }

        List<ESRoleClusterHostDTO> roleClusterHosts = param.getRoleClusterHosts();
        if (CollectionUtils.isEmpty(roleClusterHosts)) {
            return Result.buildParamIllegal("集群节点信息为空");
        }

        Set<Integer> roleForNode = roleClusterHosts.stream().map(ESRoleClusterHostDTO::getRole)
            .collect(Collectors.toSet());

        if (!roleForNode.contains(MASTER_NODE.getCode())) {
            return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", MASTER_NODE.getDesc()));
        }
        if (!roleForNode.contains(DATA_NODE.getCode())) {
            return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", DATA_NODE.getDesc()));
        }
        if (!roleForNode.contains(CLIENT_NODE.getCode())) {
            return Result.buildParamIllegal(String.format("集群缺少类型为%s的节点", CLIENT_NODE.getDesc()));
        }

        Map<Integer, List<String>> role2IpsMap = ConvertUtil.list2MapOfList(roleClusterHosts,
                ESRoleClusterHostDTO::getRole, ESRoleClusterHostDTO::getIp);

        List<String> masterIps = role2IpsMap.get(MASTER_NODE.getCode());
        if (masterIps.size() < MASTER_NODE_MIN_NUMBER) {
            return Result.buildParamIllegal(String.format("集群%s的masternode角色节点个数要求大于等于3", param.getCluster()));
        }

        String duplicateIpForMaster = ClusterUtils.getDuplicateIp(masterIps);
        if (!AriusObjUtils.isBlack(duplicateIpForMaster)) {
            return Result.buildParamIllegal(String.format("集群ip:%s重复, 请重新输入", duplicateIpForMaster));
        }

        String duplicateIpForClient = ClusterUtils.getDuplicateIp(role2IpsMap.get(CLIENT_NODE.getCode()));
        if (!AriusObjUtils.isBlack(duplicateIpForClient)) {
            return Result.buildParamIllegal(String.format("集群ip:%s重复, 请重新输入", duplicateIpForClient));
        }

        String duplicateIpForData = ClusterUtils.getDuplicateIp(role2IpsMap.get(DATA_NODE.getCode()));
        if (!AriusObjUtils.isBlack(duplicateIpForData)) {
            return Result.buildParamIllegal(String.format("集群ip:%s重复, 请重新输入", duplicateIpForData));
        }

        ESClusterPhy clusterByName = esClusterPhyService.getClusterByName(param.getCluster());
        if (!AriusObjUtils.isNull(clusterByName)) {
            return Result.buildParamIllegal(String.format("物理集群名称%s已存在", param.getCluster()));
        }

        ESClusterLogic esClusterLogic = esClusterLogicService.getLogicClusterByName(param.getLogicCluster());
        if (!AriusObjUtils.isNull(esClusterLogic)) {
            return Result.buildParamIllegal(String.format("逻辑集群名称%s已存在", param.getLogicCluster()));
        }

        return Result.buildSucc();
    }

    private void doDeleteClusterJoin(ESClusterPhy esClusterPhy, String operator) throws AdminOperateException {
        ESClusterPhyContext esClusterPhyContext = clusterContextManager
                .getESClusterPhyContext(esClusterPhy.getCluster());

        List<Long> associatedRegionIds = esClusterPhyContext.getAssociatedRegionIds();
        for (Long associatedRegionId : associatedRegionIds) {
            Result unbindRegionResult = esRegionRackService.unbindRegion(associatedRegionId, operator);
            if (unbindRegionResult.failed()) {
                throw new AdminOperateException(String.format("解绑region(%s)失败", associatedRegionId));
            }

            Result deletePhyClusterRegionResult = esRegionRackService.deletePhyClusterRegion(associatedRegionId,
                    operator);
            if (deletePhyClusterRegionResult.failed()) {
                throw new AdminOperateException(String.format("删除region(%s)失败", associatedRegionId));
            }
        }

        List<Long> clusterLogicIds = esClusterPhyContext.getAssociatedClusterLogicIds();
        for (Long clusterLogicId : clusterLogicIds) {
            Result deleteLogicClusterResult = esClusterLogicService.deleteLogicClusterById(clusterLogicId, operator);
            if (deleteLogicClusterResult.failed()) {
                throw new AdminOperateException(String.format("删除逻辑集群(%s)失败", clusterLogicId));
            }
        }

        Result deleteClusterResult = esClusterPhyService.deleteClusterById(esClusterPhy.getId(), operator);
        if (deleteClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群(%s)失败", esClusterPhy.getCluster()));
        }

        Result deleteRoleClusterResult = esRoleClusterService.deleteRoleClusterByClusterId(esClusterPhy.getId());
        if (deleteRoleClusterResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群角色(%s)失败", esClusterPhy.getCluster()));
        }

        Result deleteRoleClusterHostResult = esRoleClusterHostService.deleteByCluster(esClusterPhy.getCluster());
        if (deleteRoleClusterHostResult.failed()) {
            throw new AdminOperateException(String.format("删除物理集群节点(%s)失败", esClusterPhy.getCluster()));
        }
    }

}
