package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Alias;
import com.didichuxing.datachannel.arius.admin.client.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.client.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.gateway.GatewayNodeVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplatePhysicalDeployVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.template.GatewayTemplateVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayManageService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.TemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisService;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

@Component
public class GatewayManagerImpl implements GatewayManager {

    private static final ILog LOGGER                      = LogFactory.getLog(GatewayManagerImpl.class);

    private static final String                 GATEWAY_GET_APP_TICKET      = "xTc59aY72";
    private static final String                 GATEWAY_GET_APP_TICKET_NAME = "X-ARIUS-GATEWAY-TICKET";

    private static final int                    TIMEOUT                     = 10 * 60 * 1000;

    @Autowired
    private AppService appService;

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private TemplateLogicService templateLogicService;

    @Autowired
    private TemplatePhyService templatePhyService;

    @Autowired
    private TemplateLogicAliasesManager templateLogicAliasesManager;

    @Autowired
    private GatewayManageService gatewayManageService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private DslStatisService dslStatisService;

    @Override
    public Result heartbeat(GatewayHeartbeat heartbeat) {
        return gatewayManageService.heartbeat(heartbeat);
    }

    @Override
    public Result<Integer> heartbeat(String clusterName) {
        return gatewayManageService.aliveCount(clusterName, TIMEOUT);
    }

    @Override
    public Result<List<GatewayNodeVO>> getGatewayAliveNode(String clusterName) {
        return Result
                .buildSucc( ConvertUtil.list2List(gatewayManageService.getAliveNode(clusterName, TIMEOUT), GatewayNodeVO.class));
    }

    @Override
    public Result<List<GatewayAppVO>> listApp(HttpServletRequest request) {
        String ticket = request.getHeader(GATEWAY_GET_APP_TICKET_NAME);
        if (!GATEWAY_GET_APP_TICKET.equals(ticket)) {
            return Result.buildFrom(Result.buildParamIllegal("ticket错误"));
        }

        // 查询出所有的应用
        List<App> apps = appService.getApps();

        // 查询出所有的权限
        Map<Integer, Collection<AppTemplateAuth>> appId2AppTemplateAuthsMap = appLogicTemplateAuthService.getAllAppTemplateAuths();

        // 查询出所有的配置
        List<AppConfig> appConfigs = appService.getAppConfigs();
        Map<Integer, AppConfig> appId2AppConfigMap = ConvertUtil.list2Map(appConfigs, AppConfig::getAppId);

        String defaultRIndices = ariusConfigInfoService.stringSetting(ARIUS_COMMON_GROUP,
                "app.default.read.auth.indices", "");

        Map<Integer, IndexTemplateLogic> templateId2IndexTemplateLogicMap = templateLogicService
                .getLogicTemplateMappingsWithCache();

        List<GatewayAppVO> appVOS = Lists.newArrayList();
        for (App app : apps) {
            try {
                appVOS.add(buildAppVO(app, appId2AppTemplateAuthsMap, appId2AppConfigMap,
                        templateId2IndexTemplateLogicMap, defaultRIndices));
            } catch (Exception e) {
                LOGGER.warn("method=listApp||errMsg={}||stackTrace={}",
                        e.getMessage(), JSON.toJSONString(e.getStackTrace()), e);
            }
        }

        return Result.buildSucc(appVOS);
    }

    @Override
    public Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster) {
        List<IndexTemplatePhy> indexTemplatePhysicals = templatePhyService
                .getNormalTemplateByCluster(cluster);

        if (CollectionUtils.isEmpty(indexTemplatePhysicals)) {
            return Result.buildSucc( Maps.newHashMap());
        }

        Map<Integer, IndexTemplateLogic> templateId2IndexTemplateLogicMap = templateLogicService
                .getLogicTemplateMappingsWithCache();

        List<IndexTemplateAlias> aliases = templateLogicAliasesManager.listAlias();
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil.list2MulMap(aliases,
                IndexTemplateAlias::getLogicId);

        Map<String, GatewayTemplatePhysicalVO> result = Maps.newHashMap();
        for (IndexTemplatePhy templatePhysical : indexTemplatePhysicals) {
            try {
                GatewayTemplatePhysicalVO templatePhysicalVO = ConvertUtil.obj2Obj(templatePhysical,
                        GatewayTemplatePhysicalVO.class);

                templatePhysicalVO
                        .setDataCenter(templateId2IndexTemplateLogicMap.get(templatePhysical.getLogicId()).getDataCenter());

                Collection<IndexTemplateAlias> indexTemplateAliases = logicId2IndexTemplateAliasMultiMap
                        .get(templatePhysical.getLogicId());
                if (CollectionUtils.isEmpty(indexTemplateAliases)) {
                    templatePhysicalVO.setAliases(Lists.newArrayList());
                } else {
                    templatePhysicalVO
                            .setAliases(ConvertUtil.list2List(Lists.newArrayList(indexTemplateAliases), Alias.class));
                }

                result.put(templatePhysicalVO.getExpression(), templatePhysicalVO);
            } catch (Exception e) {
                LOGGER.warn("method=getTemplateMap||cluster={}||errMsg={}", cluster, e.getMessage(), e);
            }
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(String dataCenter) {
        List<IndexTemplateLogicWithPhyTemplates> logicWithPhysicals = templateLogicService
                .getTemplateWithPhysicalByDataCenter(dataCenter);

        List<IndexTemplateAlias> logicWithAliases = templateLogicAliasesManager.listAlias();
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil
                .list2MulMap(logicWithAliases, IndexTemplateAlias::getLogicId);

        //todo：清理配置
        Set<String> pipelineClusterSet = ariusConfigInfoService.stringSettingSplit2Set(ARIUS_COMMON_GROUP,
                "has.ingest.pipeline.plugin.clusters", "", ",");

        Map<String, GatewayTemplateDeployInfoVO> result = Maps.newHashMap();
        for (IndexTemplateLogicWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    GatewayTemplateDeployInfoVO gatewayTemplateDeployInfoVO = buildGatewayTemplateDeployInfoVO(logicWithPhysical,
                            logicId2IndexTemplateAliasMultiMap, pipelineClusterSet);

                    if(null != gatewayTemplateDeployInfoVO){
                        result.put(logicWithPhysical.getName(), gatewayTemplateDeployInfoVO);
                    }
                } catch (Exception e) {
                    LOGGER.warn("method=listDeployInfo||dataCenter={}||templateName={}||errMsg={}",
                            dataCenter, logicWithPhysical.getName(), e.getMessage(), e);
                }
            }
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<ScrollDslTemplateResponse> scrollSearchDslTemplate(ScrollDslTemplateRequest request) {
        return dslStatisService.scrollSearchDslTemplate(request);
    }

    /**************************************** private method *************************************************/
    private GatewayTemplatePhysicalDeployVO buildPhysicalDeployVO(IndexTemplatePhy physical) {
        if (physical == null) {
            return null;
        }

        GatewayTemplatePhysicalDeployVO deployVO = new GatewayTemplatePhysicalDeployVO();
        deployVO.setTemplateName(physical.getName());
        deployVO.setCluster(physical.getCluster());
        deployVO.setRack(physical.getRack());
        deployVO.setShardNum(physical.getShard());
        deployVO.setGroupId(physical.getGroupId());
        deployVO.setDefaultWriterFlags(physical.fetchDefaultWriterFlags());

        if (StringUtils.isNotBlank(physical.getConfig())) {
            IndexTemplatePhysicalConfig config = JSON.parseObject(physical.getConfig(),
                    IndexTemplatePhysicalConfig.class);
            deployVO.setTopic(config.getKafkaTopic());
            deployVO.setAccessApps(config.getAccessApps());
            deployVO.setMappingIndexNameEnable(config.getMappingIndexNameEnable());
            deployVO.setTypeIndexMapping(config.getTypeIndexMapping());
        }

        return deployVO;
    }

    private GatewayAppVO buildAppVO(
            App app, Map<Integer, Collection<AppTemplateAuth>> appId2AppTemplateAuthsMap,
            Map<Integer, AppConfig> appId2AppConfigMap,
            Map<Integer, IndexTemplateLogic> templateId2IndexTemplateLogicMap,
            String defaultReadPermissionIndexes) {

        GatewayAppVO gatewayAppVO = ConvertUtil.obj2Obj(app, GatewayAppVO.class);

        if (StringUtils.isBlank(gatewayAppVO.getDataCenter())) {
            gatewayAppVO.setDataCenter("cn");
        }

        if (StringUtils.isNotBlank(app.getIp())) {
            gatewayAppVO.setIp(Lists.newArrayList(app.getIp().split(",")));
        } else {
            gatewayAppVO.setIp(Lists.newArrayList());
        }

        gatewayAppVO.setIsRoot(app.getIsRoot());
        if (AdminConstant.YES.equals(app.getIsRoot())) {
            gatewayAppVO.setIndexExp(Lists.newArrayList("*"));
            gatewayAppVO.setWIndexExp(Lists.newArrayList("*"));
        } else {
            Set<String> readPermissionIndexExpressions = Sets.newHashSet();
            Set<String> writePermissionIndexExpressions = Sets.newHashSet();

            if (StringUtils.isNotBlank(app.getIndexExp())) {
                readPermissionIndexExpressions.addAll(Lists.newArrayList(app.getIndexExp().split(",")));
            }
            readPermissionIndexExpressions.addAll( Arrays.asList(defaultReadPermissionIndexes.split(",")));

            if (appId2AppTemplateAuthsMap.containsKey(app.getId())) {
                Collection<AppTemplateAuth> templateAuthCollection = appId2AppTemplateAuthsMap.get(app.getId());
                if (!templateAuthCollection.isEmpty()) {
                    readPermissionIndexExpressions.addAll(fetchReadPermissionIndexExpressions(
                            app.getId(), templateAuthCollection, templateId2IndexTemplateLogicMap));

                    writePermissionIndexExpressions.addAll(fetchWritePermissionIndexExpressions(
                            app.getId(), templateAuthCollection,
                            templateId2IndexTemplateLogicMap));
                } else {
                    LOGGER.warn("method=buildAppVO||appId={}||msg=app has no permission.", app.getId());
                }
            }

            gatewayAppVO.setIndexExp(Lists.newArrayList(readPermissionIndexExpressions));
            gatewayAppVO.setWIndexExp(Lists.newArrayList(writePermissionIndexExpressions));
        }

        AppConfig config = appId2AppConfigMap.get(app.getId());
        if (config != null) {
            gatewayAppVO.setDslAnalyzeEnable(config.getDslAnalyzeEnable());
            gatewayAppVO.setAggrAnalyzeEnable(config.getAggrAnalyzeEnable());
            gatewayAppVO.setAnalyzeResponseEnable(config.getAnalyzeResponseEnable());
        } else {
            LOGGER.warn("method=buildAppVO||appId={}||msg=app config is not exists.", app.getId());
        }

        return gatewayAppVO;
    }

    /**
     * 获取模板读权限索引列表
     * @param appId appId
     * @param appTemplateAuthCollection app模板权限集合
     * @param templateId2IndexTemplateLogicMap 模板Id跟模板详情映射
     * @return
     */
    private Set<String> fetchReadPermissionIndexExpressions(
            Integer appId, Collection<AppTemplateAuth> appTemplateAuthCollection,
            Map<Integer, IndexTemplateLogic> templateId2IndexTemplateLogicMap) {
        Set<String> indexExpressions = new HashSet<>();
        if (CollectionUtils.isNotEmpty(appTemplateAuthCollection)) {
            for (AppTemplateAuth auth : appTemplateAuthCollection) {
                try {
                    indexExpressions.add(templateId2IndexTemplateLogicMap.get(
                            Integer.valueOf(auth.getTemplateId())).getExpression());
                } catch (Exception e) {
                    LOGGER.warn("method=fetchReadPermissionIndexExpressions||appId={}||templateId={}||msg=template not exists.",
                            appId, auth.getTemplateId());
                }
            }
        }
        return indexExpressions;
    }

    /**
     * 获取当前app所有写权限索引列表
     * @param appId appId
     * @param appTemplateAuthCollection app模板权限集合
     * @param templateId2IndexTemplateLogicMap 模板Id跟模板详情映射
     * @return
     */
    private Set<String> fetchWritePermissionIndexExpressions(
            Integer appId, Collection<AppTemplateAuth> appTemplateAuthCollection,
            Map<Integer, IndexTemplateLogic> templateId2IndexTemplateLogicMap) {
        Set<String> indexExpressions = new HashSet<>();
        if (CollectionUtils.isNotEmpty(appTemplateAuthCollection)) {
            for (AppTemplateAuth auth : appTemplateAuthCollection) {
                if (AppTemplateAuthEnum.OWN.getCode().equals(auth.getType()) ||
                        AppTemplateAuthEnum.RW.getCode().equals(auth.getType())) {
                    try {
                        indexExpressions.add(templateId2IndexTemplateLogicMap.get(
                                Integer.valueOf(auth.getTemplateId())).getExpression());
                    } catch (Exception e) {
                        LOGGER.warn("method=fetchWritePermissionIndexExpressions||appId={}||templateId={}||msg=template not exists.",
                                appId, auth.getTemplateId());
                    }
                }
            }
        }
        return indexExpressions;
    }

    private GatewayTemplateDeployInfoVO buildGatewayTemplateDeployInfoVO(IndexTemplateLogicWithPhyTemplates logicWithPhysical,
                                                                         Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap,
                                                                         Set<String> pipelineClusterSet) {
        if(null == logicWithPhysical || null ==logicWithPhysical.getMasterPhyTemplate()){
            return null;
        }

        GatewayTemplateVO baseInfo = ConvertUtil.obj2Obj(logicWithPhysical, GatewayTemplateVO.class);
        baseInfo.setDeployStatus( TemplateUtils.genDeployStatus(logicWithPhysical));
        baseInfo.setAliases(logicId2IndexTemplateAliasMultiMap.get(logicWithPhysical.getId()).stream()
                .map(IndexTemplateAlias::getName).collect( Collectors.toList()));
        baseInfo.setVersion(logicWithPhysical.getMasterPhyTemplate().getVersion());

        GatewayTemplatePhysicalDeployVO masterInfo = genMasterInfo(logicWithPhysical);
        List<GatewayTemplatePhysicalDeployVO> slaveInfos = genSlaveInfos(logicWithPhysical);

        GatewayTemplateDeployInfoVO deployInfoVO = new GatewayTemplateDeployInfoVO();
        deployInfoVO.setBaseInfo(baseInfo);
        deployInfoVO.setMasterInfo(masterInfo);
        deployInfoVO.setSlaveInfos(slaveInfos);

        if (!pipelineClusterSet.contains(logicWithPhysical.getMasterPhyTemplate().getCluster())) {
            deployInfoVO.getBaseInfo().setIngestPipeline("");
        }

        return deployInfoVO;
    }

    private GatewayTemplatePhysicalDeployVO genMasterInfo(IndexTemplateLogicWithPhyTemplates logicWithPhysical) {
        return buildPhysicalDeployVO(logicWithPhysical.getMasterPhyTemplate());
    }

    private List<GatewayTemplatePhysicalDeployVO> genSlaveInfos(IndexTemplateLogicWithPhyTemplates logicWithPhysical) {
        List<GatewayTemplatePhysicalDeployVO> slavesInfos = Lists.newArrayList();
        for (IndexTemplatePhy physical : logicWithPhysical.getPhysicals()) {
            if (physical.getRole().equals( TemplateDeployRoleEnum.MASTER.getCode())) {
                continue;
            }
            slavesInfos.add(buildPhysicalDeployVO(physical));
        }
        return slavesInfos;
    }
}
