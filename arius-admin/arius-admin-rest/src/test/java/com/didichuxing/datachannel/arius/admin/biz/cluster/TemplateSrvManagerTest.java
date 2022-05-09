package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateServiceEnum;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Transactional
@Rollback
public class TemplateSrvManagerTest extends AriusAdminApplicationTest {
    @Autowired
    private TemplateSrvManager templateSrvManager;

    /**
     * 测试新建集群开启冷热分离服务
     */
    // @Test
    public void addTemplateSrvTest1() {
        //没有设置冷节点的集群
        String clusterName = "dc-es02";
        Assertions.assertTrue(templateSrvManager.checkTemplateSrv(clusterName,
                TemplateServiceEnum.TEMPLATE_COLD.getCode().toString(), "wpk").failed());

        //有设置冷节点的集群
        clusterName = "cold_hot_test";
        Assertions.assertTrue(templateSrvManager.checkTemplateSrv(clusterName,
                TemplateServiceEnum.TEMPLATE_COLD.getCode().toString(), "wpk").success());
    }

    /**
     * 测试接入集群开启冷热分离服务
     */
    // @Test
    public void addTemplateSrvTest2() {
        //这里模拟使用一下自己接入的集群和已有的接入的但是没有冷节点的集群
        //具有冷节点的集群
        ClusterJoinDTO clusterJoinDTO1 = new ClusterJoinDTO();
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO1 = new ESClusterRoleHostInfoDTO();
        esClusterRoleHostInfoDTO1.setRole(ESClusterNodeRoleEnum.MASTER_NODE.getCode());
        esClusterRoleHostInfoDTO1.setIp("10.96.65.63");
        esClusterRoleHostInfoDTO1.setPort("8060");
        clusterJoinDTO1.setRoleClusterHosts(Collections.singletonList(esClusterRoleHostInfoDTO1));

        //不具有冷节点的集群
        ClusterJoinDTO clusterJoinDTO2 = new ClusterJoinDTO();
        ESClusterRoleHostInfoDTO esClusterRoleHostInfoDTO2 = new ESClusterRoleHostInfoDTO();
        esClusterRoleHostInfoDTO2.setRole(ESClusterNodeRoleEnum.CLIENT_NODE.getCode());
        esClusterRoleHostInfoDTO2.setIp("10.168.56.135");
        esClusterRoleHostInfoDTO2.setPort("8060");
        clusterJoinDTO2.setRoleClusterHosts(Collections.singletonList(esClusterRoleHostInfoDTO2));
//        Assertions.assertTrue(templateSrvManager.CheckTemplateSrv(clusterJoinDTO2,
//                TemplateServiceEnum.TEMPLATE_COLD.getCode().toString(), "wpk").failed());
    }
}
