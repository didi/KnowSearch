package com.didichuxing.datachannel.arius.admin.core.service.extend.employee.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.employee.BaseEmInfo;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.remote.employee.EmployeeHandle;

/**
 * @author linyunan
 * @date 2021-05-17
 */
@Component
public class DefaultEmployeeHandle implements EmployeeHandle {

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Override
    public <T extends BaseEmInfo> Result<T> getByDomainAccount(String domainAccount) {
        return (Result<T>) Result.buildSucc(ariusUserInfoService.getByDomainAccount(domainAccount));
    }

    @Override
    public Result<Void> checkUsers(String domainAccounts) {
        AriusUserInfo ariusUserInfo = ariusUserInfoService.getByDomainAccount(domainAccounts);
        if (AriusObjUtils.isNull(ariusUserInfo)) {
            return Result.buildFail();
        }
        return Result.buildSucc();
    }
}
