package com.didichuxing.datachannel.arius.admin.biz.thardpart.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterRegionManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.IndexTemplatePhysicalConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhyWithLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.IndexTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdPartTemplateLogicWithMasterTemplateResourceVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateLogicVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplatePhysicalVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.ThirdpartTemplateVO;
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
import java.util.List;
import java.util.stream.Collectors;
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
    private ClusterRegionManager clusterRegionManager;

    @Autowired
    private TemplateSrvManager templateSrvManager;
 /**
    @Override
    public Result<List<ThirdpartAppVO>> listApp() {
        return Result.buildSucc(ConvertUtil.list2List(appService.listApps(), ThirdpartAppVO.class));
    }

    @Override
    public Result<Void> verifyApp(HttpServletRequest request,  Integer projectId, String appSecret) throws UnsupportedEncodingException {
        appSecret = URLDecoder.decode(appSecret, "UTF-8");
        return appService.verifyAppCode(appId, appSecret);
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
                .buildSucc(ConvertUtil.list2List(indexTemplateService.listAllLogicTemplates(), ThirdpartTemplateLogicVO.class));
    }

    @Override
    public Result<List<ThirdPartTemplateLogicWithMasterTemplateResourceVO>> listLogicWithMasterTemplateAndResource() {
        List<IndexTemplateLogicWithClusterAndMasterTemplate> logicWithMasterTemplateAndResource = indexTemplateService
                .listLogicTemplatesWithClusterAndMasterTemplate();

        List<ThirdPartTemplateLogicWithMasterTemplateResourceVO> vos = logicWithMasterTemplateAndResource.stream()
                .map(entity -> {
                    ThirdPartTemplateLogicWithMasterTemplateResourceVO vo = ConvertUtil.obj2Obj(entity,
                            ThirdPartTemplateLogicWithMasterTemplateResourceVO.class);
                    vo.setMasterTemplate(ConvertUtil.obj2Obj(entity.getMasterTemplate(), IndexTemplatePhysicalVO.class));
                    vo.setMasterResource(ConvertUtil.obj2Obj(entity.getLogicCluster(), ClusterLogicVO.class));
                    return vo;
                }).collect( Collectors.toList());

        return Result.buildSucc(vos);
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
                    .buildSucc(ConvertUtil.list2List(indexTemplateService.listAllLogicTemplates(), ThirdpartTemplateLogicVO.class));
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
                templateLogics = indexTemplateService.listAllLogicTemplates().stream()
                        .filter(temp -> logicIds.contains(temp.getId())).collect(Collectors.toList());
            } else {
                templateLogics = indexTemplateService.listLogicTemplatesByIds(Lists.newArrayList(logicIds));
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