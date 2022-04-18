package com.didichuxing.datachannel.arius.admin.biz.extend.department;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.core.service.extend.department.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by linyunan on 2021-07-26
 */
@Component
public class DepartmentManagerImpl implements DepartmentManager {
    @Autowired
    private DepartmentService               departmentService;

    @Override
    public Result<String> listDepartments() {
        return departmentService.listDepartmentsByType();
    }
}
