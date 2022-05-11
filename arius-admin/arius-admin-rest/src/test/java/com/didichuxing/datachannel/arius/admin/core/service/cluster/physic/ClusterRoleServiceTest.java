package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleDAO;
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
public class ClusterRoleServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterRoleDAO roleClusterDAO;

    @Autowired
    private ClusterDAO clusterDAO;

    @Autowired
    private ClusterRoleService clusterRoleService;

    @MockBean
    private ClusterPhyService clusterPhyService;

    /**
     * 不需要做其他字段的校验吗
     */
    @Test
    public void saveTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(clusterRoleService.save(esClusterRoleDTO).success());
        Assertions.assertEquals(esClusterRoleDTO.getId(),
                roleClusterDAO.getByClusterIdAndRole(esClusterRoleDTO.getElasticClusterId(), esClusterRoleDTO.getRole()).getId());
    }

    /**
     * 仍然是没有对于null搜索的判断，容易出现NPE问题
     * 无法反映出是否创建成功
     */
    @Test
    public void createRoleClusterIfNotExistTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setId(12344);
        String role = esClusterRoleDTO.getRole();
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(esClusterDTO, ClusterPhy.class));
        Assertions.assertEquals(esClusterDTO.getCluster() + "-" + esClusterRoleDTO.getRole(),
                clusterRoleService.createRoleClusterIfNotExist(esClusterDTO.getCluster(), role).getRoleClusterName());
    }

    @Test
    public void getByIdTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleService.save(esClusterRoleDTO).success();
        Assertions.assertEquals(esClusterRoleDTO.getElasticClusterId(),
                clusterRoleService.getById(esClusterRoleDTO.getId()).getElasticClusterId());
    }

    @Test
    public void getAllRoleClusterByClusterIdTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleService.save(esClusterRoleDTO).success();
        Assertions.assertTrue(clusterRoleService.getAllRoleClusterByClusterId(esClusterRoleDTO
                        .getElasticClusterId()
                        .intValue()).stream()
                .anyMatch(esRoleCluster -> esRoleCluster.getId().equals(esClusterRoleDTO.getId())));
    }

    @Test
    public void getByClusterIdAndClusterRoleTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleService.save(esClusterRoleDTO).success();
        Assertions.assertEquals(esClusterRoleDTO.getId(),
                clusterRoleService.getByClusterIdAndClusterRole(esClusterRoleDTO.getElasticClusterId(), esClusterRoleDTO.getRoleClusterName()).getId());
    }

    /**
     * 这个方法和之前的不匹配，是否是必须要返回一个非空的值
     */
    @Test
    public void getByClusterIdAndRoleTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleService.save(esClusterRoleDTO).success();
        Assertions.assertEquals(esClusterRoleDTO.getId(),
                clusterRoleService.getByClusterIdAndRole(esClusterRoleDTO.getElasticClusterId(), esClusterRoleDTO.getRole()).getId());
    }

    @Test
    public void getByClusterNameAndRoleTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertNull(clusterRoleService
                .getByClusterNameAndRole(esClusterDTO.getCluster(), esClusterRoleDTO.getRole()));
        ClusterPO clusterPO = ConvertUtil.obj2Obj(esClusterDTO, ClusterPO.class);
        clusterDAO.insert(clusterPO);
        esClusterRoleDTO.setElasticClusterId(clusterPO.getId().longValue());
        Assertions.assertTrue(clusterRoleService.save(esClusterRoleDTO).success());
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class));
        Assertions.assertEquals(clusterPO.getId().longValue(),
                clusterRoleService
                        .getByClusterNameAndRole(esClusterDTO.getCluster(), esClusterRoleDTO.getRole()).getElasticClusterId().longValue());

    }

    /**
     * 方法的命名感觉有点问题
     * 将Result修改为Result<ESRoleClusterPO>
     */
    @Test
    public void updatePodByClusterIdAndRoleTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(clusterRoleService.save(esClusterRoleDTO).success());
        ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(esClusterRoleDTO, ClusterRoleInfo.class);
        esClusterRoleDTO.setRole(ESClusterNodeRoleEnum.DATA_NODE.getDesc());
        Result result = clusterRoleService.updatePodByClusterIdAndRole(clusterRoleInfo);
    }

    @Test
    public void updateVersionByClusterIdAndRoleTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long clusterId = 1234l;
        clusterRoleService.save(esClusterRoleDTO).success();
        ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(esClusterRoleDTO, ClusterRoleInfo.class);
        String esVersion = "7.6.0.0";
        Assertions.assertTrue(clusterRoleService.updateVersionByClusterIdAndRole(clusterId,
                esClusterRoleDTO.getRole(), esVersion).failed());
    }

    @Test
    public void deleteRoleClusterByClusterIdTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long elasticClusterId = esClusterRoleDTO.getElasticClusterId();
        Assertions.assertTrue(
                clusterRoleService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).failed());
        clusterRoleService.save(esClusterRoleDTO);
        Assertions.assertTrue(
                clusterRoleService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).success());
    }
}
