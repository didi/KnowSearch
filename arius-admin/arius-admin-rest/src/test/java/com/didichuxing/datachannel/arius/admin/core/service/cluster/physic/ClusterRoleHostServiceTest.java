package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ClusterRoleHost;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESClusterRolePO;
import com.didichuxing.datachannel.arius.admin.common.exception.AdminTaskException;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESClusterRoleHostDAO;
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
public class ClusterRoleHostServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESClusterRoleHostDAO esClusterRoleHostDAO;

    @Autowired
    private ESClusterRoleDAO esClusterRoleDAO;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Autowired
    private ClusterRoleHostService clusterRoleHostService;

    @Test
    public void queryNodeByCondtTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        clusterRoleHost.setId(clusterRoleHostService.save(clusterRoleHost).getData());
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.queryNodeByCondt(esClusterRoleHostDTO);
        Assertions.assertTrue(clusterRoleHosts
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(clusterRoleHost.getId())));
    }

    @Test
    public void getNodesByClusterTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        clusterRoleHostService.save(clusterRoleHost);
        String clusterName = clusterRoleHost.getCluster();
        List<ClusterRoleHost> clusterRoleHosts = clusterRoleHostService.getNodesByCluster(clusterName);
        Assertions.assertTrue(clusterRoleHosts
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getOnlineNodesByClusterTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        clusterRoleHost.setId(id);
        esClusterRoleHostDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        List<ClusterRoleHost> onlineNodesByCluster = clusterRoleHostService.getOnlineNodesByCluster(
                clusterRoleHost.getCluster());
        Assertions.assertTrue(onlineNodesByCluster.stream().anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(
                clusterRoleHost.getId())));
    }

    @Test
    public void editNodeTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("节点不存在").getMessage(),
                clusterRoleHostService.editNode(esClusterRoleHostDTO).getMessage());
        Long id = clusterRoleHostService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class)).getData();
        String port = "8020";
        esClusterRoleHostDTO.setPort(port);
        esClusterRoleHostDTO.setId(id);
        Assertions.assertTrue(clusterRoleHostService.editNode(esClusterRoleHostDTO).success());
        Assertions.assertEquals(port, esClusterRoleHostDAO.getById(id).getPort());
        esClusterRoleHostDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点状态非法").getMessage(),
                clusterRoleHostService.editNode(esClusterRoleHostDTO).getMessage());
        esClusterRoleHostDTO.setRole(ESClusterNodeRoleEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点角色非法").getMessage(),
                clusterRoleHostService.editNode(esClusterRoleHostDTO).getMessage());
    }

    @Test
    public void listOnlineNodeTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO1 = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleHostDTO1.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        ClusterRoleHost clusterRoleHost1 = ConvertUtil.obj2Obj(esClusterRoleHostDTO1, ClusterRoleHost.class);
        ESClusterRoleHostDTO esClusterRoleHostDTO2 = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleHostDTO2.setStatus(ESClusterNodeStatusEnum.OFFLINE.getCode());
        ClusterRoleHost clusterRoleHost2 = ConvertUtil.obj2Obj(esClusterRoleHostDTO2, ClusterRoleHost.class);
        // 由于唯一键uniq_elastic_cluster_id_role_node_set，这里做一些差异化的处理
        clusterRoleHost2.setRoleClusterId(esClusterRoleHostDTO1.getRoleClusterId() + 1);
        Long id1 = clusterRoleHostService.save(clusterRoleHost1).getData();
        Long id2 = clusterRoleHostService.save(clusterRoleHost2).getData();
        // 分别mock在线和离线的节点进行数据的测试
        Assertions.assertTrue(clusterRoleHostService
                .listOnlineNode().stream().noneMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id2)));
        Assertions.assertTrue(clusterRoleHostService
                .listOnlineNode().stream().anyMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id1)));
    }

    /**
     * 这一步的操作好像出现了问题，这个id值的设置是不合理的操作，所以进行了修改
     */
    @Test
    public void saveTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class)).getData();
        Assertions.assertEquals(esClusterRoleHostDTO.getCluster(), esClusterRoleHostDAO.getById(id).getCluster());
    }

    @Test
    public void getByIdTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = clusterRoleHostService
                .save(ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class)).getData();
        Assertions.assertEquals(esClusterRoleHostDAO.getById(id).getCluster(), clusterRoleHostService.getById(id).getCluster());
    }

    @Test
    public void getByRoleClusterIdTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(
                clusterRoleHostService.getByRoleClusterId(esClusterRoleHostDTO.getRoleClusterId()).isEmpty());
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        Assertions.assertTrue(clusterRoleHostService.getByRoleClusterId(clusterRoleHost.getRoleClusterId())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void getHostNamesByRoleAndClusterIdTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESClusterRolePO esClusterRolePO = ConvertUtil.obj2Obj(esClusterRoleDTO, ESClusterRolePO.class);
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleDAO.insert(esClusterRolePO);
        esClusterRoleHostDTO.setRoleClusterId(esClusterRolePO.getId());
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        clusterRoleHostService.save(clusterRoleHost);
        Assertions.assertTrue(clusterRoleHostService
                .getHostNamesByRoleAndClusterId(esClusterRolePO.getElasticClusterId(), esClusterRolePO.getRole())
                .stream()
                .anyMatch(s -> s.equals(esClusterRoleHostDTO.getHostname())));
    }

    @Test
    public void getByRoleAndClusterIdTest() {
        ESClusterRoleDTO esClusterRoleDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESClusterRolePO esClusterRolePO = ConvertUtil.obj2Obj(esClusterRoleDTO, ESClusterRolePO.class);
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esClusterRoleDAO.insert(esClusterRolePO);
        esClusterRoleHostDTO.setRoleClusterId(esClusterRolePO.getId());
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Result<Long> saveResult = clusterRoleHostService.save(clusterRoleHost);
        Assertions.assertTrue(saveResult.success());
        Long roleId = saveResult.getData();
        Assertions.assertTrue(clusterRoleHostService
                .getByRoleAndClusterId(esClusterRolePO.getElasticClusterId(), esClusterRolePO.getRole())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleId)));
    }

    @Test
    public void deleteByClusterTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostService.deleteByCluster(esClusterRoleHostDTO.getCluster()).getMessage());
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        clusterRoleHostService.save(clusterRoleHost);
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostService.deleteByCluster(clusterRoleHost.getCluster()).getMessage());
    }

    @Test
    public void listAllNodeTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        Assertions.assertTrue(clusterRoleHostService
                .listAllNode()
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void listAllNodeByRole() {
        List<ClusterRoleHost> dataNodeList = clusterRoleHostService.listAllNodeByRole(ESClusterNodeRoleEnum.DATA_NODE.getCode());
        Assertions.assertFalse(dataNodeList.isEmpty());

        List<ClusterRoleHost> clientNodeList = clusterRoleHostService.listAllNodeByRole(ESClusterNodeRoleEnum.CLIENT_NODE.getCode());
        Assertions.assertFalse(clientNodeList.isEmpty());

        List<ClusterRoleHost> masterNodeList = clusterRoleHostService.listAllNodeByRole(ESClusterNodeRoleEnum.MASTER_NODE.getCode());
        Assertions.assertFalse(masterNodeList.isEmpty());
    }

    @Test
    public void setHostValidTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Assertions.assertTrue(clusterRoleHostService.setHostValid(clusterRoleHost).failed());
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        Assertions.assertTrue(clusterRoleHostService.setHostValid(clusterRoleHost).failed());
        clusterRoleHostService.deleteById(id);
        // 设置mock插入的数据的id
        clusterRoleHost.setId(id);
        Assertions.assertTrue(clusterRoleHostService.setHostValid(clusterRoleHost).success());
    }

    @Test
    public void getDeleteHostByHostNameAnRoleIdTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        Assertions.assertNull(clusterRoleHostService.getDeleteHostByHostNameAnRoleId(
                clusterRoleHost.getHostname(), clusterRoleHost.getRoleClusterId()));
        clusterRoleHostService.deleteById(id);
        Assertions.assertEquals(id, clusterRoleHostService.getDeleteHostByHostNameAnRoleId(
                clusterRoleHost.getHostname(), clusterRoleHost.getRoleClusterId()).getId());
    }

    /**
     * 这里是有冲突的，需要改为列表形式，否则只会传回查找到的第一条的记录
     */
    @Test
    public void getByHostNamesTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Assertions.assertNull(clusterRoleHostService.getByHostName(clusterRoleHost.getHostname()));
        clusterRoleHostService.save(clusterRoleHost);
        Assertions.assertEquals(esClusterRoleHostDTO.getRoleClusterId(),
                clusterRoleHostService.getByHostName(clusterRoleHost.getHostname()).getRoleClusterId());
    }

    @Test
    public void deleteByIdTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Long id = clusterRoleHostService.save(clusterRoleHost).getData();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                clusterRoleHostService.deleteById(id + 1).getMessage());
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                clusterRoleHostService.deleteById(id).getMessage());
        Assertions.assertNull(clusterRoleHostService.getById(id));
    }

    @Test
    public void listByRegionIdTest() {
        ESClusterRoleHostDTO esClusterRoleHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        ClusterRoleHost clusterRoleHost = ConvertUtil.obj2Obj(esClusterRoleHostDTO, ClusterRoleHost.class);
        Result<List<ClusterRoleHost>> ret = clusterRoleHostService.listByRegionId(clusterRoleHost.getRegionId());
        if (ret.failed()) { Assertions.assertNull(ret.getMessage());}
        if (ret.success()) { Assertions.assertNull(ret.getData());}
    }

    @Test
    public void collectClusterNodeSettingsTest() {
        ClusterPhyDTO clusterPhyDTO = CustomDataSource.esClusterDTOFactory();
        boolean succ = false;
        try {
            succ = clusterRoleHostService.collectClusterNodeSettings(clusterPhyDTO.getCluster());
        } catch (AdminTaskException e) {
            Assertions.assertNotNull(e.getMessage());
        }
        Assertions.assertTrue(succ);
    }
}
