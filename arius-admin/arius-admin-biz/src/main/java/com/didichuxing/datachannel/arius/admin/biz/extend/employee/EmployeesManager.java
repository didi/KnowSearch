package com.didichuxing.datachannel.arius.admin.biz.extend.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.remote.employee.content.EmployeeTypeEnum;

/**
 * @author linyunan
 * @date 2021-05-14
 */
@Component
public class EmployeesManager {

    @Autowired
    private EmployeeService               employeeService;

    /**
     * 兼容外部不同企业的员工模块
     * @see EmployeeTypeEnum
     */
    private static final EmployeeTypeEnum DEPARTMENT_TYPE = null;

    public Result getEmployeeByDomainAccount(String domainAccount) {
        return employeeService.getByDomainAccount(domainAccount, DEPARTMENT_TYPE);
    }
}
