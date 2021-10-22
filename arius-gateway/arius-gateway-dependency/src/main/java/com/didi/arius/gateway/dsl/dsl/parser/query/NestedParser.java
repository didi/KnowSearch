package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.Nested;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class NestedParser extends DslParser {
    public NestedParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        Nested node = new Nested(name);
        NodeMap nm = new NodeMap();


        JSONObject obj = (JSONObject) root;
        for(String key : obj.keySet()) {
            Node valueNode;

            if(key.equalsIgnoreCase("query")) {
                valueNode = ParserRegister.parse(parserType, key, obj.get(key));
            } else {
                valueNode = ValueNode.getValueNode(obj.get(key));
            }

            nm.m.put(new StringNode(key), valueNode);
        }


        node.n = nm;
        return node;
    }
}
