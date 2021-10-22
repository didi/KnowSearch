package com.didi.arius.gateway.dsl.query_string.visitor;


import com.didi.arius.gateway.dsl.query_string.ast.QSFieldNode;
import com.didi.arius.gateway.dsl.query_string.ast.QSValueNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.*;
import com.didi.arius.gateway.dsl.query_string.ast.op.common.QSBinaryOpNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.common.QSSingleOpNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSANDNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSORNode;
import com.didi.arius.gateway.dsl.query_string.ast.op.logic.QSParenNode;


public class QSOutputVisitor implements QSVisitor {
    protected StringBuilder sb = new StringBuilder();

    public String output() {
        return sb.toString();
    }

    @Override
    public void visit(QSValueNode node) {
        sb.append(node.getSource());
    }

    @Override
    public void visit(QSFieldNode node) {
        sb.append(node.getSource());
    }

    @Override
    public void visit(QSParenNode node) {
        sb.append("(");
        node.getNode().accept(this);
        sb.append(")");
    }

    @Override
    public void visit(QSANDNode node) {
        doBinary(node, true);
    }

    @Override
    public void visit(QSORNode node) {
        doBinary(node, true);
    }

    @Override
    public void visit(QSEQNode node) {
        doBinary(node, false);
    }

    @Override
    public void visit(QSMinusNode node) {
        doSingle(node, false);
    }

    @Override
    public void visit(QSNotNode node) {
        doSingle(node, true);
    }

    @Override
    public void visit(QSPlusNode node) {
        doSingle(node, false);
    }

    @Override
    public void visit(QSRangeNode node) {
        if(node.isStartInc()) {
            sb.append("[");
        } else {
            sb.append("{");
        }

        node.getLeft().accept(this);
        sb.append(" TO ");
        node.getRight().accept(this);

        if(node.isEndInc()) {
            sb.append("]");
        } else {
            sb.append("}");
        }
    }


    private void doBinary(QSBinaryOpNode node, boolean white) {
        node.getLeft().accept(this);
        if(white) {
            sb.append(" ");
        }
        sb.append(node.getSource());
        if(white) {
            sb.append(" ");
        }
        node.getRight().accept(this);
    }

    private void doSingle(QSSingleOpNode node, boolean white) {
        sb.append(node.getSource());
        if(white) {
            sb.append(" ");
        }
        node.getNode().accept(this);
    }
}
