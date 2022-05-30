package com.didichuxing.datachannel.arius.admin.core.service.extend.login.impl;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.HEALTH;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL_USER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART_SSO;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_WHITE_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.LoginConstant.COOKIE_OR_SESSION_MAX_AGE_UNIT_MS;
import static com.didichuxing.datachannel.arius.admin.common.constant.LoginConstant.REDIRECT_CODE;

import com.didichuxing.datachannel.arius.admin.common.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.account.LoginDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.dto.user.AriusUserInfoDTO;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParams;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.arius.AriusUserInfo;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.login.Login;
import com.didichuxing.datachannel.arius.admin.common.constant.LoginConstant;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.ConvertUtil;
import com.didichuxing.datachannel.arius.admin.core.component.HandleFactory;
import com.didichuxing.datachannel.arius.admin.core.service.app.AppUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusUserInfoService;
import com.didichuxing.datachannel.arius.admin.core.service.extend.login.LoginService;
import com.didichuxing.datachannel.arius.admin.remote.protocol.LoginProtocolHandle;
import com.didichuxing.datachannel.arius.admin.remote.protocol.content.LoginProtocolTypeEnum;
import com.didichuxing.datachannel.arius.admin.remote.storage.content.FileStorageTypeEnum;
import com.didiglobal.logi.security.util.HttpRequestUtil;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * @author linyunan
 * @date 2021-04-20
 */
@Service("oldLoginService")
@NoArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final Logger  LOGGER                  = LoggerFactory.getLogger(LoginServiceImpl.class);

    @Autowired
    private AriusUserInfoService ariusUserInfoService;

    @Autowired
    private AppUserInfoService   appUserInfoService;

    @Autowired
    private HandleFactory        handleFactory;

    @Value("${extend.loginProtocol}")
    private String loginProtocolType;

    private static final String  LOGIN_CHECK_COOKIE_NAME = "domainAccount";

    @PostConstruct
    public void loginProtocolTypeCheck() {
        LoginProtocolTypeEnum loginProtocolTypeEnum = LoginProtocolTypeEnum.valueOfType(loginProtocolType);
        if (loginProtocolTypeEnum.getCode().equals(FileStorageTypeEnum.UNKNOWN.getCode())) {
            LOGGER.info("class=LoginServiceImpl||method=loginProtocolTypeCheck||loginProtocolType={}", loginProtocolTypeEnum);
        }
    }

    @Override
    public Result<Boolean> loginAuthenticate(HttpServletRequest request, HttpServletResponse response, LoginDTO loginDTO) {
        //1. 登录校验
        Result<String> loginProtocolTypeRet = getLoginProtocolType();
        if (loginProtocolTypeRet.failed()) {
            return Result.buildFail(loginProtocolTypeRet.getMessage());
        }

        LOGGER.info("class=LoginServiceImpl||method=loginAuthenticate||ProtocolType={}", loginProtocolType);

        LoginProtocolHandle loginProtocolHandle = (LoginProtocolHandle) handleFactory
            .getByHandlerNamePer(loginProtocolType);

        Result<Void> checkProtocolResult = loginProtocolHandle.doAuthentication(ConvertUtil.obj2Obj(loginDTO, Login.class));
        if (checkProtocolResult.failed()) {
            return Result.buildFrom(checkProtocolResult);
        }

        //2. 同步校验信息到本地
        ariusUserInfoService.syncUserInfoToDbFromLoginProtocol(loginDTO, loginProtocolType);

        return Result.buildSucc();
    }

    @Override
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        response.setStatus(REDIRECT_CODE);
        return Result.buildSucc();
    }

    @Override
    public boolean interceptorCheck(HttpServletRequest request, HttpServletResponse response,
                                    String classRequestMappingValues) {
        if (AriusObjUtils.isBlack(classRequestMappingValues)) {
            LOGGER.error("class=LoginServiceImpl||method=interceptorCheck||msg=uri illegal||uri={}",
                request.getRequestURI());
            return Boolean.FALSE;
        }

        // 白名单接口
        if (HEALTH.equals(classRequestMappingValues) || V3_THIRD_PART_SSO.equals(classRequestMappingValues)
            || classRequestMappingValues.contains(V2_THIRD_PART) || classRequestMappingValues.contains(V3_THIRD_PART)
            || V3_NORMAL_USER.equals(classRequestMappingValues) || classRequestMappingValues.contains(V3_WHITE_PART)) {
            return Boolean.TRUE;
        }

        // 登录权限检查
        if (!hasLoginValid(request)) {
            logout(request, response);
            return Boolean.FALSE;
        }

        String operator = HttpRequestUtil.getOperator(request);
        Integer appId = HttpRequestUtil.getProjectId(request);
        // 登陆成功后, 设置session属性, 后续操作人从该session中获取
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(COOKIE_OR_SESSION_MAX_AGE_UNIT_MS);
        session.setAttribute(HttpRequestUtil.USER, operator);

        //添加到threadLocal 里面
        GlobalParams.CURRENT_USER.set(operator);
        GlobalParams.CURRENT_PROJECT_ID.set(appId);

        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> register(AriusUserInfoDTO userInfoDTO, Integer appId) {
        if (AriusObjUtils.isNull(appId)) {
            return Result.buildParamIllegal("项目Id为空");
        }

        Long userId = -1L;
        try {
            Result<Long> ret = ariusUserInfoService.save(userInfoDTO);
            if (ret.success()) {
                userId = ret.getData();
            }

            if (userId < 0) {
                return Result.buildFail();
            }

            appUserInfoService.recordAppidAndUser(appId, userInfoDTO.getDomainAccount());

        } catch (Exception e) {
            LOGGER.error("class=LoginServiceImpl||method=register||msg={}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return Result.buildFail(e.getMessage());
        }

        return Result.buildSucc(userId);
    }

    /*************************************private****************************************************/
    private Result<String> getLoginProtocolType() {
        if (AriusObjUtils.isNull(loginProtocolType)) {
            return Result.build(Boolean.TRUE, LoginProtocolTypeEnum.DEFAULT.getType());
        }

        if (LoginProtocolTypeEnum.valueOfType(loginProtocolType).getCode() == -1) {
            return Result.buildFail(String.format("获取 %s 类型出错, 检查枚举类型是否定义出错", loginProtocolType));
        }

        return Result.buildSucc();
    }

    private boolean hasLoginValid(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Object username = request.getSession().getAttribute(LoginConstant.SESSION_DOMAIN_ACCOUNT);

        if(AriusObjUtils.isNull(username)) {
            return false;
        }
        if (AriusObjUtils.isNull(cookies)) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (LOGIN_CHECK_COOKIE_NAME.equals(cookie.getName())) {
                AriusUserInfo ariusUserInfo = ariusUserInfoService.getByDomainAccount(cookie.getValue());
                if (!AriusObjUtils.isNull(ariusUserInfo)) {
                    return true;
                }
            }
        }

        return false;
    }
}