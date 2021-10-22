package com.didichuxing.datachannel.arius.admin.core.service.cluster.ecm;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ecm.ESRoleClusterHost;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.jupiter.api.Test;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.physic.ESRoleClusterHostService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.CLIENT_NODE;

/**
 * @author lyn
 * @date 2021-01-06
 */
public class ESRoleClusterHostServiceTest extends AriusAdminApplicationTests {
    @Autowired
    private ESRoleClusterHostService esRoleClusterHostService;
    @Test
    public void test01(){
        List<ESRoleClusterHost> datanode = esRoleClusterHostService.getByRoleAndClusterId(1059L, "datanode");
        System.out.println(datanode);
    }

    @Test
    public void test02(){
        ESRoleClusterHost esRoleClusterHost = new ESRoleClusterHost();
        esRoleClusterHost.setRoleClusterId(99L);
        esRoleClusterHost.setHostname("testlyn.docket");
        esRoleClusterHost.setIp("1.1.1.1");
        esRoleClusterHost.setCluster("testlyn");
        esRoleClusterHost.setPort("");
        esRoleClusterHost.setRack("");
        esRoleClusterHost.setRole(CLIENT_NODE.getCode());
        esRoleClusterHost.setStatus(ESClusterNodeStatusEnum.ONLINE.getCode());
        esRoleClusterHost.setNodeSet("");
        esRoleClusterHostService.save(esRoleClusterHost);

    }

    @Test
    public void test03(){
        Result result = esRoleClusterHostService.deleteById(9154L);
        if(result.failed()){
            System.out.println("failed");
        }

    }

}