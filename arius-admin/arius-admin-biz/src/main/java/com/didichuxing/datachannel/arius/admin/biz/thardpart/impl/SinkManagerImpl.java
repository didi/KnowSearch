package com.didichuxing.datachannel.arius.admin.biz.thardpart.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.thardpart.SinkManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.SinkSdkESUserVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkIDCTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkTemplateDeployInfoVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkTemplatePhysicalDeployVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.SinkSdkTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.TemplateUtils;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class SinkManagerImpl implements SinkManager {

    private static final String         FLINK_GET_APP_TICKET      = "xTc59aY72";
    private static final String         FLINK_GET_APP_TICKET_NAME = "X-ARIUS-FLINK-TICKET";

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ESUserService esUserService;

    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @Override
    public Result<SinkSdkESUserVO> listApp(HttpServletRequest request, Integer projectId) {
        String ticket = request.getHeader(FLINK_GET_APP_TICKET_NAME);
        if (!FLINK_GET_APP_TICKET.equals(ticket)) {
            return Result.buildParamIllegal("ticket错误");
        }

      
        if (!projectService.checkProjectExist(projectId)) {
            return Result.buildNotExist("应用不存在");
        }

        Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap = indexTemplateService
                .getAllLogicTemplatesMap();
        ESUser esUser = esUserService.getDefaultESUserByProject(projectId);
    
        return Result.buildSucc(buildESUserAuthInfo(esUser, projectLogicTemplateAuthService.getTemplateAuthsByProjectId(
                        projectId),
                templateId2IndexTemplateLogicMap, ""));
    }

    @Override
    public Result<SinkSdkTemplateDeployInfoVO> listDeployInfo(String templateName) {
        // 主键顺序存放
        Result<IndexTemplateWithPhyTemplates> result = getIndexTemplateMetaByName(templateName);
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = result.getData();

        return Result.buildSucc(buildTemplateDeployInfoVO(templateLogicWithPhysical, getPipelineClusters()));
    }

    @Override
    public Result<SinkSdkIDCTemplateDeployInfoVO> getIDCDeployInfo(@RequestParam(value = "templateName") String templateName) {
        Result<IndexTemplateWithPhyTemplates> result = fetchIndexTemplateMetaByName(templateName);
        if (result.failed()) {
            return Result.buildFrom(result);
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = result.getData();

        return Result.buildSucc(buildIDCTemplateDeployInfo(templateLogicWithPhysical, getPipelineClusters()));
    }

    /***************************************** private method ****************************************************/

    /**
     * 构造IDC模板部署信息
     * @param templateLogicWithPhysical 逻辑模板详情
     * @param pipelineClusterSet pipeline集群列表
     * @return
     */
    private SinkSdkIDCTemplateDeployInfoVO buildIDCTemplateDeployInfo(
            IndexTemplateWithPhyTemplates templateLogicWithPhysical,
            List<String> pipelineClusterSet) {

        SinkSdkIDCTemplateDeployInfoVO deployInfoVO = new SinkSdkIDCTemplateDeployInfoVO();

        SinkSdkTemplateVO baseInfo = ConvertUtil.obj2Obj(templateLogicWithPhysical, SinkSdkTemplateVO.class);
        baseInfo.setDeployStatus(TemplateUtils.genDeployStatus(templateLogicWithPhysical));
        baseInfo.setVersion(templateLogicWithPhysical.getMasterPhyTemplate().getVersion());
        deployInfoVO.setBaseInfo(baseInfo);

        Map<String, SinkSdkIDCTemplateDeployInfoVO.SinkSdkIDCTemplateMasterSlaveMeta> idcTemplateMetas = new HashMap<>();
        for (IndexTemplatePhy master : templateLogicWithPhysical.fetchMasterPhysicalTemplates()) {
            SinkSdkIDCTemplateDeployInfoVO.SinkSdkIDCTemplateMasterSlaveMeta meta =
                    new SinkSdkIDCTemplateDeployInfoVO.SinkSdkIDCTemplateMasterSlaveMeta();
            meta.setMasterInfo(buildPhysicalDeployVO(master));
            meta.setSlaveInfos(new ArrayList<>());

            for (IndexTemplatePhy slave : templateLogicWithPhysical.fetchMasterSlaves(master.getGroupId())) {
                meta.getSlaveInfos().add(buildPhysicalDeployVO(slave));
            }

            idcTemplateMetas.put(master.getGroupId(), meta);

            if (!pipelineClusterSet.contains(templateLogicWithPhysical.getMasterPhyTemplate().getCluster())) {
                deployInfoVO.getBaseInfo().setIngestPipeline("");
            }
        }

        deployInfoVO.setTemplateMasterSlaveMetas(idcTemplateMetas);

        return deployInfoVO;
    }

    /**
     * 通过模板名称获取模板详情
     * @param templateName 模板名称
     * @return
     */
    private Result<IndexTemplateWithPhyTemplates> fetchIndexTemplateMetaByName(String templateName) {
        // 主键顺序存放
        List<IndexTemplate> templateLogicList = indexTemplateService.getLogicTemplateByName(templateName);

        if (CollectionUtils.isEmpty(templateLogicList)) {
            return Result.buildFrom(Result.buildNotExist("模板不存在"));
        }

        IndexTemplate templateLogic = templateLogicList.get(templateLogicList.size() - 1);

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService
                .getTemplateByLogicId(templateLogic.getId());

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildFrom(Result.buildNotExist("模板无部署信息"));
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = ConvertUtil.obj2Obj(templateLogic,
                IndexTemplateWithPhyTemplates.class);
        templateLogicWithPhysical.setPhysicals(templatePhysicals);

        return Result.buildSucc(templateLogicWithPhysical);
    }

    /**
     * 通过模板名称获取模板详情
     * @param templateName 模板名称
     * @return
     */
    private Result<IndexTemplateWithPhyTemplates> getIndexTemplateMetaByName(String templateName) {
        // 主键顺序存放
        List<IndexTemplate> templateLogicList = indexTemplateService.getLogicTemplateByName(templateName);

        if (CollectionUtils.isEmpty(templateLogicList)) {
            return Result.buildNotExist("模板不存在");
        }

        IndexTemplate templateLogic = templateLogicList.get(templateLogicList.size() - 1);

        List<IndexTemplatePhy> templatePhysicals = indexTemplatePhyService
                .getTemplateByLogicId(templateLogic.getId());

        if (CollectionUtils.isEmpty(templatePhysicals)) {
            return Result.buildNotExist("模板无部署信息");
        }

        IndexTemplateWithPhyTemplates templateLogicWithPhysical = ConvertUtil.obj2Obj(templateLogic,
                IndexTemplateWithPhyTemplates.class);
        templateLogicWithPhysical.setPhysicals(templatePhysicals);

        return Result.buildSucc(templateLogicWithPhysical);
    }

    /**
     * 获取pipeline集群列表
     * @return
     */
    private List<String> getPipelineClusters() {
        return templateSrvManager.getPhyClusterByOpenTemplateSrv(TemplateServiceEnum.TEMPLATE_PIPELINE.getCode());
    }

    private SinkSdkESUserVO buildESUserAuthInfo(ESUser esUser, List<ProjectTemplateAuth> projectTemplateAuths,
                                                Map<Integer, IndexTemplate> templateId2IndexTemplateLogicMap,
                                                String defaultRIndices) {
        SinkSdkESUserVO appVO = ConvertUtil.obj2Obj(esUser, SinkSdkESUserVO.class);

        if (esUser.getIsRoot().equals(AdminConstant.YES)) {
            appVO.setIndexExp( Lists.newArrayList("*"));
            appVO.setWIndexExp(Lists.newArrayList("*"));
        } else {
            Set<String> rIndexExpressSet = Sets.newHashSet();
            Set<String> wIndexExpressSet = Sets.newHashSet();

            // 构建读权限列表
            rIndexExpressSet.addAll(projectTemplateAuths.stream()
                    .map(auth -> templateId2IndexTemplateLogicMap.get(auth.getTemplateId()).getExpression())
                    .collect( Collectors.toSet()));

            // 构建写权限列表
            wIndexExpressSet.addAll(projectTemplateAuths.stream()
                    .filter(auth -> (auth.getType().equals( ProjectTemplateAuthEnum.OWN.getCode())
                            || auth.getType().equals(ProjectTemplateAuthEnum.RW.getCode())))
                    .map(auth -> templateId2IndexTemplateLogicMap.get(auth.getTemplateId()).getExpression())
                    .collect(Collectors.toList()));

            if (StringUtils.isNotBlank(esUser.getIndexExp())) {
                rIndexExpressSet.addAll(Lists.newArrayList(esUser.getIndexExp().split(",")));
            }

            rIndexExpressSet.addAll(Arrays.asList(defaultRIndices.split(",")));
            appVO.setIndexExp(Lists.newArrayList(rIndexExpressSet));

            appVO.setWIndexExp(Lists.newArrayList(wIndexExpressSet));
        }

        return appVO;
    }

    private SinkSdkTemplateDeployInfoVO buildTemplateDeployInfoVO(
            IndexTemplateWithPhyTemplates templateLogicWithPhysical,
            List<String> pipelineClusterSet) {

        SinkSdkTemplateVO baseInfo = ConvertUtil.obj2Obj(templateLogicWithPhysical, SinkSdkTemplateVO.class);
        baseInfo.setDeployStatus( TemplateUtils.genDeployStatus(templateLogicWithPhysical));
        baseInfo.setVersion(templateLogicWithPhysical.getMasterPhyTemplate().getVersion());

        SinkSdkTemplatePhysicalDeployVO masterInfo = genMasterInfo(templateLogicWithPhysical);
        List<SinkSdkTemplatePhysicalDeployVO> slaveInfos = genSlaveInfos(templateLogicWithPhysical);

        SinkSdkTemplateDeployInfoVO deployInfoVO = new SinkSdkTemplateDeployInfoVO();
        deployInfoVO.setBaseInfo(baseInfo);
        deployInfoVO.setMasterInfo(masterInfo);
        deployInfoVO.setSlaveInfos(slaveInfos);

        if (!pipelineClusterSet.contains(templateLogicWithPhysical.getMasterPhyTemplate().getCluster())) {
            deployInfoVO.getBaseInfo().setIngestPipeline("");
        }

        return deployInfoVO;
    }

    private List<SinkSdkTemplatePhysicalDeployVO> genSlaveInfos(IndexTemplateWithPhyTemplates logicWithPhysical) {
        List<SinkSdkTemplatePhysicalDeployVO> slavesInfos = Lists.newArrayList();
        for (IndexTemplatePhy physical : logicWithPhysical.getPhysicals()) {
            if (physical.getRole().equals( TemplateDeployRoleEnum.MASTER.getCode())) {
                continue;
            }
            slavesInfos.add(buildPhysicalDeployVO(physical));
        }
        return slavesInfos;
    }

    private SinkSdkTemplatePhysicalDeployVO genMasterInfo(IndexTemplateWithPhyTemplates logicWithPhysical) {
        return buildPhysicalDeployVO(logicWithPhysical.getMasterPhyTemplate());
    }

    private SinkSdkTemplatePhysicalDeployVO buildPhysicalDeployVO(IndexTemplatePhy physical) {
        if (physical == null) {
            return null;
        }

        SinkSdkTemplatePhysicalDeployVO deployVO = new SinkSdkTemplatePhysicalDeployVO();
        deployVO.setTemplateName(physical.getName());
        deployVO.setCluster(physical.getCluster());
        deployVO.setPhysicalId(physical.getId());
        deployVO.setGroupId(physical.getGroupId());
        deployVO.setDefaultWriterFlags(physical.fetchDefaultWriterFlags());

        deployVO.setShardRouting(physical.getShardRouting());

        return deployVO;
    }
}