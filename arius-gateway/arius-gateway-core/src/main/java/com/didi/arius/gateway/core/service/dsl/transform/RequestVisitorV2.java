package com.didi.arius.gateway.core.service.dsl.transform;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.*;
import com.didi.arius.gateway.dsl.dsl.ast.query.Match;
import com.didi.arius.gateway.dsl.dsl.visitor.basic.OutputVisitor;

import java.util.Iterator;

public class RequestVisitorV2 extends OutputVisitor {

    @Override
    public void visit(Match node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                ((JSONObject) value).remove("auto_generate_synonyms_phrase_query");
            }
        }
    }

    @Override
    public void visit(AggrTerms node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        if (obj.containsKey("order")) {
            if (obj.get("order") instanceof JSONObject) {
                JSONObject order = obj.getJSONObject("order");
                dealOrder(order);
            } else if (obj.get("order") instanceof JSONArray) {
                for (Object o : (JSONArray) obj.get("order")) {
                    JSONObject order = (JSONObject) o;
                    dealOrder(order);
                }
            }
        }
    }

    private void dealOrder(JSONObject order) {
        Iterator<String> iter = order.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.equalsIgnoreCase("_key")) {
                Object k = order.remove("_key");
                order.put("_term", k);
            }
        }
    }
}
