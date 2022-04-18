package com.didichuxing.datachannel.arius.admin.biz.extend.employee;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;

/**
 * @author linyunan
 * @date 2021-05-14
 */
public interface EmployeesManager {

    <T extends BaseEmInfo> Result<T> getEmployeeByDomainAccount(String domainAccount);
}
