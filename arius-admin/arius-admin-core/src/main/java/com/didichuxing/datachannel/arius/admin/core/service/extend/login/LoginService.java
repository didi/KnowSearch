package com.didichuxing.datachannel.arius.admin.core.service.extend.login;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.remote.protocol.content.LoginProtocolTypeEnum;

/**
 * @author linyunan
 * @date 2021-04-20
 */
@Deprecated
public interface LoginService {

    /**
     * @see LoginProtocolTypeEnum
     */
    Result<Boolean> loginAuthenticate(HttpServletRequest request,
                            HttpServletResponse response,
                            LoginDTO loginDTO);

    /**
     * 登出
     */
    Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 检查登陆
     * @param classRequestMappingValue 带有@RequestMapping的Controller的value
     */
    boolean interceptorCheck(HttpServletRequest request, HttpServletResponse response,
                             String classRequestMappingValue) throws IOException;

    /**
     * 注册
     * @param userInfoDTO  用户信息
     * @param appId        用户所绑定项目
     */
    Result<Long> register(AriusUserInfoDTO userInfoDTO, Integer appId);
}