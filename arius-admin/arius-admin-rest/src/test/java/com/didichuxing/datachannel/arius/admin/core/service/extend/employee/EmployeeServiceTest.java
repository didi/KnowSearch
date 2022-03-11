package com.didichuxing.datachannel.arius.admin.core.service.extend.employee;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTest;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.impl.DefaultEmployeeHandle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class EmployeeServiceTest extends AriusAdminApplicationTest {

    @Autowired
    private EmployeeService employeeService;

    @MockBean
    private AriusUserInfoService ariusUserInfoService;

    @Test
    public void getByDomainAccountTest() {
        Mockito.when(ariusUserInfoService.getByDomainAccount("admin")).thenReturn(new AriusUserInfo());
        Assertions.assertNotNull(employeeService.getByDomainAccount("admin"));
    }

    @Test
    public void checkUsersTest() {
        Mockito.when(ariusUserInfoService.getByDomainAccount("admin")).thenReturn(new AriusUserInfo());
        Assertions.assertTrue(employeeService.checkUsers("admin").success());
    }
}
