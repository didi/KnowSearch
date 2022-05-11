package com.didichuxing.datachannel.arius.admin.biz.gateway.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.APP_DEFAULT_READ_AUTH_INDICES;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.gateway.GatewayManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.aliases.TemplateLogicAliasesManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Alias;
import com.didichuxing.datachannel.arius.admin.common.bean.common.GatewayHeartbeat;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.alias.IndexTemplateAliasDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateRequest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.dsl.ScrollDslTemplateResponse;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.gateway.GatewayClusterNode;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateAlias;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.GatewayAppVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.gateway.GatewayClusterNodeVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalDeployVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.GatewayTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.GatewaySqlConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.gateway.GatewayService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicAliasService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didichuxing.datachannel.arius.admin.metadata.service.DslStatisService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author didi
 */
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
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplateLogicAliasesManager templateLogicAliasesManager;

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private DslStatisService dslStatisService;

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @Autowired
    private TemplateLogicAliasService templateLogicAliasService;

    @Override
    public Result<Void> heartbeat(GatewayHeartbeat heartbeat) {
        return gatewayService.heartbeat(heartbeat);
    }

    @Override
    public Result<Integer> heartbeat(String clusterName) {
        return gatewayService.aliveCount(clusterName, TIMEOUT);
    }

    @Override
    public Result<List<GatewayClusterNodeVO>> getGatewayAliveNode(String clusterName) {
        return Result
                .buildSucc( ConvertUtil.list2List(gatewayService.getAliveNode(clusterName, TIMEOUT), GatewayClusterNodeVO.class));
    }

    @Override
    public Result<List<String>> getGatewayAliveNodeNames(String clusterName) {
        List<GatewayClusterNode> aliveNodes = gatewayService.getAliveNode(clusterName, TIMEOUT);
        List<String> list = Lists.newArrayList();
        if (aliveNodes != null && !aliveNodes.isEmpty()) {
            list = aliveNodes.stream().map(GatewayClusterNode::getHostName).collect(Collectors.toList());
        }
        return Result.buildSucc(list);
    }

    @Override
    public Result<List<GatewayAppVO>> listApp(HttpServletRequest request) {
        String ticket = request.getHeader(GATEWAY_GET_APP_TICKET_NAME);
        if (!GATEWAY_GET_APP_TICKET.equals(ticket)) {
            return Result.buildParamIllegal("ticket错误");
        }

        // 查询出所有的应用
        List<App> apps = appService.listAppWithCache();

        // 查询出所有的权限
        Map<Integer, Collection<AppTemplateAuth>> appId2AppTemplateAuthsMap = appLogicTemplateAuthService.getAllAppTemplateAuths();

        // 查询出所有的配置
        List<AppConfig> appConfigs = appService.listConfigWithCache();
        Map<Integer, AppConfig> appId2AppConfigMap = ConvertUtil.list2Map(appConfigs, AppConfig::getAppId);

        String defaultIndices = ariusConfigInfoService.stringSetting(ARIUS_COMMON_GROUP,
                APP_DEFAULT_READ_AUTH_INDICES, "");

        Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap = indexTemplateService
                .getAllLogicTemplatesMap();

        Map<Integer, List<String>> aliasMap = templateLogicAliasService.listAliasMapWithCache();

        List<GatewayAppVO> appVOS = apps.parallelStream().map(app -> {
            try {
                return buildAppVO(app, appId2AppTemplateAuthsMap, appId2AppConfigMap,templateId2IndexTemplateLogicMap, defaultIndices, aliasMap);
            } catch (Exception e) {
                LOGGER.warn("class=GatewayManagerImpl||method=listApp||errMsg={}||stackTrace={}", e.getMessage(), JSON.toJSONString(e.getStackTrace()), e);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return Result.buildSucc(appVOS);
    }

    @Override
    public Result<Map<String, GatewayTemplatePhysicalVO>> getTemplateMap(String cluster) {
        List<IndexTemplatePhy> indexTemplatePhysicalInfos = indexTemplatePhyService
                .getNormalTemplateByCluster(cluster);

        if (CollectionUtils.isEmpty(indexTemplatePhysicalInfos)) {
            return Result.buildSucc( Maps.newHashMap());
        }

        Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap = indexTemplateService
                .getAllLogicTemplatesMap();

        List<IndexTemplateAlias> aliases = templateLogicAliasesManager.listAlias();
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil.list2MulMap(aliases,
                IndexTemplateAlias::getLogicId);

        Map<String, GatewayTemplatePhysicalVO> result = Maps.newHashMap();
        for (IndexTemplatePhy templatePhysical : indexTemplatePhysicalInfos) {
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
                LOGGER.warn("class=GatewayManagerImpl||method=getTemplateMap||cluster={}||errMsg={}", cluster, e.getMessage(), e);
            }
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<Map<String, GatewayTemplateDeployInfoVO>> listDeployInfo(String dataCenter) {
        List<IndexTemplateWithPhyTemplates> logicWithPhysicals = indexTemplateService
                .getTemplateWithPhysicalByDataCenter(dataCenter);

        List<IndexTemplateAlias> logicWithAliases = templateLogicAliasesManager.listAlias(logicWithPhysicals);
        Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap = ConvertUtil
                .list2MulMap(logicWithAliases, IndexTemplateAlias::getLogicId);

        List<String> pipelineClusterSet = templateSrvManager.getPhyClusterByOpenTemplateSrv(TemplateServiceEnum.TEMPLATE_PIPELINE.getCode());

        Map<String, GatewayTemplateDeployInfoVO> result = Maps.newHashMap();
        for (IndexTemplateWithPhyTemplates logicWithPhysical : logicWithPhysicals) {
            if (logicWithPhysical.hasPhysicals()) {
                try {
                    GatewayTemplateDeployInfoVO gatewayTemplateDeployInfoVO = buildGatewayTemplateDeployInfoVO(logicWithPhysical,
                            logicId2IndexTemplateAliasMultiMap, pipelineClusterSet);

                    if(null != gatewayTemplateDeployInfoVO){
                        result.put(logicWithPhysical.getName(), gatewayTemplateDeployInfoVO);
                    }
                } catch (Exception e) {
                    LOGGER.warn("class=GatewayManagerImpl||method=listDeployInfo||dataCenter={}||templateName={}||errMsg={}",
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

    @Override
    public Result<Boolean> addAlias(IndexTemplateAliasDTO indexTemplateAliasDTO){
        return templateLogicAliasService.addAlias(indexTemplateAliasDTO);
    }

    @Override
    public Result<Boolean> delAlias(IndexTemplateAliasDTO indexTemplateAliasDTO){
        return templateLogicAliasService.delAlias(indexTemplateAliasDTO);
    }

    @Override
    public Result<String> sqlExplain(String sql, Integer appId) {
        return gatewayService.sqlOperate(sql, null, appId, GatewaySqlConstant.SQL_EXPLAIN);
    }

    @Override
    public Result<String> directSqlSearch(String sql, String phyClusterName, Integer appId) {
        return gatewayService.sqlOperate(sql, phyClusterName, appId, GatewaySqlConstant.SQL_SEARCH);
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
            Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap,
            String defaultReadPermissionIndexes, Map<Integer, List<String>> aliasMap) {

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
            List<String> readPermissionIndexExpressions = new ArrayList<>();
            List<String> writePermissionIndexExpressions = new ArrayList<>();

            if (StringUtils.isNotBlank(app.getIndexExp())) {
                readPermissionIndexExpressions.addAll(Lists.newArrayList(app.getIndexExp().split(",")));
            }
            readPermissionIndexExpressions.addAll( Arrays.asList(defaultReadPermissionIndexes.split(",")));

            if (appId2AppTemplateAuthsMap.containsKey(app.getId())) {
                Collection<AppTemplateAuth> templateAuthCollection = appId2AppTemplateAuthsMap.get(app.getId());
                if (!templateAuthCollection.isEmpty()) {
                    fetchPermissionIndexExpressions(app.getId(), templateAuthCollection, templateId2IndexTemplateLogicMap,aliasMap, readPermissionIndexExpressions,writePermissionIndexExpressions);
                } else {
                    LOGGER.warn("class=GatewayManagerImpl||method=buildAppVO||appId={}||msg=app has no permission.", app.getId());
                }
            }

            gatewayAppVO.setIndexExp(readPermissionIndexExpressions);
            gatewayAppVO.setWIndexExp(writePermissionIndexExpressions);
        }

        AppConfig config = appId2AppConfigMap.get(app.getId());
        if (config != null) {
            gatewayAppVO.setDslAnalyzeEnable(config.getDslAnalyzeEnable());
            gatewayAppVO.setAggrAnalyzeEnable(config.getAggrAnalyzeEnable());
            gatewayAppVO.setAnalyzeResponseEnable(config.getAnalyzeResponseEnable());
        } else {
            LOGGER.warn("class=GatewayManagerImpl||method=buildAppVO||appId={}||msg=app config is not exists.", app.getId());
        }

        return gatewayAppVO;
    }

    /**
     * 获取当前app所有读写权限索引列表
     * @param appId appId
     * @param appTemplateAuthCollection app模板权限集合
     * @param templateId2IndexTemplateLogicMap  模板Id跟模板详情映射
     * @param indexExpressions  当前app读权限列表
     * @param writeExpressions  当前app写权限列表
     */
    private void fetchPermissionIndexExpressions(Integer appId, Collection<AppTemplateAuth> appTemplateAuthCollection,
                                                 Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap,
                                                 Map<Integer, List<String>> aliasMap,
                                                 List<String> indexExpressions, List<String> writeExpressions) {
        if (CollectionUtils.isNotEmpty(appTemplateAuthCollection)) {
            appTemplateAuthCollection.stream().forEach(auth -> {
                List<String> alias = aliasMap.getOrDefault(auth.getTemplateId(), new ArrayList<>(0));
                String expression;
                try {
                    expression = templateId2IndexTemplateLogicMap.get(auth.getTemplateId()).getExpression();
                } catch (Exception e) {
                    LOGGER.warn("class=GatewayManagerImpl||method=fetchPermissionIndexExpressions||appId={}||templateId={}||msg=template not exists.", appId, auth.getTemplateId());
                    return;
                }
                indexExpressions.add(expression);
                indexExpressions.addAll(alias);
                if (AppTemplateAuthEnum.OWN.getCode().equals(auth.getType()) || AppTemplateAuthEnum.RW.getCode().equals(auth.getType())) {
                    writeExpressions.add(expression);
                    writeExpressions.addAll(alias);
                }
            });
        }
    }

    private GatewayTemplateDeployInfoVO buildGatewayTemplateDeployInfoVO(IndexTemplateWithPhyTemplates logicWithPhysical,
                                                                         Multimap<Integer, IndexTemplateAlias> logicId2IndexTemplateAliasMultiMap,
                                                                         List<String> pipelineClusterSet) {
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

    private GatewayTemplatePhysicalDeployVO genMasterInfo(IndexTemplateWithPhyTemplates logicWithPhysical) {
        return buildPhysicalDeployVO(logicWithPhysical.getMasterPhyTemplate());
    }

    private List<GatewayTemplatePhysicalDeployVO> genSlaveInfos(IndexTemplateWithPhyTemplates logicWithPhysical) {
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