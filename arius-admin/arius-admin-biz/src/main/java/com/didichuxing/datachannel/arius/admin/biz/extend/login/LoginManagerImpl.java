package com.didichuxing.datachannel.arius.admin.biz.extend.login;

import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.client.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.component.RSATool;
import com.didichuxing.datachannel.arius.admin.common.constant.LoginConstant;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserRoleEnum;
import com.didichuxing.datachannel.arius.admin.common.constant.arius.AriusUserStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.login.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author linyunan
 * @date 2021-04-28
 */
@Component
public class LoginManagerImpl implements  LoginManager{

    @Autowired
    private LoginService                       loginService;

    @Autowired
    private AriusUserInfoService               ariusUserInfoService;

    @Autowired
    private RSATool                            rsaTool;

    private static final int                   DEFAULT_APPID = 1;

    @Override
    public Result<Boolean> loginAuthenticateAndGetUserInfo(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  LoginDTO loginDTO) {
        //1. 校验
        Result<Boolean> loginResult = loginService.loginAuthenticate(request, response, loginDTO);
        if (loginResult.failed()) {
            return loginResult;
        }

        AriusUserInfo userInfo = ariusUserInfoService.getByDomainAccount(loginDTO.getDomainAccount());
        if (AriusObjUtils.isNull(userInfo)) {
            return Result.buildFail(String.format("请检查用户{%s}是否注册成功", loginDTO.getDomainAccount()));
        }

        //2. 初始化登录上下文
        initLoginContext(request, response, userInfo);

        return Result.buildSucc(true);
    }

    @Override
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        return loginService.logout(request, response);
    }

    @Override
    public Result<Long> register(AriusUserInfoDTO userInfoDTO, Integer appId) {
        init(userInfoDTO);

        if (AriusObjUtils.isNull(appId)) {
            appId = DEFAULT_APPID;
        }

        Result<String> decryptResult = rsaTool.decrypt(userInfoDTO.getPassword());
        if (decryptResult.failed()) {
            return Result.buildFail(decryptResult.getMessage());
        }

        userInfoDTO.setPassword(decryptResult.getData());

        return loginService.register(userInfoDTO, appId);
    }

    private void init(AriusUserInfoDTO userInfoDTO) {
        if (AriusObjUtils.isNull(userInfoDTO.getEmail())) {
            userInfoDTO.setEmail("");
        }

        if (AriusObjUtils.isNull(userInfoDTO.getMobile())) {
            userInfoDTO.setMobile("");
        }

        if (AriusObjUtils.isNull(userInfoDTO.getName())) {
            userInfoDTO.setName(userInfoDTO.getDomainAccount());
        }

        userInfoDTO.setStatus(AriusUserStatusEnum.NORMAL.getCode());
        userInfoDTO.setRole(AriusUserRoleEnum.OP.getRole());
    }

    private void initLoginContext(HttpServletRequest request, HttpServletResponse response, AriusUserInfo userInfo) {
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(LoginConstant.COOKIE_OR_SESSION_MAX_AGE_UNIT_MS);
        session.setAttribute(LoginConstant.SESSION_DOMAIN_ACCOUNT, userInfo.getDomainAccount());

        Cookie cookieDomainAccount = new Cookie(LoginConstant.COOKIE_DOMAIN_ACCOUNT, userInfo.getDomainAccount());

        cookieDomainAccount.setMaxAge(LoginConstant.COOKIE_OR_SESSION_MAX_AGE_UNIT_MS);
        cookieDomainAccount.setPath("/");

        response.addCookie(cookieDomainAccount);
    }
}
