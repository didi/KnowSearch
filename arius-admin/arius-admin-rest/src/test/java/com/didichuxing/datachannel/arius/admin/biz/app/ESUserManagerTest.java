package com.didichuxing.datachannel.arius.admin.biz.app;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.didichuxing.datachannel.arius.admin.biz.app.impl.ESUserManagerImpl;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ESUserDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ESUser;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ESUserPO;
import com.didichuxing.datachannel.arius.admin.common.bean.vo.app.ConsoleESUserVO;
import com.didichuxing.datachannel.arius.admin.common.tuple.Tuple;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.component.SpringTool;
import com.didichuxing.datachannel.arius.admin.core.service.app.ESUserService;
import com.didichuxing.datachannel.arius.admin.core.service.common.OperateRecordService;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SpringTool.class })
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ESUserManagerTest {
	
	@Mock
	private ProjectService projectService;
	
	@Mock
	private ESUserService        esUserService;
	@Mock
	private RoleTool             roleTool;
	@MockBean
	private OperateRecordService operateRecordService;
	@InjectMocks
	private ESUserManagerImpl    esUserManager;
	

	@Test
	void testListESUsersByProjectId() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
		final ProjectVO projectVO = CustomDataSource.projectVO();
		
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(null);
		
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		
		Assertions.assertEquals(Result.buildParamIllegal(String.format("项目:[%s]不存在成员:[%s]", 1, "aaaa")).getMessage(),
				esUserManager.listESUsersByProjectId(1, "aaaa").getMessage());
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(CustomDataSource.projectVO());
		
		Assertions.assertEquals(Result.buildParamIllegal(String.format("项目:[%s]不存在成员:[%s]", 1, "aaaa")).getMessage(),
				esUserManager.listESUsersByProjectId(1, "aaaa").getMessage());
		when(roleTool.isAdmin(anyString())).thenReturn(true);
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(null);
		Assertions.assertTrue(esUserManager.listESUsersByProjectId(1, "aaaa").success());
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(projectVO);
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser));
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		Assertions.assertTrue(esUserManager.listESUsersByProjectId(1, "admin").success());
		
	}
	
	@Test
	void testRegisterESUser() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		esUserDTO.setResponsible("aaa");
		Assertions.assertEquals(
				Result.buildParamIllegal(String.format("当前操作[%s] 不能创建es user", esUserDTO.getResponsible()))
						.getMessage(), esUserManager.registerESUser(esUserDTO, 1, "admin").getMessage());
		when(roleTool.isAdmin(anyString())).thenReturn(true);
		when(esUserService.validateESUser(any(), any())).thenReturn(Result.buildParamIllegal("应用信息为空"));
		when(esUserService.registerESUser(null, null)).thenReturn(
				Tuple.of(Result.buildParamIllegal("应用信息为空"), esUserPO));
		Assertions.assertEquals(Result.buildParamIllegal("应用信息为空").getMessage(),
				esUserManager.registerESUser(null, null, null).getMessage());
		when(esUserService.registerESUser(any(), anyString())).thenReturn(Tuple.of(Result.buildSucc(1), esUserPO));
		Assertions.assertTrue(esUserManager.registerESUser(esUserDTO, 1, "a").success());
	}
	
	@Test
	void testEditESUser() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ESUserDTO esUserDTO = ConvertUtil.obj2Obj(esUserPO, ESUserDTO.class);
		when(projectService.checkProjectExist(anyInt())).thenReturn(false);
		when(esUserService.validateESUser(any(), any())).thenReturn(Result.buildFail("应用不存在"));
		Assertions.assertEquals(Result.buildFail("应用不存在").getMessage(),
				esUserManager.editESUser(esUserDTO, "admin").getMessage());
		when(esUserService.validateESUser(any(), any())).thenReturn(Result.buildSucc());
		when(esUserService.editUser(any())).thenReturn(Tuple.of(Result.buildSucc(), esUserPO));
		Assertions.assertTrue(
				Assertions.assertDoesNotThrow(() -> esUserManager.editESUser(esUserDTO, "admin").success()));
		
	}
	
	@Test
	void testDeleteESUserByProject() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ProjectVO projectVO = CustomDataSource.projectVO();
		final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
		
		Assertions.assertEquals(Result.buildFail("当前操作者权限不足,需要管理员权限"), esUserManager.deleteESUserByProject(1, 1, null));
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser));
		when(roleTool.isAdmin("admin")).thenReturn(true);
		Assertions.assertEquals(Result.buildFail(String.format("当前项目[%s]下只存在一个es user,不能被删除", 1)).getMessage(),
				esUserManager.deleteESUserByProject(1, 1, "admin").getMessage());
		esUser.setDefaultDisplay(true);
		esUser.setId(1);
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser, esUser));
		Assertions.assertEquals(Result.buildParamIllegal(String.format("当前项目[%s]不存在es user:[%s]", 1,
						Lists.newArrayList(esUser, esUser).stream().map(ESUser::getId).collect(Collectors.toList())))
				.getMessage(), esUserManager.deleteESUserByProject(2, 1, "admin").getMessage());
		
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser, esUser));
		Assertions.assertEquals(
				Result.buildFail(String.format("项目[%s]中es user:[%s],属于项目默认的es user,请先进行解绑", 1, 1)).getMessage(),
				esUserManager.deleteESUserByProject(1, 1, "admin").getMessage());
		esUser.setDefaultDisplay(false);
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser, esUser));
		when(esUserService.deleteESUserById(1)).thenReturn(Tuple.of(Result.buildSucc(), esUserPO));
		Assertions.assertTrue(esUserManager.deleteESUserByProject(1, 1, "admin").success());
		
	}
	
	@Test
	void testDeleteAllESUserByProject() {
		when(roleTool.isAdmin("admin")).thenReturn(true);
		when(roleTool.isAdmin("")).thenReturn(false);
		when(esUserService.deleteByESUsers(anyInt())).thenReturn(
				Tuple.of(Result.buildSucc(), Lists.newArrayList(CustomDataSource.esUserPO())));
		Assertions.assertEquals(Result.buildFail("当前操作者权限不足,需要管理员权限").getMessage(),
				esUserManager.deleteAllESUserByProject(1, "").getMessage());
		
		Assertions.assertTrue(esUserManager.deleteAllESUserByProject(1, "admin").success());
		
	}
	

	
	@Test
	void testGet() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
		when(esUserService.getEsUserById(anyInt())).thenReturn(esUser);
		final Result<ConsoleESUserVO> consoleESUserVOResult = esUserManager.get(1);
		Assertions.assertEquals(consoleESUserVOResult.getData().getId(), esUser.getId());
		
	}
	
	@Test
	void testGetNoCodeESUser() {
		final ESUserPO esUserPO = CustomDataSource.esUserPO();
		final ESUser esUser = ConvertUtil.obj2Obj(esUserPO, ESUser.class);
		final ProjectVO projectVO = CustomDataSource.projectVO();
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		projectVO.setOwnerList(null);
		projectVO.setUserList(null);
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(projectVO);
		
		when(esUserService.listESUsers(anyList())).thenReturn(Lists.newArrayList(esUser));
		Assertions.assertEquals(Result.buildFail("权限不足").getMessage(),
				esUserManager.getNoCodeESUser(1, "a1231").getMessage());
		when(roleTool.isAdmin(anyString())).thenReturn(false);
		projectVO.setUserList(CustomDataSource.projectVO().getUserList());
		when(projectService.getProjectDetailByProjectId(anyInt())).thenReturn(projectVO);
		
		Assertions.assertTrue(esUserManager.getNoCodeESUser(1, "admin").success());
		
	}
	
}