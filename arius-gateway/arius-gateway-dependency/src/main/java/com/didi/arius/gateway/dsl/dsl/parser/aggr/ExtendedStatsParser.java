package com.didi.arius.gateway.dsl.dsl.parser.aggr;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.ExtendedStats;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class ExtendedStatsParser extends DslParser {

    public ExtendedStatsParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        ExtendedStats node = new ExtendedStats(name);
        NodeMap nm = new NodeMap();

        JSONObject jsonObject = (JSONObject) obj;
        for(String key : jsonObject.keySet()) {
            Node value;

            if(key.equalsIgnoreCase("field")) {
                value = new FieldNode(jsonObject.get(key));
            } else if(key.equalsIgnoreCase("script")) {
                value = ParserRegister.parse(parserType, key, jsonObject.get(key));
            } else {
                value = ValueNode.getValueNode(jsonObject.get(key));
            }

            nm.m.put(new StringNode(key), value);
        }

        node.n = nm;
        return node;
    }
}
