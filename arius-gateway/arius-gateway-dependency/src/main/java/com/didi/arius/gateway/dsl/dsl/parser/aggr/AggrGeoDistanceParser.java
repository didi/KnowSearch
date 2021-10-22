package com.didi.arius.gateway.dsl.dsl.parser.aggr;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.AggrGeoDistance;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

import java.util.HashSet;
import java.util.Set;

public class AggrGeoDistanceParser extends DslParser {
    private static final Set<String> keyWords = new HashSet<>();

    static {
        keyWords.add("distance");
        keyWords.add("distance_type");
        keyWords.add("optimize_bbox");
        keyWords.add("_name");
        keyWords.add("ignore_malformed");
        keyWords.add("validation_method");
    }

    public AggrGeoDistanceParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object root) throws Exception {
        AggrGeoDistance node = new AggrGeoDistance(name);
        NodeMap nm = new NodeMap();

        JSONObject obj = (JSONObject) root;
        boolean haveField = false;
        for (String key : obj.keySet()) {
            if (keyWords.contains(key.toLowerCase())) {
                nm.m.put(new StringNode(key), ValueNode.getValueNode(obj.get(key)));

            } else {
//                if (haveField) {
//                    throw new Exception("wrong geo, json:" + root);
//                }

                haveField = true;
                nm.m.put(new FieldNode(key), ValueNode.getValueNode(obj.get(key)));
            }
        }

        node.n = nm;
        return node;
    }
}
