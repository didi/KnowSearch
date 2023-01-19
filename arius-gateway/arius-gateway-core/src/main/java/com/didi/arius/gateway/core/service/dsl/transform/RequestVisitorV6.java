package com.didi.arius.gateway.core.service.dsl.transform;

import org.elasticsearch.common.unit.TimeValue;

import com.alibaba.fastjson.JSONObject;
import com.didiglobal.knowframework.dsl.parse.dsl.ast.aggr.DateHistoGram;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class RequestVisitorV6 extends BaseRequestVisitor {

    protected static final ILog logger = LogFactory.getLog(RequestVisitorV6.class);

    protected static final String FIXED_INTERVAL = "fixed_interval";
    protected static final String INTERVAL = "interval";
    protected static final String CALENDAR_INTERVAL = "calendar_interval";

    /**
     * interval不再支持小数，如果遇到小数，则转换成ms值
     * @param node
     */
    @Override
    public void visit(DateHistoGram node) {
        super.visit(node);

        try {
            JSONObject obj = (JSONObject) this.ret;
            if (obj.containsKey(FIXED_INTERVAL)) {
                Object fixedInterval = obj.get(FIXED_INTERVAL);
                obj.remove(FIXED_INTERVAL);
                obj.put(INTERVAL, fixedInterval);
            } else if (obj.containsKey(CALENDAR_INTERVAL)) {
                Object calendarInterval = obj.get(CALENDAR_INTERVAL);
                obj.remove(CALENDAR_INTERVAL);
                obj.put(INTERVAL, calendarInterval);
            }

            String interval = obj.getString(INTERVAL);

            //高版本不支持时间为小数
            if (interval.contains(".")) {
                TimeValue timeValue = TimeValue.parseTimeValue(interval, null, "settings");
                obj.put(INTERVAL , timeValue.getMillis() + "ms");
            }
        } catch (Exception e) {
            logger.warn("parse date histogram exception", e);
        }
    }
}
