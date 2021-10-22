package com.didichuxing.datachannel.arius.admin.common.util;

/**
 * @author d06679
 * @date 2019-07-26
 */
public class PercentUtils {

    public static int getWithLimit(Double value) {
        if (value == null) {
            return 0;
        }

        if (value > 1.0) {
            return 100;
        }

        if (value < 0.0) {
            return 0;
        }

        return (int) (value * 100);

    }

    public static String getStrWithLimit(Double value) {
        return getWithLimit(value) + "%";
    }

    public static int get(Double value) {
        if (value == null) {
            return 0;
        }

        return (int) (value * 100);

    }

}
