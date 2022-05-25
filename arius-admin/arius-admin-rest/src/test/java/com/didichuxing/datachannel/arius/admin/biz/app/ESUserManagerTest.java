package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ConsoleESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserConfigDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUserConfig;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.constant.operaterecord.OperationEnum;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ESUserDAO;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class ESUserManagerTest extends AriusAdminApplicationTest {
	
	@Autowired
	private ESUserManager esUserManagerTest;
	@Autowired
	private ESUserDAO esUserDAO;
	
	@Test
	public void registerESUserTest() {
		// Setup
		final ESUserDTO appDTO = new ESUserDTO();
		appDTO.setIsRoot(1);
		appDTO.setVerifyCode("azAWiJhxkho33ac");
		appDTO.setMemo("管理员APP");
		appDTO.setIsActive(1);
		appDTO.setQueryThreshold(100);
		appDTO.setCluster("logi-elasticsearch-7.6.0");
		appDTO.setSearchType(1);
		appDTO.setDataCenter("cn");
		appDTO.setProjectId(1595);
		appDTO.setResponsible("admin");
		final Result<Integer> expectedResult = Result.buildSucc(1);
		
		
		// Run the test
		//final Result<Integer> result = esUserManagerTest.registerESUser(appDTO, 1595, "operator");
		ESUserPO esUserPO = new ESUserPO();
		esUserPO.setDefaultDisplay(true);
		
		esUserPO.setIsRoot(1);
		esUserPO.setVerifyCode("azAWiJhxkho33ac");
		esUserPO.setMemo("管理员APP");
		esUserPO.setIsActive(1);
		esUserPO.setQueryThreshold(100);
		esUserPO.setCluster("logi-elasticsearch-7.6.0");
		esUserPO.setSearchType(1);
		esUserPO.setDataCenter("cn");
		esUserPO.setProjectId(1595);
		esUserPO.setResponsible("admin");
		esUserDAO.insert(esUserPO);
		
	
	}
	
	@Test
	public void testListESUsersByAllProject() {
		// Setup
		final Result<List<ESUser>> expectedResult = Result.buildFail(Arrays.asList(
				new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
						"cluster", 0, "dataCenter", 0, false, "ip", "indexExp")));
		
		// Run the test
		final Result<List<ESUser>> result = esUserManagerTest.listESUsersByAllProject();
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testListESUsersByProjectId() {
		// Setup
		final Result<List<ESUser>> expectedResult = Result.buildFail(Arrays.asList(
				new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible", "memo", 0, 0,
						"cluster", 0, "dataCenter", 0, false, "ip", "indexExp")));
		
		// Run the test
		final Result<List<ESUser>> result = esUserManagerTest.listESUsersByProjectId(0, "operator");
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testValidateESUser() {
		// Setup
		final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0,
				"dataCenter", 0);
		
		// Run the test
		final Result<Void> result = esUserManagerTest.validateESUser(esUserDTO, OperationEnum.ADD);
		
		// Verify the results
	}
	
	@Test
	public void testGetESUsersMap() {
		// Setup
		final Result<Map<Integer, List<ESUser>>> expectedResult = Result.buildFail(new HashMap<>());
		
		// Run the test
		final Result<Map<Integer, List<ESUser>>> result = esUserManagerTest.getESUsersMap();
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testGetProjectName() {
		// Setup
		final Result<String> expectedResult = Result.buildFail("value");
		
		// Run the test
		final Result<String> result = esUserManagerTest.getProjectName(0);
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testUpdateESUserConfig() {
		// Setup
		final ESUserConfigDTO configDTO = new ESUserConfigDTO(0, 0, 0, 0, 0);
		
		// Run the test
		final Result<Void> result = esUserManagerTest.updateESUserConfig(configDTO, "operator");
		
		// Verify the results
	}
	
	@Test
	public void testEditESUser() {
		// Setup
		final ESUserDTO esUserDTO = new ESUserDTO(0, 0, "verifyCode", "responsible", "memo", 0, 0, "cluster", 0,
				"dataCenter", 0);
		
		// Run the test
		final Result<Void> result = esUserManagerTest.editESUser(esUserDTO, "operator");
		
		// Verify the results
	}
	
	@Test
	public void testDeleteESUserByProject() {
		// Setup
		// Run the test
		final Result<Void> result = esUserManagerTest.deleteESUserByProject(0, 0, "operator");
		
		// Verify the results
	}
	
	@Test
	public void testDeleteAllESUserByProject() {
		// Setup
		// Run the test
		final Result<Void> result = esUserManagerTest.deleteAllESUserByProject(0, "operator");
		
		// Verify the results
	}
	
	@Test
	public void testGetESUserConfig() {
		// Setup
		final ESUserConfig expectedResult = new ESUserConfig(0, 0, 0, 0, 0);
		
		// Run the test
		final ESUserConfig result = esUserManagerTest.getESUserConfig(0);
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testIsESUserExists1() {
		// Setup
		// Run the test
		final boolean result = esUserManagerTest.isESUserExists(0);
		
		// Verify the results
		assertThat(result).isTrue();
	}
	
	@Test
	public void testIsESUserExists2() {
		// Setup
		final ESUser esUser = new ESUser(0, "name", 0, "verifyCode", "departmentId", "department", "responsible",
				"memo", 0, 0, "cluster", 0, "dataCenter", 0, false, "ip", "indexExp");
		
		// Run the test
		final boolean result = esUserManagerTest.isESUserExists(esUser);
		
		// Verify the results
		assertThat(result).isTrue();
	}
	
	@Test
	public void testIsSuperESUser() {
		// Setup
		// Run the test
		final boolean result = esUserManagerTest.isSuperESUser(0);
		
		// Verify the results
		assertThat(result).isTrue();
	}
	
	@Test
	public void testVerifyAppCode() {
		// Setup
		// Run the test
		final Result<Void> result = esUserManagerTest.verifyAppCode(0, "verifyCode");
		
		// Verify the results
	}
	
	@Test
	public void testUpdate() {
		// Setup
		final HttpServletRequest request = new MockHttpServletRequest();
		final ConsoleESUserDTO consoleESUserDTO = new ConsoleESUserDTO(0, "memo", "dataCenter");
		
		// Run the test
		final Result<Void> result = esUserManagerTest.update(request, consoleESUserDTO);
		
		// Verify the results
	}
	
	@Test
	public void testGet() {
		// Setup
		final Result<ConsoleESUserVO> expectedResult = Result.buildFail(
				new ConsoleESUserVO(0, "memo", 0, "dataCenter"));
		
		// Run the test
		final Result<ConsoleESUserVO> result = esUserManagerTest.get(0);
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
	
	@Test
	public void testList() {
		// Setup
		final Result<List<ConsoleESUserVO>> expectedResult = Result.buildFail(
				Arrays.asList(new ConsoleESUserVO(0, "memo", 0, "dataCenter")));
		
		// Run the test
		final Result<List<ConsoleESUserVO>> result = esUserManagerTest.list();
		
		// Verify the results
		assertThat(result).isEqualTo(expectedResult);
	}
}