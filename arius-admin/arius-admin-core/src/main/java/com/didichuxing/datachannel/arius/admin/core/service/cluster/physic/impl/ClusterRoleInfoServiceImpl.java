package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRoleInfoPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleInfoDAO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ES集群对应角色集群 服务实现类
 *
 * @author didi
 * @since 2020-08-24
 */
@Service
public class ClusterRoleInfoServiceImpl implements ClusterRoleInfoService {

    @Autowired
    private ESClusterRoleInfoDAO roleClusterDAO;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Override
    public Result<Void> save(ESClusterRoleInfoDTO esClusterRoleInfoDTO) {
        ESClusterRoleInfoPO esClusterRoleInfoPO = ConvertUtil.obj2Obj(esClusterRoleInfoDTO, ESClusterRoleInfoPO.class);
        boolean succ = (1 == roleClusterDAO.insert(esClusterRoleInfoPO));
        if(succ) {
            esClusterRoleInfoDTO.setId(esClusterRoleInfoPO.getId());
        }

        return Result.build(succ);
    }

    @Override
    public ClusterRoleInfo createRoleClusterIfNotExist(String clusterName, String role) {

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);

        ESClusterRoleInfoPO roleClusterPO = roleClusterDAO.getByClusterIdAndRole(clusterPhy.getId().longValue(), role);
        if (roleClusterPO == null) {
            roleClusterPO = new ESClusterRoleInfoPO();
            roleClusterPO.setElasticClusterId(clusterPhy.getId().longValue());
            roleClusterPO.setRoleClusterName(clusterName + "-" + role);
            roleClusterPO.setRole(role);
            roleClusterPO.setPodNumber(0);
            roleClusterPO.setPidCount(1);
            roleClusterPO.setMachineSpec("");
            roleClusterPO.setEsVersion(clusterPhy.getEsVersion());
            roleClusterPO.setCfgId(ClusterConstant.INVALID_VALUE.intValue());
            roleClusterDAO.insert(roleClusterPO);
        }

        return ConvertUtil.obj2Obj(roleClusterPO, ClusterRoleInfo.class);
    }

    @Override
    public ClusterRoleInfo getById(Long id) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getById(id), ClusterRoleInfo.class);
    }

    @Override
    public List<ClusterRoleInfo> getAllRoleClusterByClusterId(Integer clusterId) {
        List<ESClusterRoleInfoPO> roleClusterPos = roleClusterDAO.listByClusterId(clusterId.toString());
        return ConvertUtil.list2List(roleClusterPos, ClusterRoleInfo.class);
    }

    @Override
    public Map<Long, List<ClusterRoleInfo>> getAllRoleClusterByClusterIds(List<Integer> clusterIds) {
        List<String> clusterStrIds = clusterIds.stream().map(i -> String.valueOf(i)).collect( Collectors.toList());
        List<ESClusterRoleInfoPO> roleClusterPos = roleClusterDAO.listByClusterIds(clusterStrIds);

        Map<Long, List<ClusterRoleInfo>> ret = new HashMap<>();

        if (CollectionUtils.isNotEmpty(roleClusterPos)) {
            List<ClusterRoleInfo> list = ConvertUtil.list2List(roleClusterPos, ClusterRoleInfo.class);
            ret = list.stream().collect(Collectors.groupingBy(ClusterRoleInfo::getElasticClusterId));
        }

        return ret;
    }

    @Override
    public ClusterRoleInfo getByClusterIdAndClusterRole(Long clusterId, String roleClusterName) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getByClusterIdAndClusterRole(clusterId, roleClusterName),
            ClusterRoleInfo.class);
    }

    @Override
    public ClusterRoleInfo getByClusterIdAndRole(Long clusterId, String role) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getByClusterIdAndRole(clusterId, role), ClusterRoleInfo.class);
    }

    @Override
    public ClusterRoleInfo getByClusterNameAndRole(String clusterName, String role) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);

        return clusterPhy == null ? null : getByClusterIdAndRole(clusterPhy.getId().longValue(), role);
    }

    @Override
    public Result<Void> updatePodByClusterIdAndRole(ClusterRoleInfo clusterRoleInfo) {
        ESClusterRoleInfoPO esClusterRoleInfoPo = ConvertUtil.obj2Obj(clusterRoleInfo, ESClusterRoleInfoPO.class);
        boolean succ = (1 == roleClusterDAO.update(esClusterRoleInfoPo));

        return Result.build(succ);
    }

    @Override
    public Result<Void> updateVersionByClusterIdAndRole(Long clusterId, String role, String version) {
        boolean succ = (1 == roleClusterDAO.updateVersionByClusterIdAndRole(clusterId, role, version));

        return Result.build(succ);
    }

    @Override
    public Result<Void> deleteRoleClusterByClusterId(Integer clusterId) {
        boolean success = (roleClusterDAO.delete(clusterId) > 0);
        if (!success) {
            return Result.buildFail();
        }
        return Result.buildSucc();
    }

    @Override
    public Result deleteRoleClusterByClusterIdAndRole(Long clusterId, String role) {
        boolean success = (roleClusterDAO.deleteRoleClusterByCluterIdAndRole(clusterId,role) > 0);
        if (!success) {
            return Result.buildFail();
        }
        return Result.buildSucc();
    }
}
