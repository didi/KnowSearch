package com.didichuxing.datachannel.arius.admin;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.constant.result.ResultType;
import com.didichuxing.datachannel.arius.admin.common.exception.AriusRunTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompareUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareUtils.class);

    private CompareUtils() {}

    /**
     * src为用户传入的参数，dst为系统返回，src和dst类型不需要相同，dst中可能包含src中没有的属性
     * 如果设置ignore对象的子对象属性，可以用'.'分隔，ignore属性只针对src
     *
     * @param src
     * @param dst
     * @param ignoreFields
     * @return
     */
    public static boolean objectEquals(Object src, Object dst, String... ignoreFields) {
        if (src == null) {
            LOGGER.warn("class=CompareUtils||method=objectEquals||msg=src and dst is null");
            return dst == null;
        } else if (dst == null) {
            return false;
        }
        Class<?> c = src.getClass();
        Field[] fields = c.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            if ("id".equals(name)) {
                continue;
            }
            if (search(ignoreFields, name) >= 0) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object srcValue = field.get(src);
                Object dstValue = field.get(dst);
                if (srcValue == null) {
                    return dstValue == null;
                }
                if (srcValue.getClass() != dstValue.getClass()) {
                    LOGGER.error("class=CompareUtils||method=objectEquals||msg=type not match, field name: {}, src type: {}, dst type: {}", name, srcValue.getClass(), dstValue.getClass());
                    return false;
                }
                if (isJsonTypeOrObject(srcValue)) {
                    if (!srcValue.equals(dstValue)) {
                        LOGGER.error("class=CompareUtils||method=objectEquals||errMsg=field not equals, name: {}, src: {}, dst: {}", name, srcValue, dstValue);
                        return false;
                    }
                } else {
                    String[] nextFields = multiSlice(ignoreFields, '.');
                    if (List.class.isAssignableFrom(srcValue.getClass())) {
                        if (!listEqualsIgnoreOrder((List) srcValue, (List) dstValue, nextFields)) {
                            return false;
                        }
                    } else {
                         if (!objectEquals(srcValue, dstValue, nextFields)) {
                             return false;
                         }
                    }
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("", e);
                throw new AriusRunTimeException("", ResultType.FAIL);
            }
        }
        return true;
    }

    public static String[] multiSlice(String[] s, char c) {
        List<String> result = new ArrayList<>();
        for (String s1 : s) {
            int index = s1.indexOf(c);
            if (index >= 0) {
                result.add(s1.substring(index + 1));
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * dst为系统返回列表，不可少于src
     *
     * @param src
     * @param dst
     * @param ignoreFields
     * @return
     */
    public static boolean listEqualsIgnoreOrder(List<?> src, List<?> dst, String... ignoreFields) {
        if (src == null) {
            LOGGER.warn("class=CompareUtils||method=listEqualsIgnoreOrder||msg=src and dst is null");
            return dst == null;
        } else if (dst == null) {
            return false;
        }
        if (src.size() != dst.size()) {
            LOGGER.error("class=CompareUtils||method=listEqualsIgnoreOrder||errMsg=size not equal, src: {}, dst: {}", src.size(), dst.size());
            return false;
        }
        for (int i = 0; i < src.size(); i++) {
            boolean found = false;
            for (int j = 0; j < dst.size(); j++) {
                if (objectEquals(src.get(i), dst.get(j), ignoreFields)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LOGGER.error("class=CompareUtils||method=listEqualsIgnoreOrder||errMsg=no equal element found in dst, index: {}, element: {}", i, JSON.toJSONString(src.get(i)));
                return false;
            }
        }
        return true;
    }

    public static int search(String[] array, String s) {
        if (s == null) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < array.length; i++) {
                if (s.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 判断json对象属性的类型，包括bool, number, string, date
     *
     * @return true属性为基础类型 false属性为其他对象
     */
    public static boolean isJsonTypeOrObject(Object o) {
        Class<?> c = o.getClass();
        return String.class.isAssignableFrom(c) || Number.class.isAssignableFrom(c) || Boolean.class == c || Date.class == c;
    }
}
