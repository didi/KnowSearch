package com.didi.arius.gateway.dsl.dsl.parser.logic;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.logic.Bool;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class BoolParser extends DslParser {
    public BoolParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Bool node = new Bool(name);
        NodeMap.toString2Node(parserType, (JSONObject)obj, node.m);
        return node;
    }
}
