package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import com.didichuxing.datachannel.arius.admin.biz.app.ProjectClusterPhyAuthManager;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterContextManager;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterPhyAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogicContext;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhyContext;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppClusterPhyAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.threadpool.AriusScheduleThreadPool;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterLogicAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.ProjectClusterPhyAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.common.vo.project.ProjectBriefVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author linyunan
 * @date 2021-04-28
 */
@Component
public class ProjectClusterPhyAuthManagerImpl implements ProjectClusterPhyAuthManager {
    private static final ILog           LOGGER = LogFactory.getLog(ProjectClusterPhyAuthManagerImpl.class);
    @Autowired
    private              ProjectService projectService;
    @Autowired
    private ESUserService esUserService;

    @Autowired
    private ProjectClusterPhyAuthService projectClusterPhyAuthService;

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private ClusterContextManager      clusterContextManager;

    @Autowired
    private ClusterPhyService          clusterPhyService;

    @Autowired
    private ClusterLogicService        clusterLogicService;

    @Autowired
    private AriusScheduleThreadPool    ariusScheduleThreadPool;

    private static final Map<Integer/*projectId*/, List<ProjectClusterPhyAuth> /*AppClusterPhyAuthList*/> PROJECT_ID_2_APP_CLUSTER_PHY_AUTH_LIST_MAP = Maps.newConcurrentMap();

    @PostConstruct
    private void init(){
        ariusScheduleThreadPool.submitScheduleAtFixedDelayTask(this::refreshAppClusterPhyAuth,60, 120);
    }

    @Override
    public List<ProjectClusterPhyAuth> getByClusterPhyListAndProjectIdFromCache(Integer projectId,
                                                                                List<ClusterPhy> clusterPhyList) {
        if (null == projectId || CollectionUtils.isEmpty(clusterPhyList)) { return Lists.newArrayList();}
        
        List<ProjectClusterPhyAuth> projectClusterPhyAuthList = PROJECT_ID_2_APP_CLUSTER_PHY_AUTH_LIST_MAP.get(
                projectId);
        if (CollectionUtils.isEmpty(projectClusterPhyAuthList)) {
            return buildInitAppClusterPhyAuth(clusterPhyList);
        }

        return projectClusterPhyAuthList;
    }

    private List<ProjectClusterPhyAuth> buildInitAppClusterPhyAuth(List<ClusterPhy> clusterPhyList) {
        List<ProjectClusterPhyAuth> initProjectClusterPhyAuthList = Lists.newArrayList();
        for (ClusterPhy clusterPhy : clusterPhyList) {
            ProjectClusterPhyAuth projectClusterPhyAuth = new ProjectClusterPhyAuth();
            projectClusterPhyAuth.setClusterPhyName(clusterPhy.getCluster());
            projectClusterPhyAuth.setType(ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode());
            initProjectClusterPhyAuthList.add(projectClusterPhyAuth);
        }

        return initProjectClusterPhyAuthList;
    }

    @Override
    public List<ProjectClusterPhyAuth> getAppAccessClusterPhyAuths(Integer projectId) {
        List<ProjectClusterPhyAuth> appAccessClusterPhyAuthList = Lists.newArrayList();
        List<ProjectClusterLogicAuth> logicClusterAccessAuths   = projectClusterLogicAuthService.getLogicClusterAccessAuths(
                projectId);
        if (CollectionUtils.isEmpty(logicClusterAccessAuths)) {
            return appAccessClusterPhyAuthList;
        }
        
        List<List<String>> clusterPhyLists = logicClusterAccessAuths
                                            .stream()
                                            .map(ProjectClusterLogicAuth::getLogicClusterId)
                                            .map(clusterContextManager::getClusterLogicContext)
                                            .filter(Objects::nonNull)
                                            .map(ClusterLogicContext::getAssociatedClusterPhyNames)
                                            .collect(Collectors.toList());
        
        for (List<String> clusterPhyList : clusterPhyLists) {
            clusterPhyList.forEach(clusterPhy -> {
                ProjectClusterPhyAuth projectClusterPhyAuth = projectClusterPhyAuthService.buildClusterPhyAuth(projectId, clusterPhy,
                                                      AppClusterPhyAuthEnum.ACCESS);
                appAccessClusterPhyAuthList.add(projectClusterPhyAuth);
            });
        }
        
        return appAccessClusterPhyAuthList;
    }

    /**
     * 定时刷新权限信息
     */
    private void refreshAppClusterPhyAuth() {
        long currentTimeMillis = System.currentTimeMillis();
        LOGGER.info("class=ProjectClusterPhyAuthManagerImpl||method=refreshAppClusterPhyAuth||msg=start...");

        Map<String, ClusterPhyContext> name2clusterPhyContextMap = clusterContextManager.listClusterPhyContextMap();
        if (MapUtils.isEmpty(name2clusterPhyContextMap)) { return;}
        //刷新项目信息和es user的信息
        final List<Integer> projectIds = projectService.getProjectBriefList().stream().map(ProjectBriefVO::getId)
                .collect(Collectors.toList());
        final List<ESUser> esUsers = esUserService.listESUsers(projectIds);
        if (CollectionUtils.isEmpty(esUsers)) { return;}

        List<ClusterPhy> esClusterPhyList = clusterPhyService.listClustersByCondt(null);
        if (CollectionUtils.isEmpty(esClusterPhyList)) { return;}

        List<ClusterLogic> clusterLogicList = clusterLogicService.listAllClusterLogics();

        Map<Long, ClusterLogic> id2ClusterLogicMap = ConvertUtil.list2Map(clusterLogicList, ClusterLogic::getId);

        List<ProjectClusterLogicAuth> projectClusterLogicAuthList = projectClusterLogicAuthService.list();
        // 注意这里的key projectId@clusterLogiId
        Map<String, ProjectClusterLogicAuth> key2AppClusterLogicAuthMap = ConvertUtil.list2Map(
                projectClusterLogicAuthList,
                                                a -> a.getProjectId() + "@" + a.getLogicClusterId());

        for (ESUser esUser : esUsers) {
            // 超级es user，有最高权限
            int esUserName = esUser.getId();
            final Integer projectId = esUser.getProjectId();
            List<ProjectClusterPhyAuth> projectClusterPhyAuthList = Lists.newArrayList();
            if (esUser.getIsRoot() == 1) {
                projectClusterPhyAuthList = esClusterPhyList.stream()
                        .map(r -> buildClusterPhyAuth(projectId, r.getCluster(), AppClusterPhyAuthEnum.OWN))
                        .collect(Collectors.toList());
                PROJECT_ID_2_APP_CLUSTER_PHY_AUTH_LIST_MAP.put(esUser.getId(), projectClusterPhyAuthList);
                continue;
            }
            
            // 处理非超级es user 拥有的资源权限
            // todo 除了管理员 其他用户不具有物理集群 需要讨论
            for (ClusterPhy clusterPhy : esClusterPhyList) {
                ClusterPhyContext clusterPhyContext = name2clusterPhyContextMap.get(clusterPhy.getCluster());
                if (null == clusterPhyContext) {
                    projectClusterPhyAuthList.add(buildClusterPhyAuth(projectId, clusterPhy.getCluster(),
                            AppClusterPhyAuthEnum.NO_PERMISSIONS));
                    continue;
                }

                List<Long> associatedClusterLogicIds        =   clusterPhyContext.getAssociatedClusterLogicIds();
                if (CollectionUtils.isEmpty(associatedClusterLogicIds)) {
                    projectClusterPhyAuthList.add(buildClusterPhyAuth(projectId, clusterPhy.getCluster(), AppClusterPhyAuthEnum.NO_PERMISSIONS));
                    continue;
                }

                //  合并多个逻辑集群权限，即取最低权限
                Set<Integer> appClusterLogicAuthTypeSet = Sets.newHashSet();
                for (Long associatedClusterLogicId : associatedClusterLogicIds) {
                    // 从逻辑集群表获取创建信息
                    ClusterLogic clusterLogic = id2ClusterLogicMap.get(associatedClusterLogicId);
                    ProjectClusterLogicAuthEnum authFromCreateRecord = (clusterLogic != null && esUserName == clusterLogic.getProjectId())
                            ? ProjectClusterLogicAuthEnum.OWN
                            : ProjectClusterLogicAuthEnum.NO_PERMISSIONS;

                    // 从权限表获取权限信息
                    ProjectClusterLogicAuth authPO = key2AppClusterLogicAuthMap.get(esUserName + "@" + clusterLogic);
                    ProjectClusterLogicAuthEnum authFromAuthRecord = authPO != null ? ProjectClusterLogicAuthEnum.valueOf(authPO.getType()) :
                            ProjectClusterLogicAuthEnum.NO_PERMISSIONS;

                    // 都没有权限
                    if (authFromCreateRecord == ProjectClusterLogicAuthEnum.NO_PERMISSIONS
                            && authFromAuthRecord == ProjectClusterLogicAuthEnum.NO_PERMISSIONS) {
                        ProjectClusterLogicAuth projectClusterLogicAuth = buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.NO_PERMISSIONS);
                        appClusterLogicAuthTypeSet.add(null == projectClusterLogicAuth
                                ? ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode() :
                                projectClusterLogicAuth.getType());
                        continue;
                    }

                    ProjectClusterLogicAuth projectClusterLogicAuth = authFromAuthRecord.higherOrEqual(authFromCreateRecord)
                            ? ConvertUtil.obj2Obj(authPO, ProjectClusterLogicAuth.class)
                            : buildLogicClusterAuth(clusterLogic, ProjectClusterLogicAuthEnum.OWN);

                    appClusterLogicAuthTypeSet.add(null == projectClusterLogicAuth
                            ? ProjectClusterLogicAuthEnum.NO_PERMISSIONS.getCode() :
                            projectClusterLogicAuth.getType());
                }

                // 合并计算
                AppClusterPhyAuthEnum appClusterPhyAuthEnum = computeAppClusterPhyAuthEnum(appClusterLogicAuthTypeSet);
                projectClusterPhyAuthList.add(buildClusterPhyAuth(esUserName, clusterPhy.getCluster(), appClusterPhyAuthEnum));
            }
            PROJECT_ID_2_APP_CLUSTER_PHY_AUTH_LIST_MAP.put(esUser.getId(), projectClusterPhyAuthList);
        }

        LOGGER.info("class=ProjectClusterPhyAuthManagerImpl||method=refreshAppClusterPhyAuth||msg=finish...||consumingTime={}",
                System.currentTimeMillis() - currentTimeMillis);
    }

    private ProjectClusterPhyAuth buildClusterPhyAuth(Integer projectId, String clusterPhyName, AppClusterPhyAuthEnum appClusterPhyAuthEnum) {
        if (null == appClusterPhyAuthEnum || null == projectId || AriusObjUtils.isBlack(clusterPhyName)) {
            return null;
        }

        if (!AppClusterPhyAuthEnum.isExitByCode(appClusterPhyAuthEnum.getCode())) {
            return null;
        }

        ProjectClusterPhyAuth projectClusterPhyAuth = new ProjectClusterPhyAuth();
        projectClusterPhyAuth.setProjectId(projectId);
        projectClusterPhyAuth.setClusterPhyName(clusterPhyName);
        projectClusterPhyAuth.setType(appClusterPhyAuthEnum.getCode());
        return projectClusterPhyAuth;
    }

    /**
     * 由逻辑集群记录构建owner APP的权限数据
     * @param clusterLogic 逻辑集群记录
     */
    private ProjectClusterLogicAuth buildLogicClusterAuth(ClusterLogic clusterLogic, ProjectClusterLogicAuthEnum projectClusterLogicAuthEnum) {
        if (clusterLogic == null) { return null;}

        ProjectClusterLogicAuth appLogicClusterAuth = new ProjectClusterLogicAuth();
        appLogicClusterAuth.setId(null);
        appLogicClusterAuth.setProjectId(clusterLogic.getProjectId());
        appLogicClusterAuth.setLogicClusterId(clusterLogic.getId());
        appLogicClusterAuth.setType(projectClusterLogicAuthEnum.getCode());
        appLogicClusterAuth.setResponsible(clusterLogic.getResponsible());
        return appLogicClusterAuth;
    }

    /**
     * 合并多个逻辑集群权限，即取最低权限
     * @param appClusterLogicAuthTypeSet  多个逻辑集群权限
     * @return AppClusterPhyAuthEnum
     */
    private AppClusterPhyAuthEnum computeAppClusterPhyAuthEnum(Set<Integer> appClusterLogicAuthTypeSet) {
        if(appClusterLogicAuthTypeSet.contains(AppClusterPhyAuthEnum.NO_PERMISSIONS.getCode())) {
            return AppClusterPhyAuthEnum.NO_PERMISSIONS;
        }

        // 0 超管  1 管理 2 访问
        int max = 0;
        for (Integer authCode : appClusterLogicAuthTypeSet) {
            max = authCode > max ? authCode : max;
        }

        return AppClusterPhyAuthEnum.valueOf(max);
    }
}