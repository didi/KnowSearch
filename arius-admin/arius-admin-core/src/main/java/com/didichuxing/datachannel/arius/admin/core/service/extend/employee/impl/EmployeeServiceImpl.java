package com.didichuxing.datachannel.arius.admin.core.service.extend.employee.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.extend.employee.EmployeeService;
import com.didichuxing.datachannel.arius.admin.remote.employee.EmployeeHandle;
import com.didichuxing.datachannel.arius.admin.remote.employee.content.EmployeeTypeEnum;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author linyunan
 * @date 2021-05-17
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final ILog LOGGER = LogFactory.getLog(EmployeeServiceImpl.class);

    @Autowired
    private HandleFactory     handleFactory;

    @Override
    public Result getByDomainAccount(String domainAccount, EmployeeTypeEnum employeeType) {
        Result<String> getEmployeeTypeResult = getEmployeeType(employeeType);
        if (getEmployeeTypeResult.failed()) {
            return getEmployeeTypeResult;
        }

        String finalEmployeeType = getEmployeeTypeResult.getData();

        LOGGER.info("class=EmployeeServiceImpl||method=getByDomainAccount||employeeType={}", finalEmployeeType);

        return ((EmployeeHandle) handleFactory.getByHandlerNamePer(finalEmployeeType)).getByDomainAccount(domainAccount);
    }

    @Override
    public Result checkUsers(String domainAccounts, EmployeeTypeEnum employeeType) {

        Result<String> getEmployeeTypeResult = getEmployeeType(employeeType);
        if (getEmployeeTypeResult.failed()) {
            return getEmployeeTypeResult;
        }

        String finalEmployeeType = getEmployeeTypeResult.getData();

        LOGGER.info("class=EmployeeServiceImpl||method=checkUsers||employeeType={}", finalEmployeeType);

        return ((EmployeeHandle) handleFactory.getByHandlerNamePer(finalEmployeeType)).checkUsers(domainAccounts);
    }

    @Override
    public Result searchOnJobStaffByKeyWord(String keyWord, EmployeeTypeEnum employeeType) {

        Result<String> getEmployeeTypeResult = getEmployeeType(employeeType);
        if (getEmployeeTypeResult.failed()) {
            return getEmployeeTypeResult;
        }

        String finalEmployeeType = getEmployeeTypeResult.getData();

        LOGGER.info("class=EmployeeServiceImpl||method=searchOnJobStaffByKeyWord||employeeType={}", finalEmployeeType);

        return ((EmployeeHandle) handleFactory.getByHandlerNamePer(finalEmployeeType))
            .searchOnJobStaffByKeyWord(keyWord);
    }

    /*************************************private****************************************************/
    private Result<String> getEmployeeType(EmployeeTypeEnum typeEnum) {
        if (AriusObjUtils.isNull(typeEnum)) {
            return Result.build(Boolean.TRUE, EmployeeTypeEnum.DEFAULT.getType());
        }

        if (EmployeeTypeEnum.valueOfCode(typeEnum.getCode()).getCode() == -1) {
            return Result.buildFail(String.format("获取 %s 类型出错", typeEnum.getType()));
        }

        return Result.build(Boolean.TRUE, EmployeeTypeEnum.valueOfCode(typeEnum.getCode()).getType());
    }
}
