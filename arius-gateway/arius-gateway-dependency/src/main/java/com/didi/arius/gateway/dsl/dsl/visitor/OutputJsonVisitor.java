package com.didi.arius.gateway.dsl.dsl.visitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.JsonNode;
import com.didi.arius.gateway.dsl.dsl.ast.common.value.QueryStringValueNode;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.SeekVisitor;

public class OutputJsonVisitor extends SeekVisitor {
    public boolean haveJson = false;

    public void visit(JsonNode node) {
        String str = node.json.toJSONString();
        if(str.equalsIgnoreCase("[{\"_term\":\"desc\"}]")) {
            return;
        }

        if(str.equalsIgnoreCase("[{\"_term\":\"asc\"}]")) {
            return;
        }

        if(str.contains("shape") && str.contains("coordinates") &&
                str.contains("relation")) {
            return;
        }

        if(str.contains("lon") && str.contains("lat")) {
            return;
        }

        if(str.contains("top_left") && str.contains("bottom_right")) {
            return;
        }

        if(str.contains("from") && str.contains("to") ) {
            return;
        }

        if(str.contains("key") && str.contains("to") ) {
            return;
        }

        if(str.contains("\"query\":[1]")) {
            return;
        }


        JSON json = node.json;
        if(json instanceof JSONArray) {
            for(Object o : (JSONArray)json) {
                if(o instanceof  JSON) {
                    System.out.println(node.json.toJSONString());
                    haveJson = true;
                    return;
                }
            }

        }  else {

            JSONObject jsonObject = (JSONObject) json;
            if(((JSONObject) json).containsKey("min_value")) {
                return;
            }

            for(String key : jsonObject.keySet()) {
                if(jsonObject.get(key) instanceof JSON) {
                    System.out.println(node.json.toJSONString());
                    haveJson = true;
                    return;
                }
            }

        }
    }

    @Override
    public void visit(QueryStringValueNode node) {

    }

}
