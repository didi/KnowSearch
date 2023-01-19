package com.didichuxing.datachannel.arius.admin.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

public class RandomFilledBean {

    private RandomFilledBean() {
    }

    private static final ILog LOGGER = LogFactory.getLog(RandomFilledBean.class);

    public static <T> T construct(Class<T> cls) throws NoSuchMethodException, IllegalAccessException,
                                                InvocationTargetException, InstantiationException {
        Constructor<T> constructor = cls.getConstructor();
        return constructor.newInstance();
    }

    public static <T> void fill(T bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ("createTime".equals(field.getName()) || "updateTime".equals(field.getName())
                || "id".equals(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Class<?> cls = field.getType();
            try {
                setBean(bean, field, cls);
            } catch (IllegalAccessException e) {
                LOGGER.error("class=RandomFilledBean||method=fill||errMsg=field generate failed");
            }
        }
    }

    private static <T> void setBean(T bean, Field field, Class cls) throws IllegalAccessException {
        if (String.class.isAssignableFrom(cls)) {
            field.set(bean, RandomGenerator.randomString(RandomConfig.STRING_LENGTH));
        } else if (Number.class.isAssignableFrom(cls)) {
            setBeanRandom(bean, field, cls);
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
    }

    private static <T> void setBeanRandom(T bean, Field field, Class cls) throws IllegalAccessException {
        if (cls == Integer.class) {
            field.set(bean, RandomGenerator.randomInt(RandomConfig.INT_LOW, RandomConfig.INT_HIGH));
        } else if (cls == Long.class) {
            field.set(bean, Long.valueOf(RandomGenerator.randomInt(RandomConfig.LONG_LOW, RandomConfig.LONG_HIGH)));
        } else if (cls == Double.class) {
            field.set(bean, RandomGenerator.randomDouble(RandomConfig.DOUBLE_LOW, RandomConfig.DOUBLE_HIGH));
        } else if (cls == BigDecimal.class) {
            field.set(bean,
                BigDecimal.valueOf(RandomGenerator.randomDouble(RandomConfig.DOUBLE_LOW, RandomConfig.DOUBLE_HIGH)));
        }
    }

    public static <T> T getRandomBeanOfType(Class<T> cls) {
        T t = null;
        try {
            t = construct(cls);
            fill(t);
        } catch (NoSuchMethodException e) {
            LOGGER.error("class=RandomFilledBean||method=fill||errMsg=field generate failed");
            return null;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOGGER.error("class=RandomFilledBean||method=fill||errMsg=field generate failed");
        }
        return t;
    }
}
