package com.didichuxing.datachannel.arius.admin.metadata.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class MonitorUtil {

    private MonitorUtil() {
    }

    public static final String COLLECTION_ROUTE_SPLIT            = ".";

    public static final String COLLECTION_ROUTE_COMPUTE_DIVISION = "/";

    public static Object getValueByRoute(Map map, String route) {
        if (StringUtils.isEmpty(route)) {
            return null;
        }

        if (map == null || map.size() == 0) {
            return null;
        }

        //这个逻辑是实现两个指标相除加的;后面如果还有其他的算法,可以独立出去;根据符号来运算
        if (route.contains(COLLECTION_ROUTE_COMPUTE_DIVISION)) {
            String[] routers = route.split(COLLECTION_ROUTE_COMPUTE_DIVISION);
            Double dividend = obj2Double(getValueByRoute(map, routers[0]));
            Double divisor = obj2Double(getValueByRoute(map, routers[1]));

            if (dividend == null || divisor == null || divisor == 0 || dividend + divisor == 0) {
                return 0d;
            } else {
                return dividend / (divisor * 1.0);
            }
        }

        if (route.contains(COLLECTION_ROUTE_SPLIT)) {
            int offset = route.indexOf(COLLECTION_ROUTE_SPLIT);
            Object object = map.get(route.substring(0, offset));
            if (object instanceof Map) {
                return getValueByRoute((Map) object, route.substring(offset + COLLECTION_ROUTE_SPLIT.length()));
            } else {
                return null;
            }
        } else {
            Object object = map.get(route);
            if (object instanceof Map) {
                return null;
            } else {
                return object;
            }
        }
    }

    public static Double obj2Double(Object obj) {
        if (null == obj) {
            return 0d;
        }

        if (obj instanceof Number) {
            return Double.valueOf(obj.toString());
        } else {
            return 0d;
        }
    }
}
