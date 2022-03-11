package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.App;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.cluster.ClusterLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithClusterAndMasterTemplate;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.core.service.template.logic.TemplateLogicService;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppTemplateAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.google.common.collect.Lists;
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
public class AppLogicTemplateAuthServiceTest extends AriusAdminApplicationTest {
    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private AppTemplateAuthDAO appTemplateAuthDAO;

    @MockBean
    private TemplateLogicService templateLogicService;

    @MockBean
    private AppClusterLogicAuthService logicClusterAuthService;

    @MockBean
    private AppService appService;

    @Test
    public void deleteExcessTemplateAuthsIfNeedTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = mockAddTemplateAuth();
        AppTemplateAuthPO byAppIdAndTemplateId = appTemplateAuthDAO.getByAppIdAndTemplateId(appTemplateAuthDTO.getAppId(), appTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //删除冗余数据
        Assertions.assertTrue(appLogicTemplateAuthService.deleteExcessTemplateAuthsIfNeed(true));
        //检测冗余的数据是否被清除掉
        Assertions.assertNull(appTemplateAuthDAO.getById(authId));
    }

    @Test
    public void ensureSetLogicTemplateAuthTest() {
        Integer appId = 1;
        Integer logicTemplateId = 1147;
        AppTemplateAuthEnum auth = AppTemplateAuthEnum.RW;
        String responsible = "admin";
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(null,logicTemplateId,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,null,auth,responsible,CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,auth,responsible,null).failed());
        //之前表中无权限
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,null,responsible,CustomDataSource.OPERATOR).success());
        //插入读写权限
        AppTemplateAuthDTO appTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertNotNull(appTemplateAuthDAO.getByAppIdAndTemplateId(appId,logicTemplateId.toString()).getId());
        //对于权限进行更新操作
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,AppTemplateAuthEnum.R,responsible,CustomDataSource.OPERATOR).success());
        //确认权限是否真的被更新
        Assertions.assertEquals(AppTemplateAuthEnum.R.getCode(),appTemplateAuthDAO.getByAppIdAndTemplateId(appId,logicTemplateId.toString()).getType());
    }

    @Test
    public void getTemplateAuthsByAppIdTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertFalse(appLogicTemplateAuthService.getTemplateAuthsByAppId(appTemplateAuthDTO.getAppId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getAppId().equals(appTemplateAuthDTO.getAppId())
                                             &&appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO.getTemplateId())
                                             &&appTemplateAuth.getType().equals(appTemplateAuthDTO.getType())));
        //插入对应的数据
        AppTemplateAuthDTO appTemplateAuthDTO1 = mockAddTemplateAuth();
        Assertions.assertTrue(appLogicTemplateAuthService.getTemplateAuthsByAppId(appTemplateAuthDTO1.getAppId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getAppId().equals(appTemplateAuthDTO1.getAppId())
                        &&appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO1.getTemplateId())
                        &&appTemplateAuth.getType().equals(appTemplateAuthDTO1.getType())));
    }

    @Test
    public void addTemplateAuthTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = mockAddTemplateAuth();
        //设置Owner权限，这是不允许的
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(null, null).failed());
        //设置责任人字段为空
        appTemplateAuthDTO.setResponsible(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置权限字段为空
        appTemplateAuthDTO.setType(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置不存在的逻辑模板id字段
        appTemplateAuthDTO.setTemplateId(1111111111);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
        //设置所属的appid字段为空
        appTemplateAuthDTO.setAppId(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void updateTemplateAuthTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = mockAddTemplateAuth();
        //对于插入的数据的权限信息进行相应的修改
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.R.getCode());
        AppTemplateAuthPO byAppIdAndTemplateId = appTemplateAuthDAO.getByAppIdAndTemplateId(appTemplateAuthDTO.getAppId(), appTemplateAuthDTO.getTemplateId().toString());
        appTemplateAuthDTO.setId(byAppIdAndTemplateId.getId());
        Assertions.assertTrue(appLogicTemplateAuthService.updateTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).success());
        //再次确认权限的修改已经成功
        AppTemplateAuthPO updateAppTemplateAuthPO = appTemplateAuthDAO.getById(byAppIdAndTemplateId.getId());
        Assertions.assertEquals(AppTemplateAuthEnum.R.getCode(), updateAppTemplateAuthPO.getType());
        //设置Owner权限，这是不允许的
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).failed());
    }

    @Test
    public void deleteTemplateAuthTest() {
        //插入数据，对数据进行对应的删除
        AppTemplateAuthDTO appTemplateAuthDTO = mockAddTemplateAuth();
        AppTemplateAuthPO byAppIdAndTemplateId = appTemplateAuthDAO.getByAppIdAndTemplateId(appTemplateAuthDTO.getAppId(), appTemplateAuthDTO.getTemplateId().toString());
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
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setTemplateId(1741);
        //插入mock的数据
        AppTemplateAuthDTO appTemplateAuthDTO1 = mockAddTemplateAuth();
        //插入数据之后可以获取map下对应集合中的数值
        Assertions.assertTrue(appLogicTemplateAuthService
                              .getAllAppTemplateAuths()
                              .get(appTemplateAuthDTO.getAppId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO1.getTemplateId())));
    }

    @Test
    public void getAuthEnumByAppIdAndLogicIdTest() {
        //未插入数据，显示无权限
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setTemplateId(1741);
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(appTemplateAuthDTO.getAppId(),appTemplateAuthDTO.getTemplateId()),
                AppTemplateAuthEnum.NO_PERMISSION);
        //插入数据之后获取设置的权限值
        appTemplateAuthDTO = mockAddTemplateAuth();
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(appTemplateAuthDTO.getAppId(),appTemplateAuthDTO.getTemplateId()),
                AppTemplateAuthEnum.valueOf(appTemplateAuthDTO.getType()));
    }

    private AppTemplateAuthDTO mockAddTemplateAuth() {
        // 各种对象的Mock操作
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Integer templateId = 1147;
        appTemplateAuthDTO.setAppId(1);
        appTemplateAuthDTO.setTemplateId(templateId);
        IndexTemplateLogic indexTemplateLogic = new IndexTemplateLogic();
        indexTemplateLogic.setAppId(1);
        indexTemplateLogic.setId(templateId);
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
        Mockito.when(templateLogicService.getAllLogicTemplates()).thenReturn(Collections.singletonList(indexTemplateLogic));
        Mockito.when(templateLogicService.getLogicTemplateById(Mockito.anyInt())).thenReturn(indexTemplateLogic);
        Mockito.when(templateLogicService.getLogicTemplateWithClusterAndMasterTemplate(Mockito.anyInt())).thenReturn(indexTemplateLogicWithClusterAndMasterTemplate);
        Mockito.when(logicClusterAuthService.getLogicClusterAuthEnum(Mockito.anyInt(), Mockito.anyLong())).thenReturn(AppClusterLogicAuthEnum.OWN);
        Mockito.when(appService.getAppById(Mockito.anyInt())).thenReturn(app);
        Mockito.when(appService.isSuperApp(Mockito.anyInt())).thenReturn(false);
        Mockito.when(appService.isAppExists(Mockito.anyInt())).thenReturn(true);
        Mockito.when(appService.getAppsMap()).thenReturn(appMap);

        // 创建插入的mock数据
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, CustomDataSource.OPERATOR).success());

        return appTemplateAuthDTO;
    }
}
