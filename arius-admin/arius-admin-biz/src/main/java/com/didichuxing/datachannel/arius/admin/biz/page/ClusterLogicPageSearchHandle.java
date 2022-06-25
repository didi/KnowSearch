package com.didichuxing.datachannel.arius.admin.biz.page;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicDiskUsedInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class ClusterLogicPageSearchHandle extends AbstractPageSearchHandle<ClusterLogicConditionDTO, ClusterLogicVO> {
    private static final ILog LOGGER = LogFactory.getLog(ClusterLogicPageSearchHandle.class);

  

    @Autowired
    private ClusterLogicService clusterLogicService;

    @Autowired
    private ClusterLogicManager clusterLogicManager;

    @Autowired
    private ClusterContextManager clusterContextManager;

    private static final FutureUtil<Void> futureUtilForClusterNum = FutureUtil.init("futureUtilForClusterNum", 10, 10, 100);

    /**
     * 1. 设置项目名称
     * 2. 关联物理集群标识
     * 3. 集群版本
     *
     * @param clusterLogicVO 逻辑集群源信息
     */
    private void setClusterLogicBasicInfo(ClusterLogicVO clusterLogicVO) {
        if (null == clusterLogicVO) {
            return;
        }
        setResponsible(clusterLogicVO);
        setProjectName(clusterLogicVO);
        setClusterPhyFlagAndDataNodeNum(clusterLogicVO);
        setDiskUsedInfo(clusterLogicVO);
    }

    private void setDiskUsedInfo(ClusterLogicVO clusterLogicVO) {
        ClusterLogicDiskUsedInfoPO clusterLogicDiskUsedInfoPO =
                clusterLogicService.getDiskInfo(clusterLogicVO.getId());
        clusterLogicVO.setDiskTotal(clusterLogicDiskUsedInfoPO.getDiskTotal());
        clusterLogicVO.setDiskUsage(clusterLogicDiskUsedInfoPO.getDiskUsage());
        clusterLogicVO.setDiskUsagePercent(clusterLogicDiskUsedInfoPO.getDiskUsagePercent());
    }

    private void setResponsible(ClusterLogicVO clusterLogicVO) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(clusterLogicVO.getId());
        if (clusterLogic == null) {
            return;
        }
        clusterLogicVO.setResponsible(clusterLogic.getResponsible());
    }

    private void setClusterPhyFlagAndDataNodeNum(ClusterLogicVO clusterLogicVO) {
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(clusterLogicVO.getId());
        if (null == clusterLogicContext || CollectionUtils.isEmpty(clusterLogicContext.getAssociatedClusterPhyNames())) {
            clusterLogicVO.setPhyClusterAssociated(false);
            clusterLogicVO.setDataNodesNumber(0);
        } else {
            clusterLogicVO.setPhyClusterAssociated(true);
            clusterLogicVO.setDataNodesNumber(clusterLogicContext.getAssociatedDataNodeNum());
        }
    }

    private void setProjectName(ClusterLogicVO clusterLogicVO) {
        Optional.ofNullable(clusterLogicVO.getProjectId())
                .map(projectService::getProjectBriefByProjectId)
                .map(ProjectBriefVO::getProjectName).ifPresent(clusterLogicVO::setProjectName);
        
    }
    @Override
    protected Result<Boolean> checkCondition(ClusterLogicConditionDTO clusterLogicConditionDTO, Integer projectId) {

        Integer status = clusterLogicConditionDTO.getHealth();
        if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
            return Result.buildParamIllegal("逻辑集群状态类型不存在");
        }

        if (null != clusterLogicConditionDTO.getType()
                && !ClusterResourceTypeEnum.isExist(clusterLogicConditionDTO.getType())) {
            return Result.buildParamIllegal("逻辑集群类型不存在");
        }

            if (null != clusterLogicConditionDTO.getProjectId()
                    && !projectService.checkProjectExist(clusterLogicConditionDTO.getProjectId())) {
            return Result.buildParamIllegal("逻辑集群所属项目不存在");
        }

        String clusterLogicName = clusterLogicConditionDTO.getName();
        if (!AriusObjUtils.isBlack(clusterLogicName) && (clusterLogicName.startsWith("*") || clusterLogicName.startsWith("?"))) {
            return Result.buildParamIllegal("逻辑集群名称不允许带类似*, ?等通配符查询");
        }

        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(ClusterLogicConditionDTO condition, Integer projectId) {
        
        // 1. 获取登录用户，当前项目下的我的集群列表
        List<String> clusterNames = new ArrayList<>();
        if (!Objects.equals(projectId, AuthConstant.SUPER_PROJECT_ID)) {
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
    protected PaginationResult<ClusterLogicVO> buildPageData(ClusterLogicConditionDTO condition, Integer projectId) {
        List<ClusterLogic> pagingGetClusterLogicList = clusterLogicService.pagingGetClusterLogicByCondition(condition);

        List<ClusterLogicVO> clusterLogicVOS = clusterLogicManager.buildClusterLogics(pagingGetClusterLogicList);

        //7. 设置逻辑集群基本信息
        for (ClusterLogicVO clusterLogicVO : clusterLogicVOS) {
            futureUtilForClusterNum.runnableTask(() -> setClusterLogicBasicInfo(clusterLogicVO));
        }
        futureUtilForClusterNum.waitExecute();
        long totalHit = clusterLogicService.fuzzyClusterLogicHitByCondition(condition);
        return PaginationResult.buildSucc(clusterLogicVOS, totalHit, condition.getPage(), condition.getSize());
    }
}