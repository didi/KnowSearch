package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSONArray;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.Docs;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class DocsParser extends DslParser {

    public DocsParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Docs node = new Docs(name);

        if(obj instanceof JSONArray) {
          node.n = new NodeList() ;
          NodeList.toList((JSONArray)obj, (NodeList)node.n);

        } else {
            node.n = ValueNode.getValueNode(obj);
        }

        return node;
    }
}
