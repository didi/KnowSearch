package com.didichuxing.datachannel.arius.admin.persistence;

/**
 * @author jinbinbin
 * @version $Id: SqlMonitorInterceptor.java, v 0.1 2017年12月28日 20:47 jinbinbin Exp $
 */

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author jinbinbin
 * @version $Id: SqlMonitorInterceptor.java, v 0.1 2017年04月18日 19:35 jinbinbin Exp $
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }),
        @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class,
                CacheKey.class, BoundSql.class }) })
public class SqlMonitorInterceptor implements Interceptor {

    private static final ILog SQL_STAT_LOGGER = LogFactory.getLog("mysqlStatLogger");
    private boolean           showSql         = true;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!showSql) {
            return invocation.proceed();
        }

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        if (mappedStatement == null) {
            return invocation.proceed();
        }

        String sqlId = mappedStatement.getId();
        Object returnValue = null;
        int resultCode = 0;
        long start = System.currentTimeMillis();
        try {
            returnValue = invocation.proceed();
        } catch (Exception e) {
            resultCode = 1;
            throw e;
        } finally {
            long end = System.currentTimeMillis();
            long time = end - start;
            StringBuilder sb = new StringBuilder();
            sb.append("sqlId=").append(sqlId).append(",");
            sb.append("resultCode=").append(resultCode).append(",");
            sb.append("timeCost=").append(time);
            SQL_STAT_LOGGER.info(sb.toString());
        }
        return returnValue;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        if (properties.containsKey("show_sql")) {
            String value = properties.getProperty("show_sql");
            if (Boolean.TRUE.toString().equals(value)) {
                this.showSql = true;
            }
        }
    }
}
