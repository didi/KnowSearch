package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppLogicClusterAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.cluster.logic.ClusterLogicService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectLogicClusterAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.common.vo.user.UserBriefVO;
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
    private AppService appService;

    @MockBean
    private AriusUserInfoService ariusUserInfoService;

    final static String OPERATOR = "wpk";

    @Test
    public void ensureSetLogicClusterAuthTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertEquals("参数错误:未指定appId，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(null, null, null, null, null).getMessage());
        Assertions.assertEquals("参数错误:未指定逻辑集群ID，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(), null, null, null, null).getMessage());
        Long logicClusterId = 173L;
        Assertions.assertEquals("参数错误:未指定操作人，请检查后再提交！",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(), logicClusterId, null, null, null).getMessage());
        mockRuleSet();
        Assertions.assertTrue(projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ALL, appClusterLogicAuth.getResponsible(), OPERATOR).failed());
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(appClusterLogicAuth.getLogicClusterId());
        clusterLogic.setProjectId(appClusterLogicAuth.getProjectId() + 1);
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(clusterLogic);
        Assertions.assertTrue(projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ACCESS, appClusterLogicAuth.getResponsible(), OPERATOR).success());
        Assertions.assertFalse(projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(),
                logicClusterId, ProjectClusterLogicAuthEnum.ALL, appClusterLogicAuth.getResponsible(), OPERATOR).success());
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.ACCESS.getCode(),
                projectLogicClusterAuthDAO.getByProjectIdAndLogicClusterId(appClusterLogicAuth.getProjectId(), logicClusterId).getType());
        mockRuleSet();
        Assertions.assertEquals("不支持对集群owner的权限进行修改",
                projectClusterLogicAuthService.ensureSetLogicClusterAuth(appClusterLogicAuth.getProjectId(),
                        logicClusterId, ProjectClusterLogicAuthEnum.ACCESS, appClusterLogicAuth.getResponsible(), OPERATOR).getMessage());
    }

    @Test
    public void getAllLogicClusterAuthsTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertTrue(projectClusterLogicAuthService.getAllLogicClusterAuths(appClusterLogicAuth.getProjectId())
                .stream()
                .map(AppClusterLogicAuth::getLogicClusterId)
                .collect(Collectors.toList())
                .contains(appClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void canCreateLogicTemplateTest() {
        Assertions.assertFalse(projectClusterLogicAuthService.canCreateLogicTemplate(null, null));
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertTrue(projectClusterLogicAuthService.canCreateLogicTemplate(appClusterLogicAuth.getProjectId(), appClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void addLogicClusterAuthWithoutCheckTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
    }

    @Test
    public void buildClusterLogicAuthTest() {
        Assertions.assertNotNull(
                projectClusterLogicAuthService.buildClusterLogicAuth(1, 1234L, ProjectClusterLogicAuthEnum.OWN));
    }

    @Test
    public void getLogicClusterAuthsTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        List<AppClusterLogicAuth> allLogicClusterAuths = projectClusterLogicAuthService.getAllLogicClusterAuths(appClusterLogicAuth.getProjectId());
        Assertions.assertTrue(allLogicClusterAuths.stream().map(AppClusterLogicAuth::getResponsible).anyMatch(s -> s.equals(appClusterLogicAuth.getResponsible())));
    }

    @Test
    public void getLogicClusterAccessAuthsTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        List<AppClusterLogicAuth> allLogicClusterAuths = projectClusterLogicAuthService.getLogicClusterAccessAuths(appClusterLogicAuth.getProjectId());
        Assertions.assertTrue(allLogicClusterAuths.stream().map(AppClusterLogicAuth::getResponsible).anyMatch(s -> s.equals(appClusterLogicAuth.getResponsible())));
    }

    @Test
    public void getLogicClusterAuthEnumTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.NO_PERMISSIONS,
                projectClusterLogicAuthService.getLogicClusterAuthEnum(appClusterLogicAuth.getProjectId(), appClusterLogicAuth.getLogicClusterId()));
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        Assertions.assertEquals(ProjectClusterLogicAuthEnum.valueOf(appClusterLogicAuth.getType()),
                projectClusterLogicAuthService.getLogicClusterAuthEnum(appClusterLogicAuth.getProjectId(), appClusterLogicAuth.getLogicClusterId()));
    }

    @Test
    public void getLogicClusterAuthByIdTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        AppClusterLogicAuth logicClusterAuth = projectClusterLogicAuthService.getLogicClusterAuth(appClusterLogicAuth.getProjectId(), appClusterLogicAuth.getLogicClusterId());
        Assertions.assertEquals(logicClusterAuth.getProjectId(),
                projectClusterLogicAuthService.getLogicClusterAuthById(logicClusterAuth.getId()).getProjectId());
    }

    @Test
    public void addLogicClusterAuthTest() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        AppLogicClusterAuthDTO appLogicClusterAuthDTO = ConvertUtil.obj2Obj(appClusterLogicAuth, AppLogicClusterAuthDTO.class);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(appLogicClusterAuthDTO, OPERATOR).failed());
        mockRuleSet();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(appLogicClusterAuthDTO, OPERATOR).success());
        //Mockito.when(ariusUserInfoService.getByDomainAccount(Mockito.any())).thenReturn(null);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(appLogicClusterAuthDTO, OPERATOR).failed());
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(false);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(appLogicClusterAuthDTO, OPERATOR).failed());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(null);
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuth(appLogicClusterAuthDTO, OPERATOR).failed());
    }

    @Test
    public void deleteLogicClusterAuthByLogicClusterIdTest() {
        // mock插入数据
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Assertions.assertTrue(projectClusterLogicAuthService.addLogicClusterAuthWithoutCheck(ConvertUtil.obj2Obj(appClusterLogicAuth,
                AppLogicClusterAuthDTO.class), OPERATOR).success());
        AppClusterLogicAuth logicClusterAuth = projectClusterLogicAuthService.getLogicClusterAuth(appClusterLogicAuth.getProjectId(), appClusterLogicAuth.getLogicClusterId());
        // 删除mock的插入数据
        Assertions.assertTrue(
                projectClusterLogicAuthService.deleteLogicClusterAuthById(logicClusterAuth.getId(), OPERATOR).success());
    }

    private void mockRuleSet() {
        AppClusterLogicAuth appClusterLogicAuth = CustomDataSource.appClusterLogicAuthSource();
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(true);
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(appClusterLogicAuth.getLogicClusterId());
        clusterLogic.setProjectId(appClusterLogicAuth.getProjectId());
        Mockito.when(clusterLogicService.getClusterLogicById(Mockito.anyLong())).thenReturn(clusterLogic);
        //Mockito.when(ariusUserInfoService.getByDomainAccount(Mockito.any())).thenReturn(new UserBriefVO());
    }
}