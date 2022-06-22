package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.ProjectTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.app.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.ProjectTemplateAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
public class AppLogicTemplateAuthServiceTest extends AriusAdminApplicationTest {
    @Autowired
    private ProjectLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private ProjectTemplateAuthDAO appTemplateAuthDAO;

    @MockBean
    private IndexTemplateService indexTemplateService;

    @MockBean
    private ProjectClusterLogicAuthService logicClusterAuthService;



    @Test
    public void deleteExcessTemplateAuthsIfNeedTest() {
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = mockAddTemplateAuth();
        ProjectTemplateAuthPO byAppIdAndTemplateId =
                appTemplateAuthDAO.getByProjectIdAndTemplateId(ProjectTemplateAuthDTO.getProjectId(),
                        ProjectTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //删除冗余数据
        Assertions.assertTrue(appLogicTemplateAuthService.deleteRedundancyTemplateAuths(true));
        //检测冗余的数据是否被清除掉
        Assertions.assertNull(appTemplateAuthDAO.getById(authId));
    }

    @Test
    public void ensureSetLogicTemplateAuthTest() {
        Integer appId = 1;
        Integer logicTemplateId = 1147;
        ProjectTemplateAuthEnum auth = ProjectTemplateAuthEnum.RW;
        String responsible = "admin";
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(null,logicTemplateId,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,null,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,auth,responsible,null).failed());
        //之前表中无权限
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,null,responsible,CustomDataSource.OPERATOR).success());
        //插入读写权限
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertNotNull(appTemplateAuthDAO.getByProjectIdAndTemplateId(appId,logicTemplateId.toString()).getId());
        //对于权限进行更新操作
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,ProjectTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).success());
        //确认权限是否真的被更新
        Assertions.assertEquals(ProjectTemplateAuthEnum.R.getCode(),appTemplateAuthDAO.getByProjectIdAndTemplateId(appId,
                logicTemplateId.toString()).getType());
    }

    @Test
    public void getTemplateAuthsByAppIdTest() {
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertFalse(appLogicTemplateAuthService.getTemplateAuthsByProjectId(ProjectTemplateAuthDTO.getProjectId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getProjectId().equals(ProjectTemplateAuthDTO.getProjectId())
                                             &&appTemplateAuth.getTemplateId().equals(ProjectTemplateAuthDTO.getTemplateId())
                                             &&appTemplateAuth.getType().equals(ProjectTemplateAuthDTO.getType())));
        //插入对应的数据
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO1 = mockAddTemplateAuth();
        Assertions.assertTrue(appLogicTemplateAuthService.getTemplateAuthsByProjectId(ProjectTemplateAuthDTO1.getProjectId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getProjectId().equals(ProjectTemplateAuthDTO1.getProjectId())
                        &&appTemplateAuth.getTemplateId().equals(ProjectTemplateAuthDTO1.getTemplateId())
                        &&appTemplateAuth.getType().equals(ProjectTemplateAuthDTO1.getType())));
    }

    @Test
    public void addTemplateAuthTest() {
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = mockAddTemplateAuth();
        //设置Owner权限，这是不允许的
        ProjectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(null, null).failed());
        //设置责任人字段为空
        ProjectTemplateAuthDTO.setResponsible(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置权限字段为空
        ProjectTemplateAuthDTO.setType(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置不存在的逻辑模板id字段
        ProjectTemplateAuthDTO.setTemplateId(1111111111);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置所属的appid字段为空
        ProjectTemplateAuthDTO.setProjectId(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void updateTemplateAuthTest() {
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = mockAddTemplateAuth();
        //对于插入的数据的权限信息进行相应的修改
        ProjectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.R.getCode());
        ProjectTemplateAuthPO byAppIdAndTemplateId =
                appTemplateAuthDAO.getByProjectIdAndTemplateId(ProjectTemplateAuthDTO.getProjectId(),
                        ProjectTemplateAuthDTO.getTemplateId().toString());
        ProjectTemplateAuthDTO.setId(byAppIdAndTemplateId.getId());
        Assertions.assertTrue(appLogicTemplateAuthService.updateTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).success());
        //再次确认权限的修改已经成功
        ProjectTemplateAuthPO updateProjectTemplateAuthPO = appTemplateAuthDAO.getById(byAppIdAndTemplateId.getId());
        Assertions.assertEquals(ProjectTemplateAuthEnum.R.getCode(), updateProjectTemplateAuthPO.getType());
        //设置Owner权限，这是不允许的
        ProjectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void deleteTemplateAuthTest() {
        //插入数据，对数据进行对应的删除
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = mockAddTemplateAuth();
        ProjectTemplateAuthPO byAppIdAndTemplateId =
                appTemplateAuthDAO.getByProjectIdAndTemplateId(ProjectTemplateAuthDTO.getProjectId(),
                        ProjectTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //对插入的数据进行删除
        Assertions.assertTrue(appLogicTemplateAuthService.deleteTemplateAuth(authId, CustomDataSource.OPERATOR).success());
        //确认是否真的被删除
        Assertions.assertNull(appTemplateAuthDAO.getById(authId));
        //null异常情况的判断
        Assertions.assertTrue(appLogicTemplateAuthService.deleteTemplateAuth(null,CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void getAllAppTemplateAuthsTest() {
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        ProjectTemplateAuthDTO.setTemplateId(1741);
        //插入mock的数据
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO1 = mockAddTemplateAuth();
        //插入数据之后可以获取map下对应集合中的数值
        Assertions.assertTrue(appLogicTemplateAuthService
                              .getAllProjectTemplateAuths()
                              .get(ProjectTemplateAuthDTO.getProjectId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(ProjectTemplateAuthDTO1.getTemplateId())));
    }

    @Test
    public void getAuthEnumByAppIdAndLogicIdTest() {
        //未插入数据，显示无权限
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        ProjectTemplateAuthDTO.setTemplateId(1741);
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(ProjectTemplateAuthDTO.getProjectId(),ProjectTemplateAuthDTO.getTemplateId()),
                ProjectTemplateAuthEnum.NO_PERMISSION);
        //插入数据之后获取设置的权限值
        ProjectTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(ProjectTemplateAuthDTO.getProjectId(),ProjectTemplateAuthDTO.getTemplateId()),
                ProjectTemplateAuthEnum.valueOf(ProjectTemplateAuthDTO.getType()));
    }

    private ProjectTemplateAuthDTO mockAddTemplateAuth() {
        // 各种对象的Mock操作
        ProjectTemplateAuthDTO ProjectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Integer templateId = 1147;
        ProjectTemplateAuthDTO.setProjectId(1);
        ProjectTemplateAuthDTO.setTemplateId(templateId);
        IndexTemplate indexTemplate = new IndexTemplate();
        indexTemplate.setProjectId(1);
        indexTemplate.setId(templateId);
        IndexTemplateLogicWithClusterAndMasterTemplate indexTemplateLogicWithClusterAndMasterTemplate = new IndexTemplateLogicWithClusterAndMasterTemplate();
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(1L);
        indexTemplateLogicWithClusterAndMasterTemplate.setLogicCluster(clusterLogic);
   

        // 创建mock规则
        Mockito.when(indexTemplateService.listAllLogicTemplates()).thenReturn(Collections.singletonList(indexTemplate));
        Mockito.when(indexTemplateService.getLogicTemplateById(Mockito.anyInt())).thenReturn(indexTemplate);
        Mockito.when(indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(Mockito.anyInt())).thenReturn(indexTemplateLogicWithClusterAndMasterTemplate);
        Mockito.when(logicClusterAuthService.getLogicClusterAuthEnum(Mockito.anyInt(), Mockito.anyLong())).thenReturn(
                ProjectClusterLogicAuthEnum.OWN);

        // 创建插入的mock数据
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(ProjectTemplateAuthDTO, CustomDataSource.OPERATOR).success());

        return ProjectTemplateAuthDTO;
    }
}