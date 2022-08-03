package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.UnavailableTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chengxiang
 * @date 2022/5/18
 */
@Component
public class TemplateSrvPageSearchHandle extends AbstractPageSearchHandle<TemplateQueryDTO, TemplateWithSrvVO> {
    private static final FutureUtil<TemplateWithSrvVO> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL         =
            FutureUtil
        .init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL", 10, 10, 100);
 

    
    @Autowired
    private IndexTemplateService          indexTemplateService;

    @Autowired
    private IndexTemplatePhyService       indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;

    @Override
    protected Result<Boolean> checkCondition(TemplateQueryDTO condition, Integer projectId) {

        String templateName = condition.getName();
        if (!AriusObjUtils.isBlack(templateName) && (templateName.startsWith("*") || templateName.startsWith("?"))) {
            return Result.buildParamIllegal("模板名称不能以*或者?开头");
        }

        return Result.buildSucc(Boolean.TRUE);
    }

    @Override
    protected void initCondition(TemplateQueryDTO condition, Integer projectId) {
        // nothing to do
    }

    @Override
    protected PaginationResult<TemplateWithSrvVO> buildPageData(TemplateQueryDTO condition, Integer projectId) {
    
        // 注意这里的condition是物理集群
        Integer totalHit;
        List<IndexTemplate> matchIndexTemplateList;
        if (AriusObjUtils.isBlank(condition.getCluster())) {
            matchIndexTemplateList = indexTemplateService.pagingGetTemplateSrvByCondition(condition);
            totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();
        } else {
            List<IndexTemplate> meetConditionTemplateList = getMatchConditionTemplateListByClusterName(condition);
            totalHit = meetConditionTemplateList.size();
            matchIndexTemplateList = filterFullDataByPage(meetConditionTemplateList, condition);
        }

        List<TemplateWithSrvVO> templateWithSrvVOList = buildExtraAttribute(matchIndexTemplateList);
        
        
        
        return PaginationResult.buildSucc(templateWithSrvVOList, totalHit, condition.getPage(), condition.getSize());
    }

    /******************************************private***********************************************/
    /**
     * 根据模板Id、名称、归属projectId、归属物理集群等进行组合查询
     *
     * @param condition
     * @return
     */
    private List<IndexTemplate> getMatchConditionTemplateListByClusterName(TemplateQueryDTO condition) {
        List<IndexTemplate> meetConditionTemplateList = Lists.newArrayList();
        List<IndexTemplatePhy> indexTemplatePhyList = indexTemplatePhyService
            .getNormalTemplateByCluster(condition.getCluster());
        if (CollectionUtils.isEmpty(indexTemplatePhyList)) {
            return meetConditionTemplateList;
        }

        List<Integer> matchTemplateLogicIdList = indexTemplatePhyList.stream().map(IndexTemplatePhy::getLogicId)
            .distinct().collect(Collectors.toList());
    
        Predicate<IndexTemplate> conditionNotNullIdPre = indexTemplate -> Objects.isNull(condition.getId())
                                                                          || Objects.equals(indexTemplate.getId(),
                condition.getId());
    
        Predicate<IndexTemplate> conditionNotNullNamePre = indexTemplate -> !StringUtils.isNotBlank(condition.getName())
                                                                            || StringUtils.equals(
                indexTemplate.getName(), condition.getName());
    
        Predicate<IndexTemplate> conditionNotNullProjectIdPre = indexTemplate ->
                Objects.isNull(condition.getProjectId()) || Objects.equals(indexTemplate.getProjectId(),
                        condition.getProjectId());
        
        return indexTemplateService.listLogicTemplatesByIds(matchTemplateLogicIdList).stream()
                .filter(conditionNotNullProjectIdPre).filter(conditionNotNullIdPre).filter(conditionNotNullNamePre)
                .collect(Collectors.toList());
                
        
    }

    private List<TemplateWithSrvVO> buildExtraAttribute(List<IndexTemplate> templateList) {
        if (CollectionUtils.isEmpty(templateList)) {
            return Lists.newArrayList();
        }
        final Map<Integer, String> projectId2ProjectName = ConvertUtil.list2Map(projectService.getProjectBriefList(),
                ProjectBriefVO::getId, ProjectBriefVO::getProjectName);
        // 构建基础信息
        for (IndexTemplate template : templateList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.callableTask(()-> buildTemplateWithSrvVO(
                    template,projectId2ProjectName));
        }
        return  TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.waitResult();
    }
    
    private TemplateWithSrvVO buildTemplateWithSrvVO(IndexTemplate template, Map<Integer, String> projectId2ProjectName) {
        TemplateWithSrvVO templateWithSrvVO = ConvertUtil.obj2Obj(template, TemplateWithSrvVO.class);
        templateWithSrvVO.setCluster(Lists.newArrayList());
        templateWithSrvVO.setOpenSrv(
                ConvertUtil.list2List(TemplateSrv.codeStr2SrvList(template.getOpenSrv()), TemplateSrvVO.class));
        Optional.ofNullable(template).map(IndexTemplate::getProjectId).map(projectId2ProjectName::get)
                .ifPresent(templateWithSrvVO::setProjectName);
        //这里会出现多个集群，那么就会产生问题，比如lyn-dcdr-1，lyn-dcdr-2两个集群，一种有插件，一个没有插件，那么其实关注master即可
        final List<IndexTemplatePhy> indexTemplatePhies = indexTemplatePhyService.getTemplateByLogicId(
                templateWithSrvVO.getId());
        String materClusterPhy = indexTemplatePhies.stream()
                .filter(i -> TemplateDeployRoleEnum.MASTER.getCode().equals(i.getRole()))
                .map(IndexTemplatePhy::getCluster).findFirst().orElse(null);
       
        indexTemplatePhies.stream().map(IndexTemplatePhy::getCluster).distinct()
                .forEach(templateWithSrvVO.getCluster()::add);
        templateWithSrvVO.setUnavailableSrv(ConvertUtil.list2List(
                templateSrvManager.getUnavailableSrvByTemplateAndMasterPhy(template, materClusterPhy),
                UnavailableTemplateSrvVO.class));
        templateWithSrvVO.setPartition(StringUtils.endsWith(template.getExpression(), "*"));
    
        return templateWithSrvVO;
        
    }

   



    /**
     * 对全量查询结果根据分页条件进行过滤
     *
     * @param condition 分页条件
     * @param source    全量查询结果
     * @return
     */
    <T> List<T> filterFullDataByPage(List<T> source, PageDTO condition) {
        //这里页码和前端对应起来，第一页页码是1 而不是0
        long fromIndex = condition.getSize() * (condition.getPage() - 1);
        long toIndex = getLastPageSize(condition, source.size());
        return source.subList((int) fromIndex, (int) toIndex);
    }

    /**
     * 获取最后一条数据的index，以防止数组溢出
     *
     * @param condition      分页条件
     * @param pageSizeFromDb 查询结果
     * @return
     */
    long getLastPageSize(PageDTO condition, Integer pageSizeFromDb) {
        //分页最后一条数据的index
        long size = condition.getPage() * condition.getSize();
        if (pageSizeFromDb < size) {
            size = pageSizeFromDb;
        }
        return size;
    }
}