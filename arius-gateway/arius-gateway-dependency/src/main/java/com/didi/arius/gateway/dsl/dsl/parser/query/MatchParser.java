package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.query.Match;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class MatchParser extends DslParser {

    public MatchParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Match node = new Match(name);
        NodeMap.toField4Value((JSONObject) obj, node.m);
        return node;
    }
}
