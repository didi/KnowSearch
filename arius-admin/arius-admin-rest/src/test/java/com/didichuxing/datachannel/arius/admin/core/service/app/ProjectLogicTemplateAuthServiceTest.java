package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectTemplateAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
@Rollback
public class ProjectLogicTemplateAuthServiceTest extends AriusAdminApplicationTest {
    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @Autowired
    private ProjectTemplateAuthDAO projectTemplateAuthDAO;

    @MockBean
    private IndexTemplateService indexTemplateService;

    @MockBean
    private ProjectClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private AppService appService;

    @Test
    public void deleteExcessTemplateAuthsIfNeedTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = mockAddTemplateAuth();
        ProjectTemplateAuthPO byAppIdAndTemplateId = projectTemplateAuthDAO.getByProjectIdAndTemplateId(
                projectTemplateAuthDTO.getProjectId(), projectTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //删除冗余数据
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteRedundancyTemplateAuths(true));
        //检测冗余的数据是否被清除掉
        Assertions.assertNull(projectTemplateAuthDAO.getById(authId));
    }

    @Test
    public void ensureSetLogicTemplateAuthTest() {
        Integer appId = 1;
        Integer logicTemplateId = 1147;
        ProjectTemplateAuthEnum auth = ProjectTemplateAuthEnum.RW;
        String responsible = "admin";
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(null,logicTemplateId,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,null,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,auth,responsible,null).failed());
        //之前表中无权限
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,null,responsible,CustomDataSource.OPERATOR).success());
        //插入读写权限
        ProjectTemplateAuthDTO projectTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertNotNull(projectTemplateAuthDAO.getByProjectIdAndTemplateId(appId,logicTemplateId.toString()).getId());
        //对于权限进行更新操作
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,
                        ProjectTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).success());
        //确认权限是否真的被更新
        Assertions.assertEquals(ProjectTemplateAuthEnum.R.getCode(),
                projectTemplateAuthDAO.getByProjectIdAndTemplateId(appId,logicTemplateId.toString()).getType());
    }

    @Test
    public void getTemplateAuthsByAppIdTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertFalse(projectLogicTemplateAuthService.getTemplateAuthsByProjectId(projectTemplateAuthDTO.getProjectId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getProjectId().equals(projectTemplateAuthDTO.getProjectId())
                                             &&appTemplateAuth.getTemplateId().equals(projectTemplateAuthDTO.getTemplateId())
                                             &&appTemplateAuth.getType().equals(projectTemplateAuthDTO.getType())));
        //插入对应的数据
        ProjectTemplateAuthDTO projectTemplateAuthDTO1 = mockAddTemplateAuth();
        Assertions.assertTrue(projectLogicTemplateAuthService.getTemplateAuthsByProjectId(projectTemplateAuthDTO1.getProjectId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getProjectId().equals(projectTemplateAuthDTO1.getProjectId())
                        &&appTemplateAuth.getTemplateId().equals(projectTemplateAuthDTO1.getTemplateId())
                        &&appTemplateAuth.getType().equals(projectTemplateAuthDTO1.getType())));
    }

    @Test
    public void addTemplateAuthTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = mockAddTemplateAuth();
        //设置Owner权限，这是不允许的
        projectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(projectLogicTemplateAuthService.addTemplateAuth(null, null).failed());
        //设置责任人字段为空
        projectTemplateAuthDTO.setResponsible(null);
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置权限字段为空
        projectTemplateAuthDTO.setType(null);
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置不存在的逻辑模板id字段
        projectTemplateAuthDTO.setTemplateId(1111111111);
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置所属的appid字段为空
        projectTemplateAuthDTO.setProjectId(null);
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void updateTemplateAuthTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = mockAddTemplateAuth();
        //对于插入的数据的权限信息进行相应的修改
        projectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.R.getCode());
        ProjectTemplateAuthPO byAppIdAndTemplateId = projectTemplateAuthDAO.getByProjectIdAndTemplateId(
                projectTemplateAuthDTO.getProjectId(), projectTemplateAuthDTO.getTemplateId().toString());
        projectTemplateAuthDTO.setId(byAppIdAndTemplateId.getId());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.updateTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).success());
        //再次确认权限的修改已经成功
        ProjectTemplateAuthPO updateProjectTemplateAuthPO = projectTemplateAuthDAO.getById(byAppIdAndTemplateId.getId());
        Assertions.assertEquals(ProjectTemplateAuthEnum.R.getCode(), updateProjectTemplateAuthPO.getType());
        //设置Owner权限，这是不允许的
        projectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void deleteTemplateAuthTest() {
        //插入数据，对数据进行对应的删除
        ProjectTemplateAuthDTO projectTemplateAuthDTO = mockAddTemplateAuth();
        ProjectTemplateAuthPO byAppIdAndTemplateId = projectTemplateAuthDAO.getByProjectIdAndTemplateId(
                projectTemplateAuthDTO.getProjectId(), projectTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //对插入的数据进行删除
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteTemplateAuth(authId, CustomDataSource.OPERATOR).success());
        //确认是否真的被删除
        Assertions.assertNull(projectTemplateAuthDAO.getById(authId));
        //null异常情况的判断
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteTemplateAuth(null,CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void getAllAppTemplateAuthsTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        projectTemplateAuthDTO.setTemplateId(1741);
        //插入mock的数据
        ProjectTemplateAuthDTO projectTemplateAuthDTO1 = mockAddTemplateAuth();
        //插入数据之后可以获取map下对应集合中的数值
        Assertions.assertTrue(projectLogicTemplateAuthService
                              .getAllProjectTemplateAuths()
                              .get(projectTemplateAuthDTO.getProjectId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(
                                      projectTemplateAuthDTO1.getTemplateId())));
    }

    @Test
    public void getAuthEnumByAppIdAndLogicIdTest() {
        //未插入数据，显示无权限
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        projectTemplateAuthDTO.setTemplateId(1741);
        Assertions.assertEquals(projectLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(projectTemplateAuthDTO.getProjectId(),
                        projectTemplateAuthDTO.getTemplateId()),
                ProjectTemplateAuthEnum.NO_PERMISSION);
        //插入数据之后获取设置的权限值
        projectTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertEquals(projectLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(projectTemplateAuthDTO.getProjectId(),
                        projectTemplateAuthDTO.getTemplateId()),
                ProjectTemplateAuthEnum.valueOf(projectTemplateAuthDTO.getType()));
    }

    private ProjectTemplateAuthDTO mockAddTemplateAuth() {
        // 各种对象的Mock操作
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Integer templateId = 1147;
        projectTemplateAuthDTO.setProjectId(1);
        projectTemplateAuthDTO.setTemplateId(templateId);
        IndexTemplate indexTemplate = new IndexTemplate();
        indexTemplate.setProjectId(1);
        indexTemplate.setId(templateId);
        IndexTemplateLogicWithClusterAndMasterTemplate indexTemplateLogicWithClusterAndMasterTemplate = new IndexTemplateLogicWithClusterAndMasterTemplate();
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(1L);
        indexTemplateLogicWithClusterAndMasterTemplate.setLogicCluster(clusterLogic);
        App app = new App();
        app.setId(1);
        app.setIsRoot(0);
        Map<Integer, App> appMap = new HashMap<>();
        appMap.put(app.getId(),app);

        // 创建mock规则
        Mockito.when(indexTemplateService.getAllLogicTemplates()).thenReturn(Collections.singletonList(indexTemplate));
        Mockito.when(indexTemplateService.getLogicTemplateById(Mockito.anyInt())).thenReturn(indexTemplate);
        Mockito.when(indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(Mockito.anyInt())).thenReturn(indexTemplateLogicWithClusterAndMasterTemplate);
        Mockito.when(logicClusterAuthService.getLogicClusterAuthEnum(Mockito.anyInt(), Mockito.anyLong())).thenReturn(
                ProjectClusterLogicAuthEnum.OWN);
        Mockito.when(appService.getAppById(Mockito.anyInt())).thenReturn(app);
        Mockito.when(appService.isSuperApp(Mockito.anyInt())).thenReturn(false);
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(true);
        Mockito.when(appService.getAppsMap()).thenReturn(appMap);

        // 创建插入的mock数据
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR).success());

        return projectTemplateAuthDTO;
    }
}