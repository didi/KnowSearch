package com.didichuxing.datachannel.arius.admin.rest.interceptor;

import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.HEALTH;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.SWAGGER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V2_THIRD_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_NORMAL_USER;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_THIRD_PART_SSO;
import static com.didichuxing.datachannel.arius.admin.common.constant.ApiVersion.V3_WHITE_PART;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.ARIUS_COMMON_GROUP;
import static com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant.REQUEST_INTERCEPTOR_SWITCH_OPEN;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.GlobalParams;
import com.didichuxing.datachannel.arius.admin.common.util.AriusObjUtils;
import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.didiglobal.logi.security.service.LoginService;
import com.google.common.collect.Lists;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登陆拦截 && 权限校验
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private static final ILog      LOGGER = LogFactory.getLog(PermissionInterceptor.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    /**
     * 拦截预处理
     * @return boolean false:拦截, 不向下执行, true:放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (hasNoInterceptor(request)) {
            return true;
        }

        String classRequestMappingValue = null;
        try {
            classRequestMappingValue = getClassRequestMappingValue(handler);
        } catch (Exception e) {
            LOGGER.error(
                "class=PermissionInterceptor||method=preHandle||uri={}||msg=parse class request-mapping failed",
                request.getRequestURI(), e);
        }
        List<String> whiteMappingValues = Lists.newArrayList(HEALTH, V3_THIRD_PART_SSO, V2_THIRD_PART, V3_THIRD_PART,
                V3_NORMAL_USER, V3_WHITE_PART);
    
        return loginService.interceptorCheck(request, response, classRequestMappingValue,whiteMappingValues);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        GlobalParams.CURRENT_USER.remove();
        GlobalParams.CURRENT_PROJECT_ID.remove();
    }

    /**
     * 通过反射获取带有@RequestMapping的Controller
     * @param handler 请求处理器
     * @return @RequestMapping的value
     */
    private String getClassRequestMappingValue(Object handler) {
        RequestMapping requestMapping;
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            requestMapping = hm.getMethod().getDeclaringClass().getAnnotation(RequestMapping.class);
        } else if (handler instanceof org.springframework.web.servlet.mvc.Controller) {
            org.springframework.web.servlet.mvc.Controller hm = (org.springframework.web.servlet.mvc.Controller) handler;
            Class<? extends org.springframework.web.servlet.mvc.Controller> hmClass = hm.getClass();
            requestMapping = hmClass.getAnnotation(RequestMapping.class);
        } else {
            requestMapping = handler.getClass().getAnnotation(RequestMapping.class);
        }
        if (AriusObjUtils.isNull(requestMapping) || requestMapping.value().length == 0) {
            return null;
        }
        return requestMapping.value()[0];
    }

    /**
     * 是否需要拦截
     */
    private boolean hasNoInterceptor(HttpServletRequest request) {

        if (EnvUtil.isDev()) {
            return Boolean.TRUE;
        }

        if (request.getServletPath().contains(SWAGGER)) {
            return Boolean.TRUE;
        }

        boolean interceptorSwitch = ariusConfigInfoService.booleanSetting(ARIUS_COMMON_GROUP,
            REQUEST_INTERCEPTOR_SWITCH_OPEN, Boolean.TRUE);
        if (!interceptorSwitch) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}