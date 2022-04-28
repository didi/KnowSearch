package com.didichuxing.datachannel.arius.admin.core.service.extend.employee;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.remote.employee.EmployeeTypeEnum;

/**
 * @author linyunan
 * @date 2021-05-17
 */
public interface EmployeeService {
    /**
     * 校验用户域账号是否有效
     * @param domainAccounts 域账号列表
     * @see	 EmployeeTypeEnum
     */
    Result<Void> checkUsers(String domainAccounts);
}
