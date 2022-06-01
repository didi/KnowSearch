package com.didichuxing.datachannel.arius.admin.core.service.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.Tuple;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
@Rollback
class ESUserServiceTest extends AriusAdminApplicationTest {
	

	
	@Autowired
	private ESUserService esUserService;
	
	@Test
	public void testDeleteByESUsers() {
		
		Tuple<Result<Void>, List<ESUserPO>> resultListTuple = esUserService.deleteByESUsers(151);
		Assertions.assertEquals(resultListTuple.getV1(),Result.<Void>buildSucc());
		
	}
	
	@Test
	void testRegisterESUser_testGetEsUserById() {
		// Setup
		final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "admin", "memo", 0, 0, "cluster", 0, "code", 1);
		
		// Run the test
		final Tuple<Result<Integer>, ESUserPO> result = esUserService.registerESUser(esUserDTO, "operator");
		
		// Verify the results
		Assertions.assertNotNull(result.getV1().getData());
		final ESUser esUser = esUserService.getEsUserById(result.getV1().getData());
		
		Assertions.assertNotNull(esUser);
		
	}
	
	@Test
	void testListESUsers() {
		
		final List<ESUser> result = esUserService.listESUsers(Collections.singletonList(1595));
		
		// Verify the results
		Assertions.assertTrue(CollectionUtils.isNotEmpty(result));
		;
	}
	
	@Test
	void testEditUser() {
		// Setup
		final ESUser esUserByProject = esUserService.getDefaultESUserByProject(1);
		Assertions.assertNotNull(esUserByProject);
		esUserByProject.setName("adfafsafsa");
		final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserByProject, ESUserDTO.class);
		
		// Run the test
		final Tuple<Result<Void>, ESUserPO> result = esUserService.editUser(esUserDTO);
		Assertions.assertEquals(result.getV1(), Result.<Void>buildSucc());
	}
	
	@Test
	public void testDeleteESUserById() {
		final Tuple<Result<Void>, ESUserPO> resultESUserPOTuple = esUserService.deleteESUserById(Mockito.anyInt());
		Assertions.assertEquals(resultESUserPOTuple.getV1(), Result.buildFail());
	}
	
	@Test
	void testCountByProjectId() {
		// Setup
		
		// Run the test
		final int result = esUserService.countByProjectId(0);
		
		// Verify the results
		assertThat(result).isZero();
	}
	
	@Test
	void testGetEsUserById() {
		
		// Run the test
		final ESUser result = esUserService.getEsUserById(99);
		
		// Verify the results
		Assertions.assertNotNull(result);
	}
	
	@Test
	void testVerifyAppCode() {
		// Setup
		// Configure ESUserDAO.getByESUser(...).
		
		final Result<Void> objectResult = Result.<Void>buildSucc();
		// Run the test
		final Result<Void> result = esUserService.verifyAppCode(99, "verifyCode");
		Assertions.assertEquals(result, objectResult);
		
		// Verify the results
	}
	
	@Test
	void testValidateESUser() {
		
		final ESUser project = esUserService.getDefaultESUserByProject(1);
		project.setProjectId(1595);
		final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(project, ESUserDTO.class);
		
		// Run the test
		final Result<Void> result = esUserService.validateESUser(esUserDTO, OperationEnum.ADD);
		Assertions.assertEquals(result, Result.buildFail());
		
		// Verify the results
	}
	
	@Test
	void testGetProjectWithoutCodeApps() {
		
		// Run the test
		final List<ESUser> result = esUserService.getProjectWithoutCodeApps(1);
		
		Assertions.assertTrue(CollectionUtils.isNotEmpty(result));
	}
	
}