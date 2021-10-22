package com.didi.arius.gateway.dsl.dsl.visitor;

import com.didi.arius.gateway.dsl.constant.DslItemType;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.Aggs;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.script.Script;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.Query;
import com.didi.arius.gateway.dsl.dsl.ast.query.QueryString;
import com.didi.arius.gateway.dsl.dsl.ast.root.FieldDataFields;
import com.didi.arius.gateway.dsl.dsl.ast.root.Fields;
import com.didi.arius.gateway.dsl.dsl.ast.root.Sort;
import com.didi.arius.gateway.dsl.dsl.ast.root.Source;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.SeekVisitor;
import com.didi.arius.gateway.dsl.query_string.visitor.QSExportFieldVisitor;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/6 上午11:49
 * @Modified By
 */
public class EsDslExportParameterVisitor extends SeekVisitor {

    private boolean isOutput = false;

    /**
     * select 字段名集合
     */
    private Set<String> selectFieldNameSet = new TreeSet<>();
    /**
     * where 字段名集合
     */
    private Set<String> whereFieldNameSet = new TreeSet<>();
    /**
     * group by 字段名集合
     */
    private Set<String> groupByFieldNameSet = new TreeSet<>();
    /**
     * sort by 字段名集合
     */
    private Set<String> sortByFieldNameSet = new TreeSet<>();

    /**
     * dsl语句类型
     */
    private DslItemType dslItemType = DslItemType.NONE;

    public EsDslExportParameterVisitor() {

    }

    /**
     * 获取source字段
     *
     * @param node
     */
    @Override
    public void visit(Source node) {
        dslItemType = DslItemType.SELECT;
        node.n.accept(this);
        dslItemType = DslItemType.NONE;
    }

    /**
     * 获取fields字段
     *
     * @param node
     */
    @Override
    public void visit(Fields node) {
        dslItemType = DslItemType.SELECT;
        node.n.accept(this);
        dslItemType = DslItemType.NONE;
    }

    /**
     * 获取query string 中的过滤字段
     * 如果之前不包含*，在query_string之后有*就去除，因为在kibana中输入条件默认为*，此时需要排除query_string中的*
     *
     * @param node
     */
    @Override
    public void visit(QueryStringValueNode node) {
        QSExportFieldVisitor qsExportFieldVisitor = new QSExportFieldVisitor();
        node.getQsNode().accept(qsExportFieldVisitor);
        whereFieldNameSet.addAll(qsExportFieldVisitor.getFieldNameSet());
        isOutput = true;
    }


    /**
     * 获取query string 中的过滤字段
     *
     * @param node
     */
    @Override
    public void visit(QueryString node) {
        boolean isContainsAll = whereFieldNameSet.contains("*");
        dslItemType = DslItemType.WHERE;
        node.n.accept(this);
        dslItemType = DslItemType.NONE;
        // 如果之前不包含*，在query_string之后有*就去除，因为在kibana中输入条件默认为*，此时需要排除query_string中的*
        if (!isContainsAll) {
            whereFieldNameSet.remove("*");
        }
        isOutput = true;
    }

    /**
     * 获取脚本中过滤字段
     *
     * @param node
     */
    @Override
    public void visit(Script node) {
//        dslItemType = DslItemType.WHERE;
//        node.n.accept(this);
//        dslItemType = DslItemType.NONE;
        whereFieldNameSet.add("*");
        isOutput = true;
    }

    /**
     * 获取过滤字段
     *
     * @param node
     */
    @Override
    public void visit(Query node) {
        // 这里需要判断是否为none，由于聚合语句中也会有query,此时提取的字段还是聚合
        if (dslItemType == DslItemType.NONE) {
            dslItemType = DslItemType.WHERE;
            for (KeyNode n : node.m.m.keySet()) {
                n.accept(this);
                node.m.m.get(n).accept(this);
            }
            dslItemType = DslItemType.NONE;
        }
    }

    /**
     * 获取聚合字段
     *
     * @param node
     */
    @Override
    public void visit(Aggs node) {
        dslItemType = DslItemType.GROUP_BY;
        for (KeyNode n : node.m.m.keySet()) {
            n.accept(this);
            node.m.m.get(n).accept(this);
        }
        dslItemType = DslItemType.NONE;
    }

    /**
     * 获取排序字段
     *
     * @param node
     */
    @Override
    public void visit(Sort node) {
        dslItemType = DslItemType.ORDER_BY;
        node.n.accept(this);
        dslItemType = DslItemType.NONE;
    }

    /**
     * 获取排序字段
     *
     * @param node
     */
    @Override
    public void visit(FieldDataFields node) {
        dslItemType = DslItemType.ORDER_BY;
        node.n.accept(this);
        dslItemType = DslItemType.NONE;
    }

    /**
     * 字段节点
     *
     * @param node
     */
    @Override
    public void visit(FieldNode node) {
        if (DslItemType.SELECT == this.dslItemType) {
            setAddItem(this.selectFieldNameSet, node.value);

        } else if (DslItemType.GROUP_BY == this.dslItemType) {
            setAddItem(this.groupByFieldNameSet, node.value);

        } else if (DslItemType.ORDER_BY == this.dslItemType) {
            setAddItem(this.sortByFieldNameSet, node.value);

        } else if (DslItemType.WHERE == this.dslItemType) {
            setAddItem(this.whereFieldNameSet, node.value);
        }
    }

    /**
     * 获取select字段
     *
     * @return
     */
    public String getSelectFieldNames() {
        // 如果没有指定字段，则查询所有字段为*
        if (this.selectFieldNameSet.isEmpty()) {
            return "*";
        }

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

    public boolean isOutput() {
        return isOutput;
    }

    public void setOutput(boolean output) {
        isOutput = output;
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
                set.add(formatField(item));
            }
        }
    }

    /**
     * 格式化字段，去除"，\
     *
     * @param field
     * @return
     */
    private String formatField(String field) {
        if (StringUtils.isBlank(field)) {
            return field;
        }

        field = field.trim();

        // 去除的 \ 和 "
        field = field.replaceAll("\\\\", "");
        field = field.replaceAll("\"", "");

        return field;
    }

}
