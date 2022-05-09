package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHost;
import com.didichuxing.datachannel.arius.admin.common.bean.po.ecm.ESRoleClusterPO;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESClusterNodeDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.ecm.ESRoleClusterHostInfoDAO;
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
public class RoleClusterHostServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESRoleClusterHostInfoDAO roleClusterHostInfoDAO;

    @Autowired
    private ESRoleClusterDAO esRoleClusterDAO;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Autowired
    private RoleClusterHostService roleClusterHostService;

    @Test
    public void queryNodeByCondtTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        roleClusterHost.setId(roleClusterHostService.save(roleClusterHost).getData());
        List<RoleClusterHost> roleClusterHosts = roleClusterHostService.queryNodeByCondt(esRoleClusterHostDTO);
        Assertions.assertTrue(roleClusterHosts
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleClusterHost.getId())));
    }

    @Test
    public void getNodesByClusterTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        roleClusterHostService.save(roleClusterHost);
        String clusterName = roleClusterHost.getCluster();
        List<RoleClusterHost> roleClusterHosts = roleClusterHostService.getNodesByCluster(clusterName);
        Assertions.assertTrue(roleClusterHosts
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getOnlineNodesByClusterTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        roleClusterHost.setId(id);
        esRoleClusterHostDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        List<RoleClusterHost> onlineNodesByCluster = roleClusterHostService.getOnlineNodesByCluster(
                roleClusterHost.getCluster());
        Assertions.assertTrue(onlineNodesByCluster.stream().anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(
                roleClusterHost.getId())));
    }

    @Test
    public void editNodeTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("节点不存在").getMessage(),
                roleClusterHostService.editNode(esRoleClusterHostDTO).getMessage());
        Long id = roleClusterHostService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class)).getData();
        String port = "8020";
        esRoleClusterHostDTO.setPort(port);
        esRoleClusterHostDTO.setId(id);
        Assertions.assertTrue(roleClusterHostService.editNode(esRoleClusterHostDTO).success());
        Assertions.assertEquals(port, roleClusterHostInfoDAO.getById(id).getPort());
        esRoleClusterHostDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点状态非法").getMessage(),
                roleClusterHostService.editNode(esRoleClusterHostDTO).getMessage());
        esRoleClusterHostDTO.setRole(ESClusterNodeRoleEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点角色非法").getMessage(),
                roleClusterHostService.editNode(esRoleClusterHostDTO).getMessage());
    }

    @Test
    public void getIndicesCountTest() {
        int count = 1;
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Mockito.when(esClusterNodeDAO.getIndicesCount(Mockito.anyString(), Mockito.anyString())).thenReturn(count);
        Assertions.assertEquals(count, roleClusterHostService
                .getIndicesCount(esRoleClusterHostDTO.getCluster(), esRoleClusterHostDTO.getRack()));
    }

    @Test
    public void listOnlineNodeTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO1 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostDTO1.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        RoleClusterHost roleClusterHost1 = ConvertUtil.obj2Obj(esRoleClusterHostDTO1, RoleClusterHost.class);
        ESRoleClusterHostDTO esRoleClusterHostDTO2 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostDTO2.setStatus(ESClusterNodeStatusEnum.OFFLINE.getCode());
        RoleClusterHost roleClusterHost2 = ConvertUtil.obj2Obj(esRoleClusterHostDTO2, RoleClusterHost.class);
        // 由于唯一键uniq_elastic_cluster_id_role_node_set，这里做一些差异化的处理
        roleClusterHost2.setRoleClusterId(esRoleClusterHostDTO1.getRoleClusterId() + 1);
        Long id1 = roleClusterHostService.save(roleClusterHost1).getData();
        Long id2 = roleClusterHostService.save(roleClusterHost2).getData();
        // 分别mock在线和离线的节点进行数据的测试
        Assertions.assertTrue(roleClusterHostService
                .listOnlineNode().stream().noneMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id2)));
        Assertions.assertTrue(roleClusterHostService
                .listOnlineNode().stream().anyMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id1)));
    }

    /**
     * 这一步的操作好像出现了问题，这个id值的设置是不合理的操作，所以进行了修改
     */
    @Test
    public void saveTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = roleClusterHostService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class)).getData();
        Assertions.assertEquals(esRoleClusterHostDTO.getCluster(), roleClusterHostInfoDAO.getById(id).getCluster());
    }

    @Test
    public void getByIdTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = roleClusterHostService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class)).getData();
        Assertions.assertEquals(roleClusterHostInfoDAO.getById(id).getCluster(), roleClusterHostService.getById(id).getCluster());
    }

    @Test
    public void getByRoleClusterIdTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(
                roleClusterHostService.getByRoleClusterId(esRoleClusterHostDTO.getRoleClusterId()).isEmpty());
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertTrue(roleClusterHostService.getByRoleClusterId(roleClusterHost.getRoleClusterId())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void getHostNamesByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esRoleClusterHostDTO.setRoleClusterId(esRoleClusterPO.getId());
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        roleClusterHostService.save(roleClusterHost);
        Assertions.assertTrue(roleClusterHostService
                .getHostNamesByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(s -> s.equals(esRoleClusterHostDTO.getHostname())));
    }

    @Test
    public void getByRoleAndClusterIdTest() {
        ESRoleClusterDTO esRoleClusterDTO = CustomDataSource.esRoleClusterDTOFactory();
        ESRoleClusterPO esRoleClusterPO = ConvertUtil.obj2Obj(esRoleClusterDTO, ESRoleClusterPO.class);
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterDAO.insert(esRoleClusterPO);
        esRoleClusterHostDTO.setRoleClusterId(esRoleClusterPO.getId());
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Result<Long> saveResult = roleClusterHostService.save(roleClusterHost);
        Assertions.assertTrue(saveResult.success());
        Long roleId = saveResult.getData();
        Assertions.assertTrue(roleClusterHostService
                .getByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleId)));
    }

    @Test
    public void deleteByClusterTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                roleClusterHostService.deleteByCluster(esRoleClusterHostDTO.getCluster()).getMessage());
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        roleClusterHostService.save(roleClusterHost);
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                roleClusterHostService.deleteByCluster(roleClusterHost.getCluster()).getMessage());
    }

    @Test
    public void listAllNodeTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertTrue(roleClusterHostService
                .listAllNode()
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void listRacksNodesTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(roleClusterHostService.listRacksNodes(null, null).isEmpty());
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertTrue(roleClusterHostService
                .listRacksNodes(roleClusterHost.getCluster(), roleClusterHost.getRack())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void setHostValidTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Assertions.assertTrue(roleClusterHostService.setHostValid(roleClusterHost).failed());
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertTrue(roleClusterHostService.setHostValid(roleClusterHost).failed());
        roleClusterHostService.deleteById(id);
        // 设置mock插入的数据的id
        roleClusterHost.setId(id);
        Assertions.assertTrue(roleClusterHostService.setHostValid(roleClusterHost).success());
    }

    @Test
    public void getDeleteHostByHostNameAnRoleIdTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertNull(roleClusterHostService.getDeleteHostByHostNameAnRoleId(
                roleClusterHost.getHostname(), roleClusterHost.getRoleClusterId()));
        roleClusterHostService.deleteById(id);
        Assertions.assertEquals(id, roleClusterHostService.getDeleteHostByHostNameAnRoleId(
                roleClusterHost.getHostname(), roleClusterHost.getRoleClusterId()).getId());
    }

    /**
     * 这里是有冲突的，需要改为列表形式，否则只会传回查找到的第一条的记录
     */
    @Test
    public void getByHostNamesTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Assertions.assertNull(roleClusterHostService.getByHostName(roleClusterHost.getHostname()));
        roleClusterHostService.save(roleClusterHost);
        Assertions.assertEquals(esRoleClusterHostDTO.getRoleClusterId(),
                roleClusterHostService.getByHostName(roleClusterHost.getHostname()).getRoleClusterId());
    }

    @Test
    public void deleteByIdTest() {
        ESRoleClusterHostDTO esRoleClusterHostDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHost roleClusterHost = ConvertUtil.obj2Obj(esRoleClusterHostDTO, RoleClusterHost.class);
        Long id = roleClusterHostService.save(roleClusterHost).getData();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                roleClusterHostService.deleteById(id + 1).getMessage());
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                roleClusterHostService.deleteById(id).getMessage());
        Assertions.assertNull(roleClusterHostService.getById(id));
    }
}
