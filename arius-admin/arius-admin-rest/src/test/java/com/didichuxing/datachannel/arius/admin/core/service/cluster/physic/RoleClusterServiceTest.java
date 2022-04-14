package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleCluster;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.ClusterDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class RoleClusterServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESRoleClusterDAO roleClusterDAO;

    @Autowired
    private ClusterDAO clusterDAO;

    @Autowired
    private RoleClusterService roleClusterService;

    @MockBean
    private ClusterPhyService clusterPhyService;

    /**
     * 不需要做其他字段的校验吗
     */
    @Test
    public void saveTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(roleClusterService.save(esRoleClusterDTO).success());
        Assertions.assertEquals(esRoleClusterDTO.getId(),
                roleClusterDAO.getByClusterIdAndRole(esRoleClusterDTO.getElasticClusterId(), esRoleClusterDTO.getRole()).getId());
    }

    /**
     * 仍然是没有对于null搜索的判断，容易出现NPE问题
     * 无法反映出是否创建成功
     */
    @Test
    public void createRoleClusterIfNotExistTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setId(12344);
        String role = esRoleClusterDTO.getRole();
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(esClusterDTO, ClusterPhy.class));
        Assertions.assertEquals(esClusterDTO.getCluster() + "-" + esRoleClusterDTO.getRole(),
                roleClusterService.createRoleClusterIfNotExist(esClusterDTO.getCluster(), role).getRoleClusterName());
    }

    @Test
    public void getByIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        roleClusterService.save(esRoleClusterDTO).success();
        Assertions.assertEquals(esRoleClusterDTO.getElasticClusterId(),
                roleClusterService.getById(esRoleClusterDTO.getId()).getElasticClusterId());
    }

    @Test
    public void getAllRoleClusterByClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        roleClusterService.save(esRoleClusterDTO).success();
        Assertions.assertTrue(roleClusterService.getAllRoleClusterByClusterId(esRoleClusterDTO
                        .getElasticClusterId()
                        .intValue()).stream()
                .anyMatch(esRoleCluster -> esRoleCluster.getId().equals(esRoleClusterDTO.getId())));
    }

    @Test
    public void getByClusterIdAndClusterRoleTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        roleClusterService.save(esRoleClusterDTO).success();
        Assertions.assertEquals(esRoleClusterDTO.getId(),
                roleClusterService.getByClusterIdAndClusterRole(esRoleClusterDTO.getElasticClusterId(), esRoleClusterDTO.getRoleClusterName()).getId());
    }

    /**
     * 这个方法和之前的不匹配，是否是必须要返回一个非空的值
     */
    @Test
    public void getByClusterIdAndRoleTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        roleClusterService.save(esRoleClusterDTO).success();
        Assertions.assertEquals(esRoleClusterDTO.getId(),
                roleClusterService.getByClusterIdAndRole(esRoleClusterDTO.getElasticClusterId(), esRoleClusterDTO.getRole()).getId());
    }

    @Test
    public void getByClusterNameAndRoleTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertNull(roleClusterService
                .getByClusterNameAndRole(esClusterDTO.getCluster(), esRoleClusterDTO.getRole()));
        ClusterPO clusterPO = ConvertUtil.obj2Obj(esClusterDTO, ClusterPO.class);
        clusterDAO.insert(clusterPO);
        esRoleClusterDTO.setElasticClusterId(clusterPO.getId().longValue());
        Assertions.assertTrue(roleClusterService.save(esRoleClusterDTO).success());
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class));
        Assertions.assertEquals(clusterPO.getId().longValue(),
                roleClusterService
                        .getByClusterNameAndRole(esClusterDTO.getCluster(), esRoleClusterDTO.getRole()).getElasticClusterId().longValue());

    }

    /**
     * 方法的命名感觉有点问题
     * 将Result修改为Result<ESRoleClusterPO>
     */
    @Test
    public void updatePodByClusterIdAndRoleTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(roleClusterService.save(esRoleClusterDTO).success());
        RoleCluster roleCluster = ConvertUtil.obj2Obj(esRoleClusterDTO, RoleCluster.class);
        esRoleClusterDTO.setRole(ESClusterNodeRoleEnum.DATA_NODE.getDesc());
        Result result = roleClusterService.updatePodByClusterIdAndRole(roleCluster);
    }

    @Test
    public void updateVersionByClusterIdAndRoleTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long clusterId = 1234l;
        roleClusterService.save(esRoleClusterDTO).success();
        RoleCluster roleCluster = ConvertUtil.obj2Obj(esRoleClusterDTO, RoleCluster.class);
        String esVersion = "7.6.0.0";
        Assertions.assertTrue(roleClusterService.updateVersionByClusterIdAndRole(clusterId,
                esRoleClusterDTO.getRole(), esVersion).failed());
    }

    @Test
    public void deleteRoleClusterByClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long elasticClusterId = esRoleClusterDTO.getElasticClusterId();
        Assertions.assertTrue(
                roleClusterService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).failed());
        roleClusterService.save(esRoleClusterDTO);
        Assertions.assertTrue(
                roleClusterService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).success());
    }
}
