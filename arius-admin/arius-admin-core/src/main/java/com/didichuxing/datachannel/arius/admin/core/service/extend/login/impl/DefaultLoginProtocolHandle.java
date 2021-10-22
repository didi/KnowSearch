package com.didichuxing.datachannel.arius.admin.core.service.extend.login.impl;

import com.didichuxing.datachannel.arius.admin.common.component.RSATool;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.Login;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.remote.protocol.LoginProtocolHandle;

/**
 * @author linyunan
 * @date 2021-04-29
 */
@Component
public class DefaultLoginProtocolHandle implements LoginProtocolHandle {

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private RSATool              rsaTool;

    @Override
    public Result doAuthentication(Login login) {
        AriusUserInfo userInfo = ariusUserInfoService.getByDomainAccount(login.getDomainAccount());
        if (AriusObjUtils.isNull(userInfo)) {
            return Result.buildFail("登录失败, 请确认用户是否注册");
        }

        Result<String> decryptResult = rsaTool.decrypt(login.getPassword());
        if (decryptResult.failed()) {
            return decryptResult;
        }

        if (!Objects.equals(userInfo.getPassword(), decryptResult.getData())) {
            return Result.buildFail("登录失败, 请确认密码是否正确");
        }

        return Result.buildSucc();
    }

    @Override
    public AriusUserInfo getUserInfoFromLoginProtocol(Login login) {
        return ariusUserInfoService.getByDomainAccount(login.getDomainAccount());
    }
}
