package com.didi.arius.gateway.core.service.dsl.transform;

import com.alibaba.fastjson.JSONObject;
import com.didi.arius.gateway.dsl.dsl.ast.aggr.DateHistoGram;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestVisitorV6 extends BaseRequestVisitor {

    protected static final Logger logger = LoggerFactory.getLogger(RequestVisitorV6.class);

    /**
     * interval不再支持小数，如果遇到小数，则转换成ms值
     * @param node
     */
    @Override
    public void visit(DateHistoGram node) {
        super.visit(node);

        try {
            JSONObject obj = (JSONObject) this.ret;
            if (obj.containsKey("fixed_interval")) {
                Object fixed_interval = obj.get("fixed_interval");
                obj.remove("fixed_interval");
                obj.put("interval", fixed_interval);
            } else if (obj.containsKey("calendar_interval")) {
                Object calendar_interval = obj.get("calendar_interval");
                obj.remove("calendar_interval");
                obj.put("interval", calendar_interval);
            }

            String interval = obj.getString("interval");

            //高版本不支持时间为小数
            if (interval.contains(".")) {
                TimeValue timeValue = TimeValue.parseTimeValue(interval, null, "settings");
                obj.put("interval" , timeValue.getMillis() + "ms");
            }
        } catch (Throwable e) {
            logger.warn("parse date histogram exception", e);
        }
    }
}
