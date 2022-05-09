package com.didichuxing.datachannel.arius.admin.core.service.cluster.physic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESRoleClusterHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.RoleClusterHostInfo;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeStatusEnum;
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
public class RoleClusterHostInfoInfoServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ESRoleClusterHostInfoDAO roleClusterHostInfoDAO;

    @Autowired
    private ESRoleClusterDAO esRoleClusterDAO;

    @MockBean
    private ESClusterNodeDAO esClusterNodeDAO;

    @Autowired
    private RoleClusterHostInfoService roleClusterHostInfoService;

    @Test
    public void queryNodeByCondtTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        roleClusterHostInfo.setId(roleClusterHostInfoService.save(roleClusterHostInfo).getData());
        List<RoleClusterHostInfo> roleClusterHostInfos = roleClusterHostInfoService.queryNodeByCondt(esRoleClusterHostInfoDTO);
        Assertions.assertTrue(roleClusterHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleClusterHostInfo.getId())));
    }

    @Test
    public void getNodesByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        roleClusterHostInfoService.save(roleClusterHostInfo);
        String clusterName = roleClusterHostInfo.getCluster();
        List<RoleClusterHostInfo> roleClusterHostInfos = roleClusterHostInfoService.getNodesByCluster(clusterName);
        Assertions.assertTrue(roleClusterHostInfos
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getCluster().equals(clusterName)));
    }

    @Test
    public void getOnlineNodesByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        roleClusterHostInfo.setId(id);
        esRoleClusterHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        List<RoleClusterHostInfo> onlineNodesByCluster = roleClusterHostInfoService.getOnlineNodesByCluster(
                roleClusterHostInfo.getCluster());
        Assertions.assertTrue(onlineNodesByCluster.stream().anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(
                roleClusterHostInfo.getId())));
    }

    @Test
    public void editNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("节点不存在").getMessage(),
                roleClusterHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
        Long id = roleClusterHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class)).getData();
        String port = "8020";
        esRoleClusterHostInfoDTO.setPort(port);
        esRoleClusterHostInfoDTO.setId(id);
        Assertions.assertTrue(roleClusterHostInfoService.editNode(esRoleClusterHostInfoDTO).success());
        Assertions.assertEquals(port, roleClusterHostInfoDAO.getById(id).getPort());
        esRoleClusterHostInfoDTO.setStatus(ESClusterNodeStatusEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点状态非法").getMessage(),
                roleClusterHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
        esRoleClusterHostInfoDTO.setRole(ESClusterNodeRoleEnum.UNKNOWN.getCode());
        Assertions.assertEquals(Result.buildParamIllegal("节点角色非法").getMessage(),
                roleClusterHostInfoService.editNode(esRoleClusterHostInfoDTO).getMessage());
    }

    @Test
    public void getIndicesCountTest() {
        int count = 1;
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Mockito.when(esClusterNodeDAO.getIndicesCount(Mockito.anyString(), Mockito.anyString())).thenReturn(count);
        Assertions.assertEquals(count, roleClusterHostInfoService
                .getIndicesCount(esRoleClusterHostInfoDTO.getCluster(), esRoleClusterHostInfoDTO.getRack()));
    }

    @Test
    public void listOnlineNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO1 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostInfoDTO1.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        RoleClusterHostInfo roleClusterHostInfo1 = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO1, RoleClusterHostInfo.class);
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO2 = CustomDataSource.esRoleClusterHostDTOFactory();
        esRoleClusterHostInfoDTO2.setStatus(ESClusterNodeStatusEnum.OFFLINE.getCode());
        RoleClusterHostInfo roleClusterHostInfo2 = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO2, RoleClusterHostInfo.class);
        // 由于唯一键uniq_elastic_cluster_id_role_node_set，这里做一些差异化的处理
        roleClusterHostInfo2.setRoleClusterId(esRoleClusterHostInfoDTO1.getRoleClusterId() + 1);
        Long id1 = roleClusterHostInfoService.save(roleClusterHostInfo1).getData();
        Long id2 = roleClusterHostInfoService.save(roleClusterHostInfo2).getData();
        // 分别mock在线和离线的节点进行数据的测试
        Assertions.assertTrue(roleClusterHostInfoService
                .listOnlineNode().stream().noneMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id2)));
        Assertions.assertTrue(roleClusterHostInfoService
                .listOnlineNode().stream().anyMatch(esRoleClusterHost -> esRoleClusterHost.getId().equals(id1)));
    }

    /**
     * 这一步的操作好像出现了问题，这个id值的设置是不合理的操作，所以进行了修改
     */
    @Test
    public void saveTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = roleClusterHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class)).getData();
        Assertions.assertEquals(esRoleClusterHostInfoDTO.getCluster(), roleClusterHostInfoDAO.getById(id).getCluster());
    }

    @Test
    public void getByIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Long id = roleClusterHostInfoService
                .save(ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class)).getData();
        Assertions.assertEquals(roleClusterHostInfoDAO.getById(id).getCluster(), roleClusterHostInfoService.getById(id).getCluster());
    }

    @Test
    public void getByRoleClusterIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(
                roleClusterHostInfoService.getByRoleClusterId(esRoleClusterHostInfoDTO.getRoleClusterId()).isEmpty());
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertTrue(roleClusterHostInfoService.getByRoleClusterId(roleClusterHostInfo.getRoleClusterId())
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
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        roleClusterHostInfoService.save(roleClusterHostInfo);
        Assertions.assertTrue(roleClusterHostInfoService
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
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Result<Long> saveResult = roleClusterHostInfoService.save(roleClusterHostInfo);
        Assertions.assertTrue(saveResult.success());
        Long roleId = saveResult.getData();
        Assertions.assertTrue(roleClusterHostInfoService
                .getByRoleAndClusterId(esRoleClusterPO.getElasticClusterId(), esRoleClusterPO.getRole())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(roleId)));
    }

    @Test
    public void deleteByClusterTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                roleClusterHostInfoService.deleteByCluster(esRoleClusterHostInfoDTO.getCluster()).getMessage());
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        roleClusterHostInfoService.save(roleClusterHostInfo);
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                roleClusterHostInfoService.deleteByCluster(roleClusterHostInfo.getCluster()).getMessage());
    }

    @Test
    public void listAllNodeTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertTrue(roleClusterHostInfoService
                .listAllNode()
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void listRacksNodesTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        Assertions.assertTrue(roleClusterHostInfoService.listRacksNodes(null, null).isEmpty());
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertTrue(roleClusterHostInfoService
                .listRacksNodes(roleClusterHostInfo.getCluster(), roleClusterHostInfo.getRack())
                .stream()
                .anyMatch(esRoleClusterHost1 -> esRoleClusterHost1.getId().equals(id)));
    }

    @Test
    public void setHostValidTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Assertions.assertTrue(roleClusterHostInfoService.setHostValid(roleClusterHostInfo).failed());
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertTrue(roleClusterHostInfoService.setHostValid(roleClusterHostInfo).failed());
        roleClusterHostInfoService.deleteById(id);
        // 设置mock插入的数据的id
        roleClusterHostInfo.setId(id);
        Assertions.assertTrue(roleClusterHostInfoService.setHostValid(roleClusterHostInfo).success());
    }

    @Test
    public void getDeleteHostByHostNameAnRoleIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertNull(roleClusterHostInfoService.getDeleteHostByHostNameAnRoleId(
                roleClusterHostInfo.getHostname(), roleClusterHostInfo.getRoleClusterId()));
        roleClusterHostInfoService.deleteById(id);
        Assertions.assertEquals(id, roleClusterHostInfoService.getDeleteHostByHostNameAnRoleId(
                roleClusterHostInfo.getHostname(), roleClusterHostInfo.getRoleClusterId()).getId());
    }

    /**
     * 这里是有冲突的，需要改为列表形式，否则只会传回查找到的第一条的记录
     */
    @Test
    public void getByHostNamesTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Assertions.assertNull(roleClusterHostInfoService.getByHostName(roleClusterHostInfo.getHostname()));
        roleClusterHostInfoService.save(roleClusterHostInfo);
        Assertions.assertEquals(esRoleClusterHostInfoDTO.getRoleClusterId(),
                roleClusterHostInfoService.getByHostName(roleClusterHostInfo.getHostname()).getRoleClusterId());
    }

    @Test
    public void deleteByIdTest() {
        ESRoleClusterHostInfoDTO esRoleClusterHostInfoDTO = CustomDataSource.esRoleClusterHostDTOFactory();
        RoleClusterHostInfo roleClusterHostInfo = ConvertUtil.obj2Obj(esRoleClusterHostInfoDTO, RoleClusterHostInfo.class);
        Long id = roleClusterHostInfoService.save(roleClusterHostInfo).getData();
        Assertions.assertEquals(Result.buildFail("failed to delete the clusterHost").getMessage(),
                roleClusterHostInfoService.deleteById(id + 1).getMessage());
        Assertions.assertEquals(Result.buildSuccWithMsg("success to delete the clusterHost").getMessage(),
                roleClusterHostInfoService.deleteById(id).getMessage());
        Assertions.assertNull(roleClusterHostInfoService.getById(id));
    }
}
