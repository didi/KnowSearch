package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHostInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleHostInfoDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Rollback
public class ClusterRoleHostInfoInfoServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterRoleHostInfoDAO roleClusterHostInfoDAO;

    @Autowired
    private ESRoleClusterDAO esRoleClusterDAO;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Autowired
    private ClusterRoleHostInfoService clusterRoleHostInfoService;

    @Test
    public void queryNodeByCondtTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfo.setId(clusterRoleHostInfoService.save(clusterRoleHostInfo).getData());
        List<ClusterRoleHostInfo> clusterRoleHostInfos = clusterRoleHostInfoService.queryNodeByCondt(esRoleClusterHostInfoDTO);
        Assertions.assertTrue(clusterRoleHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(clusterRoleHostInfo.getId())));
    }

    @Test
    public void getNodesByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        String clusterName = clusterRoleHostInfo.getCluster();
        List<ClusterRoleHostInfo> clusterRoleHostInfos = clusterRoleHostInfoService.getNodesByCluster(clusterName);
        Assertions.assertTrue(clusterRoleHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getOnlineNodesByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        clusterRoleHostInfo.setId(id);
        esRoleClusterHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        List<ClusterRoleHostInfo> onlineNodesByCluster = clusterRoleHostInfoService.getOnlineNodesByCluster(
                clusterRoleHostInfo.getCluster());
        Assertions.assertTrue(onlineNodesByCluster.stream().anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(
                clusterRoleHostInfo.getId())));
    }

    @Test
    public void editNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("节点不存在").getMessage(),
                clusterRoleHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        String port = "8020";
        esRoleClusterHostInfoDTO.setPort(port);
        esRoleClusterHostInfoDTO.setId(id);
        Assertions.assertTrue(clusterRoleHostInfoService.editNode(esRoleClusterHostInfoDTO).success());
        Assertions.assertEquals(port, roleClusterHostInfoDAO.getById(id).getPort());
        esRoleClusterHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点状态非法").getMessage(),
                clusterRoleHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
        esRoleClusterHostInfoDTO.setRole(ESClusterNodeRoleEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点角色非法").getMessage(),
                clusterRoleHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
    }

    @Test
    public void getIndicesCountTest() {
        int count = 1;
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Mockito.when(esClusterNodeDAO.getIndicesCount(Mockito.anyString(), Mockito.anyString())).thenReturn(count);
        Assertions.assertEquals(count, clusterRoleHostInfoService
                .getIndicesCount(esRoleClusterHostInfoDTO.getCluster(), esRoleClusterHostInfoDTO.getRack()));
    }

    @Test
    public void listOnlineNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO1 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostInfoDTO1.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        ClusterRoleHostInfo clusterRoleHostInfo1 = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO1, ClusterRoleHostInfo.class);
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO2 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostInfoDTO2.setStatus(ESClusterNodeStatusEnum.OFFLINE.getCode());
        ClusterRoleHostInfo clusterRoleHostInfo2 = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO2, ClusterRoleHostInfo.class);
        // 由于唯一键uniq_elastic_cluster_id_role_node_set，这里做一些差异化的处理
        clusterRoleHostInfo2.setRoleClusterId(esRoleClusterHostInfoDTO1.getRoleClusterId() + 1);
        Long id1 = clusterRoleHostInfoService.save(clusterRoleHostInfo1).getData();
        Long id2 = clusterRoleHostInfoService.save(clusterRoleHostInfo2).getData();
        // 分别mock在线和离线的节点进行数据的测试
        Assertions.assertTrue(clusterRoleHostInfoService
                .listOnlineNode().stream().noneMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id2)));
        Assertions.assertTrue(clusterRoleHostInfoService
                .listOnlineNode().stream().anyMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id1)));
    }

    /**
     * 这一步的操作好像出现了问题，这个id值的设置是不合理的操作，所以进行了修改
     */
    @Test
    public void saveTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        Assertions.assertEquals(esRoleClusterHostInfoDTO.getCluster(), roleClusterHostInfoDAO.getById(id).getCluster());
    }

    @Test
    public void getByIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        Assertions.assertEquals(roleClusterHostInfoDAO.getById(id).getCluster(), clusterRoleHostInfoService.getById(id).getCluster());
    }

    @Test
    public void getByRoleClusterIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(
                clusterRoleHostInfoService.getByRoleClusterId(esRoleClusterHostInfoDTO.getRoleClusterId()).isEmpty());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService.getByRoleClusterId(clusterRoleHostInfo.getRoleClusterId())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void getHostNamesByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esRoleClusterHostInfoDTO.setRoleClusterId(esRoleClusterPO.getId());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertTrue(clusterRoleHostInfoService
                .getHostNamesByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(s -> s.equals(esRoleClusterHostInfoDTO.getHostname())));
    }

    @Test
    public void getByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esRoleClusterHostInfoDTO.setRoleClusterId(esRoleClusterPO.getId());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Result<Long> saveResult = clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertTrue(saveResult.success());
        Long roleId = saveResult.getData();
        Assertions.assertTrue(clusterRoleHostInfoService
                .getByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleId)));
    }

    @Test
    public void deleteByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteByCluster(esRoleClusterHostInfoDTO.getCluster()).getMessage());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteByCluster(clusterRoleHostInfo.getCluster()).getMessage());
    }

    @Test
    public void listAllNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService
                .listAllNode()
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void listRacksNodesTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(clusterRoleHostInfoService.listRacksNodes(null, null).isEmpty());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService
                .listRacksNodes(clusterRoleHostInfo.getCluster(), clusterRoleHostInfo.getRack())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void setHostValidTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Assertions.assertTrue(clusterRoleHostInfoService.setHostValid(clusterRoleHostInfo).failed());
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService.setHostValid(clusterRoleHostInfo).failed());
        clusterRoleHostInfoService.deleteById(id);
        // 设置mock插入的数据的id
        clusterRoleHostInfo.setId(id);
        Assertions.assertTrue(clusterRoleHostInfoService.setHostValid(clusterRoleHostInfo).success());
    }

    @Test
    public void getDeleteHostByHostNameAnRoleIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertNull(clusterRoleHostInfoService.getDeleteHostByHostNameAnRoleId(
                clusterRoleHostInfo.getHostname(), clusterRoleHostInfo.getRoleClusterId()));
        clusterRoleHostInfoService.deleteById(id);
        Assertions.assertEquals(id, clusterRoleHostInfoService.getDeleteHostByHostNameAnRoleId(
                clusterRoleHostInfo.getHostname(), clusterRoleHostInfo.getRoleClusterId()).getId());
    }

    /**
     * 这里是有冲突的，需要改为列表形式，否则只会传回查找到的第一条的记录
     */
    @Test
    public void getByHostNamesTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Assertions.assertNull(clusterRoleHostInfoService.getByHostName(clusterRoleHostInfo.getHostname()));
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertEquals(esRoleClusterHostInfoDTO.getRoleClusterId(),
                clusterRoleHostInfoService.getByHostName(clusterRoleHostInfo.getHostname()).getRoleClusterId());
    }

    @Test
    public void deleteByIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteById(id + 1).getMessage());
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteById(id).getMessage());
        Assertions.assertNull(clusterRoleHostInfoService.getById(id));
    }
}
