package com.didi.arius.gateway.dsl.dsl.ast.common.multi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.KeyNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.StringNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.ParserRegister;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;
import com.didi.arius.gateway.dsl.dsl.util.ConstValue;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

import java.util.HashMap;
import java.util.Map;

public class NodeMap extends Node {
    public Map<KeyNode, Node> m = new HashMap<>();

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);

    }

    public static void toField4Value(JSONObject root, NodeMap node) throws Exception {
        for(String key : root.keySet()) {
            if(checkName(key, root, node)) {
                continue;
            }

            node.m.put(new FieldNode(key), ValueNode.getValueNode(root.get(key)));
        }
    }

    public static void toField2ValueList(JSONObject root, NodeMap node) throws Exception {
        for(String key : root.keySet()) {
            if (checkName(key, root, node)) {
                continue;
            }

            Object obj = root.get(key);
            if(obj instanceof JSON) {
                NodeList l = new NodeList();
                NodeList.toValueList((JSONArray) obj, l);
                node.m.put(new FieldNode(key), l);
            } else {
                // TODO
            }
        }
    }

    public static void toString2Node(ParserType type, JSONObject root, NodeMap node) throws Exception {
        for(String key : root.keySet()) {

            Object obj = root.get(key);
            Node n = ParserRegister.parse(type, key, obj);
            if(n==null) {
                n = ValueNode.getValueNode(obj);
            }

            node.m.put(new StringNode(key), n);
        }
    }

    public static void toString2ValueWithField(ParserType type, JSONObject root, NodeMap node, String fieldColumn) {
        for(String key : root.keySet()) {
            Object obj = root.get(key);


            if(fieldColumn!=null && key.equalsIgnoreCase(fieldColumn)) {
                if(!(obj instanceof JSONArray)) {
                    node.m.put(new StringNode(key), new FieldNode(obj));
                } else {
                    //             "fields" : ["title", "description"]
                    NodeList nl = new NodeList();
                    JSONArray array = (JSONArray) obj;
                    for(Object o : array) {
                        nl.l.add(new FieldNode(o));
                    }
                    node.m.put(new StringNode(key), nl);
                }

            } else {
                node.m.put(new StringNode(key), ValueNode.getValueNode(obj));
            }
        }
    }

    private static boolean checkName(String key, JSONObject obj, NodeMap node) {
        if (key.equalsIgnoreCase(ConstValue.NAME)) {
            node.m.put(new StringNode(key), ValueNode.getValueNode(obj.get(key)));
            return true;
        } else if(key.equalsIgnoreCase(ConstValue.BOOST)) {
            node.m.put(new StringNode(key), ValueNode.getValueNode(obj.get(key)));
            return true;
        } else {
            return false;
        }
    }
}
