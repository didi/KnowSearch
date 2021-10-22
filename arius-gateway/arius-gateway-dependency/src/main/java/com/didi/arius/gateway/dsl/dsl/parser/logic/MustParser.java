package com.didi.arius.gateway.dsl.dsl.parser.logic;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.logic.Must;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class MustParser extends DslParser {

    public MustParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Must node = new Must(name);
        node.n = NodeList.toNodeList(parserType, (JSON) obj, false);
        return node;
    }
}
