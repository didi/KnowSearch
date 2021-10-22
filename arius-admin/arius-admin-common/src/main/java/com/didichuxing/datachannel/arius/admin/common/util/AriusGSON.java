package com.didichuxing.datachannel.arius.admin.common.util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author d06679
 * @date 2019/3/18
 */
public class AriusGSON {

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else {
            return new Gson().toJson(obj);
        }
    }

    public static <T> T toObject(String json, Type resultType) {
        if (resultType instanceof Class) {
            Class<T> clazz = (Class<T>) resultType;
            return toObject(json, clazz);
        }
        return new Gson().fromJson(json, resultType);
    }

    public static <T> T toObject(String json, Class<T> resultType) {
        if (resultType.getName().equals(String.class.getName())) {
            return resultType.cast(json);
        }
        return new Gson().fromJson(json, resultType);
    }

    /**
     * AriusJSON.toList(result.getMessage(), new TypeToken<List<Label>>() {
     })
     * @param json
     * @param typeToken
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(String json, TypeToken typeToken) {
        return new Gson().fromJson(json, typeToken.getType());
    }

}
