package com.didichuxing.datachannel.arius.admin.biz.template.srv;

import static com.didichuxing.datachannel.arius.admin.common.constant.PageSearchHandleTypeEnum.TEMPLATE_SRV;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.page.TemplateSrvPageSearchHandle;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.base.BaseTemplateSrv;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.cold.ColdManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.ColdSrvOpenDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.UnavailableTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminOperateException;
import com.didichuxing.datachannel.arius.admin.common.exception.NotFindSubclassException;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ESVersionUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ListUtils;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 * @author chengxiang
 * @date 2022/5/9
 */
@Service("newTemplateSrvService")
@DependsOn("springTool")
public class TemplateSrvManagerImpl implements TemplateSrvManager {
    protected static final ILog                                         LOGGER                                                    = LogFactory
        .getLog(TemplateSrvManagerImpl.class);

    private static final String                                         NO_PERMISSION_CONTENT                                     = "只有运维或者研发才有权限操作";

    private static final String                                         CLUSTER_LOGIC_NOT_EXISTS                                  = "逻辑集群不存在";
    private static final String                                         PHYSICAL_CLUSTER_NOT_EXISTS                               = "物理集群不存在";

    private final Map<Integer, BaseTemplateSrv>                         BASE_TEMPLATE_SRV_MAP                                     = Maps
        .newConcurrentMap();

    /**
     * 本地cache 加快无效索引服务过滤
     */
    private static final Cache<Integer, String/*ESClusterVersionEnum*/> LOGIC_TEMPLATE_ID_2_ASSOCIATED_CLUSTER_VERSION_ENUM_CACHE = CacheBuilder
        .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(10000).build();

    @Autowired
    private IndexTemplateService                                        indexTemplateService;

    @Autowired
    private ClusterPhyService                                           clusterPhyService;

    @Autowired
    private HandleFactory                                               handleFactory;
    @Autowired
    private RoleTool                                                    roleTool;
    @Autowired
    private OperateRecordService                                        operateRecordService;
    @Autowired
    private ClusterLogicService                                         clusterLogicService;
    @Autowired
    private ClusterContextManager                                       clusterContextManager;
    @Autowired
    private ColdManager                                                 coldManager;

    @PostConstruct
    public void init() {
        Map<String, BaseTemplateSrv> strTemplateSrvHandleMap = SpringTool.getBeansOfType(BaseTemplateSrv.class);
        strTemplateSrvHandleMap.forEach((k, v) -> {
            try {
                TemplateServiceEnum srvEnum = v.templateSrv();
                BASE_TEMPLATE_SRV_MAP.put(srvEnum.getCode(), v);
            } catch (Exception e) {
                LOGGER.error("class=TemplateSrvManagerImpl||method=init||error=", e);
            }
        });
        LOGGER.info("class=TemplateSrvManagerImpl||method=init||init finish");
    }

    @Override
    public Result<List<TemplateSrv>> getTemplateOpenSrv(Integer logicTemplateId) {
        try {
            IndexTemplate template = indexTemplateService.getLogicTemplateById(logicTemplateId);
            if (null == template) {
                return Result.buildNotExist("逻辑模板不存在");
            }

            return Result.buildSucc(TemplateSrv.codeStr2SrvList(template.getOpenSrv()));
        } catch (Exception e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=getTemplateOpenSrv||logicTemplateId={}", logicTemplateId,
                e);
            return Result.buildFail("获取模板开启服务失败");
        }
    }

    @Override
    public boolean isTemplateSrvOpen(Integer logicTemplateId, Integer srvCode) {
        Result<List<TemplateSrv>> openSrvResult = getTemplateOpenSrv(logicTemplateId);
        if (openSrvResult.failed()) {
            return false;
        }

        List<TemplateSrv> openSrv = openSrvResult.getData();
        for (TemplateSrv srv : openSrv) {
            if (srvCode.equals(srv.getSrvCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<UnavailableTemplateSrv> getUnavailableSrv(Integer logicTemplateId) {
        List<UnavailableTemplateSrv> unavailableSrvList = Lists.newCopyOnWriteArrayList();
        List<TemplateServiceEnum> allSrvList = TemplateServiceEnum.allTemplateSrv();

        String esVersionFromESCluster = getLogicTemplateAssociatedEsVersionByLogicTemplateId(logicTemplateId);
        // isPartition为true代表能分区，false不能分区
        boolean isPartition = indexTemplateService.getLogicTemplateById(logicTemplateId).getExpression().endsWith("*");

        for (TemplateServiceEnum srvEnum : allSrvList) {
            if (ESVersionUtil.isHigher(srvEnum.getEsClusterVersion().getVersion(), esVersionFromESCluster)) {
                unavailableSrvList.add(new UnavailableTemplateSrv(srvEnum.getCode(), srvEnum.getServiceName(),
                    srvEnum.getEsClusterVersion().getVersion(),
                    String.format("不支持该模板服务, 模板[%s]归属集群目前版本[%s], 模板服务需要的最低版本为[%s]", logicTemplateId,
                        esVersionFromESCluster, srvEnum.getEsClusterVersion().getVersion())));
            }

            if(!isPartition && (TemplateServiceEnum.TEMPLATE_PRE_CREATE.getCode().equals(srvEnum.getCode()) ||
                    TemplateServiceEnum.TEMPLATE_DEL_EXPIRE.getCode().equals(srvEnum.getCode()) ||
                    TemplateServiceEnum.TEMPLATE_COLD.getCode().equals(srvEnum.getCode()))){
                unavailableSrvList.add(new UnavailableTemplateSrv(srvEnum.getCode(), srvEnum.getServiceName(),
                        srvEnum.getEsClusterVersion().getVersion(), "非分区模版不支持预创建和过期删除"));
            }
        }
        return unavailableSrvList;
    }

    @Override
    public PaginationResult<TemplateWithSrvVO> pageGetTemplateWithSrv(TemplateQueryDTO condition, Integer projectId) throws NotFindSubclassException {
        BaseHandle baseHandle = handleFactory.getByHandlerNamePer(TEMPLATE_SRV.getPageSearchType());
        if (baseHandle instanceof TemplateSrvPageSearchHandle) {
            if (condition.getProjectId() == null && !AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
                condition.setProjectId(projectId);
            }
            TemplateSrvPageSearchHandle handler = (TemplateSrvPageSearchHandle) baseHandle;
            return handler.doPage(condition, projectId);
        }
        return PaginationResult.buildFail("没有找到对应的处理器");
    }

    @Override
    public Result<Void> openSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId,
                                ColdSrvOpenDTO data) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) {
            return Result.buildParamIllegal("未找到对应的服务");
        }

        try {
            if (Objects.nonNull(data)) {
                Result<Integer> result = coldManager.batchChangeHotDay(data.getColdSaveDays(), operator, templateIdList,
                    projectId);
                if (result.failed()) {
                    return Result.buildFrom(result);
                }
            }
            return srvHandle.openSrv(templateIdList, operator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=openSrv||templateIdList={}||srvCode={}"
                         + "||errMsg=failed to open template srv",
                ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }

    @Override
    public Result<Void> closeSrv(Integer srvCode, List<Integer> templateIdList, String operator, Integer projectId) {
        BaseTemplateSrv srvHandle = BASE_TEMPLATE_SRV_MAP.get(srvCode);
        if (null == srvHandle) {
            return Result.buildParamIllegal("未找到对应服务");
        }

        try {
            return srvHandle.closeSrv(templateIdList, operator, projectId);
        } catch (AdminOperateException e) {
            LOGGER.error("class=TemplateSrvManagerImpl||method=closeSrv||templateIdList={}||srvCode={}"
                         + "||errMsg=failed to open template srv",
                ListUtils.intList2String(templateIdList), srvCode);
            return Result.buildFail(e.getMessage());
        }
    }

    private String getLogicTemplateAssociatedEsVersionByLogicTemplateId(Integer logicTemplateId) {
        try {
            return LOGIC_TEMPLATE_ID_2_ASSOCIATED_CLUSTER_VERSION_ENUM_CACHE.get(logicTemplateId, () -> {
                IndexTemplateLogicWithClusterAndMasterTemplate template = indexTemplateService
                    .getLogicTemplateWithClusterAndMasterTemplate(logicTemplateId);
                if (null == template || null == template.getMasterTemplate()) {
                    LOGGER.warn(
                        "class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId"
                                + "||templateId={}||errMsg=masterPhyTemplate is null",
                        logicTemplateId);
                    return "";
                }

                String masterCluster = template.getMasterTemplate().getCluster();
                ClusterPhy clusterPhy = clusterPhyService.getClusterByName(masterCluster);
                if (null == clusterPhy) {
                    LOGGER.warn(
                        "class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId"
                                + "||templateId={}||errMsg=clusterPhy of template is null",
                        logicTemplateId);
                    return "";
                }

                return clusterPhy.getEsVersion();
            });
        } catch (ExecutionException e) {
            LOGGER
                .error("class=TemplateSrvPageSearchHandle||method=getLogicTemplateAssociatedEsVersionByLogicTemplateId"
                       + "||templateId={}||errMsg={}",
                    logicTemplateId, e.getMessage(), e);
            return "";
        }
    }

    @Override
    public List<Integer> getPhyClusterTemplateSrvIds(String phyCluster) {
        Result<List<ClusterTemplateSrv>> ret = clusterPhyService.getPhyClusterTemplateSrv(phyCluster);
        if (ret.success()) {
            return ret.getData().stream().map(ClusterTemplateSrv::getServiceId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    /**
     * @param phyClusterName
     * @param clusterTemplateSrvIdList
     * @param operator
     * @return
     */
    @Override
    public Result<Boolean> replaceTemplateServes(String phyClusterName, List<Integer> clusterTemplateSrvIdList,
                                                 String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(phyClusterName);
        if (null == clusterPhy) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }

        clusterPhy.setTemplateSrvs(ListUtils.intList2String(clusterTemplateSrvIdList));
        return clusterPhyService.editCluster(ConvertUtil.obj2Obj(clusterPhy, ClusterPhyDTO.class), operator);
    }

    /**
     * @param clusterPhy 物理集群名称
     * @param operator   操作人
     * @return
     */
    @Override
    public Result<Boolean> delAllTemplateSrvByClusterPhy(String clusterPhy, String operator) {
        if (!isRDOrOP(operator)) {
            return Result.buildNotExist(NO_PERMISSION_CONTENT);
        }

        ClusterPhy cluster = clusterPhyService.getClusterByName(clusterPhy);
        if (null == cluster) {
            return Result.buildNotExist(PHYSICAL_CLUSTER_NOT_EXISTS);
        }
        cluster.setTemplateSrvs("");
        //此处属于工单的特定使用是不需要进行冗余的操作记录的
        return clusterPhyService.editCluster(ConvertUtil.obj2Obj(cluster, ClusterPhyDTO.class), operator);
    }

    private boolean isRDOrOP(String operator) {
        return roleTool.isAdmin(operator);
    }

    /**
     * @param clusterPhies
     * @param srvId
     * @return
     */
    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(List<ClusterPhy> clusterPhies, int srvId) {
        List<String> clusterPhyNames = new ArrayList<>();
        if (CollectionUtils.isEmpty(clusterPhies)) {
            return clusterPhyNames;
        }
        clusterPhies.forEach(clusterPhy -> {
            if (isPhyClusterOpenTemplateSrv(clusterPhy, srvId)) {
                clusterPhyNames.add(clusterPhy.getCluster());
            }
        });
        return clusterPhyNames;
    }

    public boolean isPhyClusterOpenTemplateSrv(ClusterPhy phyCluster, int srvId) {
        try {
            Result<List<ClusterTemplateSrv>> result = clusterPhyService.getPhyClusterTemplateSrv(phyCluster);
            if (result.failed()) {
                return false;
            }

            List<ClusterTemplateSrv> clusterTemplateSrvs = result.getData();
            for (ClusterTemplateSrv templateSrv : clusterTemplateSrvs) {
                if (srvId == templateSrv.getServiceId()) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.warn("class=TemplateSrvManager||method=isPhyClusterOpenTemplateSrv||phyCluster={}||srvId={}",
                phyCluster, srvId, e);

            return true;
        }
    }

    /**
    * 根据物理集群名称和模板服务的映射id校验是否能够开启指定的模板服务
    * @param phyCluster 物理集群名称
    * @param templateSrvId 模板服务id
    * @return 校验结果
    */
    private Result<Boolean> validCanOpenTemplateSrvId(String phyCluster, String templateSrvId) {
        TemplateServiceEnum templateServiceEnum = TemplateServiceEnum.getById(Integer.parseInt(templateSrvId));
        if (templateServiceEnum == null
            || AriusObjUtils.isNull(BASE_TEMPLATE_SRV_MAP.get(Integer.parseInt(templateSrvId)))) {
            return Result.buildFail("指定模板服务id有误");
        }

        return BASE_TEMPLATE_SRV_MAP.get(Integer.parseInt(templateSrvId)).checkOpenTemplateSrvByCluster(phyCluster);
    }

    /**
     * 判断物理集群是否打开了某个索引服务
     *
     * @param phyCluster 物理集群名称
     * @param srvId
     * @return
     */
    @Override
    public boolean isPhyClusterOpenTemplateSrv(String phyCluster, int srvId) {
        try {
            Result<List<ClusterTemplateSrv>> result = clusterPhyService.getPhyClusterTemplateSrv(phyCluster);
            if (null == result || result.failed()) {
                return false;
            }

            List<ClusterTemplateSrv> clusterTemplateSrvs = result.getData();
            for (ClusterTemplateSrv templateSrv : clusterTemplateSrvs) {
                if (srvId == templateSrv.getServiceId()) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.warn("class=TemplateSrvManager||method=isPhyClusterOpenTemplateSrv||phyCluster={}||srvId={}",
                phyCluster, srvId, e);

            return true;
        }
    }

    /**
     * 查询开启了某个索引服务的物理集群列表
     *
     * @param srvId
     * @return
     */
    @Override
    public List<String> getPhyClusterByOpenTemplateSrv(int srvId) {
        List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
        return getPhyClusterByOpenTemplateSrv(clusterPhies, srvId);
    }
}