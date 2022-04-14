package com.didichuxing.datachannel.arius.admin.core.service.app;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.po.app.AppPO;
import com.didichuxing.datachannel.arius.admin.core.service.app.impl.AppUserInfoServiceImpl;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.app.AppDAO;

import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Rollback
public class AppUserInfoServiceTest extends AriusAdminApplicationTest {

    private static final ILog LOGGER = LogFactory.getLog(AppUserInfoServiceTest.class);

    @Autowired
    private AppUserInfoServiceImpl service;

    @Autowired
    private AppDAO appDAO;

    @Test
    public void recordNullTest() {
        String user = "1";
        boolean result = service.recordAppidAndUser(null, user);
        Assertions.assertFalse(result);
        String user1 = "";
        boolean result1 = service.recordAppidAndUser(1, user1);
        Assertions.assertFalse(result1);
    }

    private AppPO getAppPO() {
        AppPO appPO = new AppPO();
        appPO.setName("test1");
        appPO.setDataCenter("");
        appPO.setIsRoot(1);
        appPO.setMemo("");
        appPO.setIp("");
        appPO.setVerifyCode("");
        appPO.setIsActive(1);
        appPO.setQueryThreshold(100);
        appPO.setCluster("");
        appPO.setDepartmentId("");
        appPO.setDepartment("");
        appPO.setResponsible("");
        appPO.setSearchType(0);
        return appPO;
    }

    @Test
    public void recordInsertTest() {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        LOGGER.debug("{}", appPO.getId());
        Integer id = appPO.getId();
        String username = "test";
        boolean result = service.recordAppidAndUser(id, username);
        Assertions.assertTrue(result);
    }

    @Test
    public void recordUpdateTest() {
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        Integer id = appPO.getId();
        String username = "test";
        boolean result = service.recordAppidAndUser(id, username);
        boolean result1 = service.recordAppidAndUser(id, username);
        Assertions.assertTrue(result1);
    }

    @Test
    public void getByUserTest() {
        String username = "test";
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        AppPO appPO1 = getAppPO();
        appDAO.insert(appPO1);
        Integer id = appPO.getId();
        Integer id1 = appPO1.getId();
        boolean result = service.recordAppidAndUser(id, username);
        boolean result1 = service.recordAppidAndUser(id1, username);
        List<AppUserInfo> list = service.getByUser(username);
        List<Integer> idList = list.stream().map(AppUserInfo::getAppId).collect(Collectors.toList());
        Assertions.assertTrue(idList.contains(id));
        Assertions.assertTrue(idList.contains(id1));
    }

    @Test
    public void getByUserNullTest() {
        List<AppUserInfo> list = service.getByUser(null);
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    public void getRecordNullTest() {
        AppUserInfo info = service.getAppLastLoginRecord(null);
        Assertions.assertNull(info);
    }

    @Test
    public void getRecordTest() {
        String username = "test";
        AppPO appPO = getAppPO();
        appDAO.insert(appPO);
        Integer id = appPO.getId();
        boolean result = service.recordAppidAndUser(id, username);
        AppUserInfo info = service.getAppLastLoginRecord(id);
        Assertions.assertEquals(1, info.getLoginCount().intValue());
        for (int i = 0; i < 10; i++) {
            service.recordAppidAndUser(id, username);
        }
        AppUserInfo info1 = service.getAppLastLoginRecord(id);
        Assertions.assertEquals(11, info1.getLoginCount().intValue());
    }

}
