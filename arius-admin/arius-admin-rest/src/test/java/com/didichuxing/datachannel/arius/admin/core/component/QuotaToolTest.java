package com.didichuxing.datachannel.arius.admin.core.component;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.NodeSpecifyEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.quota.Resource;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class QuotaToolTest extends AriusAdminApplicationTests {

    @Autowired
    @InjectMocks
    private QuotaTool quotaTool;

    @MockBean
    private AriusConfigInfoService service;

    /**
     * 16C 64G 3*1024G
     * 当前只支持docker类型的resource
     */
    private Resource resource = NodeSpecifyEnum.DOCKER.getResource();

    private static final double DELTA = 1E-7;

    private static double value = 1.06;

    @BeforeEach
    public void initMock() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(service.doubleSetting(Mockito.anyString(), Mockito.anyString(), Mockito.eq(value))).thenReturn(value);
        Mockito.when(service.doubleSetting(Mockito.anyString(), Mockito.anyString(), Mockito.eq(1000D))).thenReturn(1000D);
        Mockito.when(service.doubleSetting(Mockito.anyString(), Mockito.anyString(), Mockito.eq(2300D))).thenReturn(2300D);
    }

    @Test
    public void quotaCountByDiskTest() {
        double result = quotaTool.getQuotaCountByDisk(-1, -1, -1);
        Assertions.assertEquals(1.0D / 3, result, DELTA);
        double result1 = quotaTool.getQuotaCountByDisk(-1, 0, -1);
        Assertions.assertEquals(0, result1, DELTA);

        double disk = Math.random() * resource.getDisk();
        double quotaMin = Math.random();
        double result2 = quotaTool.getQuotaCountByDisk(-1, disk, quotaMin);
        Assertions.assertEquals(Math.max(disk / resource.getDisk(), quotaMin), result2, DELTA);
    }

    @Test
    public void quotaCountByCpuTest() {
        double cpu = -1;
        double result = quotaTool.getQuotaCountByCpu(-1, cpu, -1);
        Assertions.assertEquals(cpu / resource.getCpu(), result, DELTA);
        double cpu1 = Math.random() * resource.getCpu();
        double quotaMin1 = Math.random();
        double result1 = quotaTool.getQuotaCountByCpu(-1, cpu1, quotaMin1);
        Assertions.assertEquals(Math.max(cpu1 / resource.getCpu(), quotaMin1), result1, DELTA);
    }

    @Test
    public void templateQuotaTest() {
        double cpu = Math.random();
        double disk = Math.random();
        double result = quotaTool.getTemplateQuotaCountByCpuAndDisk(-1, cpu, disk, 0);
        Assertions.assertEquals(Math.max(cpu / resource.getCpu(), disk / resource.getDisk()), result, DELTA);
    }

    @Test
    public void resourceQuotaTest() {
        double cpu = Math.random();
        double disk = Math.random();
        double result = quotaTool.getResourceQuotaCountByCpuAndDisk(-1, cpu, disk, 0);
        Assertions.assertEquals(Math.min(cpu / resource.getCpu(), disk / resource.getDisk()), result, DELTA);
    }

    @Test
    public void getResourceTest() {
        double quota = Math.random();
        Resource resource1 = quotaTool.getResourceOfQuota(-1, quota);
        Assertions.assertEquals(resource1.getCpu(), resource.getCpu() * quota, DELTA);
        Assertions.assertEquals(resource1.getMem(), resource.getMem() * quota, DELTA);
        Assertions.assertEquals(resource1.getDisk(), resource.getDisk() * quota, DELTA);
    }

    @Test
    public void getTpsTest() {
        double d1 = quotaTool.getTpsPerCpu(true);
        Assertions.assertEquals(1000, d1, DELTA);
        double d2 = quotaTool.getTpsPerCpu(false);
        Assertions.assertEquals(2300, d2, DELTA);
    }

    @Test
    public void computeCostTest() {
        double quota = Math.random();
        double cost = quotaTool.computeCostByQuota(-1, quota);
        Assertions.assertEquals(resource.getDisk() * value * quota, cost, DELTA);
    }

}
