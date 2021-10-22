package com.didi.arius.gateway.dsl.query_string.ast;

import com.didi.arius.gateway.dsl.query_string.visitor.QSVisitor;

public class QSFieldNode extends QSNode {
    public QSFieldNode(String source) {
        super(source, 0);
    }

    @Override
    public void accept(QSVisitor vistor) {
        vistor.visit(this);
    }
}
