package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import com.didichuxing.datachannel.arius.admin.common.constant.ESClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;

/**
 * ES集群对应角色集群 服务实现类
 *
 * @author didi
 * @since 2020-08-24
 */
@Service
public class ESRoleClusterServiceImpl implements ESRoleClusterService {

    @Autowired
    private ESRoleClusterDAO roleClusterDAO;

    @Autowired
    private ClusterDAO       clusterDAO;

    @Override
    public Result save(ESRoleClusterDTO esRoleClusterDTO) {
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        boolean succ = (1 == roleClusterDAO.insert(esRoleClusterPO));
        if(succ) {
            esRoleClusterDTO.setId(esRoleClusterPO.getId());
        }

        return Result.build(succ);
    }

    @Override
    public ESRoleCluster createRoleClusterIfNotExist(String clusterName, String role) {

        ClusterPO clusterPO = clusterDAO.getByName(clusterName);

        ESRoleClusterPO roleClusterPO = roleClusterDAO.getByClusterIdAndRole(clusterPO.getId().longValue(), role);
        if (roleClusterPO == null) {
            roleClusterPO = new ESRoleClusterPO();
            roleClusterPO.setElasticClusterId(clusterPO.getId().longValue());
            roleClusterPO.setRoleClusterName(clusterName + "-" + role);
            roleClusterPO.setRole(role);
            roleClusterPO.setPodNumber(0);
            roleClusterPO.setPidCount(0);
            roleClusterPO.setMachineSpec("");
            roleClusterPO.setEsVersion(clusterPO.getEsVersion());
            roleClusterPO.setCfgId(ESClusterConstant.INVALID_VALUE.intValue());
            roleClusterDAO.insert(roleClusterPO);
        }

        return ConvertUtil.obj2Obj(roleClusterPO, ESRoleCluster.class);
    }

    @Override
    public ESRoleCluster getById(Long id) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getById(id), ESRoleCluster.class);
    }

    @Override
    public List<ESRoleCluster> getAllRoleClusterByClusterId(Integer clusterId) {
        List<ESRoleClusterPO> roleClusterPos = roleClusterDAO.listByClusterId(clusterId.toString());
        return ConvertUtil.list2List(roleClusterPos, ESRoleCluster.class);
    }

    @Override
    public ESRoleCluster getByClusterIdAndClusterRole(Long clusterId, String roleClusterName) {
        return ConvertUtil.obj2Obj(roleClusterDAO.getByClusterIdAndClusterRole(clusterId, roleClusterName),
            ESRoleCluster.class);
    }

    @Override
    public ESRoleCluster getByClusterIdAndRole(Long clusterId, String role) {
        ESRoleClusterPO esRoleClusterPo = roleClusterDAO.getByClusterIdAndRole(clusterId, role);

        if (null == esRoleClusterPo) {
            return new ESRoleCluster();
        }
        return ConvertUtil.obj2Obj(esRoleClusterPo, ESRoleCluster.class);
    }

    @Override
    public ESRoleCluster getByClusterNameAndRole(String clusterName, String role) {
        ClusterPO clusterPO = clusterDAO.getByName(clusterName);

        return clusterPO == null ? null : getByClusterIdAndRole(clusterPO.getId().longValue(), role);
    }

    @Override
    public Result<ESRoleClusterPO> updatePodByClusterIdAndRole(ESRoleCluster esRoleCluster) {
        ESRoleClusterPO esRoleClusterPo = ConvertUtil.obj2Obj(esRoleCluster, ESRoleClusterPO.class);
        boolean succ = (1 == roleClusterDAO.update(esRoleClusterPo));

        return Result.build(succ, esRoleClusterPo);
    }

    @Override
    public Result updateVersionByClusterIdAndRole(Long clusterId, String role, String version) {
        boolean succ = (1 == roleClusterDAO.updateVersionByClusterIdAndRole(clusterId, role, version));

        return Result.build(succ);
    }

    @Override
    public Result deleteRoleClusterByClusterId(Integer clusterId) {
        boolean success = (roleClusterDAO.delete(clusterId) > 0);
        if (!success) {
            return Result.buildFail();
        }
        return Result.buildSucc();
    }
}
