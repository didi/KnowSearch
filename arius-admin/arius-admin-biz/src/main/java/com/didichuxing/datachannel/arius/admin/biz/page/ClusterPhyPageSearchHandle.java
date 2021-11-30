package com.didichuxing.datachannel.arius.admin.biz.page;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.app.AppClusterPhyAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterPhyManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.PaginationResult;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.PageDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ClusterPhyConditionDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.cluster.ConsoleClusterPhyVO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.constant.cluster.ClusterHealthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.FutureUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * Created by linyunan on 2021-10-14
 */
@Component
public class ClusterPhyPageSearchHandle extends BasePageSearchHandle<ConsoleClusterPhyVO> {

    private static final ILog        LOGGER = LogFactory.getLog(ClusterPhyPageSearchHandle.class);

    @Autowired
    private AppService               appService;

    @Autowired
    private ClusterPhyService        clusterPhyService;

    @Autowired
    private ClusterPhyManager        clusterPhyManager;

    @Autowired
    private AppClusterPhyAuthManager appClusterPhyAuthManager;

    private static final FutureUtil<Void> futureUtil = FutureUtil.initBySystemAvailableProcessors("ClusterPhyPageSearchHandle",50);

    @Override
    protected Result<Boolean> validCheckForAppId(Integer appId) {
        if (!appService.isAppExists(appId)) {
            return Result.buildParamIllegal("项目不存在");
        }
        return Result.buildSucc(true);
    }

    @Override
    protected Result<Boolean> validCheckForCondition(PageDTO pageDTO, Integer appId) {
        if (pageDTO instanceof ClusterPhyConditionDTO) {
            ClusterPhyConditionDTO clusterPhyConditionDTO = (ClusterPhyConditionDTO) pageDTO;
            Integer authType = clusterPhyConditionDTO.getAuthType();
            if (null != authType && !AppClusterPhyAuthEnum.isExitByCode(authType)) {
                return Result.buildParamIllegal("权限类型不存在");
            }

            Integer status = clusterPhyConditionDTO.getHealth();
            if (null != status && !ClusterHealthEnum.isExitByCode(status)) {
                return Result.buildParamIllegal("集群状态类型不存在");
            }

            String clusterPhyName = clusterPhyConditionDTO.getCluster();
            if (!AriusObjUtils.isBlack(clusterPhyName) && (clusterPhyName.startsWith("*") || clusterPhyName.startsWith("?"))) {
                return Result.buildParamIllegal("物理集群名称不允许带类似*, ?等通配符查询");
            }

            return Result.buildSucc(true);
        }

        LOGGER.error("class=ClusterPhyPageSearchHandle||method=validCheckForCondition||errMsg=failed to convert PageDTO to ClusterPhyConditionDTO");

        return Result.buildFail();
    }

    @Override
    protected void init(PageDTO pageDTO) {
        // Do nothing
    }

    @Override
    protected PaginationResult<ConsoleClusterPhyVO> buildWithAuthType(PageDTO pageDTO, Integer authType, Integer appId) {
        ClusterPhyConditionDTO condition = buildClusterPhyConditionDTO(pageDTO);
        if (null == condition) {
            LOGGER.error(
                    "class=TemplateLogicPageSearchHandle||method=buildWithAuthType||errMsg=failed to convert PageDTO to ClusterPhyConditionDTO");
            return PaginationResult.buildFail("获取查询信息失败");
        }
        
        //1. 获取管理/读写/读/无权限的物理集群信息
        List<ClusterPhy> appAuthClusterPhyList = clusterPhyManager.getClusterPhyByAppIdAndAuthType(appId, condition.getAuthType());
        if (CollectionUtils.isEmpty(appAuthClusterPhyList)) {
            return PaginationResult.buildSucc();
        }

        //2. 过滤出符合条件的列表
        List<ClusterPhy> meetConditionClusterPhyList = getMeetConditionClusterPhyList(condition, appAuthClusterPhyList);

        //3. 设置命中数
        long hitTotal = meetConditionClusterPhyList.size();

        //4. 根据匹配结果进行对模板id进行排序, 根据分页信息过滤出需要获取的模板id
        Collections.sort(meetConditionClusterPhyList);

        //5. 最后页临界点处理
        long size = getLastPageSize(condition, meetConditionClusterPhyList.size());

        List<ClusterPhy> fuzzyAndLimitClusterPhyList      = meetConditionClusterPhyList.subList(condition.getFrom().intValue(), (int)size);
        List<ConsoleClusterPhyVO> consoleClusterPhyVOList = ConvertUtil.list2List(fuzzyAndLimitClusterPhyList, ConsoleClusterPhyVO.class);
        //6. 设置权限
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> consoleClusterPhyVO.setCurrentAppAuth(condition.getAuthType()));

        //2.设置物理集群的所属项目和所属AppId
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> clusterPhyManager.buildBelongAppIdAndName(consoleClusterPhyVO));

        //7. 设置集群基本统计信息：磁盘使用信息
        for (ConsoleClusterPhyVO consoleClusterPhyVO : consoleClusterPhyVOList) {
            futureUtil.runnableTask(() -> clusterPhyManager.buildPhyClusterStatics(consoleClusterPhyVO));
            futureUtil.runnableTask(() -> clusterPhyManager.buildClusterRole(consoleClusterPhyVO));
        }
        futureUtil.waitExecute();
        
        return PaginationResult.buildSucc(consoleClusterPhyVOList, hitTotal, condition.getFrom(), condition.getSize());
    }

    @Override
    protected PaginationResult<ConsoleClusterPhyVO> buildWithoutAuthType(PageDTO pageDTO, Integer appId) {
        ClusterPhyConditionDTO condition = buildClusterPhyConditionDTO(pageDTO);
        if (null == condition) {
            LOGGER.error(
                "class=ClusterPhyPageSearchHandle||method=buildWithoutAuthType||errMsg=failed to convert PageDTO to ClusterPhyConditionDTO");
            return PaginationResult.buildFail("获取物理集群查询信息失败");
        }

        List<ClusterPhy> pagingGetClusterPhyList      =  clusterPhyService.pagingGetClusterPhyByCondition(condition);

        List<ConsoleClusterPhyVO> consoleClusterPhyVOList = doBuildWithoutAuthType(pagingGetClusterPhyList, appId);

        long totalHit = clusterPhyService.fuzzyClusterPhyHitByCondition(condition);
        return PaginationResult.buildSucc(consoleClusterPhyVOList, totalHit, condition.getFrom(), condition.getSize());
    }
    
    /****************************************private***********************************************/
    private List<ConsoleClusterPhyVO> doBuildWithoutAuthType(List<ClusterPhy> clusterPhyList, Integer appId) {
        if (CollectionUtils.isEmpty(clusterPhyList)) {
            return Lists.newArrayList();
        }

        //获取项目对集群列表的权限信息
        List<AppClusterPhyAuth> appClusterPhyAuthList      = appClusterPhyAuthManager.getByClusterPhyListAndAppId(appId, clusterPhyList);
        Map<String, Integer>    clusterPhyName2AuthTypeMap = ConvertUtil.list2Map(appClusterPhyAuthList, AppClusterPhyAuth::getClusterPhyName, AppClusterPhyAuth::getType);

        List<ConsoleClusterPhyVO> consoleClusterPhyVOList = ConvertUtil.list2List(clusterPhyList, ConsoleClusterPhyVO.class);

        //1. 设置单个集群权限
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> consoleClusterPhyVO.setCurrentAppAuth(clusterPhyName2AuthTypeMap.get(consoleClusterPhyVO.getCluster())));

        //2.设置物理集群的所属项目和所属AppId
        consoleClusterPhyVOList.forEach(consoleClusterPhyVO -> clusterPhyManager.buildBelongAppIdAndName(consoleClusterPhyVO));

        //3. 设置集群基本统计信息：磁盘使用信息
        for (ConsoleClusterPhyVO consoleClusterPhyVO : consoleClusterPhyVOList) {
            futureUtil.runnableTask(() -> clusterPhyManager.buildClusterRole(consoleClusterPhyVO));
            futureUtil.runnableTask(() -> clusterPhyManager.buildPhyClusterStatics(consoleClusterPhyVO));
        }
        futureUtil.waitExecute();

        return consoleClusterPhyVOList;
    }

    private ClusterPhyConditionDTO buildClusterPhyConditionDTO(PageDTO pageDTO) {
        if (pageDTO instanceof ClusterPhyConditionDTO) {
            return (ClusterPhyConditionDTO) pageDTO;
        }
        return null;
    }

    /**
     * 3个条件（集群名称、健康状态、版本）等组合查询 共7种情况
     *
     * @param condition               条件列表
     * @param appAuthClusterPhyList   带指定权限的物理集群列表
     * @return
     */
    private List<ClusterPhy> getMeetConditionClusterPhyList(ClusterPhyConditionDTO condition, List<ClusterPhy> appAuthClusterPhyList) {
        List<ClusterPhy> meetConditionClusterPhyList = Lists.newArrayList();
        //分页查询参数为空
        if (AriusObjUtils.isBlack(condition.getCluster()) && AriusObjUtils.isBlack(condition.getEsVersion())
            && null == condition.getHealth()) {
            meetConditionClusterPhyList.addAll(appAuthClusterPhyList);
            return meetConditionClusterPhyList;
        }

        //分页查询条件中只存在集群名称
        if (!AriusObjUtils.isBlack(condition.getCluster()) && null == condition.getHealth()
            && AriusObjUtils.isBlack(condition.getEsVersion())) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                  .stream()
                                  .filter(r -> r.getCluster().contains(condition.getCluster()))
                                  .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中只存在健康状态
        if (AriusObjUtils.isBlack(condition.getCluster())
                && AriusObjUtils.isBlack(condition.getEsVersion()) && null != condition.getHealth()) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getHealth().equals(condition.getHealth()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中只存在版本
        if (AriusObjUtils.isBlack(condition.getCluster())
                && !AriusObjUtils.isBlack(condition.getEsVersion()) && null == condition.getHealth()) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getEsVersion().equals(condition.getEsVersion()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中仅存在版本、健康状态
        if (!AriusObjUtils.isBlack(condition.getEsVersion())
                && null != condition.getHealth() && AriusObjUtils.isBlack(condition.getCluster())) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getEsVersion().equals(condition.getEsVersion()) && r.getHealth().equals(condition.getHealth()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中仅存在版本、集群名称
        if (!AriusObjUtils.isBlack(condition.getEsVersion())
                && !AriusObjUtils.isBlack(condition.getCluster()) && null == condition.getHealth()) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getCluster().contains(condition.getCluster()) && r.getEsVersion().equals(condition.getEsVersion()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中仅存在健康状态、集群名称
        if (AriusObjUtils.isBlack(condition.getEsVersion())
                && !AriusObjUtils.isBlack(condition.getCluster()) && null != condition.getHealth()) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getCluster().contains(condition.getCluster()) && r.getHealth().equals(condition.getHealth()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        //分页查询条件中存在健康状态、集群名称、版本
        if (!AriusObjUtils.isBlack(condition.getEsVersion())
                && !AriusObjUtils.isBlack(condition.getCluster()) && null != condition.getHealth()) {
            meetConditionClusterPhyList = appAuthClusterPhyList
                                .stream()
                                .filter(r -> r.getCluster().contains(condition.getCluster())
                                        &&   r.getHealth().equals(condition.getHealth())
                                        &&   r.getEsVersion().equals(condition.getEsVersion()))
                                .collect(Collectors.toList());
            return meetConditionClusterPhyList;
        }

        return meetConditionClusterPhyList;
    }
}
