package com.didichuxing.datachannel.arius.admin.core.service.project;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.ProjectTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.project.ProjectTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.project.ProjectTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.RoleTool;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.IndexTemplateService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.project.ProjectTemplateAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.didiglobal.logi.security.common.vo.project.ProjectVO;
import com.didiglobal.logi.security.service.ProjectService;
import com.google.common.collect.Lists;
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
public class ProjectLogicTemplateAuthServiceTest extends AriusAdminApplicationTest {
    @Autowired
    private ProjectLogicTemplateAuthService projectLogicTemplateAuthService;

    @MockBean
    private ProjectTemplateAuthDAO templateAuthDAO;

    @MockBean
    private IndexTemplateService indexTemplateService;

    @MockBean
    private ProjectClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private ProjectService  projectService;
    @MockBean
    private RoleTool roleTool;

    @Test
    public void deleteExcessTemplateAuthsIfNeedTest() {
        Mockito.when( templateAuthDAO.getByProjectIdAndTemplateId(
              Mockito.anyInt(),Mockito.anyString())).thenReturn(CustomDataSource.projectTemplateAuthPO());
        //删除冗余数据
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteRedundancyTemplateAuths(true));
    }

    @Test
    public void ensureSetLogicTemplateAuthTest() {
        Integer projectId = 1;
        Integer logicTemplateId = 1147;
        ProjectTemplateAuthEnum auth = ProjectTemplateAuthEnum.RW;
        String responsible = "admin";
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(null,logicTemplateId,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,null,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,logicTemplateId,auth,responsible,null).failed());
        //之前表中无权限
        Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,logicTemplateId,null,responsible,CustomDataSource.OPERATOR).success());
        //插入读写权限
        Mockito.when(templateAuthDAO.getByProjectIdAndTemplateId(Mockito.anyInt(),Mockito.anyString())).thenReturn(CustomDataSource.projectTemplateAuthPO());
        Mockito.when(templateAuthDAO.getByProjectIdAndTemplateId(Mockito.anyInt(),Mockito.anyString())).thenReturn(CustomDataSource.projectTemplateAuthPO());
        //对于权限进行更新操作
        Assertions.assertEquals("权限不存在",
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,logicTemplateId,
                        ProjectTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).getMessage());
        Mockito.when( templateAuthDAO.getById(Mockito.anyLong())).thenReturn(CustomDataSource.projectTemplateAuthPO());
      Assertions.assertEquals("参数错误:责任人非法，请检查后再提交！",
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,logicTemplateId,
                        ProjectTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).getMessage());
      Mockito.when(roleTool.isAdmin(Mockito.anyString())).thenReturn(true);
      Mockito.when(templateAuthDAO.update(Mockito.any())).thenReturn(1);
          Assertions.assertTrue(
                projectLogicTemplateAuthService.ensureSetLogicTemplateAuth(projectId,logicTemplateId,ProjectTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).success());
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
        mockAddTemplateAuth();
        ProjectTemplateAuthPO projectTemplateAuthPO = CustomDataSource.projectTemplateAuthPO();
        ProjectTemplateAuthDTO projectTemplateAuthDTO1 = ConvertUtil.obj2Obj(projectTemplateAuthPO,
                ProjectTemplateAuthDTO.class);
        Assertions.assertTrue(projectLogicTemplateAuthService.getTemplateAuthsByProjectId(projectTemplateAuthDTO1.getProjectId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getProjectId().equals(projectTemplateAuthDTO1.getProjectId())
                        &&appTemplateAuth.getTemplateId().equals(projectTemplateAuthDTO1.getTemplateId())
                        &&appTemplateAuth.getType().equals(projectTemplateAuthDTO1.getType())));
    }

    @Test
    public void addTemplateAuthTest() {
         mockAddTemplateAuth();
        ProjectTemplateAuthPO projectTemplateAuthPO = CustomDataSource.projectTemplateAuthPO();
        ProjectTemplateAuthDTO projectTemplateAuthDTO = ConvertUtil.obj2Obj(projectTemplateAuthPO,
                ProjectTemplateAuthDTO.class);
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
        mockAddTemplateAuth();
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        //对于插入的数据的权限信息进行相应的修改
        projectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.R.getCode());
        projectTemplateAuthDTO.setId(1L);
        Assertions.assertTrue(
                projectLogicTemplateAuthService.updateTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR)
                        .success());
      
        //设置Owner权限，这是不允许的
        projectTemplateAuthDTO.setType(ProjectTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(
                projectLogicTemplateAuthService.addTemplateAuth(projectTemplateAuthDTO, CustomDataSource.OPERATOR)
                        .failed());
    }

    @Test
    public void deleteTemplateAuthTest() {
         mockAddTemplateAuth();
        //插入数据，对数据进行对应的删除
        Mockito.when(templateAuthDAO.getByProjectIdAndTemplateId(
               Mockito.anyInt(),Mockito.anyString())).thenReturn(CustomDataSource.projectTemplateAuthPO());
        //对插入的数据进行删除
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteTemplateAuth(CustomDataSource.projectTemplateAuthPO().getId(), CustomDataSource.OPERATOR).success());
        //确认是否真的被删除
        //null异常情况的判断
        Assertions.assertTrue(projectLogicTemplateAuthService.deleteTemplateAuth(null,CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void getAllAppTemplateAuthsTest() {
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        projectTemplateAuthDTO.setTemplateId(1741);
        //插入数据之后获取设置的权限值
        mockAddTemplateAuth();
        //插入数据之后可以获取map下对应集合中的数值
        Assertions.assertTrue(projectLogicTemplateAuthService
                              .getAllProjectTemplateAuths()
                              .get(projectTemplateAuthDTO.getProjectId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(
                                     1)));
    }

    @Test
    public void getAuthEnumByAppIdAndLogicIdTest() {
        //未插入数据，显示无权限
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        projectTemplateAuthDTO.setTemplateId(1741);
        projectTemplateAuthDTO.setType(2);
        Assertions.assertEquals(projectLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(projectTemplateAuthDTO.getProjectId(),
                        projectTemplateAuthDTO.getTemplateId()),
                ProjectTemplateAuthEnum.NO_PERMISSION);
        //插入数据之后获取设置的权限值
        mockAddTemplateAuth();
        Mockito.when(templateAuthDAO.listByLogicTemplateId(Mockito.anyString())).thenReturn(Lists.newArrayList(CustomDataSource.projectTemplateAuthPO()));
        Assertions.assertEquals(ProjectTemplateAuthEnum.R,
                projectLogicTemplateAuthService.getAuthEnumByProjectIdAndLogicId(projectTemplateAuthDTO.getProjectId(),
                        projectTemplateAuthDTO.getTemplateId())
                );
    }

    private void mockAddTemplateAuth() {
        // 各种对象的Mock操作
        ProjectTemplateAuthDTO projectTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Integer templateId = 1;
        projectTemplateAuthDTO.setProjectId(1);
        projectTemplateAuthDTO.setTemplateId(templateId);
        IndexTemplate indexTemplate = new IndexTemplate();
        indexTemplate.setProjectId(1);
        indexTemplate.setId(templateId);
        IndexTemplateLogicWithClusterAndMasterTemplate indexTemplateLogicWithClusterAndMasterTemplate = new IndexTemplateLogicWithClusterAndMasterTemplate();
        ClusterLogic clusterLogic = new ClusterLogic();
        clusterLogic.setId(1L);
        indexTemplateLogicWithClusterAndMasterTemplate.setLogicCluster(clusterLogic);
         //ProjectVO projectVO= new ProjectVO();
        //projectVO.setId(1);
        //app.setIsRoot(0);
        //Map<Integer, App> appMap = new HashMap<>();
        //appMap.put(app.getId(),app);

        // 创建mock规则
        Mockito.when(indexTemplateService.listAllLogicTemplates()).thenReturn(Collections.singletonList(indexTemplate));
        Mockito.when(indexTemplateService.getLogicTemplateById(Mockito.anyInt())).thenReturn(indexTemplate);
        Mockito.when(indexTemplateService.getLogicTemplateWithClusterAndMasterTemplate(Mockito.anyInt())).thenReturn(indexTemplateLogicWithClusterAndMasterTemplate);
        Mockito.when(logicClusterAuthService.getLogicClusterAuthEnum(Mockito.anyInt(), Mockito.anyLong())).thenReturn(
                ProjectClusterLogicAuthEnum.OWN);
        Mockito.when(projectService.getProjectDetailByProjectId(Mockito.anyInt())).thenReturn(new ProjectVO());
        Mockito.when(roleTool.isAdmin(Mockito.anyString())).thenReturn(true);
        Mockito.when(projectService.checkProjectExist(Mockito.anyInt())).thenReturn(true);
        Mockito.when(indexTemplateService.listAllLogicTemplates()).thenReturn(ConvertUtil.list2List(CustomDataSource.getTemplateLogicPOList(),IndexTemplate.class));
        Mockito.when(templateAuthDAO.listWithRwAuths()).thenReturn(Lists.newArrayList(CustomDataSource.projectTemplateAuthPO()));
        Mockito.when(templateAuthDAO.insert(Mockito.any())).thenReturn(1);
        Mockito.when(templateAuthDAO.getByProjectIdAndTemplateId(
                Mockito.anyInt(),Mockito.anyString())).thenReturn(ConvertUtil.obj2Obj(projectTemplateAuthDTO,
                ProjectTemplateAuthPO.class));
          //再次确认权限的修改已经成功
        Mockito.when(templateAuthDAO.getById(Mockito.anyLong()))
                .thenReturn(CustomDataSource.projectTemplateAuthPO());
        Mockito.when( templateAuthDAO.update(Mockito.any())).thenReturn(1);
        Mockito.when(templateAuthDAO.delete(Mockito.anyLong())).thenReturn(1);
        Mockito.when(templateAuthDAO.listWithRwAuthsByProjectId(Mockito.anyInt())).thenReturn(Lists.newArrayList(CustomDataSource.projectTemplateAuthPO()));
        Mockito.when(indexTemplateService.listProjectLogicTemplatesByProjectId(Mockito.anyInt())).thenReturn(Lists.newArrayList(ConvertUtil.obj2Obj(CustomDataSource.indexTemplateLogicDTOFactory(),IndexTemplate.class)));
    }
}