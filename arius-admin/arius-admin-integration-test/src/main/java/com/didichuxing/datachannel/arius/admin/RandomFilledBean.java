package com.didichuxing.datachannel.arius.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class RandomFilledBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomFilledBean.class);

    public static <T> T construct(Class<T> cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = cls.getConstructor();
        return (T) constructor.newInstance();
    }

    public static <T> void fill(T bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("createTime".equals(field.getName()) || "updateTime".equals(field.getName()) || "id".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Class cls = field.getType();
            try {
                if (String.class.isAssignableFrom(cls)) {
                    field.set(bean, RandomGenerator.randomString(10));
                } else if (Number.class.isAssignableFrom(cls)) {
                    if (cls == Integer.class) {
                        field.set(bean, RandomGenerator.randomInt(0, 1));
                    } else if (cls == Long.class) {
                        field.set(bean, Long.valueOf(RandomGenerator.randomInt(0, 100000)));
                    } else if (cls == Double.class) {
                        field.set(bean, RandomGenerator.randomDouble(0, 1));
                    } else if (cls == BigDecimal.class) {
                        field.set(bean, BigDecimal.valueOf(RandomGenerator.randomInt(0, 100000)));
                    }
                } else if (cls == Boolean.class) {
                    field.set(bean, RandomGenerator.randomBoolean());
                } else if (cls == Date.class) {
                    field.set(bean, new Date());
                } else if (Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls)) {
                    field.set(bean, null);
                } else if (Enum.class.isAssignableFrom(cls)) {
                    field.set(bean, RandomGenerator.randomFromEnum(cls));
                } else {
                    field.set(bean, null);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("field generate failed", e);
            }
        }
    }

    public static <T> T getRandomBeanOfType(Class<T> cls) {
        T t = null;
        try {
            t = construct(cls);
            fill(t);
        } catch (NoSuchMethodException e) {
            LOGGER.error("field generate failed", e);
            return null;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOGGER.error("", e);
        }
        return t;
    }
}
