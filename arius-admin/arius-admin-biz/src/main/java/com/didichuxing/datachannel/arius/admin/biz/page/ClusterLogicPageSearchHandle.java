package com.didichuxing.datachannel.arius.admin.biz.page;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.didichuxing.datachannel.arius.admin.biz.app.ProjectClusterLogicAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterLogicConditionDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ConsoleClusterVO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
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
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class ClusterLogicPageSearchHandle extends BasePageSearchHandle<ConsoleClusterVO> {

    private static final ILog          LOGGER = LogFactory.getLog(ClusterLogicPageSearchHandle.class);

    @Autowired
    private AppService                 appService;

    @Autowired
    private ClusterLogicService        clusterLogicService;

    @Autowired
    private ClusterLogicManager        clusterLogicManager;

    @Autowired
    private ProjectClusterLogicAuthManager projectClusterLogicAuthManager;

    @Autowired
    private ClusterContextManager      clusterContextManager;

    private static final FutureUtil<Void> futureUtilForClusterNum      = FutureUtil.init("futureUtilForClusterNum",10,10,100);

    @Override
    protected Result<Boolean> validCheckForAppId(Integer projectId) {
        if (!appService.isAppExists(projectId)) {
            return Result.buildParamIllegal("项目不存在");
        }
        return Result.buildSucc(true);
    }

    @Override
    protected Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer projectId) {
        if (pageDTO instanceof ClusterLogicConditionDTO) {
            ClusterLogicConditionDTO clusterLogicConditionDTO = (ClusterLogicConditionDTO) pageDTO;
            Integer authType = clusterLogicConditionDTO.getAuthType();
            if (null != authType && !ProjectClusterLogicAuthEnum.isExitByCode(authType)) {
                return Result.buildParamIllegal("权限类型不存在");
            }

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

        LOGGER.error("class=ClusterLogicPageSearchHandle||method=validCheckForCondition||errMsg=failed to convert PageDTO to ClusterLogicConditionDTO");

        return Result.buildFail();
    }

    @Override
    protected void init(PageDTO pageDTO) {
        // Do nothing
    }

    @Override
    protected PaginationResult<ConsoleClusterVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer projectId) {
        ClusterLogicConditionDTO condition = buildClusterLogicConditionDTO(pageDTO);

        //1. 获取管理/访问/无权限的逻辑集群信息
        List<ClusterLogic> appAuthClusterLogicList = clusterLogicManager.getClusterLogicByAppIdAndAuthType(projectId, condition.getAuthType());
        if (CollectionUtils.isEmpty(appAuthClusterLogicList)) {
            return PaginationResult.buildSucc(null, 0, condition.getPage(), condition.getSize());
        }

        //2. 过滤出符合条件的列表
        List<ClusterLogic> meetConditionClusterLogicList = getMeetConditionClusterLogicList(condition, appAuthClusterLogicList);

        //3. 设置命中数
        long hitTotal = meetConditionClusterLogicList.size();

        //4. 对条件匹配后的结果集进行排序
        sort(meetConditionClusterLogicList, condition.getSortTerm(), condition.getOrderByDesc());

        //5.内存分页
        List<ClusterLogic> fuzzyAndLimitClusterPhyList  = filterFullDataByPage(meetConditionClusterLogicList, condition) ;
        List<ConsoleClusterVO> consoleClusterVOList     = ConvertUtil.list2List(fuzzyAndLimitClusterPhyList, ConsoleClusterVO.class);

        //6. 设置集群权限类型
        consoleClusterVOList.forEach(consoleClusterVO -> consoleClusterVO.setAuthType(condition.getAuthType()));

        //7. 设置逻辑集群基本信息
        for (ConsoleClusterVO consoleClusterVO : consoleClusterVOList) {
            futureUtilForClusterNum.runnableTask(() -> setConsoleClusterBasicInfo(consoleClusterVO));
        }
        futureUtilForClusterNum.waitExecute();

        return PaginationResult.buildSucc(consoleClusterVOList, hitTotal, condition.getPage(), condition.getSize());
    }

    @Override
    protected PaginationResult<ConsoleClusterVO> buildWithoutAuthType(PageDTO pageDTO, Integer projectId) {
        ClusterLogicConditionDTO condition = buildClusterLogicConditionDTO(pageDTO);
        
        List<ClusterLogic> pagingGetClusterLogicList   =  clusterLogicService.pagingGetClusterLogicByCondition(condition);
        List<ConsoleClusterVO> consoleClusterPhyVOList =  doBuildWithoutAuthType(pagingGetClusterLogicList, projectId);

        long totalHit = clusterLogicService.fuzzyClusterLogicHitByCondition(condition);

        return PaginationResult.buildSucc(consoleClusterPhyVOList, totalHit, pageDTO.getPage(), pageDTO.getSize());
    }

    private List<ConsoleClusterVO> doBuildWithoutAuthType(List<ClusterLogic> clusterLogicList, Integer appId) {
        if (CollectionUtils.isEmpty(clusterLogicList)) {
            return Lists.newArrayList();
        }

        //获取项目对集群列表的权限信息
        List<AppClusterLogicAuth> appClusterLogicAuthList = projectClusterLogicAuthManager.getByClusterLogicListAndAppId(appId, clusterLogicList);
        Map<Long, AppClusterLogicAuth> clusterLogicId2AppClusterLogicAuthMap = ConvertUtil.list2Map(appClusterLogicAuthList,
                AppClusterLogicAuth::getLogicClusterId);

        List<ConsoleClusterVO> consoleClusterVOList = ConvertUtil.list2List(clusterLogicList, ConsoleClusterVO.class);
        //1. 设置权限
        for (ConsoleClusterVO consoleClusterVO : consoleClusterVOList) {
            AppClusterLogicAuth appClusterLogicAuth = clusterLogicId2AppClusterLogicAuthMap.get(consoleClusterVO.getId());
            if (appClusterLogicAuth == null) {
                continue;
            }
            consoleClusterVO.setAuthType(appClusterLogicAuth.getType());
            consoleClusterVO.setAuthId(appClusterLogicAuth.getId());
        }

        //2. 设置基本信息
        for (ConsoleClusterVO consoleClusterVO : consoleClusterVOList) {
            futureUtilForClusterNum.runnableTask(() -> setConsoleClusterBasicInfo(consoleClusterVO));
        }
        futureUtilForClusterNum.waitExecute();

        return consoleClusterVOList;
    }

    private ClusterLogicConditionDTO buildClusterLogicConditionDTO(PageDTO pageDTO) {
        if (pageDTO instanceof ClusterLogicConditionDTO) {
            return (ClusterLogicConditionDTO) pageDTO;
        }
        return null;
    }

    private List<ClusterLogic> getMeetConditionClusterLogicList(ClusterLogicConditionDTO condition,
                                                              List<ClusterLogic> appAuthClusterLogicList) {
        List<ClusterLogic> meetConditionClusterLogicList = Lists.newArrayList();

        //分页查询条件中只存在集群名称
        if (!AriusObjUtils.isBlack(condition.getName())) {
            appAuthClusterLogicList = appAuthClusterLogicList
                    .stream()
                    .filter(r -> r.getName().contains(condition.getName()))
                    .collect(Collectors.toList());
        }

        //分页查询条件中仅存在集群类型
        if (null != condition.getType()) {
            appAuthClusterLogicList = appAuthClusterLogicList
                    .stream()
                    .filter(r -> r.getType().equals(condition.getType()))
                    .collect(Collectors.toList());
        }

        //分页查询条件中仅存在状态名称
        if (null != condition.getHealth()) {
            appAuthClusterLogicList = appAuthClusterLogicList
                    .stream()
                    .filter(r -> r.getHealth().equals(condition.getHealth()))
                    .collect(Collectors.toList());
        }

        //分页查询条件中仅存在项目Id
        if (null != condition.getAppId()) {
            appAuthClusterLogicList = appAuthClusterLogicList
                    .stream()
                    .filter(r -> r.getProjectId().equals(condition.getAppId()))
                    .collect(Collectors.toList());
        }
        meetConditionClusterLogicList.addAll(appAuthClusterLogicList);
        return meetConditionClusterLogicList;
    }

    /**
     * 1. 设置项目名称
     * 2. 关联物理集群标识
     * 3. 集群版本
     * @param consoleClusterVO   逻辑集群源信息
     */
    private void setConsoleClusterBasicInfo(ConsoleClusterVO consoleClusterVO) {
        if (null == consoleClusterVO) {
            return;
        }
        setResponsible(consoleClusterVO);
        setAppName(consoleClusterVO);
        setClusterPhyFlagAndDataNodeNum(consoleClusterVO);
    }

    private void setResponsible(ConsoleClusterVO consoleClusterVO) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(consoleClusterVO.getId());
        if(clusterLogic == null) {
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
        App app = appService.getAppById(consoleClusterVO.getProjectId());
        if (null != app && !AriusObjUtils.isBlack(app.getName())) {
            consoleClusterVO.setAppName(app.getName());
        }
    }


    /**
     * 对条件匹配后的结果集进行排序
     * @param meetConditionClusterLogicList  条件匹配结果集
     * @param sortTerm                       排序字段
     * @see   SortTermEnum                   支持的排序字段枚举
     * @param orderByDesc                    是否降序排序 true 是 false 否
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
}