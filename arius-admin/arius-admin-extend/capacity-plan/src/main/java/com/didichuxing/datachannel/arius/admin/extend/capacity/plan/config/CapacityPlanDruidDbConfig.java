package com.didichuxing.datachannel.arius.admin.extend.capacity.plan.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.alibaba.druid.pool.DruidDataSource;

/**
 *
 * @author d06679
 */
@Configuration
@MapperScan(value = "com.didichuxing.datachannel.arius.admin.extend.capacity.plan.dao", sqlSessionFactoryRef = "capacityPlanSqlSessionFactory")
public class CapacityPlanDruidDbConfig {

    @Bean("capacityPlanDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean("capacityPlanSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("capacityPlanDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:mybatis/*.xml"));
        return bean.getObject();
    }
}