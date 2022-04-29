package com.didichuxing.datachannel.arius.admin.biz.app.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.biz.app.AppLogicTemplateAuthManager;
import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.app.AppTemplateAuthDTO;
import com.didichuxing.datachannel.arius.admin.common.constant.app.AppTemplateAuthEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.app.AppTemplateAuth;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogic;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppLogicTemplateAuthService;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppService;

/**
 * Created by linyunan on 2021-06-15
 */
@Component
public class AppLogicTemplateAuthManagerImpl implements AppLogicTemplateAuthManager {

    @Autowired
    private AppLogicTemplateAuthService appLogicTemplateAuthService;

    @Autowired
    private AppService                  appService;

    @Override
    public List<AppTemplateAuth> getTemplateAuthListByTemplateListAndAppId(Integer appId,
                                                                           List<IndexTemplateLogic> indexTemplateLogicList) {
        List<AppTemplateAuth> appTemplateAuthList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(indexTemplateLogicList)) {
            return appTemplateAuthList;
        }

        if (!appService.isAppExists(appId)) {
            appTemplateAuthList = indexTemplateLogicList.stream()
                .map(r -> appLogicTemplateAuthService.buildTemplateAuth(r, AppTemplateAuthEnum.NO_PERMISSION))
                .collect(Collectors.toList());
            return appTemplateAuthList;
        }

        if (appService.isSuperApp(appId)) {
            appTemplateAuthList = indexTemplateLogicList.stream()
                .map(r -> appLogicTemplateAuthService.buildTemplateAuth(r, AppTemplateAuthEnum.OWN))
                .collect(Collectors.toList());
            return appTemplateAuthList;
        }

        List<AppTemplateAuth> appActiveTemplateRWAuths = appLogicTemplateAuthService.getAppActiveTemplateRWAndRAuths(appId);
        Map<Integer, AppTemplateAuth> templateId2AppTemplateAuthMap = ConvertUtil.list2Map(appActiveTemplateRWAuths,
            AppTemplateAuth::getTemplateId);

        for (IndexTemplateLogic indexTemplateLogic : indexTemplateLogicList) {
            Integer templateLogicId = indexTemplateLogic.getId();
            if (null != appId && appId.equals(indexTemplateLogic.getAppId())) {
                appTemplateAuthList.add(
                    appLogicTemplateAuthService.buildTemplateAuth(indexTemplateLogic, AppTemplateAuthEnum.OWN));
                continue;
            }

            if (null != templateLogicId && templateId2AppTemplateAuthMap.containsKey(templateLogicId)) {
                appTemplateAuthList.add(templateId2AppTemplateAuthMap.get(templateLogicId));
                continue;
            }

            appTemplateAuthList.add(appLogicTemplateAuthService.buildTemplateAuth(indexTemplateLogic,
                AppTemplateAuthEnum.NO_PERMISSION));
        }

        return appTemplateAuthList;
    }

    @Override
    public Result<Void> updateTemplateAuth(AppTemplateAuthDTO authDTO, String operator) {
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
            .getTemplateRWAuthByLogicTemplateIdAndAppId(authDTO.getTemplateId(), authDTO.getAppId());

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
        appTemplateAuth.setResponsible(authDTO.getResponsible());
        return appLogicTemplateAuthService
            .updateTemplateAuth(ConvertUtil.obj2Obj(appTemplateAuth, AppTemplateAuthDTO.class), operator);
    }
}
