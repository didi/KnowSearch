package com.didiglobal.logi.op.manager.infrastructure.common.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.didiglobal.logi.op.manager.infrastructure.common.properties.OpManagerProper;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author d06679
 */
@EnableTransactionManagement
@Configuration("opManagerDataSourceConfig")
@MapperScan(value = "com.didiglobal.logi.op.manager.infrastructure.db.mapper",sqlSessionFactoryRef = "opSqlSessionFactory")
public class OpManagerDruidDbConfig {

    @Bean("opDataSource")
    public DataSource dataSource(OpManagerProper proper) throws SQLException {
        final DruidDataSource dataSource = new DruidDataSource();
        dataSource.setName(proper.getDataSourceName());
        dataSource.setUsername(proper.getUsername());
        dataSource.setPassword(proper.getPassword());
        dataSource.setDriverClassName(proper.getDriverClassName());
        dataSource.setUrl(proper.getUrl());
        dataSource.setInitialSize(proper.getInitialSize());
        dataSource.setValidationQueryTimeout(proper.getValidationQueryTimeout());
        dataSource.setTransactionQueryTimeout(proper.getTransactionQueryTimeout());
        dataSource.setMinIdle(proper.getMinIdle());
        dataSource.setKeepAlive(proper.getKeepAlive());
        dataSource.setMaxActive(proper.getMaxActive());
        dataSource.setTimeBetweenEvictionRunsMillis(proper.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(proper.getMinEvictableIdleTimeMillis());
        dataSource.setDefaultAutoCommit(proper.getDefaultAutoCommit());
        dataSource.setValidationQuery(proper.getValidationQuery());
        dataSource.setTestWhileIdle(proper.getTestWhileIdle());
        dataSource.setTestOnReturn(proper.getTestOnReturn());
        dataSource.setTestOnBorrow(proper.getTestOnBorrow());
        dataSource.setLogAbandoned(proper.getLogAbandoned());
        dataSource.setPoolPreparedStatements(proper.getPoolPreparedStatements());
        dataSource.setMaxOpenPreparedStatements(proper.getMaxOpenPreparedStatements());
        dataSource.setFilters(proper.getFilters());
        return dataSource;
    }

    @Bean("globalConfigByOpManager")
    public GlobalConfig globalConfigByOpManager() {
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
    @Bean("paginationInterceptorByOpManager")
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
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("opDataSource") DataSource dataSource) throws Exception {
        //将SqlSessionFactoryBean 替换为 MybatisSqlSessionFactoryBean， 否则mybatis-plus 提示 Invalid bound statement (not found)
        
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver()
            
                .getResources("classpath:mybatis/op-manager/*.xml"));
        bean.setGlobalConfig(globalConfigByOpManager());
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
    public SqlSessionTemplate primarySqlSessionTemplate(@Qualifier("opSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }
}