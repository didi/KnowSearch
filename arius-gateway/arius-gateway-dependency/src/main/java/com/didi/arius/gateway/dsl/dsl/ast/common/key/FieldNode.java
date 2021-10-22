package com.didi.arius.gateway.dsl.dsl.ast.common.key;

import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

public class FieldNode extends KeyNode {

    public FieldNode(Object obj) {
        super(obj);
    }

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);
    }
}
