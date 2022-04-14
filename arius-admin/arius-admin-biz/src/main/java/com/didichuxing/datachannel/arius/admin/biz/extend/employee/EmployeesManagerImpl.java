package com.didichuxing.datachannel.arius.admin.biz.extend.employee;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-07-26
 */
@Component
public class EmployeesManagerImpl implements EmployeesManager {

    @Autowired
    private EmployeeService               employeeService;

    @Override
    public <T extends BaseEmInfo> Result<T> getEmployeeByDomainAccount(String domainAccount) {
        return employeeService.getByDomainAccount(domainAccount);
    }
}
