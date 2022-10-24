package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import java.util.List;

/**
 * 索引业务相关.
 *
 * @ClassName ClusterIndexManager
 * @Author gyp
 * @Date 2022/6/13
 * @Version 1.0
 */
public interface ClusterIndexManager {
    /**
     * 获取逻辑集群索引列表
     *
     * @param clusterId 集群id
     * @param projectId 项目id
     * @return {@link Result}<{@link List}<{@link ESClusterRoleHostVO}>>
     */
    Result<List<ESClusterRoleHostVO>> listClusterLogicIndices(Integer clusterId, Integer projectId);
}