package com.didi.arius.gateway.dsl.dsl.parser.query;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.KeyWord;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeList;
import com.didi.arius.gateway.dsl.dsl.ast.common.multi.NodeMap;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.ast.query.FunctionScore;
import com.didi.arius.gateway.dsl.dsl.parser.DslParser;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;

import java.util.HashSet;
import java.util.Set;

public class FunctionScoreParser extends DslParser {
    public FunctionScoreParser(ParserType type) {
        super(type);
    }

    @Override
    public KeyWord parse(String name, Object obj) throws Exception {
        FunctionScore node = new FunctionScore(name);

        NodeMap nm = new NodeMap();

        JSONObject jsonObject = (JSONObject) obj;
        for(String key : jsonObject.keySet()) {
            StringNode kn = new StringNode(key);
            Node vn;

            if(key.equalsIgnoreCase("query")) {
                vn = ParserRegister.parse(parserType, key, jsonObject.get(key));
            } else if(key.equalsIgnoreCase("functions")) {
                vn = parserFunctions((JSON) jsonObject.get(key));
            } else {
                vn = parserKey(key, jsonObject.get(key));
            }

            nm.m.put(kn, vn);
        }

        node.n = nm;
        return node;
    }

    private Node parserFunctions(JSON root) throws Exception {
        if(root instanceof  JSONObject) {
            return parserFuncObj((JSONObject) root);
        }

        NodeList nl = new NodeList();
        JSONArray array = (JSONArray) root;
        for(Object obj : array) {
            nl.l.add(parserFuncObj((JSONObject) obj));
        }
        return nl;
    }

    private Node parserFuncObj(JSONObject root) throws Exception {
        NodeMap nm = new NodeMap();

        for(String key : root.keySet()) {
            nm.m.put(new StringNode(key), parserKey(key, root.get(key)));
        }

        return nm;
    }

    private Node parserKey(String key , Object obj) throws Exception {
        if (key.equalsIgnoreCase(FILTER_STR)) {
            return parseFilter((JSONObject) obj);

        } else if (key.equalsIgnoreCase(WEIGHT_STR)) {
            return parseWeight(obj);

        } else if (key.equalsIgnoreCase(RANDOM_SCORE_STR)) {
            return parseRandomScore(obj);

        } else if (key.equalsIgnoreCase(SCRIPT_SCORE_STR)) {
            return parseScriptScore(obj);

        } else if (key.equalsIgnoreCase(FIELD_VALUE_FACTOR_str)) {
            return parseFieldValueFactor((JSONObject) obj);

        } else if (funcNames.contains(key.toLowerCase())) {
            return parseFunc((JSONObject) obj);

        } else {
            return ValueNode.getValueNode(obj);
        }
    }


    private static final String FILTER_STR = "filter";
    private Node parseFilter(JSONObject obj) throws Exception {
        return ParserRegister.parse(ParserType.QUERY, FILTER_STR, obj);
    }

    private static final String WEIGHT_STR = "weight";
    private Node parseWeight(Object obj) {
        return ValueNode.getValueNode(obj);
    }

    private static final String RANDOM_SCORE_STR = "random_score";
    private Node parseRandomScore(Object obj) {
        return ValueNode.getValueNode(obj);
    }

    private static final String SCRIPT_SCORE_STR = "script_socre";
    private Node parseScriptScore(Object obj) throws Exception {
        NodeMap nm = new NodeMap();

        JSONObject jsonObject = (JSONObject) obj;
        for(String key : jsonObject.keySet()) {
            StringNode keyNode = new StringNode(key);
            Object o = jsonObject.get(key);

            if(key.equalsIgnoreCase("script")) {
                nm.m.put(keyNode, ParserRegister.parse(ParserType.QUERY, key, o));
            } else {
                nm.m.put(keyNode, ValueNode.getValueNode(o));
            }
        }

        return nm;
    }

    private static final String FIELD_VALUE_FACTOR_str = "field_value_factor";
    private Node parseFieldValueFactor(JSONObject obj) {
        NodeMap nm = new NodeMap();
        for (String k : obj.keySet()) {
            if (k.equalsIgnoreCase("field")) {
                nm.m.put(new StringNode(k), new FieldNode(obj.get(k)));
            } else {
                nm.m.put(new StringNode(k), ValueNode.getValueNode(obj.get(k)));
            }
        }

        return nm;
    }

    private static final Set<String> funcNames = new HashSet<>();
    static {
        funcNames.add("gauss");
        funcNames.add("exp");
        funcNames.add("linear");
    }
    private Node parseFunc(JSONObject obj) {
        NodeMap nm = new NodeMap();
        for(String key : obj.keySet()) {
            Object o = obj.get(key);
            if(o instanceof JSONObject) {
                nm.m.put(new FieldNode(key), ValueNode.getValueNode(o));
            } else {
                nm.m.put(new StringNode(key), ValueNode.getValueNode(o));
            }
        }
        return nm;
    }
}
