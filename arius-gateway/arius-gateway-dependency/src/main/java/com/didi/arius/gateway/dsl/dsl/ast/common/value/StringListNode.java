package com.didi.arius.gateway.dsl.dsl.ast.common.value;

import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

public class StringListNode extends ValueNode {
    public NodeList l = new NodeList();

    @Override
    public int hashCode() {
        return l.hashCode();
    }

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);
    }
}
