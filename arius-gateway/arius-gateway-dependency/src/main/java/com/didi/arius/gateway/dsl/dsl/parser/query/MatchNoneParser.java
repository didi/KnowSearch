package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.MatchNone;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class MatchNoneParser extends DslParser {
    public MatchNoneParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        MatchNone node = new MatchNone(name);
        node.n = ValueNode.getValueNode(root);
        return node;
    }
}
