package com.didichuxing.datachannel.arius.admin.biz.thardpart.impl;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Sets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonManagerImpl implements CommonManager {

    private static final ILog      LOGGER = LogFactory.getLog(CommonManagerImpl.class);

    @Autowired
    private ClusterPhyService      esClusterPhyService;

    @Autowired
    private TemplateSrvManager     templateSrvManager;
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    @Override
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        List<ThirdPartClusterVO> clusterVOS = ConvertUtil.list2List(esClusterPhyService.listAllClusters(),
            ThirdPartClusterVO.class);
        List<String> hasSecurityClusters = templateSrvManager
            .getPhyClusterByOpenTemplateSrv(TemplateServiceEnum.TEMPLATE_SECURITY.getCode());

        clusterVOS.forEach(vo -> {
            if (hasSecurityClusters.contains(vo.getCluster())) {
                vo.setPlugins(Sets.newHashSet("security"));
            }
        });

        return Result.buildSucc(clusterVOS);
    }

    @Override
    public Result<ThirdPartClusterVO> getDataCluster(String cluster) {
        return Result
            .buildSucc(ConvertUtil.obj2Obj(esClusterPhyService.getClusterByName(cluster), ThirdPartClusterVO.class));
    }

    @Override
    public Result<List<ThirdpartConfigVO>> queryConfig(AriusConfigInfoDTO param) {
        return Result
            .buildSucc(ConvertUtil.list2List(ariusConfigInfoService.queryByCondition(param), ThirdpartConfigVO.class));
    }

}