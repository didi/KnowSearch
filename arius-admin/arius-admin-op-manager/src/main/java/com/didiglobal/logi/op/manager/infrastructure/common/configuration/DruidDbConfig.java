package com.didiglobal.logi.op.manager.infrastructure.common.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author d06679
 */
@Configuration
@MapperScan(value = "com.didiglobal.logi.op.manager.infrastructure.db.mapper",
        sqlSessionFactoryRef = "opSqlSessionFactory")
public class DruidDbConfig {

    @Bean("opDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean
    public GlobalConfig globalConfigByArius() {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setBanner(false);
        GlobalConfig.DbConfig dbConfig = new GlobalConfig.DbConfig();
        dbConfig.setIdType(IdType.AUTO);
        globalConfig.setDbConfig(dbConfig);
        return globalConfig;
    }

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MARIADB));
        return interceptor;
    }

    /**
     * 配置SqlSessionFactory.
     *
     * @param dataSource dataSource
     * @return SqlSessionFactory
     * @throws Exception Exception
     */
    @Bean("opSqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("opDataSource") DataSource dataSource) throws Exception {
        //将SqlSessionFactoryBean 替换为 MybatisSqlSessionFactoryBean， 否则mybatis-plus 提示 Invalid bound statement (not found)
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mybatis/*.xml"));
        bean.setGlobalConfig(globalConfigByArius());
        MybatisConfiguration mc = new MybatisConfiguration();
        //查看打印sql日志
        //org.apache.ibatis.logging.stdout.StdOutImpl.class 只能打印到控制台
        //org.apache.ibatis.logging.slf4j.Slf4jImpl.class 打印到具体的文件中
        mc.setLogImpl(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
        bean.setConfiguration(mc);
        //添加分页插件，不加这个，分页不生效
        bean.setPlugins(paginationInterceptor());
        // 设置mybatis的xml所在位置
        return bean.getObject();
    }

    @Bean({"opSqlSessionTemplate"})
    @Primary
    public SqlSessionTemplate primarySqlSessionTemplate(@Qualifier("opSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}