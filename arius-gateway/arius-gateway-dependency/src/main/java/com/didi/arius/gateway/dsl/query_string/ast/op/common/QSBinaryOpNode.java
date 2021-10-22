package com.didi.arius.gateway.dsl.query_string.ast.op.common;

import com.didi.arius.gateway.dsl.query_string.ast.QSNode;
import com.didi.arius.gateway.dsl.query_string.parser.ParseException;

public abstract class QSBinaryOpNode extends QSOPNode {
    private QSNode left;
    private QSNode right;

    public QSBinaryOpNode(String source) {
        super(source, 2);
    }

    public QSNode getLeft() {
        return left;
    }

    public void setLeft(QSNode left) throws ParseException {
        addValue();
        this.left = left;
    }

    public QSNode getRight() {
        return right;
    }

    public void setRight(QSNode right) throws ParseException {
        addValue();
        this.right = right;
    }
}
