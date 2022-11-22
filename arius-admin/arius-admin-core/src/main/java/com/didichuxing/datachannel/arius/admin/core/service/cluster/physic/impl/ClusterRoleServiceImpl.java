package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.impl;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRolePO;
import com.didichuxing.datachannel.arius.admin.common.constant.ClusterConstant;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.common.util.ProjectUtils;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterPhyService;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ClusterRoleService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleDAO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ES集群对应角色集群 服务实现类
 *
 * @author didi
 * @since 2020-08-24
 */
@Service
public class ClusterRoleServiceImpl implements ClusterRoleService {

    @Autowired
    private ESClusterRoleDAO  roleClusterDAO;

    @Autowired
    private ClusterPhyService clusterPhyService;

    @Override
    public Result<Void> save(ESClusterRoleDTO esClusterRoleDTO) {
        ESClusterRolePO esClusterRolePO = ConvertUtil.obj2Obj(esClusterRoleDTO, ESClusterRolePO.class);
        boolean succ = (1 == roleClusterDAO.insert(esClusterRolePO));
        if (succ) {
            esClusterRoleDTO.setId(esClusterRolePO.getId());
        }

        return Result.build(succ);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClusterRoleInfo createRoleClusterIfNotExist(String clusterName, String role) {

        ClusterPhy clusterPhy = clusterPhyService.getClusterByName(clusterName);

        ESClusterRolePO roleClusterPO = roleClusterDAO.getByClusterIdAndRole(clusterPhy.getId().longValue(), role);
        if (roleClusterPO == null) {
            roleClusterPO = new ESClusterRolePO();
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
    public List<ClusterRoleInfo> getAllRoleClusterByClusterId(Integer clusterId) {
        List<ESClusterRolePO> roleClusterPos = roleClusterDAO.listByClusterId(clusterId.toString());
        return ConvertUtil.list2List(roleClusterPos, ClusterRoleInfo.class);
    }

    @Override
    public Map<Long, List<ClusterRoleInfo>> getAllRoleClusterByClusterIds(List<Integer> clusterIds) {
        List<String> clusterStrIds = clusterIds.stream().map(i -> String.valueOf(i)).collect(Collectors.toList());
        List<ESClusterRolePO> roleClusterPos = roleClusterDAO.listByClusterIds(clusterStrIds);

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
        ESClusterRolePO esClusterRolePo = ConvertUtil.obj2Obj(clusterRoleInfo, ESClusterRolePO.class);
        boolean succ = (roleClusterDAO.update(esClusterRolePo) != 0);

        return Result.build(succ);
    }

    @Override
    public Result<Void> updateVersionByClusterIdAndRole(Long clusterId, String role, String version) {
        boolean succ = (1 == roleClusterDAO.updateVersionByClusterIdAndRole(clusterId, role, version));

        return Result.build(succ);
    }

    @Override
    public Result<Void> deleteRoleClusterByClusterId(Integer clusterId, Integer projectId) {
        //校验操作项目的合法性
        final Result<Void> result = ProjectUtils.checkProjectCorrectly(i -> i, projectId, projectId);
        if (result.failed()) {
            return result;
        }
        //在接入集群阶段可能会存在角色表未插入的脏数据，直接用>0判断可能导致结果不准确
        final int countByClusterId = roleClusterDAO.countByClusterId(clusterId);
        if (countByClusterId>0){
            return Result.build(roleClusterDAO.delete(clusterId) >0);
        }
        return Result.buildSucc();
    }
    
    @Override
    public boolean deleteByIds(List<Long> ids) {
        return ids.stream().mapToInt(roleClusterDAO::deleteById).count()==ids.size();
    }
}