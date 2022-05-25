package com.didichuxing.datachannel.arius.admin.common.util;

import com.didichuxing.datachannel.arius.admin.common.exception.OperateForbiddenException;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * http请求操作类
 * @author Created by d06679 on 2019/2/26.
 */
public class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    private static final ILog   LOGGER         = LogFactory.getLog(HttpRequestUtils.class);

    public static final String  USER           = "X-SSO-USER";

    

    public static final String PROJECT_ID = "X-ARIUS-PROJECT-ID";

    public static String getFromHeader(HttpServletRequest request, String key, String defaultValue) {
        Object value = request.getHeader(key);
        return value == null ? defaultValue : (String) value;
    }

    public static String getOperator(HttpServletRequest request) {
        return getOperatorFromHeader(request);
    }

    

    public static String getOperatorFromHeader(HttpServletRequest request) {
        Object value = request.getHeader(USER);
        if (value == null) {
            throw new OperateForbiddenException("请携带操作人信息,HTTP_HEADER_KEY:X-SSO-USER");
        }
        return String.valueOf(value);
    }

    public static Integer getProjectId(HttpServletRequest request, int defaultProjectId) {
        String appidStr = request.getHeader(PROJECT_ID);

        if (StringUtils.isBlank(appidStr)) {
            return defaultProjectId;
        }

        return Integer.valueOf(appidStr);
    }

    public static Integer getProjectId(HttpServletRequest request) {
        String projectIdStr = request.getHeader(PROJECT_ID);
        
        try {
            return Integer.valueOf(projectIdStr);
        } catch (Exception e) {
            LOGGER.warn("class=HttpRequestUtils||method=getProjectId||errMsg={}||projectIdStr={}", e.getMessage(), projectIdStr, e);
        }

        return null;
    }

}