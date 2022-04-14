package com.didichuxing.datachannel.arius.admin.remote.protocol.ldap;

import com.didichuxing.datachannel.arius.admin.remote.protocol.LoginProtocolHandle;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.Login;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author linyunan
 * @date 2021-04-29
 */
@Component
public class LdapLoginProtocolHandle implements LoginProtocolHandle {
    private static final ILog LOGGER = LogFactory.getLog(LdapLoginProtocolHandle.class);

    @Override
    public Result doAuthentication(Login login) {
        return Result.buildSucc();
    }

    @Override
    public AriusUserInfo getUserInfoFromLoginProtocol(Login login) {
        AriusUserInfo userInfo = null;
        try {
            userInfo = new AriusUserInfo();
        } catch (Exception e) {
            LOGGER.error("class=LdapProtocolService||method=getUserInfoFromLoginProtocol||msg={}", e.getMessage());
        }
        return userInfo;
    }
}
