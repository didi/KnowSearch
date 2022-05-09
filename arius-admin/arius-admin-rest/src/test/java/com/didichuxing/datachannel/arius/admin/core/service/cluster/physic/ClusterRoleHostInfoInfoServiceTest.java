package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostInfoDTO;
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
    private ESClusterRoleHostInfoDAO esClusterRoleHostInfoDAO;

    @Autowired
    private ESRoleClusterDAO esRoleClusterDAO;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Autowired
    private ClusterRoleHostInfoService clusterRoleHostInfoService;

    @Test
    public void queryNodeByCondtTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfo.setId(clusterRoleHostInfoService.save(clusterRoleHostInfo).getData());
        List<ClusterRoleHostInfo> clusterRoleHostInfos = clusterRoleHostInfoService.queryNodeByCondt(esClusterRoleHostInfoDTO);
        Assertions.assertTrue(clusterRoleHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(clusterRoleHostInfo.getId())));
    }

    @Test
    public void getNodesByClusterTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        String clusterName = clusterRoleHostInfo.getCluster();
        List<ClusterRoleHostInfo> clusterRoleHostInfos = clusterRoleHostInfoService.getNodesByCluster(clusterName);
        Assertions.assertTrue(clusterRoleHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getOnlineNodesByClusterTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        clusterRoleHostInfo.setId(id);
        esClusterRoleHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        List<ClusterRoleHostInfo> onlineNodesByCluster = clusterRoleHostInfoService.getOnlineNodesByCluster(
                clusterRoleHostInfo.getCluster());
        Assertions.assertTrue(onlineNodesByCluster.stream().anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(
                clusterRoleHostInfo.getId())));
    }

    @Test
    public void editNodeTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("节点不存在").getMessage(),
                clusterRoleHostInfoService.editNode(esClusterRoleHostInfoDTO).getMessage());
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        String port = "8020";
        esClusterRoleHostInfoDTO.setPort(port);
        esClusterRoleHostInfoDTO.setId(id);
        Assertions.assertTrue(clusterRoleHostInfoService.editNode(esClusterRoleHostInfoDTO).success());
        Assertions.assertEquals(port, esClusterRoleHostInfoDAO.getById(id).getPort());
        esClusterRoleHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点状态非法").getMessage(),
                clusterRoleHostInfoService.editNode(esClusterRoleHostInfoDTO).getMessage());
        esClusterRoleHostInfoDTO.setRole(ESClusterNodeRoleEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点角色非法").getMessage(),
                clusterRoleHostInfoService.editNode(esClusterRoleHostInfoDTO).getMessage());
    }

    @Test
    public void getIndicesCountTest() {
        int count = 1;
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Mockito.when(esClusterNodeDAO.getIndicesCount(Mockito.anyString(), Mockito.anyString())).thenReturn(count);
        Assertions.assertEquals(count, clusterRoleHostInfoService
                .getIndicesCount(esClusterRoleHostInfoDTO.getCluster(), esClusterRoleHostInfoDTO.getRack()));
    }

    @Test
    public void listOnlineNodeTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO1 = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleHostInfoDTO1.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        ClusterRoleHostInfo clusterRoleHostInfo1 = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO1, ClusterRoleHostInfo.class);
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO2 = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleHostInfoDTO2.setStatus(ESClusterNodeStatusEnum.OFFLINE.getCode());
        ClusterRoleHostInfo clusterRoleHostInfo2 = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO2, ClusterRoleHostInfo.class);
        // 由于唯一键uniq_elastic_cluster_id_role_node_set，这里做一些差异化的处理
        clusterRoleHostInfo2.setRoleClusterId(esClusterRoleHostInfoDTO1.getRoleClusterId() + 1);
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
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        Assertions.assertEquals(esClusterRoleHostInfoDTO.getCluster(), esClusterRoleHostInfoDAO.getById(id).getCluster());
    }

    @Test
    public void getByIdTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostInfoService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class)).getData();
        Assertions.assertEquals(esClusterRoleHostInfoDAO.getById(id).getCluster(), clusterRoleHostInfoService.getById(id).getCluster());
    }

    @Test
    public void getByRoleClusterIdTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(
                clusterRoleHostInfoService.getByRoleClusterId(esClusterRoleHostInfoDTO.getRoleClusterId()).isEmpty());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService.getByRoleClusterId(clusterRoleHostInfo.getRoleClusterId())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void getHostNamesByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esClusterRoleHostInfoDTO.setRoleClusterId(esRoleClusterPO.getId());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertTrue(clusterRoleHostInfoService
                .getHostNamesByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(s -> s.equals(esClusterRoleHostInfoDTO.getHostname())));
    }

    @Test
    public void getByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esClusterRoleHostInfoDTO.setRoleClusterId(esRoleClusterPO.getId());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
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
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteByCluster(esClusterRoleHostInfoDTO.getCluster()).getMessage());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteByCluster(clusterRoleHostInfo.getCluster()).getMessage());
    }

    @Test
    public void listAllNodeTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService
                .listAllNode()
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void listRacksNodesTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(clusterRoleHostInfoService.listRacksNodes(null, null).isEmpty());
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertTrue(clusterRoleHostInfoService
                .listRacksNodes(clusterRoleHostInfo.getCluster(), clusterRoleHostInfo.getRack())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void setHostValidTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
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
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
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
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Assertions.assertNull(clusterRoleHostInfoService.getByHostName(clusterRoleHostInfo.getHostname()));
        clusterRoleHostInfoService.save(clusterRoleHostInfo);
        Assertions.assertEquals(esClusterRoleHostInfoDTO.getRoleClusterId(),
                clusterRoleHostInfoService.getByHostName(clusterRoleHostInfo.getHostname()).getRoleClusterId());
    }

    @Test
    public void deleteByIdTest() {
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHostInfo clusterRoleHostInfo = ConvertUtil.obj2Obj(esClusterRoleHostInfoDTO, ClusterRoleHostInfo.class);
        Long id = clusterRoleHostInfoService.save(clusterRoleHostInfo).getData();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteById(id + 1).getMessage());
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostInfoService.deleteById(id).getMessage());
        Assertions.assertNull(clusterRoleHostInfoService.getById(id));
    }
}
