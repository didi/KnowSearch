package com.didichuxing.datachannel.arius.admin.metadata.job.cluster.monitor;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by linyunan on 2021-06-10
 */
public class ClusterMonitorJobHandlerTest extends AriusAdminApplicationTests {

	@Autowired
	private ClusterMonitorJobHandler clusterMonitorJobHandler;

	@Test
	public void handleJobTaskTest(){
		Object o = clusterMonitorJobHandler.handleJobTask("");
		Assert.assertNotNull(o);
	}
}