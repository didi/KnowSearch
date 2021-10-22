package com.didi.arius.gateway.dsl.dsl.parser.root;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.root.Sort;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

import java.util.HashSet;
import java.util.Set;

public class SortParser extends DslParser {
    public SortParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        Sort node = new Sort(name);

        if(!(obj instanceof JSONObject) && !(obj instanceof JSONArray)) {
            node.n = new FieldNode(obj);
            return node;
        }

        if(obj instanceof JSONObject) {
            node.n = parserJsonObject((JSONObject) obj);
        }

        if(obj instanceof JSONArray) {
            NodeList l = new NodeList();
            JSONArray array = (JSONArray) obj;
            for(Object o : array) {
                if(o instanceof JSONObject) {
                    l.l.add(parserJsonObject((JSONObject) o));
                } else {
                    // eg "sort": ["_doc"]
                    l.l.add(new FieldNode(o));
                }
            }

            node.n = l;
        }

        return node;
    }


    private NodeMap parserJsonObject(JSONObject root) throws Exception {
        NodeMap m = new NodeMap();

        for(String key : root.keySet()) {
            StringNode keyNode = new StringNode(key);
            Object o = root.get(key);

            if(key.equalsIgnoreCase(GEO_DISTANCE)) {
                m.m.put(keyNode, parserGEO((JSONObject) o));

            } else if(key.equalsIgnoreCase(SCRIPT)) {
                m.m.put(keyNode, parseScript((JSONObject) o));

            } else if(key.equalsIgnoreCase(SCORE)) {
                if(o instanceof JSONObject) {
                    m.m.put(keyNode, parseScore((JSONObject) o));
                } else {
                    m.m.put(keyNode, ValueNode.getValueNode(o));
                }

            } else {
                m.m.put(new FieldNode(key), ValueNode.getValueNode(o));
            }
        }

        return m;
    }

    private static final String GEO_DISTANCE = "_geo_distance";
    private static final Set<String> GEOKeyWords = new HashSet<>();
    static {
        GEOKeyWords.add("order");
        GEOKeyWords.add("unit");
        GEOKeyWords.add("distance_type");
        GEOKeyWords.add("mode");
        GEOKeyWords.add("distance_type");
        GEOKeyWords.add("validation_method");
        GEOKeyWords.add("ignore_unmapped");
    }
    private NodeMap parserGEO(JSONObject root) throws Exception {
        NodeMap m = new NodeMap();

        boolean haveField = false;
        for(String key : root.keySet()) {
            if(GEOKeyWords.contains(key.toLowerCase())) {
                m.m.put(new StringNode(key), ValueNode.getValueNode(root.get(key)));
            } else {
//                if(haveField) {
//                    throw new Exception("wrong geo, json:" + root);
//                }

                haveField = true;
                m.m.put(new FieldNode(key), ValueNode.getValueNode(root.get(key)));
            }
        }

        return m;
    }

    private static final String SCRIPT = "_script";
    private NodeMap parseScript(JSONObject root) throws Exception {
        NodeMap nm = new NodeMap();

        for(String key : root.keySet()) {
            Node valueNode;
            if(key.equalsIgnoreCase("script")) {
                valueNode = ParserRegister.parse(parserType, key, root.get(key));
            } else {
                valueNode = ValueNode.getValueNode(root.get(key));
            }

            nm.m.put(new StringNode(key), valueNode);
        }

        return nm;
    }

    private static final String SCORE = "_score";
    private NodeMap parseScore(JSONObject root) throws Exception {
        NodeMap nm = new NodeMap();

        for(String key : root.keySet()) {
            nm.m.put(new FieldNode(key), ValueNode.getValueNode(root.get(key)));
        }

        return nm;
    }

}
