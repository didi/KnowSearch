package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterPhy;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleInfoDAO;
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
public class ClusterRoleInfoServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterRoleInfoDAO roleClusterDAO;

    @Autowired
    private ClusterDAO clusterDAO;

    @Autowired
    private ClusterRoleInfoService clusterRoleInfoService;

    @MockBean
    private ClusterPhyService clusterPhyService;

    /**
     * 不需要做其他字段的校验吗
     */
    @Test
    public void saveTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(clusterRoleInfoService.save(esClusterRoleInfoDTO).success());
        Assertions.assertEquals(esClusterRoleInfoDTO.getId(),
                roleClusterDAO.getByClusterIdAndRole(esClusterRoleInfoDTO.getElasticClusterId(), esClusterRoleInfoDTO.getRole()).getId());
    }

    /**
     * 仍然是没有对于null搜索的判断，容易出现NPE问题
     * 无法反映出是否创建成功
     */
    @Test
    public void createRoleClusterIfNotExistTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        esClusterDTO.setId(12344);
        String role = esClusterRoleInfoDTO.getRole();
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(esClusterDTO, ClusterPhy.class));
        Assertions.assertEquals(esClusterDTO.getCluster() + "-" + esClusterRoleInfoDTO.getRole(),
                clusterRoleInfoService.createRoleClusterIfNotExist(esClusterDTO.getCluster(), role).getRoleClusterName());
    }

    @Test
    public void getByIdTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleInfoService.save(esClusterRoleInfoDTO).success();
        Assertions.assertEquals(esClusterRoleInfoDTO.getElasticClusterId(),
                clusterRoleInfoService.getById(esClusterRoleInfoDTO.getId()).getElasticClusterId());
    }

    @Test
    public void getAllRoleClusterByClusterIdTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleInfoService.save(esClusterRoleInfoDTO).success();
        Assertions.assertTrue(clusterRoleInfoService.getAllRoleClusterByClusterId(esClusterRoleInfoDTO
                        .getElasticClusterId()
                        .intValue()).stream()
                .anyMatch(esRoleCluster -> esRoleCluster.getId().equals(esClusterRoleInfoDTO.getId())));
    }

    @Test
    public void getByClusterIdAndClusterRoleTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleInfoService.save(esClusterRoleInfoDTO).success();
        Assertions.assertEquals(esClusterRoleInfoDTO.getId(),
                clusterRoleInfoService.getByClusterIdAndClusterRole(esClusterRoleInfoDTO.getElasticClusterId(), esClusterRoleInfoDTO.getRoleClusterName()).getId());
    }

    /**
     * 这个方法和之前的不匹配，是否是必须要返回一个非空的值
     */
    @Test
    public void getByClusterIdAndRoleTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        clusterRoleInfoService.save(esClusterRoleInfoDTO).success();
        Assertions.assertEquals(esClusterRoleInfoDTO.getId(),
                clusterRoleInfoService.getByClusterIdAndRole(esClusterRoleInfoDTO.getElasticClusterId(), esClusterRoleInfoDTO.getRole()).getId());
    }

    @Test
    public void getByClusterNameAndRoleTest() {
        ESClusterDTO esClusterDTO = CustomDataSource.esClusterDTOFactory();
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertNull(clusterRoleInfoService
                .getByClusterNameAndRole(esClusterDTO.getCluster(), esClusterRoleInfoDTO.getRole()));
        ClusterPO clusterPO = ConvertUtil.obj2Obj(esClusterDTO, ClusterPO.class);
        clusterDAO.insert(clusterPO);
        esClusterRoleInfoDTO.setElasticClusterId(clusterPO.getId().longValue());
        Assertions.assertTrue(clusterRoleInfoService.save(esClusterRoleInfoDTO).success());
        Mockito.when(clusterPhyService.getClusterByName(Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(clusterPO, ClusterPhy.class));
        Assertions.assertEquals(clusterPO.getId().longValue(),
                clusterRoleInfoService
                        .getByClusterNameAndRole(esClusterDTO.getCluster(), esClusterRoleInfoDTO.getRole()).getElasticClusterId().longValue());

    }

    /**
     * 方法的命名感觉有点问题
     * 将Result修改为Result<ESRoleClusterPO>
     */
    @Test
    public void updatePodByClusterIdAndRoleTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        Assertions.assertTrue(clusterRoleInfoService.save(esClusterRoleInfoDTO).success());
        ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(esClusterRoleInfoDTO, ClusterRoleInfo.class);
        esClusterRoleInfoDTO.setRole(ESClusterNodeRoleEnum.DATA_NODE.getDesc());
        Result result = clusterRoleInfoService.updatePodByClusterIdAndRole(clusterRoleInfo);
    }

    @Test
    public void updateVersionByClusterIdAndRoleTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long clusterId = 1234l;
        clusterRoleInfoService.save(esClusterRoleInfoDTO).success();
        ClusterRoleInfo clusterRoleInfo = ConvertUtil.obj2Obj(esClusterRoleInfoDTO, ClusterRoleInfo.class);
        String esVersion = "7.6.0.0";
        Assertions.assertTrue(clusterRoleInfoService.updateVersionByClusterIdAndRole(clusterId,
                esClusterRoleInfoDTO.getRole(), esVersion).failed());
    }

    @Test
    public void deleteRoleClusterByClusterIdTest() {
        ESClusterRoleInfoDTO esClusterRoleInfoDTO = CustomDataSource.esRoleClusterDTOFactory();
        Long elasticClusterId = esClusterRoleInfoDTO.getElasticClusterId();
        Assertions.assertTrue(
                clusterRoleInfoService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).failed());
        clusterRoleInfoService.save(esClusterRoleInfoDTO);
        Assertions.assertTrue(
                clusterRoleInfoService.deleteRoleClusterByClusterId(elasticClusterId.intValue()).success());
    }
}
