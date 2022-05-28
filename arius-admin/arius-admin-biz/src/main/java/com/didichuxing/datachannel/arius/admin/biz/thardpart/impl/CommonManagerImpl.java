package com.didichuxing.datachannel.arius.admin.biz.thardpart.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.quota.TemplateQuotaManager;
import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.QuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.oprecord.OperateRecordDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicRackInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicWithRack;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.quota.ESTemplateQuotaUsage;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.LogicClusterRackVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartLogicClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.DataCenterEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonManagerImpl implements CommonManager {

    private static final ILog LOGGER = LogFactory.getLog(CommonManagerImpl.class);
    public static final int MAX_LOGIC_ID_NUM = 200;

    @Autowired
    private ProjectService projectService;


    @Autowired
    private ClusterPhyService esClusterPhyService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private TemplateQuotaManager templateQuotaManager;

    @Autowired
    private ClusterRegionManager clusterRegionManager;

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @Override
    public Result<Void> addOperateRecord(OperateRecordDTO param) {
        return operateRecordService.save(param);
    }

    @Override
    public Result<List<ThirdPartLogicClusterVO>> listLogicCluster() {
        return Result.buildSucc( ConvertUtil.list2List( clusterLogicService.listAllClusterLogics(), ThirdPartLogicClusterVO.class));
    }

    @Override
    public Result<List<ThirdPartLogicClusterVO>> listLogicClusterWithRack() {
        List<ThirdPartLogicClusterVO> thirdPartLogicClusterVOS = new ArrayList<>();

        List<ClusterLogicWithRack> resourceLogicWithItems = clusterLogicService.listAllClusterLogicsWithRackInfo();

        for (ClusterLogicWithRack resourceLogicWithItem : resourceLogicWithItems) {
            List<ClusterLogicRackInfo> clusterLogicRackInfos = new ArrayList<>(resourceLogicWithItem.getItems());
            List<LogicClusterRackVO> items = clusterRegionManager.buildLogicClusterRackVOs(
                    clusterLogicRackInfos);
            ThirdPartLogicClusterVO thirdpartLogicClusterVO = ConvertUtil.obj2Obj(resourceLogicWithItem,
                    ThirdPartLogicClusterVO.class);
            thirdpartLogicClusterVO.setItems(items);
            thirdPartLogicClusterVOS.add(thirdpartLogicClusterVO);
        }

        return Result.buildSucc(thirdPartLogicClusterVOS);
    }

    @Override
    public Result<ThirdPartLogicClusterVO> queryLogicCluster(String cluster, String rack) {
        return Result.buildSucc(
                ConvertUtil.obj2Obj(clusterLogicService.getClusterLogicByRack(cluster, rack), ThirdPartLogicClusterVO.class));
    }
    
    /**
     * @Override public Result<List<ThirdpartAppVO>> listApp() { return
     * Result.buildSucc(ConvertUtil.list2List(appService.listApps(), ThirdpartAppVO.class)); }
     **/
    /**
    @Override
    public Result<Void> verifyApp(HttpServletRequest request, Integer projectId, String appSecret) throws UnsupportedEncodingException {
        appSecret = URLDecoder.decode(appSecret, "UTF-8");
        return appService.verifyAppCode(projectId, appSecret);
    }**/

    @Override
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        List<ThirdPartClusterVO> clusterVOS = ConvertUtil.list2List(esClusterPhyService.listAllClusters(),
                ThirdPartClusterVO.class);
        //todo 这里应该把获取到的集群传入 getPhyClusterByOpenTemplateSrv
        List<String> hasSecurityClusters = templateSrvManager.getPhyClusterByOpenTemplateSrv(TemplateServiceEnum.TEMPLATE_SECURITY.getCode());

        clusterVOS.forEach(vo -> {
            if (hasSecurityClusters.contains(vo.getCluster())) {
                vo.setPlugins(Sets.newHashSet("security"));
            }
        });

        return Result.buildSucc(clusterVOS);
    }

    @Override
    public Result<ThirdPartClusterVO> getDataCluster(String cluster) {
        return Result
                .buildSucc(ConvertUtil.obj2Obj(esClusterPhyService.getClusterByName(cluster), ThirdPartClusterVO.class));
    }

    @Override
    public Result<List<ThirdpartConfigVO>> queryConfig(AriusConfigInfoDTO param) {
        return Result
                .buildSucc(ConvertUtil.list2List(ariusConfigInfoService.queryByCondition(param), ThirdpartConfigVO.class));
    }

    @Override
    public Result<List<ThirdpartTemplateLogicVO>> listLogicTemplate() {
        return Result
                .buildSucc(ConvertUtil.list2List(indexTemplateService.getAllLogicTemplates(), ThirdpartTemplateLogicVO.class));
    }

    @Override
    public Result<List<ThirdPartTemplateLogicWithMasterTemplateResourceVO>> listLogicWithMasterTemplateAndResource() {
        List<IndexTemplateLogicWithClusterAndMasterTemplate> logicWithMasterTemplateAndResource = indexTemplateService
                .getLogicTemplatesWithClusterAndMasterTemplate();

        List<ThirdPartTemplateLogicWithMasterTemplateResourceVO> vos = logicWithMasterTemplateAndResource.stream()
                .map(entity -> {
                    ThirdPartTemplateLogicWithMasterTemplateResourceVO vo = ConvertUtil.obj2Obj(entity,
                            ThirdPartTemplateLogicWithMasterTemplateResourceVO.class);
                    vo.setMasterTemplate(ConvertUtil.obj2Obj(entity.getMasterTemplate(), IndexTemplatePhysicalVO.class));
                    vo.setMasterResource(ConvertUtil.obj2Obj(entity.getLogicCluster(), ConsoleClusterVO.class));
                    return vo;
                }).collect( Collectors.toList());

        return Result.buildSucc(vos);
    }

    @Override
    public Result<List<ThirdpartTemplateLogicVO>> listLogicByName(String template) {
        List<IndexTemplate> indexTemplates = indexTemplateService.getLogicTemplateByName(template);

        List<ThirdpartTemplateLogicVO> templateLogicVOList = ConvertUtil.list2List(indexTemplates,
                ThirdpartTemplateLogicVO.class);

        List<ESTemplateQuotaUsage> templateQuotaUsages = templateQuotaManager.listAll();
        Map<Integer, ESTemplateQuotaUsage> logicId2ESTemplateQuotaUsageMap = ConvertUtil.list2Map(templateQuotaUsages,
                ESTemplateQuotaUsage::getLogicId);

        for (ThirdpartTemplateLogicVO templateLogic : templateLogicVOList) {
            // 填充quota利用率信息
            ESTemplateQuotaUsage templateQuotaUsage = logicId2ESTemplateQuotaUsageMap.get(templateLogic.getId());
            if (templateQuotaUsage != null) {
                templateLogic.setQuotaUsage(ConvertUtil.obj2Obj(templateQuotaUsage, QuotaUsage.class));
            }
        }

        return Result.buildSucc(templateLogicVOList);
    }

    @Override
    public Result<List<ThirdpartTemplatePhysicalVO>> listPhysicalTemplate() {
        List<IndexTemplatePhy> physicals = indexTemplatePhyService.listTemplate();

        List<ThirdpartTemplatePhysicalVO> result = Lists.newArrayList();
        for (IndexTemplatePhy physical : physicals) {
            ThirdpartTemplatePhysicalVO physicalVO = ConvertUtil.obj2Obj(physical, ThirdpartTemplatePhysicalVO.class);
            physicalVO.setConfigObj( JSON.parseObject(physical.getConfig(), IndexTemplatePhysicalConfig.class));
            result.add(physicalVO);
        }

        return Result.buildSucc(result);
    }

    @Override
    public Result<List<ThirdpartTemplateVO>> listPhysicalWithLogic() {
        List<IndexTemplatePhyWithLogic> templatePhysicalWithLogics = indexTemplatePhyService
                .listTemplateWithLogic();

        List<ThirdpartTemplateVO> templateVOS = Lists.newArrayList();
        for (IndexTemplatePhyWithLogic physicalWithLogic : templatePhysicalWithLogics) {
            ThirdpartTemplateVO templateVO = ConvertUtil.obj2Obj(physicalWithLogic.getLogicTemplate(),
                    ThirdpartTemplateVO.class);
            try {
                BeanUtils.copyProperties(physicalWithLogic, templateVO);
            } catch (Exception e) {
                LOGGER.warn("class=CommonManagerImpl||method=listPhysicalWithLogic||physicalId={}||name={}||errMsg={}",
                        physicalWithLogic.getId(), physicalWithLogic.getName(), e.getMessage(), e);
            }
            templateVOS.add(templateVO);
        }

        return Result.buildSucc(templateVOS);
    }

    @Override
    public Result<ThirdpartTemplateVO> getMasterByLogicId(Integer logicId) {

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = indexTemplateService.getLogicTemplateWithPhysicalsById(logicId);

        if (templateLogicWithPhysical == null || templateLogicWithPhysical.getMasterPhyTemplate() == null) {
            return Result.buildNotExist("模板不存在： " + logicId);
        }

        ThirdpartTemplateVO templateVO = ConvertUtil.obj2Obj(templateLogicWithPhysical, ThirdpartTemplateVO.class);
        BeanUtils.copyProperties(templateLogicWithPhysical.getMasterPhyTemplate(), templateVO);

        return Result.buildSucc(templateVO);
    }

    @Override
    public Result<ThirdpartTemplatePhysicalVO> getPhysicalTemplateById(Long physicalId) {
        return Result.buildSucc(ConvertUtil.obj2Obj( indexTemplatePhyService.getTemplateById(physicalId),
                ThirdpartTemplatePhysicalVO.class));
    }
/**
    @Override
    public Result<List<ThirdpartTemplateLogicVO>> listLogicByAppIdAuthDataCenter(Integer projectId, String auth, String dataCenter) {

        App app = appService.getAppById(projectId);

        if (app == null) {
            return Result.buildParamIllegal("appId非法");
        }

        if (app.getIsRoot().equals( AdminConstant.YES)) {
            return Result
                    .buildSucc(ConvertUtil.list2List(indexTemplateService.getAllLogicTemplates(), ThirdpartTemplateLogicVO.class));
        }

        List<ProjectTemplateAuth> templateAuths = projectLogicTemplateAuthService.getTemplateAuthsByProjectId(projectId);
        if (CollectionUtils.isEmpty(templateAuths)) {
            return Result.buildSucc(Lists.newArrayList());
        }

        ProjectTemplateAuthEnum authEnum = ProjectTemplateAuthEnum.valueOfName(auth);
        if (ProjectTemplateAuthEnum.NO_PERMISSION.equals(authEnum)) {
            return Result.buildParamIllegal("auth非法");
        }

        templateAuths = templateAuths.stream()
                .filter(appTemplateAuth -> appTemplateAuth.getType() <= authEnum.getCode()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(templateAuths)) {
            return Result.buildSucc(Lists.newArrayList());
        }

        Set<Integer> logicIds = templateAuths.stream()
                .map(ProjectTemplateAuth::getTemplateId).collect(Collectors.toSet());

        List<IndexTemplate> templateLogics = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(logicIds)) {
            if (logicIds.size() > MAX_LOGIC_ID_NUM) {
                templateLogics = indexTemplateService.getAllLogicTemplates().stream()
                        .filter(temp -> logicIds.contains(temp.getId())).collect(Collectors.toList());
            } else {
                templateLogics = indexTemplateService.getLogicTemplatesByIds(Lists.newArrayList(logicIds));
            }
        }

        if (dataCenter != null) {
            if (!DataCenterEnum.validate(dataCenter)) {
                return Result.buildParamIllegal("dataCenter非法");
            }

            templateLogics = templateLogics.stream()
                    .filter(indexTemplateLogic -> dataCenter.equals(indexTemplateLogic.getDataCenter()))
                    .collect(Collectors.toList());

        }

        return Result.buildSucc(ConvertUtil.list2List(templateLogics, ThirdpartTemplateLogicVO.class));
    }
    **/
}