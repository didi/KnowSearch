package com.didichuxing.datachannel.arius.admin.persistence.config;

import java.util.Properties;

import com.didichuxing.datachannel.arius.admin.persistence.SqlMonitorInterceptor;
import com.didichuxing.datachannel.arius.admin.persistence.page.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 
 * @author d06679
 */
@Component
public class InterceptorBeansConfig {

    @Bean
    public Interceptor sqlMonitorInterceptor() {
        return new SqlMonitorInterceptor();
    }

    @Bean
    public Interceptor pageHelper() {
        PageHelper pageHelper = new PageHelper();

        Properties properties = new Properties();
        properties.setProperty("dialect", "mysql");
        properties.setProperty("defaultPageSize", "20");
        properties.setProperty("maxPageSize", "10000");
        properties.setProperty("offsetAsPageNum", "true");
        properties.setProperty("rowBoundsWithCount", "true");
        properties.setProperty("pageSizeZero", "true");
        properties.setProperty("reasonable", "false");
        properties.setProperty("params",
            "pageNum=start;pageSize=limit;pageSizeZero=zero;reasonable=heli;count=contsql");

        pageHelper.setProperties(properties);

        return pageHelper;
    }

}
