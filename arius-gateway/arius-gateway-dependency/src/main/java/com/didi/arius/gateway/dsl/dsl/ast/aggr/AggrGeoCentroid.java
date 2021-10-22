package com.didi.arius.gateway.dsl.dsl.ast.aggr;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

public class AggrGeoCentroid extends KeyWord {

    public Node n;

    public AggrGeoCentroid(String name) {
        super(name);
    }

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);
    }


}
