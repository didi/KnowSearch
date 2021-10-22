/*
 * Copyright 1999-2011 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.didi.arius.gateway.dsl.sql;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EsSqlFormatVisitor extends MySqlOutputVisitor {

    private StringBuilder sb;
    /**
     * 是否忽略字段名，即替换为?
     */
    private boolean ignoreIdentifier = false;


    public EsSqlFormatVisitor(StringBuilder sb) {
        super(sb);
        this.sb = sb;
    }

    @Override
    public boolean visit(SQLQueryExpr x) {
        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();

        return false;
    }


    /**
     * 查询注释语法树 / * a, b * /
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLCommentHint x) {
        print("/*");

        String text = x.getText();
        if(text!=null && text.contains("ROUTINGS(")) {
            print("! ROUTINGS(?) ");
        } else {
            print(x.getText());
        }

        print("*/");

        return false;
    }

    /**
     * select a,b 语法树
     *
     * @param selectList
     */
    @Override
    protected void printSelectList(List<SQLSelectItem> selectList) {
        incrementIndent();

        int len = sb.length();

        List<String> columns = new ArrayList<>();

        ignoreIdentifier = true;
        for (int i = 0; i < selectList.size(); ++i) {
            SQLExpr sqlExpr = selectList.get(i).getExpr();
            // 变量及*就返回
            if (sqlExpr instanceof SQLIdentifierExpr || sqlExpr instanceof SQLAllColumnExpr) {
                continue;
            }

            selectList.get(i).accept(this);
            columns.add(sb.substring(len));
            sb.delete(len, sb.length());
        }
        ignoreIdentifier = false;

        if(columns.size()==0) {
            columns.add("?");
        }

        // 对查询字段进行排序
        Collections.sort(columns);

        for (int i = 0, size = columns.size(); i < size; ++i) {
            if (i != 0) {
                print(", ");
            }

            // 字段名称
            print(columns.get(i));
        }

        decrementIndent();
    }

    /**
     * table 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLExprTableSource x) {
        print("?");

        return false;
    }

    /**
     * table 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLJoinTableSource x) {
        print("?");

        return false;
    }

    /**
     * between a and b 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLBetweenExpr x) {
        x.getTestExpr().accept(this);

        if (x.isNot()) {
            print(" NOT BETWEEN ");
        } else {
            print(" BETWEEN ");
        }

        // 对字段名称进行替换成?
        ignoreIdentifier = true;
        x.getBeginExpr().accept(this);
        print(" AND ");
        x.getEndExpr().accept(this);
        ignoreIdentifier = false;

        return false;
    }


    private static final String STR = "str";

    /**
     * where 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLBinaryOpExpr x) {

        // 判断是否为以下二元操作符，替换右子树为？
        if (x.getOperator() == SQLBinaryOperator.GreaterThan
                || x.getOperator() == SQLBinaryOperator.GreaterThanOrEqual
                || x.getOperator() == SQLBinaryOperator.Is
                || x.getOperator() == SQLBinaryOperator.LessThan
                || x.getOperator() == SQLBinaryOperator.LessThanOrEqual
                || x.getOperator() == SQLBinaryOperator.LessThanOrEqualOrGreaterThan
                || x.getOperator() == SQLBinaryOperator.LessThanOrGreater
                || x.getOperator() == SQLBinaryOperator.Like
                || x.getOperator() == SQLBinaryOperator.NotLike
                || x.getOperator() == SQLBinaryOperator.RLike
                || x.getOperator() == SQLBinaryOperator.NotRLike
                || x.getOperator() == SQLBinaryOperator.NotEqual
                || x.getOperator() == SQLBinaryOperator.NotLessThan
                || x.getOperator() == SQLBinaryOperator.NotGreaterThan
                || x.getOperator() == SQLBinaryOperator.IsNot
                || x.getOperator() == SQLBinaryOperator.Escape
                || x.getOperator() == SQLBinaryOperator.RegExp
                || x.getOperator() == SQLBinaryOperator.NotRegExp
                || x.getOperator() == SQLBinaryOperator.Equality
                ) {

            x.getLeft().accept(this);
            print(" ");
            print(x.getOperator().name);
            print(" ");

            ignoreIdentifier = true;
            x.getRight().accept(this);
            ignoreIdentifier = false;

            return false;
        }

        List<SQLExpr> groupList = new ArrayList<>();

        // 将right加入
        groupList.add(x.getRight());

        SQLExpr left = x.getLeft();

        for (;;) {

            if (left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).getOperator() == x.getOperator()) {
                SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
                groupList.add(binaryLeft.getRight());
                left = binaryLeft.getLeft();
            } else {
                groupList.add(left);
                break;
            }
        }

        int len = sb.length();
        for(SQLExpr node : groupList) {

            node.accept(this);

            node.putAttribute(STR, sb.substring(len));
            sb.delete(len, sb.length());
        }

        // 对数组排序
        groupList.sort((o1, o2) -> {
            String s1 = (String) o1.getAttribute(STR);
            String s2 = (String) o2.getAttribute(STR);

            if (s1.length() == s2.length()) {
                return s1.compareTo(s2);
            } else {
                return s1.length() - s2.length();
            }

        });


        // 输出
        for (int i = groupList.size() - 1; i > 0; --i) {
            SQLExpr item = groupList.get(i);
            visitNode(item, x.getOperator(), true);

            print(" ");
            print(x.getOperator().name);
            print(" ");
        }

        visitNode(groupList.get(0), x.getOperator(), false);

        return false;
    }

    /**
     *
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLVariantRefExpr x) {
        print("?");

        return false;
    }

    /**
     * in (a, b) / not in (a, b) 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLInListExpr x) {
        x.getExpr().accept(this);

        if (x.isNot()) {
            print(" NOT IN (?)");
        } else {
            print(" IN (?)");
        }

        return false;
    }

    /**
     * 整数 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLIntegerExpr x) {
        print("?");

        return  false;
    }

    /**
     * 数值 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLNumberExpr x) {
        print("?");

        return false;
    }

    /**
     * char 语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLCharExpr x) {
        print('?');

        return false;
    }

    /**
     * 子节点(字段名)语法树
     *
     * @param x
     * @return
     */
    @Override
    public boolean visit(SQLIdentifierExpr x) {
        if(ignoreIdentifier) {
            print("?");
        } else {
            print(x.getName());
        }

        return false;
    }

    /**
     * 输出一个node的内容
     *
     * @param node
     * @param parentOp
     * @param isLeft
     */
    private void visitNode(SQLExpr node, SQLBinaryOperator parentOp, boolean isLeft) {
        if (node instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryNode = (SQLBinaryOpExpr) node;

            if ((isLeft&&(binaryNode.getOperator().priority > parentOp.priority))
                    || (!isLeft&&(binaryNode.getOperator().priority >= parentOp.priority))) {

                print('(');
                node.accept(this);
                print(')');

            } else {
                node.accept(this);
            }
        } else {
            node.accept(this);
        }

    }

}
