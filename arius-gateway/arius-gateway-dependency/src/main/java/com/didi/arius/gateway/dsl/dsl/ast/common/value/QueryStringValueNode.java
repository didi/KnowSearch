package com.didi.arius.gateway.dsl.dsl.ast.common.value;

import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;
import com.didi.arius.gateway.dsl.query_string.ast.QSNode;

public class QueryStringValueNode extends ValueNode {
    private QSNode qsNode;

    public QSNode getQsNode() {
        return qsNode;
    }

    public void setQsNode(QSNode qsNode) {
        this.qsNode = qsNode;
    }

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);
    }
}
