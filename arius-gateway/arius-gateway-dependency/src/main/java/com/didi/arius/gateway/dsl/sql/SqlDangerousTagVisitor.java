package com.didi.arius.gateway.dsl.sql;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.didi.arius.gateway.dsl.bean.DangerousDslTagEnum;
import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2019/1/5 下午9:42
 * @Modified By
 *
 * 危害dsl查询语句标签遍历器
 */
public class SqlDangerousTagVisitor extends MySqlOutputVisitor {

    private static final ILog LOGGER = LogFactory.getLog(SqlDangerousTagVisitor.class);
    // 标签集合
    private Set<String> tags = Sets.newHashSet();
    // aggs次数
    private int aggsLevel = 0;

    private StringBuilder stringBuilder;

    public SqlDangerousTagVisitor(StringBuilder sb) {
        super(sb);
        this.stringBuilder = sb;
    }

    /**
     * group by 语法树
     *
     * @param groupBySQLExpr
     * @return
     */
    @Override
    public boolean visit(SQLSelectGroupByClause groupBySQLExpr) {
        this.aggsLevel += groupBySQLExpr.getItems().size();

        return super.visit(groupBySQLExpr);
    }

    /**
     * group by 语法树
     *
     * @param sqlAggregateExpr
     * @return
     */
    @Override
    public boolean visit(SQLAggregateExpr sqlAggregateExpr) {
        this.aggsLevel += sqlAggregateExpr.getArguments().size();

        if ("COUNT".equals(sqlAggregateExpr.getMethodName())) {
            if (sqlAggregateExpr.getOption() != null && sqlAggregateExpr.getOption() instanceof SQLAggregateOption) {
                if ("DISTINCT".equals(((SQLAggregateOption)(sqlAggregateExpr.getOption())).name())) {
                    this.tags.add(DangerousDslTagEnum.AGGS_CARDINALITY.getTag());
                }
            }
        }

        return super.visit(sqlAggregateExpr);
    }

    /**
     * where 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLBinaryOpExpr x) {

        // 判断是否为以下二元操作符，是否为like
        if (x.getOperator() == SQLBinaryOperator.Like
                ) {

            if (x.getRight() instanceof SQLCharExpr) {
                String value = x.getRight().toString();
                if (StringUtils.isNotBlank(value) && value.length() > 2) {
                    if (value.charAt(1) == '%' || value.charAt(1) == '*') {
                        this.tags.add(DangerousDslTagEnum.WITH_WILDCARD_PRE.getTag());
                    }
                }
            }

            return super.visit(x);
        }

        return super.visit(x);
    }

    /**
     * 判断是否有script、regexp 方法
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLMethodInvokeExpr x) {

        if ("script".equals(x.getMethodName())) {
            this.tags.add(DangerousDslTagEnum.WITH_SCRIPT.getTag());

        } else if ("regexp".equals(x.getMethodName())) {
            this.tags.add(DangerousDslTagEnum.WITH_REGEXP.getTag());
        }

        return super.visit(x);
    }

    /**
     * 获取危害标签
     */
    public Set<String> getDangerousTags() {

        // 查询语句超过5k
        if (this.stringBuilder.length() > 5 * 1024) {
            // LOGGER.error("sql length more than 5k {}", stringBuilder.toString());
            this.tags.add(DangerousDslTagEnum.DSL_LENGTH_TOO_LARGE.getTag());
        }
        // aggs 嵌套层数过深
        if (this.aggsLevel >= 3) {
            this.tags.add(DangerousDslTagEnum.AGGS_DEEP_NEST.getTag());
        }

        return tags;
    }

    /**
     * 获取sql
     *
     * @return
     */
    public String getSql() {
        return stringBuilder.toString();
    }

}
