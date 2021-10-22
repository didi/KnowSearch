package com.didi.arius.gateway.dsl.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.didi.arius.gateway.dsl.constant.SqlItemType;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/8/16 下午3:09
 * @Modified By
 */
public class EsExportParameterVisitor extends MySqlASTVisitorAdapter {

    /**
     *  表名集合
     */
    private Set<String> tableNameSet = new TreeSet<>();
    /**
     *  select 字段名集合
     */
    private Set<String> selectFieldNameSet = new TreeSet<>();
    /**
     *  where 字段名集合
     */
    private Set<String> whereFieldNameSet = new TreeSet<>();
    /**
     *  group by 字段名集合
     */
    private Set<String> groupByFieldNameSet = new TreeSet<>();
    /**
     *  sort by 字段名集合
     */
    private Set<String> sortByFieldNameSet = new TreeSet<>();
    /**
     * sql语句类型
     */
    private SqlItemType sqlItemType = SqlItemType.NONE;

    public EsExportParameterVisitor() {

    }

    /**
     * select * 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLAllColumnExpr x) {

        setAddItem(this.selectFieldNameSet, x.toString());

        return false;
    }

    /**
     * select a,b 语法树
     *
     * @param sqlSelectItem
     * @return
     */
    @Override
    public boolean visit(SQLSelectItem sqlSelectItem) {

        this.sqlItemType = SqlItemType.SELECT;
        sqlSelectItem.getExpr().accept(this);
        this.sqlItemType = SqlItemType.NONE;

        return  false;
    }

    /**
     * where 语法树
     *
     * @param whereSQLExpr
     * @return
     */
    @Override
    public boolean visit(SQLBinaryOpExpr whereSQLExpr) {

        SqlItemType old = this.sqlItemType;

        // 判断是否为以下二元操作符，替换右子树为？
        if (whereSQLExpr.getOperator() == SQLBinaryOperator.GreaterThan
                || whereSQLExpr.getOperator() == SQLBinaryOperator.GreaterThanOrEqual
                || whereSQLExpr.getOperator() == SQLBinaryOperator.Is
                || whereSQLExpr.getOperator() == SQLBinaryOperator.LessThan
                || whereSQLExpr.getOperator() == SQLBinaryOperator.LessThanOrEqual
                || whereSQLExpr.getOperator() == SQLBinaryOperator.LessThanOrEqualOrGreaterThan
                || whereSQLExpr.getOperator() == SQLBinaryOperator.LessThanOrGreater
                || whereSQLExpr.getOperator() == SQLBinaryOperator.Like
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotLike
                || whereSQLExpr.getOperator() == SQLBinaryOperator.RLike
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotRLike
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotEqual
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotLessThan
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotGreaterThan
                || whereSQLExpr.getOperator() == SQLBinaryOperator.IsNot
                || whereSQLExpr.getOperator() == SQLBinaryOperator.Escape
                || whereSQLExpr.getOperator() == SQLBinaryOperator.RegExp
                || whereSQLExpr.getOperator() == SQLBinaryOperator.NotRegExp
                || whereSQLExpr.getOperator() == SQLBinaryOperator.Equality
                ) {

            this.sqlItemType = SqlItemType.WHERE;
            whereSQLExpr.getLeft().accept(this);
            // 右子树为数字可以不提取字段
            this.sqlItemType = old;

        } else {

            this.sqlItemType = SqlItemType.WHERE;
            whereSQLExpr.getLeft().accept(this);
            whereSQLExpr.getRight().accept(this);
            this.sqlItemType = old;
        }

        return false;
    }

    /**
     * between a and b 语法树
     *
     * @param betweenExpr
     * @return
     */
    @Override
    public boolean visit(SQLBetweenExpr betweenExpr) {
        SqlItemType old = this.sqlItemType;

        this.sqlItemType = SqlItemType.WHERE;
        betweenExpr.testExpr.accept(this);
        // between a and b 之间为数字可以不提取字段
        this.sqlItemType = old;

        return false;
    }


    /**
     * in (a, b) / not in (a, b) 语法树
     *
     * @param inListExpr
     * @return
     */
    @Override
    public boolean visit(SQLInListExpr inListExpr) {
        SqlItemType old = this.sqlItemType;

        this.sqlItemType = SqlItemType.WHERE;
        inListExpr.getExpr().accept(this);
        for(SQLExpr sqlExpr : inListExpr.getTargetList()) {
            sqlExpr.accept(this);
        }
        this.sqlItemType = old;

        return false;
    }


    /**
     * group by 语法树
     *
     * @param groupBySQLExpr
     * @return
     */
    @Override
    public boolean visit(SQLSelectGroupByClause groupBySQLExpr) {

        this.sqlItemType = SqlItemType.GROUP_BY;
        for (SQLExpr sqlExpr : groupBySQLExpr.getItems()) {
            sqlExpr.accept(this);
        }
        this.sqlItemType = SqlItemType.NONE;

        // having 子句中的过滤条件
        SQLExpr havingSQLExpr = groupBySQLExpr.getHaving();
        if (havingSQLExpr != null) {
            this.sqlItemType = SqlItemType.WHERE;
            havingSQLExpr.accept(this);
            this.sqlItemType = SqlItemType.NONE;
        }

        return false;
    }

    /**
     * group by 语法树
     *
     * @param sqlAggregateExpr
     * @return
     */
    @Override
    public boolean visit(SQLAggregateExpr sqlAggregateExpr) {

        this.sqlItemType = SqlItemType.GROUP_BY;
        for (SQLExpr sqlExpr : sqlAggregateExpr.getArguments()) {
            sqlExpr.accept(this);
        }
        this.sqlItemType = SqlItemType.NONE;

        return false;
    }

    /**
     * order by 语法树
     *
     * @param orderBySQLExpr
     * @return
     */
    @Override
    public boolean visit(SQLOrderBy orderBySQLExpr) {

        this.sqlItemType = SqlItemType.ORDER_BY;
        for (SQLSelectOrderByItem orderByItem : orderBySQLExpr.getItems()) {
            orderByItem.getExpr().accept(this);
        }
        this.sqlItemType = SqlItemType.NONE;

        return false;
    }

    /**
     * table 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLExprTableSource x) {
        String tableName = x.getExpr().toString();

        int index = tableName.indexOf("/");
        if (index > 0) {
            setAddItem(this.tableNameSet, tableName.substring(0, index));
        } else {
            setAddItem(this.tableNameSet, tableName);
        }

        return false;
    }

    /**
     * table 语法树，es sql 2.3.3版本不支持join 操作
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLJoinTableSource x) {
        setAddItem(this.tableNameSet, x.toString());

        return false;
    }

    /**
     * 子节点(自动名) 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLIdentifierExpr x) {

        if (SqlItemType.SELECT == this.sqlItemType) {
            setAddItem(this.selectFieldNameSet, x.getName());

        } else if (SqlItemType.GROUP_BY == this.sqlItemType) {
            setAddItem(this.groupByFieldNameSet, x.getName());

        } else if (SqlItemType.ORDER_BY == this.sqlItemType) {
            setAddItem(this.sortByFieldNameSet, x.getName());

        } else if (SqlItemType.WHERE == this.sqlItemType) {
            setAddItem(this.whereFieldNameSet, x.getName());
        }

        return false;
    }


    /**
     * 获取表名
     *
     * @return
     */
    public String getTableName() {
        return StringUtils.join(this.tableNameSet, ",");
    }

    /**
     * 获取select字段
     *
     * @return
     */
    public String getSelectFieldNames() {
        return StringUtils.join(this.selectFieldNameSet, ",");
    }

    /**
     * 获取group by字段
     *
     * @return
     */
    public String getGroupByFieldNames() {
        return StringUtils.join(this.groupByFieldNameSet, ",");
    }

    /**
     * 获取order by字段
     *
     * @return
     */
    public String getOrderByFieldNames() {
        return StringUtils.join(this.sortByFieldNameSet, ",");
    }

    /**
     * 获取where 字段
     *
     * @return
     */
    public String getWhereFieldsNames() {
        return StringUtils.join(this.whereFieldNameSet, ",");
    }

    /**
     * set添加元素
     *
     * @param set
     * @param fieldName
     */
    private void setAddItem(Set<String> set, String fieldName) {
        if (StringUtils.isNotBlank(fieldName)) {
            String value = fieldName.trim();
            String[] itemArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(value, ",");
            for (String item : itemArray) {
                set.add(item.trim());
            }
        }
    }

}
