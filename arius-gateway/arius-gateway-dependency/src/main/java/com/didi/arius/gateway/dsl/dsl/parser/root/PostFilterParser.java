package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSON;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.root.PostFilter;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class PostFilterParser extends DslParser {
    public PostFilterParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        PostFilter node = new PostFilter(name);

        node.n = NodeList.toNodeList(parserType, (JSON) obj, true);

        return node;
    }
}
