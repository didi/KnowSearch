package com.didi.arius.gateway.dsl.dsl.parser.aggr;

import com.didi.arius.gateway.dsl.dsl.ast.aggr.Global;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class GlobalParser extends DslParser {

    public GlobalParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Global node = new Global(name);
        node.n = ValueNode.getValueNode(obj);
        return node;
    }
}
