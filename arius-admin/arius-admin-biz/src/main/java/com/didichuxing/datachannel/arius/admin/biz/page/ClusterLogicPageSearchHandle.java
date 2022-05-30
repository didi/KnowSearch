package com.didichuxing.datachannel.arius.admin.biz.page;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterLogicAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterLogicDiskUsedInfoPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.constant.SortConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterResourceTypeEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.constant.SortTermEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class ClusterLogicPageSearchHandle extends AbstractPageSearchHandle<ClusterLogicConditionDTO, ConsoleClusterVO> {
    private static final ILog LOGGER = LogFactory.getLog(ClusterLogicPageSearchHandle.class);

    @Autowired
    private AppService appService;

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
     * @param consoleClusterVO 逻辑集群源信息
     */
    private void setConsoleClusterBasicInfo(ConsoleClusterVO consoleClusterVO) {
        if (null == consoleClusterVO) {
            return;
        }
        setResponsible(consoleClusterVO);
        setAppName(consoleClusterVO);
        setClusterPhyFlagAndDataNodeNum(consoleClusterVO);
        setDiskUsedInfo(consoleClusterVO);
    }

    private void setDiskUsedInfo(ConsoleClusterVO consoleClusterVO) {
        ClusterLogicDiskUsedInfoPO clusterLogicDiskUsedInfoPO =
                clusterLogicService.getDiskInfo(consoleClusterVO.getId());
        consoleClusterVO.setDiskTotal(clusterLogicDiskUsedInfoPO.getDiskTotal());
        consoleClusterVO.setDiskUsage(clusterLogicDiskUsedInfoPO.getDiskUsage());
        consoleClusterVO.setDiskUsagePercent(clusterLogicDiskUsedInfoPO.getDiskUsagePercent());
    }

    private void setResponsible(ConsoleClusterVO consoleClusterVO) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(consoleClusterVO.getId());
        if (clusterLogic == null) {
            return;
        }
        consoleClusterVO.setResponsible(clusterLogic.getResponsible());
    }

    private void setClusterPhyFlagAndDataNodeNum(ConsoleClusterVO consoleClusterVO) {
        ClusterLogicContext clusterLogicContext = clusterContextManager.getClusterLogicContext(consoleClusterVO.getId());
        if (null == clusterLogicContext || CollectionUtils.isEmpty(clusterLogicContext.getAssociatedClusterPhyNames())) {
            consoleClusterVO.setPhyClusterAssociated(false);
            consoleClusterVO.setDataNodesNumber(0);
        } else {
            consoleClusterVO.setPhyClusterAssociated(true);
            consoleClusterVO.setDataNodesNumber(clusterLogicContext.getAssociatedDataNodeNum());
        }
    }

    private void setAppName(ConsoleClusterVO consoleClusterVO) {
        App app = appService.getAppById(consoleClusterVO.getAppId());
        if (null != app && !AriusObjUtils.isBlack(app.getName())) {
            consoleClusterVO.setAppName(app.getName());
        }
    }


    /**
     * 对条件匹配后的结果集进行排序
     *
     * @param meetConditionClusterLogicList 条件匹配结果集
     * @param sortTerm                      排序字段
     * @param orderByDesc                   是否降序排序 true 是 false 否
     * @see SortTermEnum                   支持的排序字段枚举
     */
    private void sort(List<ClusterLogic> meetConditionClusterLogicList, String sortTerm, Boolean orderByDesc) {
        // TODO: 排序逻辑简化
        // 使用默认排序
        if (null == sortTerm) {
            Collections.sort(meetConditionClusterLogicList);
            return;
        }

        meetConditionClusterLogicList.sort((o1, o2) -> {
            // 可在此添加需要排序的项
            if (SortTermEnum.LEVEL.getType().equals(sortTerm)) {
                return orderByDesc ? o2.getLevel().compareTo(o1.getLevel()) : o1.getLevel().compareTo(o2.getLevel());
            }

            // 返回0 不排序
            return 0;
        });
    }

    @Override
    protected Result<Boolean> checkCondition(ClusterLogicConditionDTO clusterLogicConditionDTO, Integer appId) {

        Integer status = clusterLogicConditionDTO.getHealth();
        if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
            return Result.buildParamIllegal("逻辑集群状态类型不存在");
        }

        if (null != clusterLogicConditionDTO.getType()
                && !ClusterResourceTypeEnum.isExist(clusterLogicConditionDTO.getType())) {
            return Result.buildParamIllegal("逻辑集群类型不存在");
        }

        if (null != clusterLogicConditionDTO.getAppId()
                && !appService.isAppExists(clusterLogicConditionDTO.getAppId())) {
            return Result.buildParamIllegal("逻辑集群所属项目不存在");
        }

        String clusterLogicName = clusterLogicConditionDTO.getName();
        if (!AriusObjUtils.isBlack(clusterLogicName) && (clusterLogicName.startsWith("*") || clusterLogicName.startsWith("?"))) {
            return Result.buildParamIllegal("逻辑集群名称不允许带类似*, ?等通配符查询");
        }

        return Result.buildSucc(true);
    }

    @Override
    protected void initCondition(ClusterLogicConditionDTO condition, Integer appId) {
        boolean isSuperApp = appService.isSuperApp(appId);
        // 1. 获取登录用户，当前项目下的我的集群列表
        List<String> clusterNames = new ArrayList<>();
        if (!isSuperApp) {
            List<ClusterLogic> clusterLogicList = clusterLogicService.getOwnedClusterLogicListByAppId(appId);
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
    protected PaginationResult<ConsoleClusterVO> buildPageData(ClusterLogicConditionDTO condition, Integer appId) {
        List<ClusterLogic> pagingGetClusterLogicList = clusterLogicService.pagingGetClusterLogicByCondition(condition);

        List<ConsoleClusterVO> consoleClusterVOS = clusterLogicManager.batchBuildOpClusterVOs(pagingGetClusterLogicList, appId);
        for (ConsoleClusterVO consoleClusterVO : consoleClusterVOS) {
            setConsoleClusterBasicInfo(consoleClusterVO);
        }

        //7. 设置逻辑集群基本信息
        for (ConsoleClusterVO consoleClusterVO : consoleClusterVOS) {
            futureUtilForClusterNum.runnableTask(() -> setConsoleClusterBasicInfo(consoleClusterVO));
        }
        futureUtilForClusterNum.waitExecute();
        long totalHit = clusterLogicService.fuzzyClusterLogicHitByCondition(condition);
        return PaginationResult.buildSucc(consoleClusterVOS, totalHit, condition.getPage(), condition.getSize());
    }
}
