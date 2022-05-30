package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortTermEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.ProjectService;

/**
 *
 * @author ohushenglin_v
 * @date 2022-05-27
 */
@Component
public class ClusterPhyPageSearchHandle extends AbstractPageSearchHandle<ClusterPhyConditionDTO,ClusterPhyVO> {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterPhyPageSearchHandle.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ClusterPhyService        clusterPhyService;
    @Autowired
    private ClusterContextManager clusterContextManager;
    @Autowired
    private ClusterLogicService clusterLogicService;
    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @Override
    protected Result<Boolean> checkCondition(ClusterPhyConditionDTO condition, Integer appId) {

        Integer status = condition.getHealth();
        if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
            return Result.buildParamIllegal("集群状态类型不存在");
        }

        String clusterPhyName = condition.getCluster();
        if (StringUtils.containsAny(clusterPhyName, "*", "?")) {
            return Result.buildParamIllegal("物理集群名称不允许带类似*, ?等通配符查询");
        }

        if (null != condition.getSortTerm() && !SortTermEnum.isExit(condition.getSortTerm())) {
            return Result.buildParamIllegal(String.format("暂且不支持排序字段[%s]", condition.getSortTerm()));
        }

        return Result.buildSucc(true);
    }
    
    @Override
    protected void initCondition(ClusterPhyConditionDTO condition, Integer projectId) {
        
        // 1. 获取管理/读写/读/无权限的物理集群信息
        List<String> clusterNames = new ArrayList<>();
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByProjectId(projectId);
            //项目下的有管理权限逻辑集群会关联多个物理集群
            clusterLogicList.stream().map(ClusterLogic::getId).map(clusterContextManager::getClusterLogicContextCache)
                .map(ClusterLogicContext::getAssociatedClusterPhyNames).forEach(clusterNames::addAll);
            clusterNames = clusterNames.stream().distinct().collect(Collectors.toList());
        }
        condition.setClusterNames(clusterNames);
        String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
        String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        condition.setSortTerm(sortTerm);
        condition.setSortType(sortType);
        condition.setFrom((condition.getPage() - 1) * condition.getSize());
    }

    @Override
    protected PaginationResult<ClusterPhyVO> buildPageData(ClusterPhyConditionDTO condition, Integer projectId) {

        List<ClusterPhy> pagingGetClusterPhyList      =  clusterPhyService.pagingGetClusterPhyByCondition(condition);

        List<ClusterPhyVO> clusterPhyVOList = clusterPhyManager.buildClusterInfo(pagingGetClusterPhyList, projectId);

        long totalHit = clusterPhyService.fuzzyClusterPhyHitByCondition(condition);
        return PaginationResult.buildSucc(clusterPhyVOList, totalHit, condition.getPage(), condition.getSize());
    }
}