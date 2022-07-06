package com.didichuxing.datachannel.arius.admin.biz.cluster;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.template.new_srv.TemplateSrvManager;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ClusterJoinDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.cluster.ESClusterRoleHostDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.resource.ESClusterNodeRoleEnum;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class TemplateSrvManagerTest extends AriusAdminApplicationTest {
    @Autowired
    private TemplateSrvManager templateSrvManager;



    /**
     * 测试接入集群开启冷热分离服务
     */
    // @Test
    public void addTemplateSrvTest2() {
        //这里模拟使用一下自己接入的集群和已有的接入的但是没有冷节点的集群
        //具有冷节点的集群
        ClusterJoinDTO clusterJoinDTO1 = new ClusterJoinDTO();
        ESClusterRoleHostDTO esClusterRoleHostDTO1 = new ESClusterRoleHostDTO();
        esClusterRoleHostDTO1.setRole(ESClusterNodeRoleEnum.MASTER_NODE.getCode());
        esClusterRoleHostDTO1.setIp("10.96.65.63");
        esClusterRoleHostDTO1.setPort("8060");
        clusterJoinDTO1.setRoleClusterHosts(Collections.singletonList(esClusterRoleHostDTO1));

        //不具有冷节点的集群
        ClusterJoinDTO clusterJoinDTO2 = new ClusterJoinDTO();
        ESClusterRoleHostDTO esClusterRoleHostDTO2 = new ESClusterRoleHostDTO();
        esClusterRoleHostDTO2.setRole(ESClusterNodeRoleEnum.CLIENT_NODE.getCode());
        esClusterRoleHostDTO2.setIp("10.168.56.135");
        esClusterRoleHostDTO2.setPort("8060");
        clusterJoinDTO2.setRoleClusterHosts(Collections.singletonList(esClusterRoleHostDTO2));
//        Assertions.assertTrue(templateSrvManager.CheckTemplateSrv(clusterJoinDTO2,
//                TemplateServiceEnum.TEMPLATE_COLD.getCode().toString(), "wpk").failed());
    }
}