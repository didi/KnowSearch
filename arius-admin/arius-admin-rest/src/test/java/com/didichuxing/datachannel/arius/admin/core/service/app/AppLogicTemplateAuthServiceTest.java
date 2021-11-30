package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppTemplateAuthPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppTemplateAuthDAO;
import com.didichuxing.datachannel.arius.admin.util.CustomDataSource;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Transactional
@Rollback
public class AppLogicTemplateAuthServiceTest extends AriusAdminApplicationTests {
    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private AppTemplateAuthDAO appTemplateAuthDAO;

    private static  final String OPERATOR = "wpk";

    @Test
    public void deleteExcessTemplateAuthsIfNeedTest() {
        Assertions.assertTrue(appLogicTemplateAuthService.deleteExcessTemplateAuthsIfNeed(false));
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setAppId(1);
        appTemplateAuthDTO.setTemplateId(1147);
        //模拟插入冗余数据
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
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
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(null,logicTemplateId,auth,responsible,OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,null,auth,responsible,OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,auth,responsible,null).failed());
        //之前表中无权限
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,null,responsible,OPERATOR).success());
        //插入读写权限
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,auth,responsible,OPERATOR).success());
        Assertions.assertNotNull(appTemplateAuthDAO.getByAppIdAndTemplateId(appId,logicTemplateId.toString()).getId());
        //对于权限进行更新操作
        Assertions.assertTrue(appLogicTemplateAuthService.ensureSetLogicTemplateAuth(appId,logicTemplateId,AppTemplateAuthEnum.R,responsible,OPERATOR).success());
        //确认权限是否真的被更新
        Assertions.assertEquals(AppTemplateAuthEnum.R.getCode(),appTemplateAuthDAO.getByAppIdAndTemplateId(appId,logicTemplateId.toString()).getType());
    }

    @Test
    public void getTemplateAuthsByAppIdTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setAppId(1);
        appTemplateAuthDTO.setTemplateId(1147);
        Assertions.assertFalse(appLogicTemplateAuthService.getTemplateAuthsByAppId(appTemplateAuthDTO.getAppId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getAppId().equals(appTemplateAuthDTO.getAppId())
                                             &&appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO.getTemplateId())
                                             &&appTemplateAuth.getType().equals(appTemplateAuthDTO.getType())));
        //插入对应的数据
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO,OPERATOR).success());
        Assertions.assertTrue(appLogicTemplateAuthService.getTemplateAuthsByAppId(appTemplateAuthDTO.getAppId())
                .stream()
                .anyMatch(appTemplateAuth -> appTemplateAuth.getAppId().equals(appTemplateAuthDTO.getAppId())
                        &&appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO.getTemplateId())
                        &&appTemplateAuth.getType().equals(appTemplateAuthDTO.getType())));
    }

    @Test
    public void addTemplateAuthTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        //设置Owner权限，这是不允许的
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(null, null).failed());
        //设置责任人字段为空
        appTemplateAuthDTO.setResponsible(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
        //设置权限字段为空
        appTemplateAuthDTO.setType(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
        //设置不存在的逻辑模板id字段
        appTemplateAuthDTO.setTemplateId(1111111111);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
        //设置所属的appid字段为空
        appTemplateAuthDTO.setAppId(null);
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
    }

    @Test
    public void updateTemplateAuthTest() {
        //插入一条数据
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        //对于插入的数据的权限信息进行相应的修改
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.R.getCode());
        AppTemplateAuthPO byAppIdAndTemplateId = appTemplateAuthDAO.getByAppIdAndTemplateId(appTemplateAuthDTO.getAppId(), appTemplateAuthDTO.getTemplateId().toString());
        appTemplateAuthDTO.setId(byAppIdAndTemplateId.getId());
        Assertions.assertTrue(appLogicTemplateAuthService.updateTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        //再次确认权限的修改已经成功
        AppTemplateAuthPO updateAppTemplateAuthPO = appTemplateAuthDAO.getById(byAppIdAndTemplateId.getId());
        Assertions.assertEquals(AppTemplateAuthEnum.R.getCode(), updateAppTemplateAuthPO.getType());
        //设置Owner权限，这是不允许的
        appTemplateAuthDTO.setType(AppTemplateAuthEnum.OWN.getCode());
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).failed());
    }

    @Test
    public void deleteTemplateAuthTest() {
        //插入数据，对数据进行对应的删除
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        AppTemplateAuthPO byAppIdAndTemplateId = appTemplateAuthDAO.getByAppIdAndTemplateId(appTemplateAuthDTO.getAppId(), appTemplateAuthDTO.getTemplateId().toString());
        Long authId = byAppIdAndTemplateId.getId();
        //对插入的数据进行删除
        Assertions.assertTrue(appLogicTemplateAuthService.deleteTemplateAuth(authId, OPERATOR).success());
        //确认是否真的被删除
        Assertions.assertNull(appTemplateAuthDAO.getById(authId));
        //null异常情况的判断
        Assertions.assertTrue(appLogicTemplateAuthService.deleteTemplateAuth(null,OPERATOR).failed());
    }

    @Test
    public void getAllAppTemplateAuthsTest() {
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setTemplateId(1741);
        //插入数据之前不存在对应的数据
        Assertions.assertFalse(appLogicTemplateAuthService
                              .getAllAppTemplateAuths()
                              .get(appTemplateAuthDTO.getAppId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO.getTemplateId())));
        //插入mock的数据
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        //插入数据之后可以获取map下对应集合中的数值
        Assertions.assertTrue(appLogicTemplateAuthService
                              .getAllAppTemplateAuths()
                              .get(appTemplateAuthDTO.getAppId())
                              .stream()
                              .anyMatch(appTemplateAuth -> appTemplateAuth.getTemplateId().equals(appTemplateAuthDTO.getTemplateId())));
    }

    @Test
    public void getAuthEnumByAppIdAndLogicIdTest() {
        //未插入数据，显示无权限
        AppTemplateAuthDTO appTemplateAuthDTO = CustomDataSource.appTemplateAuthDTOFactory();
        appTemplateAuthDTO.setTemplateId(1741);
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(appTemplateAuthDTO.getAppId(),appTemplateAuthDTO.getTemplateId()),
                AppTemplateAuthEnum.NO_PERMISSION);
        //插入数据之后获取设置的权限值
        Assertions.assertTrue(appLogicTemplateAuthService.addTemplateAuth(appTemplateAuthDTO, OPERATOR).success());
        Assertions.assertEquals(appLogicTemplateAuthService.getAuthEnumByAppIdAndLogicId(appTemplateAuthDTO.getAppId(),appTemplateAuthDTO.getTemplateId()),
                AppTemplateAuthEnum.valueOf(appTemplateAuthDTO.getType()));
    }
}
