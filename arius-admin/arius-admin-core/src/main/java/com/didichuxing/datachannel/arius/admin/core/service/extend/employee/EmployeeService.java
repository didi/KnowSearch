package com.didichuxing.datachannel.arius.admin.core.service.extend.employee;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import com.didichuxing.datachannel.arius.admin.remote.employee.content.EmployeeTypeEnum;

/**
 * @author linyunan
 * @date 2021-05-17
 */
public interface EmployeeService {
    /**
     * 根据域账号获取员工信息
     * @param domainAccount 域账号
     * @see	 EmployeeTypeEnum
     * @return data
     */
    <T extends BaseEmInfo> Result<T> getByDomainAccount(String domainAccount);

    /**
     * 校验用户域账号是否有效
     * @param domainAccounts 域账号列表
     * @see	 EmployeeTypeEnum
     */
    Result<Void> checkUsers(String domainAccounts);

    /**
     * searchOnJobStaffByKeyWord
     * @param keyWord
     * @see	 EmployeeTypeEnum
     * @return
     */
    Result<Object> searchOnJobStaffByKeyWord(String keyWord);
}
