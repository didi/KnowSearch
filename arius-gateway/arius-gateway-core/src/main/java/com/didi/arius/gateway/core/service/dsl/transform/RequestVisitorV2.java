package com.didi.arius.gateway.core.service.dsl.transform;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.arius.dsl.common.dsl.ast.aggr.*;
import com.didichuxing.datachannel.arius.dsl.common.dsl.ast.query.Match;
import com.didichuxing.datachannel.arius.dsl.common.dsl.visitor.basic.OutputVisitor;

import java.util.Iterator;
import java.util.Map;

public class RequestVisitorV2 extends OutputVisitor {

    protected static final String ORDER = "order";

    @Override
    public void visit(Match node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        for (Map.Entry<String,Object> entry : obj.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof JSONObject) {
                ((JSONObject) value).remove("auto_generate_synonyms_phrase_query");
            }
        }
    }

    @Override
    public void visit(AggrTerms node) {
        super.visit(node);

        JSONObject obj = (JSONObject) this.ret;
        if (obj.containsKey(ORDER)) {
            if (obj.get(ORDER) instanceof JSONObject) {
                JSONObject order = obj.getJSONObject(ORDER);
                dealOrder(order);
            } else if (obj.get(ORDER) instanceof JSONArray) {
                for (Object o : (JSONArray) obj.get(ORDER)) {
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
