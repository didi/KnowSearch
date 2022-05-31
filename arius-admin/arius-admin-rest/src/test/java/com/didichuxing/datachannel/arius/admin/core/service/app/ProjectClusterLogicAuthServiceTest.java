package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.AuthConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.ProjectClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectLogicClusterAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.service.ProjectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(timeout = 1000)
@Rollback
public class ProjectClusterLogicAuthServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private ProjectClusterLogicAuthService projectClusterLogicAuthService;

    @Autowired
    private ProjectLogicClusterAuthDAO projectLogicClusterAuthDAO;

    @MockBean
    private ClusterLogicService clusterLogicService;

    @MockBean
    private ProjectService  projectService;


    final static String OPERATOR = "wpk";

    @Test
    public void ensureSetLogicClusterAuthTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertEquals("参数错误:未指定projectId，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(null, null, null, null, null).getMessage());
        Assertions.assertEquals("参数错误:未指定逻辑集群ID，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(), null, null, null, null).getMessage());
        Long logicClusterId = 173L;
        Assertions.assertEquals("参数错误:未指定操作人，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(), logicClusterId, null, null, null).getMessage());
        mockRuleSet();
        Assertions.assertTrue(projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ALL, projectClusterLogicAuth.getResponsible(), OPERATOR).failed());
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(projectClusterLogicAuth.getLogicClusterId());
        clusterLogic.setProjectId(projectClusterLogicAuth.getProjectId() + 1);
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(clusterLogic);
        Assertions.assertTrue(projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ACCESS, projectClusterLogicAuth.getResponsible(), OPERATOR).success());
        Assertions.assertFalse(projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ALL, projectClusterLogicAuth.getResponsible(), OPERATOR).success());
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.ACCESS.getCode(),
                projectLogicClusterAuthDAO.getByProjectIdAndLogicClusterId(projectClusterLogicAuth.getProjectId(), logicClusterId).getType());
        mockRuleSet();
        Assertions.assertEquals("不支持对集群owner的权限进行修改",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(projectClusterLogicAuth.getProjectId(),
                        logicClusterId, ProjectClusterLogicAuthEnum.ACCESS, projectClusterLogicAuth.getResponsible(), OPERATOR).getMessage());
    }

    @Test
    public void getAllLogicClusterAuthsTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertTrue(projectClusterLogicAuthService.getAllLogicClusterAuths(projectClusterLogicAuth.getProjectId())
                .stream()
                .map(ProjectClusterLogicAuth::getLogicClusterId)
                .collect(Collectors.toList())
                .contains(projectClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void canCreateLogicTemplateTest() {
        Assertions.assertFalse(projectClusterLogicAuthService.canCreateLogicTemplate(null, null));
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertTrue(projectClusterLogicAuthService.canCreateLogicTemplate(projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void addLogicClusterAuthWithoutCheckTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
    }

    @Test
    public void buildClusterLogicAuthTest() {
        Assertions.assertNotNull(
                projectClusterLogicAuthService.buildClusterLogicAuth(1, 1234L, ProjectClusterLogicAuthEnum.OWN));
    }

    @Test
    public void getLogicClusterAuthsTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        List<ProjectClusterLogicAuth> allLogicClusterAuths = projectClusterLogicAuthService.getAllLogicClusterAuths(
		        projectClusterLogicAuth.getProjectId());
        Assertions.assertTrue(allLogicClusterAuths.stream().map(ProjectClusterLogicAuth::getResponsible).anyMatch(s -> s.equals(
		        projectClusterLogicAuth.getResponsible())));
    }

    @Test
    public void getLogicClusterAccessAuthsTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        List<ProjectClusterLogicAuth> allLogicClusterAuths = projectClusterLogicAuthService.getLogicClusterAccessAuths(
		        projectClusterLogicAuth.getProjectId());
        Assertions.assertTrue(allLogicClusterAuths.stream().map(ProjectClusterLogicAuth::getResponsible).anyMatch(s -> s.equals(
		        projectClusterLogicAuth.getResponsible())));
    }

    @Test
    public void getLogicClusterAuthEnumTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.NO_PERMISSIONS,
                projectClusterLogicAuthService.getLogicClusterAuthEnum(projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId()));
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.valueOf(projectClusterLogicAuth.getType()),
                projectClusterLogicAuthService.getLogicClusterAuthEnum(projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void getLogicClusterAuthByIdTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        ProjectClusterLogicAuth logicClusterAuth = projectClusterLogicAuthService.getLogicClusterAuth(
		        projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId());
        Assertions.assertEquals(logicClusterAuth.getProjectId(),
                projectClusterLogicAuthService.getLogicClusterAuthById(logicClusterAuth.getId()).getProjectId());
    }

    @Test
    public void addLogicClusterAuthTest() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        ProjectLogicClusterAuthDTO projectLogicClusterAuthDTO = ConvertUtil.obj2Obj(projectClusterLogicAuth, ProjectLogicClusterAuthDTO.class);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(projectLogicClusterAuthDTO, OPERATOR).failed());
        mockRuleSet();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(projectLogicClusterAuthDTO, OPERATOR).success());
        //Mockito.when(ariusUserInfoService.getByDomainAccount(Mockito.any())).thenReturn(null);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(projectLogicClusterAuthDTO, OPERATOR).failed());
        Mockito.when(projectService.checkProjectExist(Mockito.anyInt())).thenReturn(false);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(projectLogicClusterAuthDTO, OPERATOR).failed());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(null);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(projectLogicClusterAuthDTO, OPERATOR).failed());
    }

    @Test
    public void deleteLogicClusterAuthByLogicClusterIdTest() {
        // mock插入数据
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(
		        projectClusterLogicAuth,
                ProjectLogicClusterAuthDTO.class), OPERATOR).success());
        ProjectClusterLogicAuth logicClusterAuth = projectClusterLogicAuthService.getLogicClusterAuth(
		        projectClusterLogicAuth.getProjectId(), projectClusterLogicAuth.getLogicClusterId());
        // 删除mock的插入数据
        Assertions.assertTrue(
                projectClusterLogicAuthService.deleteLogicClusterAuthById(logicClusterAuth.getId(), OPERATOR).success());
    }

    private void mockRuleSet() {
        ProjectClusterLogicAuth projectClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Mockito.when(projectService.checkProjectExist(Mockito.anyInt())).thenReturn(true);
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(projectClusterLogicAuth.getLogicClusterId());
        clusterLogic.setProjectId(projectClusterLogicAuth.getProjectId());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(clusterLogic);
        //Mockito.when(ariusUserInfoService.getByDomainAccount(Mockito.any())).thenReturn(new UserBriefVO());
    }
}