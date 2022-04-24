package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.metrics.DashboardMetricsManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.metrics.MetricsDashboardListDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.vo.metrics.list.MetricListVO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class DashboardMetricsManagerTest extends AriusAdminApplicationTest {
	@Autowired
	private DashboardMetricsManager dashboardMetricsManager;
	@Test
	public  void getListNodeMetricsInfoTest(){
		MetricsDashboardListDTO param=new MetricsDashboardListDTO();
		param.setMetricsTypes(Arrays.asList("largeHead"));
		param.setAggType("avg");
		
		param.setOrderByDesc(true);
		final Result<List<MetricListVO>> listNodeMetricsInfo = dashboardMetricsManager.getListNodeMetricsInfo(
				param, 1);
		Assertions.assertNotNull(listNodeMetricsInfo);
	}
	
}