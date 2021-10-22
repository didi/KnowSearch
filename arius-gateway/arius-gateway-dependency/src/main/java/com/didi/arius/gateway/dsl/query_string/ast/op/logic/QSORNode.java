package com.didi.arius.gateway.dsl.query_string.ast.op.logic;

import com.didi.arius.gateway.dsl.query_string.ast.op.common.QSBinaryOpNode;
import com.didi.arius.gateway.dsl.query_string.visitor.QSVisitor;

public class QSORNode extends QSBinaryOpNode {

    public QSORNode(String source) {
        super(source);
    }

    @Override
    public void accept(QSVisitor vistor) {
        vistor.visit(this);
    }
}
