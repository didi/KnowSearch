package com.didichuxing.datachannel.arius.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

public class RandomGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomGenerator.class);

    public static Integer randomInt(int low, int high) {
        if (high <= low) {
            throw new IllegalArgumentException("bound should be higher");
        }
        Random random = new Random();
        return low + random.nextInt(high - low);
    }

    public static Double randomDouble(double low, double high) {
        if (high <= low) {
            throw new IllegalArgumentException("bound should be higher");
        }
        Random random = new Random();
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
        Random random = new Random();
        return (char) (33 + random.nextInt(94));
    }

    public static <T> T randomFromEnum(Class<? extends Enum> c) {
        try {
            Method method = c.getMethod("values");
            T[] ts = (T[]) method.invoke(c);
            int index = new Random().nextInt(ts.length);
            return ts[index];
        } catch (NoSuchMethodException e) {
            LOGGER.error("get enum failed", e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("", e);
        }
        return null;
    }

    public static boolean randomBoolean() {
        return new Random().nextInt() % 2 == 0;
    }
}
