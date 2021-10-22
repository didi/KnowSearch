package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.MatchAll;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class MatchAllParser extends DslParser {
    public MatchAllParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        MatchAll node = new MatchAll(name);
        node.n = ValueNode.getValueNode(root);
        return node;
    }
}
