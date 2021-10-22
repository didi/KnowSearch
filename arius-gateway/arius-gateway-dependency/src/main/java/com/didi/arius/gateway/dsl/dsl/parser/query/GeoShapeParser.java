package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.query.GeoShape;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

public class GeoShapeParser extends DslParser {
    public GeoShapeParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        GeoShape node = new GeoShape(name);

        NodeMap nm = new NodeMap();
        NodeMap.toField4Value((JSONObject) obj, nm);
        node.n = nm;

        return node;
    }
}
