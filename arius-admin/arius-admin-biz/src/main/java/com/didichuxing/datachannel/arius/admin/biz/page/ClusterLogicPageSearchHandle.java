package com.didichuxing.datachannel.arius.admin.biz.page;

import com.didichuxing.datachannel.arius.admin.common.Triple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterLogicVO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.CommonUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.es.ESClusterNodeService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class ClusterLogicPageSearchHandle extends AbstractPageSearchHandle<ClusterLogicConditionDTO, ClusterLogicVO> {
    private static final ILog             LOGGER                  = LogFactory
        .getLog(ClusterLogicPageSearchHandle.class);

    @Autowired
    private ClusterLogicService           clusterLogicService;

  

    @Autowired
    private ClusterRegionService          clusterRegionService;

    @Autowired
    private ESClusterNodeService          eSClusterNodeService;



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
        setDiskUsedInfo(clusterLogicVO);
    }

    private void setDiskUsedInfo(ClusterLogicVO clusterLogicVO) {
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogicVO.getId());
        long diskTotal = 0L;
        long diskUsage = 0L;
        if (clusterRegion != null) {
            Map<String, Triple<Long, Long, Double>> map = eSClusterNodeService
                .syncGetNodesDiskUsage(clusterRegion.getPhyClusterName());
            Set<Map.Entry<String, Triple<Long, Long, Double>>> entries = map.entrySet();
            for (Map.Entry<String, Triple<Long, Long, Double>> entry : entries) {
                diskTotal += entry.getValue().v1();
                diskUsage += entry.getValue().v2();
            }
        }
        clusterLogicVO.setDiskTotal(diskTotal);
        clusterLogicVO.setDiskUsage(diskUsage);
        clusterLogicVO.setDiskUsagePercent(
            new BigDecimal((double) diskUsage / diskTotal).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue());
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
        if (!AriusObjUtils.isBlack(clusterLogicName)
            && (clusterLogicName.startsWith("*") || clusterLogicName.startsWith("?"))) {
            return Result.buildParamIllegal("逻辑集群名称不允许带类似*, ?等通配符查询");
        }

        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(ClusterLogicConditionDTO condition, Integer projectId) {
        if (!AuthConstant.SUPER_PROJECT_ID.equals(projectId)) {
            // 非超级管理员，获取拥有的逻辑集群对应的物理集群列表
            condition.setProjectId(projectId);
        }
        String sortTerm = null == condition.getSortTerm() ? SortConstant.ID : condition.getSortTerm();
        String sortType = condition.getOrderByDesc() ? SortConstant.DESC : SortConstant.ASC;
        condition.setSortTerm(sortTerm);
        condition.setSortType(sortType);
        condition.setFrom((condition.getPage() - 1) * condition.getSize());
    }

    @Override
    protected PaginationResult<ClusterLogicVO> buildPageData(ClusterLogicConditionDTO condition, Integer projectId) {
        if(StringUtils.isNotBlank(condition.getMemo())){
            condition.setMemo(CommonUtils.sqlFuzzyQueryTransfer(condition.getMemo()));
        }
        List<ClusterLogic> pagingGetClusterLogicList = clusterLogicService.pagingGetClusterLogicByCondition(condition);
        List<ClusterLogicVO> clusterLogicVOS = ConvertUtil.list2List(pagingGetClusterLogicList,ClusterLogicVO.class);
        long totalHit = clusterLogicService.fuzzyClusterLogicHitByCondition(condition);
        return PaginationResult.buildSucc(clusterLogicVOS, totalHit, condition.getPage(), condition.getSize());
    }
}