package com.didi.arius.gateway.dsl.query_string.ast.op.logic;


import com.didi.arius.gateway.dsl.query_string.ast.op.common.QSBinaryOpNode;
import com.didi.arius.gateway.dsl.query_string.visitor.QSVisitor;

public class QSANDNode extends QSBinaryOpNode {
    public QSANDNode(String source) {
        super(source);
    }

    @Override
    public void accept(QSVisitor vistor) {
        vistor.visit(this);
    }
}
