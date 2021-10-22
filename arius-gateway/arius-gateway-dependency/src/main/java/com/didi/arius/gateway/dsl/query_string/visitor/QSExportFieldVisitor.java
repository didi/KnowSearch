package com.didi.arius.gateway.dsl.query_string.visitor;

import com.didi.arius.gateway.dsl.query_string.ast.QSFieldNode;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.TreeSet;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/12/12 下午4:54
 * @Modified By
 *
 * 导出query string中的字段信息
 */
public class QSExportFieldVisitor extends QSSeekVisitor {

    /**
     *  字段名集合
     */
    private Set<String> fieldNameSet = new TreeSet<>();

    @Override
    public void visit(QSFieldNode node) {
        fieldNameSet.add(formatField(node.getSource()));
    }

    public Set<String> getFieldNameSet() {
        return fieldNameSet;
    }

    public void setFieldNameSet(Set<String> fieldNameSet) {
        this.fieldNameSet = fieldNameSet;
    }

    /**
     * 格式化字段，去除"，\
     *
     * @param field
     * @return
     */
    private String formatField(String field) {
        if(StringUtils.isBlank(field)) {
            return field;
        }

        field = field.trim();

        // 去除的 \ 和 "
        field = field.replaceAll("\\\\", "");
        field = field.replaceAll("\"", "");

        return field;
    }

}
