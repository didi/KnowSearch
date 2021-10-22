package com.didi.arius.gateway.dsl.query_string.visitor;


import com.didi.arius.gateway.dsl.query_string.ast.QSValueNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.common.QSBinaryOpNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSANDNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSORNode;

import java.util.TreeSet;


public class QSFormatVisitor extends QSOutputVisitor {

    @Override
    public void visit(QSValueNode node) {
        sb.append("?");
    }

    @Override
    public void visit(QSANDNode node) {
        doAndOrBinary(node, true);
    }

    @Override
    public void visit(QSORNode node) {
        doAndOrBinary(node, true);
    }

    /**
     * AND/OR 运算符
     * @param node
     * @param white
     */
    private void doAndOrBinary(QSBinaryOpNode node, boolean white) {
        TreeSet<String> andOprSets = new TreeSet<>();

        String beforeLeft = sb.toString();

        // 得到遍历左右节点字符串长度
        int beforeLeftLength = sb.length();
        node.getLeft().accept(this);
        int afterLeftLength = sb.length();
        node.getRight().accept(this);
        int afterRightLength = sb.length();

        // 运算符左右节点结果放入treeset中排序去重，字符串自然序
        andOprSets.add(sb.substring(beforeLeftLength, afterLeftLength));
        andOprSets.add(sb.substring(afterLeftLength, afterRightLength));

        // 恢复到遍历这个节点前字符串
        sb.setLength(0);
        sb.append(beforeLeft);

        // 添加字符串自然序后第一个元素
        sb.append(andOprSets.first());

        // 如果不存在重复元素
        if (andOprSets.size() > 1) {
            if(white) {
                sb.append(" ");
            }
            sb.append(node.getSource());
            if(white) {
                sb.append(" ");
            }
            sb.append(andOprSets.last());
        }
    }

}
