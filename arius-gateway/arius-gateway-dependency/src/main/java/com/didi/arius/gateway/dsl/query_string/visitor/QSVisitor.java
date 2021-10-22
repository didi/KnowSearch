package com.didi.arius.gateway.dsl.query_string.visitor;


import com.didi.arius.gateway.dsl.query_string.ast.QSFieldNode;
import com.didi.arius.gateway.dsl.query_string.ast.QSValueNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.QSEQNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.QSMinusNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.QSNotNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.QSPlusNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.QSRangeNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSANDNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSORNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSParenNode;


public interface QSVisitor {
    public void visit(QSValueNode node);

    public void visit(QSFieldNode node);

    public void visit(QSParenNode node);

    public void visit(QSANDNode node);

    public void visit(QSORNode node);

    public void visit(QSEQNode node);

    public void visit(QSMinusNode node);

    public void visit(QSNotNode node);

    public void visit(QSPlusNode node);

    public void visit(QSRangeNode node);
}
