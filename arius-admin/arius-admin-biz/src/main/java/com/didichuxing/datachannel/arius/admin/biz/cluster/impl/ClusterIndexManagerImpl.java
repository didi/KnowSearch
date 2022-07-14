package com.didichuxing.datachannel.arius.admin.biz.cluster.impl;

import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterIndexManager;
import com.didichuxing.datachannel.arius.admin.biz.template.TemplateLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.region.ClusterRegion;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplatePhy;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ESClusterRoleHostVO;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleHostService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.region.ClusterRegionService;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.core.service.template.physic.IndexTemplatePhyService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 详细介绍类情况.
 *
 * @ClassName ClusterIndexManagerImpl
 * @Author gyp
 * @Date 2022/6/13
 * @Version 1.0
 */
@Component
public class ClusterIndexManagerImpl implements ClusterIndexManager {
    @Autowired
    private ClusterLogicService     clusterLogicService;

    @Autowired
    private ClusterRegionService    clusterRegionService;

    @Autowired
    private ClusterRoleHostService  clusterRoleHostService;

    @Autowired
    private TemplateLogicManager    templateLogicManager;

    @Autowired
    private IndexTemplateService    indexTemplateService;

    @Autowired
    private IndexTemplatePhyService indexTemplatePhyService;

    @Override
    public Result<List<ESClusterRoleHostVO>> listClusterLogicIndices(Integer clusterId, Integer projectId) {
        ClusterLogic clusterLogic = clusterLogicService.getClusterLogicById(Long.valueOf(clusterId));
        if (AriusObjUtils.isNull(clusterLogic)) {
            return Result.buildFail(String.format("集群[%s]不存在", clusterId));
        }
        ClusterRegion clusterRegion = clusterRegionService.getRegionByLogicClusterId(clusterLogic.getId());
        Result<List<ClusterRoleHost>> result = clusterRoleHostService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (result.failed()) {
            return Result.buildFail(result.getMessage());
        }

        Result<List<IndexTemplatePhy>> indexTemplatePhy = indexTemplatePhyService
            .listByRegionId(Math.toIntExact(clusterRegion.getId()));
        if (indexTemplatePhy.failed()) {
            return Result.buildFail(indexTemplatePhy.getMessage());
        }

        return null;
    }
}