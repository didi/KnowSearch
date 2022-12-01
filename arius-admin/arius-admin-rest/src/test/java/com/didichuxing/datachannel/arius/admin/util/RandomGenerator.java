package com.didichuxing.datachannel.arius.admin.util;

import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class RandomGenerator {

    private RandomGenerator() {
    }

    private static final Random random = new Random();

    private static final ILog LOGGER = LogFactory.getLog(RandomGenerator.class);

    public static Integer randomInt(int low, int high) {
        if (high <= low) {
            throw new IllegalArgumentException("bound should be higher");
        }
        return low + random.nextInt(high - low);
    }

    public static Double randomDouble(double low, double high) {
        if (high <= low) {
            throw new IllegalArgumentException("bound should be higher");
        }
        return random.nextDouble() * (high - low) + low;
    }

    public static String randomString(int length) {
        char[] value = new char[length];
        for (int i = 0; i < length; i++) {
            value[i] = randomWritableChar();
        }
        return new String(value);
    }

    public static char randomWritableChar() {
        return (char) (97 + random.nextInt(26));
    }

    public static <T> T randomFromEnum(Class<? extends Enum> c) {
        try {
            Method method = c.getMethod("values");
            T[] ts = (T[]) method.invoke(c);
            int index = new Random().nextInt(ts.length);
            return ts[index];
        } catch (NoSuchMethodException e) {
            LOGGER.error("class=RandomGenerator||method=randomFromEnum||errMsg=get enum failed", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("class=RandomGenerator||method=randomFromEnum||errMsg=", e);
        }
        return null;
    }

    public static boolean randomBoolean() {
        return new Random().nextInt() % 2 == 0;
    }
}
