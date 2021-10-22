package com.didichuxing.datachannel.arius.admin.biz.app.impl;


import com.didichuxing.datachannel.arius.admin.client.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;

/**
 * Created by linyunan on 2021-06-15
 */
@Component
public class AppLogicTemplateAuthManagerImpl implements AppLogicTemplateAuthManager {

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Override
    public Result updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {
        if (AriusObjUtils.isNull(authDTO)) {
            return Result.buildFail("更新权限信息不存在");
        }

        if (AriusObjUtils.isNull(authDTO.getType())) {
            return Result.buildFail("更新权限类型不存在");
        }

        List<Integer> appTemplateAuthCodes = AppTemplateAuthEnum.listAppTemplateAuthCodes();
        if (!appTemplateAuthCodes.contains(authDTO.getType())) {
            return Result.buildFail("更新权限类型不支持");
        }

        AppTemplateAuth appTemplateAuth = appLogicTemplateAuthService
            .getTemplateAuthByLogicTemplateIdAndAppId(authDTO.getTemplateId(), authDTO.getAppId());

        if (AriusObjUtils.isNull(appTemplateAuth)) {
            return Result.buildFail("权限信息不存在");
        }

        if (AriusObjUtils.isNull(appTemplateAuth.getType())) {
            return Result.buildFail("权限信息不存在");
        }

        if (authDTO.getType().equals(appTemplateAuth.getType())) {
            return Result.buildSucc();
        }

		appTemplateAuth.setType(authDTO.getType());
        return appLogicTemplateAuthService
            .updateTemplateAuth(ConvertUtil.obj2Obj(appTemplateAuth, AppTemplateAuthDTO.class), operator);
    }
}
