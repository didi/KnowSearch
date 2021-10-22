package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.Body;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class BodyParser extends DslParser {

    public BodyParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Body node = new Body(name);
        NodeMap nm = new NodeMap();
        JSONObject jsonObject = (JSONObject) obj;

         for (String key : jsonObject.keySet()) {
             KeyNode keyNode = new StringNode(key);

             Node valueNode = ParserRegister.parse(parserType, key, jsonObject.get(key));
             if (valueNode == null) {
                 valueNode = ValueNode.getValueNode(jsonObject.get(key));
             }

             nm.m.put(keyNode, valueNode);
         }

        node.n = nm;
        return node;
    }
}
