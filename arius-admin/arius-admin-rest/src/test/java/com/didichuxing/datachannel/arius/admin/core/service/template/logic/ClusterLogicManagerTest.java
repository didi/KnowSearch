package com.didichuxing.datachannel.arius.admin.core.service.template.logic;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.biz.cluster.ClusterLogicManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.template.ConsoleTemplateClearDTO;
import com.didichuxing.datachannel.arius.admin.common.exception.ESOperateException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class ClusterLogicManagerTest extends AriusAdminApplicationTest {
	@Autowired
	private ClusterLogicManager clusterLogicManager;
	@Test
	public void  clearIndicesTest() throws ESOperateException {
		ConsoleTemplateClearDTO clearDTO=new ConsoleTemplateClearDTO();
		clearDTO.setDelIndices(Collections.singletonList("dcdr6.6.1-test03_2022-02-26"));
		clearDTO.setLogicId(19655);
		final Result<Void> admin = clusterLogicManager.clearIndices(clearDTO, "admin");
		Assertions.assertEquals(admin.getCode(),0);
	}
	
	
	
	
	
	
}