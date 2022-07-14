package com.didichuxing.datachannel.arius.admin.common.util;

import java.lang.reflect.Type;

import com.alibaba.fastjson.JSON;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class AriusJSON {

    private AriusJSON() {
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return JSON.toJSONString(obj);
        }
    }

    public static <T> T toObject(String json, Type resultType) {
        if (resultType instanceof Class) {
            Class<T> clazz = (Class<T>) resultType;
            return toObject(json, clazz);
        }
        return JSON.parseObject(json, resultType);
    }

    public static <T> T toObject(String json, Class<T> resultType) {
        if (resultType.isAssignableFrom(String.class)) {
            return resultType.cast(json);
        }
        return JSON.parseObject(json, resultType);
    }

}
