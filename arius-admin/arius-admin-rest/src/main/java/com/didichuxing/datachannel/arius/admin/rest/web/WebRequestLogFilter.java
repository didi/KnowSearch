package com.didichuxing.datachannel.arius.admin.rest.web;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.didiglobal.knowframework.log.common.Constants;
import com.didiglobal.knowframework.log.common.TraceContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author jinbinbin
 * @version $Id: AccessLogFilter.java, v 0.1 2018年03月05日 22:01 jinbinbin Exp $
 */
public class WebRequestLogFilter implements Ordered, Filter {

    private static final ILog        LOGGER                            = LogFactory.getLog("accessLogger");
    private static final ILog        RESP_LOGGER                       = LogFactory.getLog("responseLogger");

    public static final String       RESPONSE_LOG_ENABLE               = "responseLogEnable";
    public static final String       EXCLUDE_URLS                      = "excludeAllUrls";
    public static final String       EXCLUDE_REQUEST_BODY_URLS         = "excludeReqBodyUrls";
    public static final String       EXCLUDE_RESPONSE_BODY_URLS        = "excludeRespBodyUrls";
    public static final String       EXCLUDE_RESPONSE_BODY_URLS_PREFER = "excludeRespBodyUrlsPrefer";

    private static final Set<String> ALL_EXCLUDE_URLS                  = new HashSet<>();
    private static final Set<String> REQ_BODY_EXCLUDE_URLS             = new HashSet<>();
    private static final Set<String> RESP_BODY_EXCLUDE_URLS            = new HashSet<>();
    private static final Set<String> RESP_BODY_EXCLUDE_URLS_PREFER     = new HashSet<>();

    private Boolean                  respLogEnable                     = Boolean.TRUE;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
                                                                                      ServletException {

        long begin = System.currentTimeMillis();

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        // spring 的ContentCachingRequestWrapper存在bug，无法读取request body
        TranslateHttpServletRequestWrapper wrapperRequest = new TranslateHttpServletRequestWrapper(request);
        ContentCachingResponseWrapper wrapperResponse = new ContentCachingResponseWrapper(response);

        String requestUrl = request.getRequestURI();
        try {
            initTrace(request);
            logRequest(wrapperRequest, requestUrl);
            chain.doFilter(wrapperRequest, wrapperResponse);
        } finally {
            try {
                logResponse(begin, wrapperResponse, requestUrl);
                wrapperResponse.copyBodyToResponse();
            } finally {
                LogFactory.removeFlag();
            }
        }
    }

    private void initTrace(HttpServletRequest request) {
        String traceid = request.getHeader(Constants.TRACE_ID);
        if (traceid == null) {
            traceid = request.getHeader(Constants.X_REQUEST_ID);
        }
        if (traceid == null) {
            traceid = request.getHeader(Constants.FLAG);
        }
        if (traceid == null) {
            traceid = LogFactory.getUniqueFlag();
        }

        String cspanId = request.getHeader(Constants.CSPAN_ID);
        if (cspanId == null) {
            cspanId = "-1";
        }
        TraceContext traceContext = new TraceContext(traceid, cspanId);
        LogFactory.setTrace(traceContext);
        LogFactory.setFlag(traceid);
    }

    private void logResponse(long begin, ContentCachingResponseWrapper wrapperResponse, String requestUrl) {
        if (!respLogEnable.booleanValue()) {
            return;
        }

        if (ALL_EXCLUDE_URLS.contains(requestUrl)) {
            return;
        }

        String responseBody;

        if (RESP_BODY_EXCLUDE_URLS.contains(requestUrl) || matchPrefer(RESP_BODY_EXCLUDE_URLS_PREFER, requestUrl)) {
            responseBody = "{\"exclude\":true}";
        } else {
            responseBody = getResponseBody(wrapperResponse);
        }
        RESP_LOGGER.info("class=WebRequestLogFilter||method=logResponse||response||responseBody={}||timeCost={}",
            responseBody, (System.currentTimeMillis() - begin));
    }

    private void logRequest(TranslateHttpServletRequestWrapper request, String requestUrl) {
        if (ALL_EXCLUDE_URLS.contains(requestUrl)) {
            return;
        }

        Map<String, String> headers = getHeaders(request);
        String requestBody;
        if (REQ_BODY_EXCLUDE_URLS.contains(requestUrl)) {
            requestBody = "{\"exclude\":true}";
        } else {
            requestBody = getRequestBody(request);
        }
        LOGGER.info(
            "class=WebRequestLogFilter||method=logRequest||request||url={}||method={}||remoteAddr={}||headers={}||urlParams={}||body={}",
            request.getRequestURI(), request.getMethod(), UrlHelper.getIpAddr(request), headers,
            request.getQueryString(), requestBody, StandardCharsets.UTF_8);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        initExcludeUrl(filterConfig, EXCLUDE_URLS, ALL_EXCLUDE_URLS);
        initExcludeUrl(filterConfig, EXCLUDE_REQUEST_BODY_URLS, REQ_BODY_EXCLUDE_URLS);
        initExcludeUrl(filterConfig, EXCLUDE_RESPONSE_BODY_URLS, RESP_BODY_EXCLUDE_URLS);
        initExcludeUrl(filterConfig, EXCLUDE_RESPONSE_BODY_URLS_PREFER, RESP_BODY_EXCLUDE_URLS_PREFER);
        if (filterConfig.getInitParameter(RESPONSE_LOG_ENABLE) != null) {
            respLogEnable = Boolean.valueOf(filterConfig.getInitParameter(RESPONSE_LOG_ENABLE));
        }
    }

    /**
     * 打印请求参数
     *
     * @param response
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response,
            ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String payload;
                try {
                    payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    payload = new String(buf, 0, buf.length, StandardCharsets.UTF_8);
                }
                return payload;
            }
        }
        return "";
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("refer", request.getHeader("referer"));
        params.put("agent", request.getHeader("User-Agent"));
        params.put(WebConstant.USER_HEADER_KEY, request.getHeader(WebConstant.USER_HEADER_KEY));

        return params;
    }

    /**
     * 打印请求参数
     *
     * @param request
     */
    private String getRequestBody(TranslateHttpServletRequestWrapper request) {
        try {
            String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
            return body.replace("\n", "");
        } catch (Exception e) {
            return "";
        }
    }

    private void initExcludeUrl(FilterConfig filterConfig, String excludeUrls, Set<String> urlContainers) {
        String allExcludeUrls = filterConfig.getInitParameter(excludeUrls);
        if (StringUtils.isNotBlank(allExcludeUrls)) {
            String[] pattern = allExcludeUrls.split(",");
            urlContainers.addAll(Arrays.asList(pattern));
        }
    }

    private boolean matchPrefer(Set<String> preferSet, String requestUrl) {

        if (CollectionUtils.isEmpty(preferSet)) {
            return false;
        }

        for (String prefer : preferSet) {
            if (requestUrl.startsWith(prefer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // doNothing
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 8;
    }
}
