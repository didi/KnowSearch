package com.didichuxing.datachannel.arius.admin.biz.app;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import javax.servlet.http.HttpServletRequest;

public interface RoleExtendManager {
    Result<Void> deleteRoleByRoleId(Integer id, HttpServletRequest request);
}