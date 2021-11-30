package com.didichuxing.datachannel.arius.admin.core.service.extend.employee;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author cjm
 */
@Transactional
@Rollback
public class EmployeeServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void getByDomainAccountTest() {
        String account = "admin";
        Result<BaseEmInfo> byDomainAccount = employeeService.getByDomainAccount(account);
        Assertions.assertNotNull(byDomainAccount.getData());
        // 不存在的account
        account = "testest";
        byDomainAccount = employeeService.getByDomainAccount(account);
        Assertions.assertNull(byDomainAccount.getData());
    }

    @Test
    void checkUsersTest() {
        String account = "admin";
        Result<Void> result = employeeService.checkUsers(account);
        Assertions.assertEquals(0, (int) result.getCode());
        // 不存在的account
        account = "testest";
        result = employeeService.checkUsers(account);
        Assertions.assertEquals(19999, (int) result.getCode());
    }
}
