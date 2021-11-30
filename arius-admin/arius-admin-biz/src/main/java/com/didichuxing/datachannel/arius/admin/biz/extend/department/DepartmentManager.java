package com.didichuxing.datachannel.arius.admin.biz.extend.department;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;

/**
 * @author linyunan
 * @date 2021-04-29
 */
public interface DepartmentManager {

    /**
     * 获取部门列表
     */
    Result<String> listDepartments();
}
