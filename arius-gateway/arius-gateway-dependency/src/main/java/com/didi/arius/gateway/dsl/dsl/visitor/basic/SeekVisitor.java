package com.didi.arius.gateway.dsl.dsl.visitor.basic;

import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.IdentityNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.JsonNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.StringListNode;


public class SeekVisitor extends BaseVisitor {

    @Override
    public void visit(FieldNode node) { }

    @Override
    public void visit(IdentityNode node) { }

    @Override
    public void visit(StringNode node) { }

    @Override
    public void visit(JsonNode node) { }

    @Override
    public void visit(ObjectNode node) { }

    @Override
    public void visit(StringListNode node) {
        node.l.accept(this);
    }

    @Override
    public void visit(NodeMap node) {
        for(KeyNode key : node.m.keySet()) {
            key.accept(this);

            node.m.get(key).accept(this);
        }
    }

    @Override
    public void visit(NodeList node) {
        for(Node n : node.l) {
            n.accept(this);
        }
    }
}
