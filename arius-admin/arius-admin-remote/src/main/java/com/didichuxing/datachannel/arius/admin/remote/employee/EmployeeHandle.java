package com.didichuxing.datachannel.arius.admin.remote.employee;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author linyunan
 * @date 2021-05-14
 */
public interface EmployeeHandle extends BaseHandle {
    /**
     * 根据域账号获取员工信息
     * @param domainAccount 域账号
     * @return data
     */
    Result getByDomainAccount(String domainAccount);

    /**
     * 校验多个用户域账号是否有效
     * @param domainAccounts 域账号列表
     */
    Result checkUsers(String domainAccounts);

    /**
     * 根据关键词查找用户信息
     * @param keyWord
     * @return
     */
    Result searchOnJobStaffByKeyWord(String keyWord);
}
