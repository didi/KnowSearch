package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.dcdr.TemplateDCDRManager;
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
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private static final FutureUtil<Void> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL         = FutureUtil
        .init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL", 10, 10, 100);
 

    @Autowired
    private TemplateDCDRManager templateDCDRManager;
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

        List<TemplateWithSrvVO> templateWithSrvVOList = buildExtraAttribute(matchIndexTemplateList,condition.getProjectId());
        
        
        
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
    
        Predicate<IndexTemplate> conditionNotNullIdPre = indexTemplate -> {
            if (null != condition.getId()) {
                return Objects.equals(indexTemplate.getId(), condition.getId());
            } else {
                return true;
            }
        };
        Predicate<IndexTemplate> conditionNotNullNamePre = indexTemplate -> {
            if (StringUtils.isNotBlank(condition.getName())) {
                return StringUtils.equals(indexTemplate.getName(), condition.getName());
            } else {
                return true;
            }
        };
        Predicate<IndexTemplate> conditionNotNullProjectIdPre = indexTemplate -> {
            if (null != condition.getProjectId()) {
                return Objects.equals(indexTemplate.getProjectId(), condition.getProjectId());
            } else {
                return true;
            }
        };
        return indexTemplateService.listLogicTemplatesByIds(matchTemplateLogicIdList).stream()
                .filter(conditionNotNullProjectIdPre).filter(conditionNotNullIdPre).filter(conditionNotNullNamePre)
                .collect(Collectors.toList());
                
        
    }

    private List<TemplateWithSrvVO> buildExtraAttribute(List<IndexTemplate> templateList,Integer projectId) {
        if (CollectionUtils.isEmpty(templateList)) {
            return Lists.newArrayList();
        }
        final String projectName = Optional.ofNullable(projectId).map(projectService::getProjectBriefByProjectId)
                .map(ProjectBriefVO::getProjectName).orElse(null);
        List<TemplateWithSrvVO> templateWithSrvVOList = new CopyOnWriteArrayList<>();
        // 构建基础信息
        for (IndexTemplate template : templateList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.runnableTask(()-> buildTemplateWithSrvVO(
                    template,projectName,templateWithSrvVOList));
           
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.waitExecute();

        return templateWithSrvVOList;
    }
    
    private void buildTemplateWithSrvVO(IndexTemplate template, String projectName,
                                        List<TemplateWithSrvVO> templateWithSrvVOList) {
        TemplateWithSrvVO templateWithSrvVO = ConvertUtil.obj2Obj(template, TemplateWithSrvVO.class);
        templateWithSrvVO.setCluster(Lists.newArrayList());
        templateWithSrvVO.setOpenSrv(
                ConvertUtil.list2List(TemplateSrv.codeStr2SrvList(template.getOpenSrv()), TemplateSrvVO.class));
        if (StringUtils.isNotBlank(projectName)) {
            templateWithSrvVO.setProjectName(projectName);
        } else {
            Optional.ofNullable(template).map(IndexTemplate::getProjectId)
                    .map(projectService::getProjectBriefByProjectId).map(ProjectBriefVO::getProjectName)
                    .ifPresent(templateWithSrvVO::setProjectName);
        }
   
        indexTemplatePhyService.getTemplateByLogicId(templateWithSrvVO.getId()).stream()
                .map(IndexTemplatePhy::getCluster).distinct().forEach(templateWithSrvVO.getCluster()::add);
        templateWithSrvVO.setPartition(template.getExpression().endsWith("*"));
        templateWithSrvVO.setUnavailableSrv(
                ConvertUtil.list2List(templateSrvManager.getUnavailableSrv(templateWithSrvVO.getId()),
                        UnavailableTemplateSrvVO.class));
        templateWithSrvVOList.add(templateWithSrvVO);
        
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