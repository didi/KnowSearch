package com.didichuxing.datachannel.arius.admin.core.service.extend.department.impl;

import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.remote.department.DepartmentHandle;

/**
 * @author linyunan
 * @date 2021-05-17
 */
@Component
public class DefaultDepartmentHandle implements DepartmentHandle {

    @Override
    public Result listDepartment() {
        return Result.build(Boolean.TRUE, getDefaultDepartment());
    }

    private String getDefaultDepartment() {
        return "{" + "bu: {" + "data: [{" + "name: 商业数据产品团队," + "id: 809," + "shortName: BDP,"
               + "master: zhangliangmike}]" + "}" + "}";
    }
}
