package com.didi.arius.gateway.dsl.dsl.ast.common.multi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.Node;
import com.didi.arius.gateway.dsl.dsl.ast.common.key.FieldNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ObjectNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.ValueNode;
import com.didi.arius.gateway.dsl.dsl.parser.ParserType;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.Visitor;

import java.util.ArrayList;
import java.util.List;

public class NodeList extends Node {
    public List<Node> l = new ArrayList();

    @Override
    public void accept(Visitor vistor) {
        vistor.visit(this);
    }


    public static void toValueList(JSONArray array, NodeList node) throws Exception {
        for(Object obj : array) {
            if(obj instanceof JSON) {
                throw new Exception("wrong json, json:" + array);
            }

            node.l.add(new ObjectNode(obj));
        }
    }

    public static  void toList(JSONArray array, NodeList node) {
        for(Object obj : array) {
            node.l.add(ValueNode.getValueNode(obj));
        }
    }

    public static Node toNodeList(ParserType type, JSON root, boolean isMultiKey) throws Exception {
        if(root == null || !(root instanceof JSON) ) {
            return new ObjectNode(root);
        }

        if(root instanceof JSONObject) {
            return Node.toNodeWith1Key(type, (JSONObject) root, isMultiKey);
        }

        NodeList nl = new NodeList();
        JSONArray array = (JSONArray) root;
        for(Object obj : array) {
            if(obj instanceof JSONArray) {
                NodeList tmpnl = new NodeList();
                for(Object o : (JSONArray)obj) {
                    tmpnl.l.add(Node.toNodeWith1Key(type, (JSONObject)o, isMultiKey));
                }
                nl.l.add(tmpnl);

            } else {
                nl.l.add(Node.toNodeWith1Key(type, (JSONObject) obj, isMultiKey));
            }
        }
        return nl;
    }


    public static void toFieldList(JSONArray array, NodeList node) throws Exception {
        for(Object obj : array) {
            if(obj instanceof JSON) {
                throw new Exception("wrong json, json:" + array);
            }

            node.l.add(new FieldNode(obj));
        }
    }
}
