package com.didichuxing.datachannel.arius.admin.core.service.extend.department.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.extend.department.DepartmentService;
import com.didichuxing.datachannel.arius.admin.remote.department.DepartmentHandle;
import com.didichuxing.datachannel.arius.admin.remote.department.content.DepartmentTypeEnum;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author linyunan
 * @date 2021-04-26
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final ILog  LOGGER             = LogFactory.getLog(DepartmentServiceImpl.class);

    @Autowired
    private HandleFactory      handleFactory;

    @Override
    public Result listDepartmentsByType(DepartmentTypeEnum typeEnum) {

        Result<String> getEmployeeTypeResult = getDepartmentsType(typeEnum);
        if (getEmployeeTypeResult.failed()) {
            return getEmployeeTypeResult;
        }

        String finalDepartmentsType = getEmployeeTypeResult.getData();

        LOGGER.info("class=DepartmentServiceImpl||method=listDepartmentsByType||departmentType={}",
            finalDepartmentsType);

        return ((DepartmentHandle) handleFactory.getByHandlerNamePer(finalDepartmentsType)).listDepartment();
    }

    /*************************************private****************************************************/

    private Result<String> getDepartmentsType(DepartmentTypeEnum typeEnum) {
        if (AriusObjUtils.isNull(typeEnum)) {
            return Result.build(Boolean.TRUE, DepartmentTypeEnum.DEFAULT.getType());
        }

        if (DepartmentTypeEnum.valueOfCode(typeEnum.getCode()).getCode() == -1) {
            return Result.buildFail(String.format("获取 %s 类型出错", typeEnum.getType()));
        }

        return Result.build(Boolean.TRUE, DepartmentTypeEnum.valueOfCode(typeEnum.getCode()).getType());
    }
}
