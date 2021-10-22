package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * http请求操作类
 * Created by d06679 on 2019/2/26.
 */
public class HttpRequestUtils {

    private static final ILog  LOGGER    = LogFactory.getLog(HttpRequestUtils.class);

    public static final String USER      = "X-SSO-USER";

    public static final String PASSWORD  = "password";

    public static final String APPID     = "X-ARIUS-APP-ID";

    public static String getFromHeader(HttpServletRequest request, String key, String defaultValue) {
        Object value = request.getHeader(key);
        return value == null ? defaultValue : (String) value;
    }

    public static String getOperator(HttpServletRequest request) {
        return getOperatorFromHeader(request);
    }

    public static String getPasswordFromCookies(HttpServletRequest request) {
        List<String> passwords = Lists.newArrayList();
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (PASSWORD.equals(cookie.getName())) {
                passwords.add(cookie.getValue());
            }
        }

        if (ValidateUtils.isEmptyList(passwords)) {
            throw new OperateForbiddenException("请在请求cookie中携带用户密码:password");
        }
        return passwords.get(0);
    }

    public static String getOperatorFromHeader(HttpServletRequest request) {
        Object value = request.getHeader(USER);
        if (value == null) {
            throw new OperateForbiddenException("请携带操作人信息,HTTP_HEADER_KEY:X-SSO-USER");
        }
        return String.valueOf(value);
    }

    public static Integer getAppId(HttpServletRequest request, int defaultAppid) {
        String appidStr = request.getHeader(APPID);

        if (StringUtils.isBlank(appidStr)) {
            return defaultAppid;
        }

        return Integer.valueOf(appidStr);
    }

    public static Integer getAppId(HttpServletRequest request) {
        String appidStr = request.getHeader(APPID);

        if (StringUtils.isBlank(appidStr)) {
            return null;
        }

        try {
            return Integer.valueOf(appidStr);
        } catch (Exception e) {
            LOGGER.warn("class=HttpRequestUtils||method=getAppId||errMsg={}||appidStr={}", e.getMessage(), appidStr, e);
        }

        return null;
    }

}
