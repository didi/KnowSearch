package com.didichuxing.datachannel.arius.admin.core.service.extend.department;

import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
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
public class DepartmentServiceTest extends AriusAdminApplicationTests {

    @Autowired
    private DepartmentService departmentService;

    @Test
    void listDepartmentsByTypeTest() {
        Result<String> stringResult = departmentService.listDepartmentsByType();
        Assertions.assertFalse(stringResult.getData().isEmpty());
    }
}
