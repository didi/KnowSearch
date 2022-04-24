package com.didichuxing.datachannel.arius.admin.v3.white;

import com.didichuxing.datachannel.arius.admin.BaseContextTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.cluster.ESZeusHostInfoDTO;
import com.didichuxing.datachannel.arius.admin.method.v3.white.ESPhyClusterWhiteControllerMethodTest;
import com.didichuxing.datachannel.arius.admin.source.PhyClusterInfoSource;
import org.junit.Assert;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static com.didichuxing.datachannel.arius.admin.client.constant.resource.ESClusterNodeRoleEnum.DATA_NODE;

/**
 * @author wuxuan
 * @Date 2022/3/28
 */
public class ESPhyClusterWhiteTests extends BaseContextTest {

    protected static PhyClusterInfoSource.PhyClusterInfo phyClusterInfo;

    /**
     * 用静态变量flag来判断方法是否对集群进行了删除操作,以便后续对集群进行删除操作
     */
    static int flag=0;

    /**
     * 在当前类的每个测试方法之前执行
     * 由于本类中所有方法都需要物理集群，因此需要接入一个物理集群
     */
    @BeforeEach
    public void preHandle() throws IOException {
        BaseContextTest.init();
        phyClusterInfo = PhyClusterInfoSource.phyClusterJoin();
    }

    /**
     * 在当前类中的每个测试方法之后执行
     * 在方法调用之后结束接入的物理集群
     */
    @AfterEach
    public void afterCompletion() throws IOException{
        if(flag == 0){
            PhyClusterInfoSource.phyClusterRemove(phyClusterInfo.getPhyClusterName(), phyClusterInfo.getPhyClusterId());
        }
    }

    @Test
    public void testDeleteClusterJoin() throws IOException{
        Result<Void> deleteResult= ESPhyClusterWhiteControllerMethodTest.deleteClusterJoin(phyClusterInfo.getPhyClusterId());
        Assert.assertTrue(deleteResult.success());
        flag=1;
   }

    @Test
    public void testUpdateHttpAddress() throws IOException{
        ESZeusHostInfoDTO dto =new ESZeusHostInfoDTO();
        dto.setClusterPhyName(phyClusterInfo.getPhyClusterName());
        dto.setRole(DATA_NODE.getDesc());
        dto.setHttpAddress("");
        Result<Boolean> result=ESPhyClusterWhiteControllerMethodTest.updateHttpAddress(dto);
        Assert.assertTrue(result.success());
        flag=0;
    }

    @Test
    public void testCheckClusterHealth() throws IOException{
        Result<Boolean> result=ESPhyClusterWhiteControllerMethodTest.checkClusterHealth(phyClusterInfo.getPhyClusterName());
        Assert.assertTrue(result.success());
        flag=0;
    }

    @Test
    public void testCheckClusterIsExit() throws IOException{
        Result<Boolean> result=ESPhyClusterWhiteControllerMethodTest.checkClusterIsExit(phyClusterInfo.getPhyClusterName());
        Assert.assertTrue(result.success());
        flag=0;
    }

    @Test
    public void testDeleteClusterExit() throws IOException{
        Result<Boolean> result=ESPhyClusterWhiteControllerMethodTest.deleteClusterExit(phyClusterInfo.getPhyClusterName());
        Assert.assertTrue(result.success());
        flag=1;
    }
}
