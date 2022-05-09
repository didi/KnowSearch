package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.RoleClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterDAO;
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
public class RoleClusterServiceImpl implements RoleClusterService {

    @Autowired
    private ESRoleClusterDAO  roleClusterDAO;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Override
    public Result<Void> save(ESRoleClusterDTO esRoleClusterDTO) {
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        boolean succ = (1 == roleClusterDAO.insert(esRoleClusterPO));
        if(succ) {
            esRoleClusterDTO.setId(esRoleClusterPO.getId());
        }

        return Result.build(succ);
    }

    @Override
    public RoleCluster createRoleClusterIfNotExist(String clusterName, String role) {

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);

        ESRoleClusterPO roleClusterPO = roleClusterDAO.getByClusterIdAndRole(clusterPhy.getId().longValue(), role);
        if (roleClusterPO == null) {
            roleClusterPO = new ESRoleClusterPO();
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

        return ConvertUtil.obj2Obj(roleClusterPO, RoleCluster.class);
    }

    @Override
    public RoleCluster getById(Long id) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getById(id), RoleCluster.class);
    }

    @Override
    public List<RoleCluster> getAllRoleClusterByClusterId(Integer clusterId) {
        List<ESRoleClusterPO> roleClusterPos = roleClusterDAO.listByClusterId(clusterId.toString());
        return ConvertUtil.list2List(roleClusterPos, RoleCluster.class);
    }

    @Override
    public Map<Long, List<RoleCluster>> getAllRoleClusterByClusterIds(List<Integer> clusterIds) {
        List<String> clusterStrIds = clusterIds.stream().map(i -> String.valueOf(i)).collect( Collectors.toList());
        List<ESRoleClusterPO> roleClusterPos = roleClusterDAO.listByClusterIds(clusterStrIds);

        Map<Long, List<RoleCluster>> ret = new HashMap<>();

        if (CollectionUtils.isNotEmpty(roleClusterPos)) {
            List<RoleCluster> list = ConvertUtil.list2List(roleClusterPos, RoleCluster.class);
            ret = list.stream().collect(Collectors.groupingBy(RoleCluster::getElasticClusterId));
        }

        return ret;
    }

    @Override
    public RoleCluster getByClusterIdAndClusterRole(Long clusterId, String roleClusterName) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getByClusterIdAndClusterRole(clusterId, roleClusterName),
            RoleCluster.class);
    }

    @Override
    public RoleCluster getByClusterIdAndRole(Long clusterId, String role) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getByClusterIdAndRole(clusterId, role), RoleCluster.class);
    }

    @Override
    public RoleCluster getByClusterNameAndRole(String clusterName, String role) {
        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);

        return clusterPhy == null ? null : getByClusterIdAndRole(clusterPhy.getId().longValue(), role);
    }

    @Override
    public Result<Void> updatePodByClusterIdAndRole(RoleCluster roleCluster) {
        ESRoleClusterPO esRoleClusterPo = ConvertUtil.obj2Obj(roleCluster, ESRoleClusterPO.class);
        boolean succ = (1 == roleClusterDAO.update(esRoleClusterPo));

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
