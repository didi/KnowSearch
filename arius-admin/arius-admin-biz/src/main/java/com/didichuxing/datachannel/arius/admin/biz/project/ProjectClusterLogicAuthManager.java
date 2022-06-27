package com.didichuxing.datachannel.arius.admin.biz.project;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.project.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import java.util.List;

/**
 * Created by linyunan on 2021-10-17
 */

public interface ProjectClusterLogicAuthManager {

    /**
     * 获取当前项目对逻辑集群列表的权限信息
     * @param projectId                    项目
     * @param clusterLogicList         逻辑集群信息列表
     * @return
     */
    List<ProjectClusterLogicAuth> getByClusterLogicListAndProjectId(Integer projectId, List<ClusterLogic> clusterLogicList);
}