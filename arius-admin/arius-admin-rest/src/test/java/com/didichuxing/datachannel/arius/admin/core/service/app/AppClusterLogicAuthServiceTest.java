package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.constant.app.AppClusterLogicAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppClusterLogicAuth;
import com.didichuxing.datachannel.arius.admin.core.service.app.impl.AppClusterLogicAuthServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppLogicClusterAuthDAO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.LogicClusterDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

@Transactional
@Rollback
public class AppClusterLogicAuthServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private AppClusterLogicAuthService appClusterLogicAuthService;

    @Autowired
    private AppLogicClusterAuthDAO appLogicClusterAuthDAO;

    @BeforeEach
    public void init() {

    }

    @Test
    public void ensureSetLogicClusterAuthTest() {
        Assertions.assertEquals("参数错误:未指定appId，请检查后再提交！",
                appClusterLogicAuthService.ensureSetLogicClusterAuth(null, null, null, null, null).getMessage());
        Integer appId = 1;
        Assertions.assertEquals("参数错误:未指定逻辑集群ID，请检查后再提交！",
                appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, null, null, null, null).getMessage());
        Long logicClusterId = 173L;
        Assertions.assertEquals("参数错误:未指定操作人，请检查后再提交！",
                appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, null, null, null).getMessage());
        String operator = "wpk";
        String responsible = "admin";
        Assertions.assertTrue(appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, null, responsible, operator).success());
        Assertions.assertTrue(appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, AppClusterLogicAuthEnum.OWN, responsible, operator).success());
        Assertions.assertEquals(appLogicClusterAuthDAO.getByAppIdAndLogicCluseterId(appId, logicClusterId).getType(),
                AppClusterLogicAuthEnum.OWN.getCode());
        Assertions.assertTrue(appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, AppClusterLogicAuthEnum.ACCESS, responsible, operator).success());
        Assertions.assertEquals(AppClusterLogicAuthEnum.ACCESS.getCode(),
                appLogicClusterAuthDAO.getByAppIdAndLogicCluseterId(appId, logicClusterId).getType());
        logicClusterId = 205L;
        Assertions.assertEquals("不支持对集群owner的权限进行修改",
                appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, AppClusterLogicAuthEnum.ACCESS, responsible, operator).getMessage());
    }

    @Test
    public void getAllLogicClusterAuthsTest() {
        Integer appId = 1;
        Assertions.assertFalse(CollectionUtils.isEmpty(appClusterLogicAuthService.getAllLogicClusterAuths(appId)));
        Long logicClusterId = 173L;
        String operator = "wpk";
        String responsible = "admin";
        Assertions.assertTrue(appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, AppClusterLogicAuthEnum.OWN, responsible, operator).success());
        Assertions.assertTrue(appClusterLogicAuthService.getAllLogicClusterAuths(appId)
                .stream()
                .map(AppClusterLogicAuth::getLogicClusterId)
                .collect(Collectors.toList())
                .contains(logicClusterId));
    }

    @Test
    public void canCreateLogicTemplateTest() {
        Assertions.assertFalse(appClusterLogicAuthService.canCreateLogicTemplate(null, null));
        Integer appId = 1;
        Assertions.assertFalse(CollectionUtils.isEmpty(appClusterLogicAuthService.getAllLogicClusterAuths(appId)));
        Long logicClusterId = 173L;
        String operator = "wpk";
        String responsible = "admin";
        Assertions.assertFalse(appClusterLogicAuthService.canCreateLogicTemplate(appId, logicClusterId));
        Assertions.assertTrue(appClusterLogicAuthService.ensureSetLogicClusterAuth(appId, logicClusterId, AppClusterLogicAuthEnum.OWN, responsible, operator).success());
        Assertions.assertTrue(appClusterLogicAuthService.canCreateLogicTemplate(appId, logicClusterId));
    }

}
