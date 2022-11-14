package com.didichuxing.datachannel.arius.admin.rest;

import com.didichuxing.datachannel.arius.admin.common.util.EnvUtil;
import com.didichuxing.datachannel.arius.admin.rest.swagger.SwaggerConfiguration;
import com.didichuxing.datachannel.arius.admin.rest.web.WebConstant;
import com.didichuxing.datachannel.arius.admin.rest.web.WebRequestLogFilter;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 启动类
 */
@NoArgsConstructor
@EnableScheduling
@EnableCaching
@ServletComponentScan
@SpringBootApplication(scanBasePackages = { "com.didichuxing.datachannel.arius.admin" })
public class AriusAdminApplication {

    private static final ILog LOGGER           = LogFactory.getLog(AriusAdminApplication.class);

    static final String[]     ALL_EXCLUDE_URLS = new String[] { "/admin/api/swagger-ui.html",
                                                                "/admin/api/swagger-resources/configuration/ui",
                                                                "/admin/api/webjars/springfox-swagger-ui/favicon-32x32.png",
                                                                "/admin/api/swagger-resources/configuration/security",
                                                                "/admin/api/swagger-resources",
                                                                "/admin/api/v1/client-docs", "/admin/api/",
                                                                "/admin/api/v2/client-docs", "/admin/api/csrf",
                                                                "/admin/api/druid/login.html",
                                                                "/admin/api/druid/css/bootstrap.min.css",
                                                                "/admin/api/druid/js/bootstrap.min.js",
                                                                "/admin/api/druid/js/doT.js",
                                                                "/admin/api/druid/js/jquery.min.js",
                                                                "/admin/api/druid/index.html",
                                                                "/admin/api/druid/js/client.js",
                                                                "/admin/api/druid/css/style.css",
                                                                "/admin/api/druid/js/lang.js",
                                                                "/admin/api/druid/header.html",
                                                                "/admin/api/druid/basic.json",
                                                                "/admin/api/druid/datasource.html",
                                                                "/admin/api/druid/datasource.json",
                                                                "/admin/api/druid/sql.html",
                                                                "/admin/api/druid/sql.json",
                                                                "/admin/api/druid/wall.html",
                                                                "/admin/api/druid/wall.json",
                                                                "/admin/api/druid/webapp.html",
                                                                "/admin/api/druid/js/doT.js",
                                                                "/admin/api/druid/weburi.html",
                                                                "/admin/api/druid/webapp.json",
                                                                "/admin/api/druid/weburi.json",
                                                                "/admin/api/druid/websession.html",
                                                                "/admin/api/druid/websession.json",
                                                                "/admin/api/druid/spring.html",
                                                                "/admin/api/druid/spring.json",
                                                                "/admin/api/druid/client.html" };

    @Value(value = "${admin.port.web}")
    private int               port;

    @Value(value = "${admin.contextPath}")
    private String            contextPath;

    public static void main(String[] args) {
        try {
            LOGGER.info("AriusAdminApplication init starting!");

            EnvUtil.setLoadActiveProfiles(args);
            SwaggerConfiguration.initEnv(args);
            ApplicationContext ctx = SpringApplication.run(AriusAdminApplication.class, args);
            for (String profile : ctx.getEnvironment().getActiveProfiles()) {
                LOGGER.info("class=AriusAdminApplication||method=main||Spring Boot use profile: {}", profile);
            }
            LOGGER.info("class=AriusAdminApplication||method=main||AriusAdminApplication started");
        } catch (Exception e) {
            LOGGER.error("class=AriusAdminApplication||method=main||AriusAdminApplication start failed", e);
        }
    }

    @Bean
    public ConfigurableServletWebServerFactory configurableServletWebServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.setPort(port);
        factory.setContextPath(contextPath);
        return factory;
    }

    @Bean
    public FilterRegistrationBean<Filter> traceRegistrationBean() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.addInitParameter(WebRequestLogFilter.EXCLUDE_URLS, StringUtils.join(ALL_EXCLUDE_URLS, ","));
        registrationBean.setOrder(5);
        Filter traceFilter = new Filter() {

            @Override
            public void init(FilterConfig filterConfig) {
                // Do nothing
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
                ((HttpServletResponse) response).setHeader(WebConstant.X_REQUEST_ID, LogFactory.getFlag());
                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {
                // Do nothing
            }
        };
        registrationBean.setFilter(traceFilter);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<WebRequestLogFilter> logFilterRegistrationBean() {
        FilterRegistrationBean<WebRequestLogFilter> registrationBean = new FilterRegistrationBean<>();
        WebRequestLogFilter requestLogFilter = new WebRequestLogFilter();

        registrationBean.addInitParameter(WebRequestLogFilter.EXCLUDE_URLS, StringUtils.join(ALL_EXCLUDE_URLS, ","));
        registrationBean.addInitParameter(WebRequestLogFilter.RESPONSE_LOG_ENABLE, "false");
        registrationBean.setFilter(requestLogFilter);
        registrationBean.setOrder(4);
        return registrationBean;
    }

}

