package com.didichuxing.datachannel.arius.admin.remote.protocol;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.Login;
import com.didichuxing.datachannel.arius.admin.common.component.BaseHandle;

/**
 * @author linyunan
 * @date 2021-04-29
 * @deprecated 登录验证能力移至logicommon
 */
@Deprecated
public interface LoginProtocolHandle extends BaseHandle {

    /**
     * 协议认证
     */
    Result<Void> doAuthentication(Login login);

	/**
	 * 从协议中获取用户信息
	 */
	AriusUserInfo getUserInfoFromLoginProtocol(Login login);

}