package com.didichuxing.datachannel.arius.admin.remote.department;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author linyunan
 * @date 2021-04-26
 */
public interface DepartmentHandle extends BaseHandle {

    /**
     * 获取部门列表
     */
    Result<String> listDepartment();
}
