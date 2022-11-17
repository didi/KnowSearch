package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.impl.ClusterPhyManagerImpl;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.srv.TemplateQueryDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.srv.TemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterConnectionStatusWithTemplateVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.TemplateWithSrvVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.template.srv.UnavailableTemplateSrvVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AdminConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterConnectionStatusWithTemplateEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.tuple.TupleThree;
import com.didichuxing.datachannel.arius.admin.common.util.*;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.security.common.vo.project.ProjectBriefVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
     private static final ILog                          LOGGER                                                    = LogFactory
        .getLog(ClusterPhyManagerImpl.class);
    private static final  FutureUtil<TemplateWithSrvVO> TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL =
            FutureUtil
        .init("TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL", 10, 10, 100);
    
    private static final String HEALTH      = "health";
    private static final String CHECK_POINT_DIFF = "check_point_diff";
    @Autowired
    private IndexTemplateService          indexTemplateService;

    @Autowired
    private IndexTemplatePhyService       indexTemplatePhyService;

    @Autowired
    private TemplateSrvManager            templateSrvManager;
    @Autowired
    private   ClusterRegionService clusterRegionService;
    @Autowired
    protected ClusterPhyManager clusterPhyManager;

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
        Integer totalHit ;
        List<IndexTemplate> matchIndexTemplateList;
        if(StringUtils.isNotBlank(condition.getName())){
            condition.setName(CommonUtils.sqlFuzzyQueryTransfer(condition.getName()));
        }
        // 如果存物理集群，则需要通过物理集群找到指定的逻辑集群
        if (StringUtils.isNotBlank(condition.getCluster())) {
            List<Integer> logicClusterIdList = clusterRegionService.listPhyClusterRegions(condition.getCluster())
                    .stream().map(ClusterRegion::getLogicClusterIds)
                    .filter(clusterLogicId -> !AdminConstant.REGION_NOT_BOUND_LOGIC_CLUSTER_ID.equals(clusterLogicId))
                    .map(ListUtils::string2IntList).flatMap(Collection::stream).distinct().collect(Collectors.toList());
            if (CollectionUtils.isEmpty(logicClusterIdList)) {
                return PaginationResult.buildSucc(Collections.emptyList(),0,condition.getPage(),condition.getSize());
            }
            totalHit = indexTemplateService.fuzzyLogicTemplatesHitByConditionAndLogicClusterIdList(condition,
                    logicClusterIdList).intValue();
            matchIndexTemplateList =
                    indexTemplateService.pagingGetTemplateSrvByConditionAndLogicClusterIdList(condition,logicClusterIdList);
        } else {
            totalHit = indexTemplateService.fuzzyLogicTemplatesHitByCondition(condition).intValue();
            matchIndexTemplateList = indexTemplateService.pagingGetTemplateSrvByCondition(condition);
        }
        List<TemplateWithSrvVO> templateWithSrvVOList = buildExtraAttribute(matchIndexTemplateList);
        
        
        
        return PaginationResult.buildSucc(templateWithSrvVOList, totalHit, condition.getPage(), condition.getSize());
    }

 
    private List<TemplateWithSrvVO> buildExtraAttribute(List<IndexTemplate> templateList) {
        if (CollectionUtils.isEmpty(templateList)) {
            return Lists.newArrayList();
        }
        final Map<Integer, String> projectId2ProjectName = ConvertUtil.list2Map(projectService.getProjectBriefList(),
                ProjectBriefVO::getId, ProjectBriefVO::getProjectName);
        List<Integer> logicTemplateIds =
                templateList.stream().map(IndexTemplate::getId).distinct().collect(Collectors.toList());
        List<IndexTemplatePhy> templatePhies = indexTemplatePhyService.getTemplateByLogicIds(logicTemplateIds);
        Map<Integer, List<IndexTemplatePhy>> logicId2IndexTemplatePhyListMap = ConvertUtil.list2MapOfList(templatePhies,
                IndexTemplatePhy::getLogicId, i -> i);
        List<String> clusterPhyList = templatePhies.stream().map(IndexTemplatePhy::getCluster).distinct()
                .collect(Collectors.toList());
        Map<String, ClusterConnectionStatusWithTemplateEnum> cluster2ClusterConnectionStatusWithTemplateEnumMap = Maps.newConcurrentMap();
        Map<String, TupleThree</*dcdrExist*/Boolean,/*pipelineExist*/ Boolean,/*existColdRegion*/ Boolean>> cluster2ExistDCDRAndPipelineModuleMap= Maps.newConcurrentMap();
        for (String clusterPhy : clusterPhyList) {
            TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.runnableTask(() -> {
                cluster2ClusterConnectionStatusWithTemplateEnumMap.put(clusterPhy,
                        clusterPhyManager.getClusterConnectionStatusWithCache(clusterPhy));
                cluster2ExistDCDRAndPipelineModuleMap.put(clusterPhy,
                        clusterPhyManager.getDCDRAndPipelineAndColdRegionTupleByClusterPhyWithCache(clusterPhy));
            });
        }
        TEMPLATE_SRV_PAGE_SEARCH_HANDLE_BUILD_CLUSTER_FUTURE_UTIL.waitExecute();
       
        // 构建基础信息
    
        return templateList.stream().map(template -> buildTemplateWithSrvVO(template, projectId2ProjectName,
                logicId2IndexTemplatePhyListMap, cluster2ClusterConnectionStatusWithTemplateEnumMap,
                cluster2ExistDCDRAndPipelineModuleMap)).collect(Collectors.toList());
    }
    
    private TemplateWithSrvVO buildTemplateWithSrvVO(IndexTemplate template, Map<Integer, String> projectId2ProjectName,
                                                     Map<Integer, List<IndexTemplatePhy>> logicId2IndexTemplatePhyListMap,
                                                     Map<String, ClusterConnectionStatusWithTemplateEnum> cluster2ClusterConnectionStatusWithTemplateEnumMap,
                                                     Map<String, TupleThree<Boolean, Boolean, Boolean>> cluster2ExistDCDRAndPipelineModuleMap) {
        TemplateWithSrvVO templateWithSrvVO = ConvertUtil.obj2Obj(template, TemplateWithSrvVO.class);
        templateWithSrvVO.setCluster(Lists.newArrayList());
        templateWithSrvVO.setOpenSrv(
                ConvertUtil.list2List(TemplateSrv.codeStr2SrvList(template.getOpenSrv()), TemplateSrvVO.class));
        Optional.ofNullable(template).map(IndexTemplate::getProjectId).map(projectId2ProjectName::get)
                .ifPresent(templateWithSrvVO::setProjectName);
        templateWithSrvVO.setPartition(StringUtils.endsWith(template.getExpression(), "*"));
        //这里整改为只要校验master即可，原因是由于我们在创建链路/获取相同版本出得集群的时候，进行插件的校验，不能放在这里，会损耗性能
        final List<IndexTemplatePhy> indexTemplatePhies = logicId2IndexTemplatePhyListMap.get(templateWithSrvVO.getId());
        if (Objects.isNull(indexTemplatePhies)) {
            LOGGER.warn("class={}||method=buildTemplateWithSrvVO||logicTemplateId={} 未匹配到物理模板，属于脏数据",
                    getClass().getSimpleName(), templateWithSrvVO.getId());
            return templateWithSrvVO;
        }
        indexTemplatePhies.stream().filter(i -> TemplateDeployRoleEnum.MASTER.getCode().equals(i.getRole()))

                .map(IndexTemplatePhy::getCluster)
                .map(cluster -> templateSrvManager.getUnavailableSrvByTemplateAndMasterPhy(template,
                        cluster2ExistDCDRAndPipelineModuleMap.get(cluster)))
                .map(unavailableTemplateSrvs -> ConvertUtil.list2List(Lists.newArrayList(unavailableTemplateSrvs),
                        UnavailableTemplateSrvVO.class)).findFirst().ifPresent(templateWithSrvVO::setUnavailableSrv);
        indexTemplatePhies.stream().map(IndexTemplatePhy::getCluster).distinct()
                .forEach(templateWithSrvVO.getCluster()::add);

        final List<ClusterConnectionStatusWithTemplateVO> statusWithTemplateList = indexTemplatePhies.stream()
                //获取到主副本集群的连通状态
                .map(indexTemplatePhy -> new ClusterConnectionStatusWithTemplateVO(indexTemplatePhy.getCluster(),
                        cluster2ClusterConnectionStatusWithTemplateEnumMap.get(indexTemplatePhy.getCluster())))
                .collect(Collectors.toList());
    
        templateWithSrvVO.setClusterConnectionStatus(statusWithTemplateList);
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