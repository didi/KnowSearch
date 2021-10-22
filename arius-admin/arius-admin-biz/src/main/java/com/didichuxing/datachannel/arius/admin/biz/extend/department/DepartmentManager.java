package com.didichuxing.datachannel.arius.admin.biz.extend.department;

import com.didichuxing.datachannel.arius.admin.remote.department.content.DepartmentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.extend.department.DepartmentService;

/**
 * @author linyunan
 * @date 2021-04-29
 */
@Component
public class DepartmentManager {

    @Autowired
    private DepartmentService               departmentService;

    /**
     * 兼容外部不同的企业的部门模块
     * @see DepartmentTypeEnum
     */
    private static final DepartmentTypeEnum DEPARTMENT_TYPE = null;

    public Result listDepartments() {
        return departmentService.listDepartmentsByType(DEPARTMENT_TYPE);
    }
}
