package com.didichuxing.datachannel.arius.admin.biz.metrics;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsClusterPhyDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.metrics.ClusterPhyTypeMetricsEnum;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class MetricsTest extends AriusAdminApplicationTest {
	@Autowired
	private ClusterPhyMetricsManager clusterPhyMetricsManager;
	
	@Test
	public void getClusterMetricsByMetricsTypeTest(){
		MetricsClusterPhyDTO param=new MetricsClusterPhyDTO();
		param.setClusterPhyName("yyf-test-jr-1228");
		param.setStartTime(1650003983761L);
		param.setEndTime(1650007583761L);
		param.setAggType("max");
		param.setMetricsTypes(Arrays.asList("basic"));
		param.setTopNu(5);
		
		final Result<Object> admin = clusterPhyMetricsManager.getClusterMetricsByMetricsType(param, 1,
				"admin",
				ClusterPhyTypeMetricsEnum.CLUSTER);
		Assertions.assertNotNull(admin.getData());
	}
	
}