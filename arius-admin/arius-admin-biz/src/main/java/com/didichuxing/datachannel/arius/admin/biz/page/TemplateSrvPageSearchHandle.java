package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateSrvQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.UnavailableTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Component
public class TemplateSrvPageSearchHandle extends BasePageSearchHandle<TemplateWithSrvVO> {

    private static final ILog LOGGER = LogFactory.getLog(TemplateSrvPageSearchHandle.class);
    private static final FutureUtil<Void> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL = FutureUtil.init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL",10,10,100);
    private static final FutureUtil<Void> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL = FutureUtil.init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL",10,10,100);

    @Autowired
    private AppService appService;

    @Autowired
    private IndexTemplateService indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager templateSrvManager;

    @Override
    protected Result<Boolean> validCheckForAppId(Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal("项目不存在");
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    protected Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer appId) {
        if (!(pageDTO instanceof TemplateSrvQueryDTO)) {
            return Result.buildFail("参数错误");
        }

        TemplateSrvQueryDTO condition = (TemplateSrvQueryDTO) pageDTO;
        String templateName = condition.getName();
        if (!AriusObjUtils.isBlack(templateName) && (templateName.startsWith("*") || templateName.startsWith("?"))) {
            return Result.buildParamIllegal("模板名称不能以*或者?开头");
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    protected void init(PageDTO pageDTO) {
        // nothing to do
    }

    @Override
    protected PaginationResult<TemplateWithSrvVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId) {
        // nothing to do
        return PaginationResult.buildFail("暂时不支持带有鉴权的查询");
    }

    @Override
    protected PaginationResult<TemplateWithSrvVO> buildWithoutAuthType(PageDTO pageDTO, Integer appId) {
        Integer totalHit;
        List<IndexTemplate> matchIndexTemplateList;
        TemplateSrvQueryDTO condition = (TemplateSrvQueryDTO) pageDTO;
        if (AriusObjUtils.isBlank(condition.getCluster())) {
            matchIndexTemplateList = indexTemplateService.pagingGetTemplateSrvByCondition(condition);
            totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();
        } else {
            List<IndexTemplate> allTemplateList = indexTemplateService.getAllLogicTemplates();
            if (CollectionUtils.isEmpty(allTemplateList)) {
                return PaginationResult.buildFail("没有查询到模板");
            }

            List<IndexTemplate> meetConditionTemplateList = getMeetConditionTemplateList(condition, allTemplateList);
            totalHit = meetConditionTemplateList.size();
            matchIndexTemplateList = filterFullDataByPage(meetConditionTemplateList, condition);
        }

        List<TemplateWithSrvVO> templateWithSrvVOList = buildExtraAttribute(matchIndexTemplateList);
        return PaginationResult.buildSucc(templateWithSrvVOList, totalHit, condition.getPage(), condition.getSize());
    }

    /******************************************private***********************************************/
    /**
     * 根据无模板名称、有模板名称、有数量类型、有模板名称与数据类型等进行模糊匹配, 得出总结果
     *
     * @param condition
     * @param appAuthTemplatesList
     * @return
     */
    private List<IndexTemplate> getMeetConditionTemplateList(TemplateSrvQueryDTO condition, List<IndexTemplate> appAuthTemplatesList) {
        List<IndexTemplate> meetConditionTemplateList = Lists.newArrayList();
        if (!AriusObjUtils.isBlank(condition.getCluster())) {
            Set<String> logicIdSet = indexTemplatePhyService.getMatchNormalLogicIdByCluster(condition.getCluster());
            appAuthTemplatesList = appAuthTemplatesList.stream().filter(r -> logicIdSet.contains(r.getId().toString())).collect(Collectors.toList());
        }

        if (!AriusObjUtils.isBlack(condition.getName())) {
            appAuthTemplatesList = appAuthTemplatesList.stream().filter(r -> r.getName().contains(condition.getName())).collect(Collectors.toList());
        }

        meetConditionTemplateList.addAll(appAuthTemplatesList);
        return meetConditionTemplateList;
    }

    private List<TemplateWithSrvVO> buildExtraAttribute(List<IndexTemplate> templateList) {
        if (CollectionUtils.isEmpty(templateList)) {
            return Lists.newArrayList();
        }

        List<TemplateWithSrvVO> templateWithSrvVOList = new ArrayList<>();
        for (IndexTemplate template : templateList) {
            TemplateWithSrvVO templateWithSrvVO = ConvertUtil.obj2Obj(template, TemplateWithSrvVO.class);
            templateWithSrvVO.setOpenSrv(ConvertUtil.list2List(TemplateSrv.codeStr2SrvList(template.getOpenSrv()), TemplateSrvVO.class));
            templateWithSrvVOList.add(templateWithSrvVO);
        }

        buildTemplateCluster(templateWithSrvVOList);
        buildTemplateUnavailableSrv(templateWithSrvVOList);

        return templateWithSrvVOList;
    }

    private void buildTemplateCluster(List<TemplateWithSrvVO> templateWithSrvVOList) {
        for (TemplateWithSrvVO templateSrvVO : templateWithSrvVOList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                Set<String> clusterNameList = indexTemplatePhyService.getTemplateByLogicId(templateSrvVO.getId())
                        .stream()
                        .map(IndexTemplatePhy::getCluster)
                        .collect(Collectors.toSet());

                templateSrvVO.setCluster(Lists.newArrayList(clusterNameList));
            });
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.waitExecute();
    }

    private void buildTemplateUnavailableSrv(List<TemplateWithSrvVO> templateWithSrvVOList) {
        for (TemplateWithSrvVO templateSrvVO : templateWithSrvVOList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL.runnableTask(() -> {
                templateSrvVO.setUnavailableSrv(ConvertUtil.list2List(templateSrvManager.getUnavailableSrv(templateSrvVO.getId()), UnavailableTemplateSrvVO.class));
            });
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_UNAVAILABLE_SRV_FUTURE_UTIL.waitExecute();
    }

}
