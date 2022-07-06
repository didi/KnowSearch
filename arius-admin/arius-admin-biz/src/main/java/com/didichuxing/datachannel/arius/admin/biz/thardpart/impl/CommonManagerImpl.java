package com.didichuxing.datachannel.arius.admin.biz.thardpart.impl;

import com.didichuxing.datachannel.arius.admin.biz.thardpart.CommonManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.config.AriusConfigInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterTemplateSrv;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.cluster.ThirdPartClusterVO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.config.ThirdpartConfigVO;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonManagerImpl implements CommonManager {

    private static final ILog LOGGER = LogFactory.getLog(CommonManagerImpl.class);



    @Autowired
    private ClusterPhyService esClusterPhyService;

  
    
    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;
    @Autowired
    private ClusterPhyService clusterPhyService;


    @Override
    public Result<List<ThirdPartClusterVO>> listDataCluster() {
        List<ThirdPartClusterVO> clusterVOS = ConvertUtil.list2List(esClusterPhyService.listAllClusters(),
                ThirdPartClusterVO.class);
         List<ClusterPhy> clusterPhies = clusterPhyService.listAllClusters();
         
        List<String> hasSecurityClusters =getPhyClusterByOpenTemplateSrv(clusterPhies, TemplateServiceEnum.TEMPLATE_SECURITY.getCode());

        clusterVOS.forEach(vo -> {
            if (hasSecurityClusters.contains(vo.getCluster())) {
                vo.setPlugins(Sets.newHashSet("security"));
            }
        });

        return Result.buildSucc(clusterVOS);
    }
    public List<String> getPhyClusterByOpenTemplateSrv(List<ClusterPhy> clusterPhies, int srvId) {
        List<String> clusterPhyNames = new ArrayList<>();
        if (CollectionUtils.isEmpty(clusterPhies)) {
            return clusterPhyNames;
        }
        clusterPhies
                .stream()
                .filter(clusterPhy ->isPhyClusterOpenTemplateSrv(clusterPhy, srvId))
                .map(ClusterPhy::getCluster)
                .forEach(clusterPhyNames::add);
        return clusterPhyNames;
    }
    public boolean isPhyClusterOpenTemplateSrv(ClusterPhy phyCluster, int srvId) {
        try {
            Result<List<ClusterTemplateSrv>> result =clusterPhyService. getPhyClusterTemplateSrv(phyCluster);
            if ( result.failed()) {
                return false;
            }

            List<ClusterTemplateSrv> clusterTemplateSrvs = result.getData();
            for (ClusterTemplateSrv templateSrv : clusterTemplateSrvs) {
                if (srvId == templateSrv.getServiceId()) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            LOGGER.warn("class=TemplateSrvManager||method=isPhyClusterOpenTemplateSrv||phyCluster={}||srvId={}",
                    phyCluster, srvId, e);

            return true;
        }
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