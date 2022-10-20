package com.didichuxing.datachannel.arius.admin.common.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class ClusterDynamicConfigTypeCheckFunUtil {

    private ClusterDynamicConfigTypeCheckFunUtil() {
    }

    public static final Set<String> reBalanceEnableTypes  = new HashSet<>();

    public static final Set<String> allowReBalanceTypes   = new HashSet<>();

    public static final Set<String> allocationEnableTypes = new HashSet<>();

    public static final Set<String> masterBlockTypes      = new HashSet<>();
    public static final Set<String> bandwidthUnits        = new HashSet<>();
    static {
        reBalanceEnableTypes.add("all");
        reBalanceEnableTypes.add("primaries");
        reBalanceEnableTypes.add("replicas");
        reBalanceEnableTypes.add("none");

        allowReBalanceTypes.add("always");
        allowReBalanceTypes.add("indices_primaries_active");
        allowReBalanceTypes.add("indices_all_active");

        allocationEnableTypes.add("all");
        allocationEnableTypes.add("primaries");
        allocationEnableTypes.add("new_primaries");
        allocationEnableTypes.add("none");

        masterBlockTypes.add("all");
        masterBlockTypes.add("write");

        bandwidthUnits.addAll(Arrays.asList("k", "kb", "m", "mb", "g", "gb", "t", "tb", "p", "pb", "b"));
    }

    public static boolean percentCheck(String value) {
        String reg = "^(100|[1-9]?\\d(\\.\\d\\d?)?)%$";
        return value.matches(reg);
    }

    public static boolean floatCheck(String value) {
        try {
            Float.valueOf(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean floatValueCheck1to100(String value) {
        if (floatCheck(value)) {
            float number = Float.valueOf(value);
            if (number < 100 && number > 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean floatValueCheckPositive(String value) {
        if (floatCheck(value)) {
            float number = Float.valueOf(value);
            if (number >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean timeCheck(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        int length = value.length();

        if (value.charAt(length - 1) != 's') {
            return false;
        }

        int number = 0;
        try {
            number = Integer.valueOf(value.substring(0, length - 1));
        } catch (NumberFormatException e) {
            return false;
        }

        if (number < 0 || number > 120) {
            return false;
        }

        return true;
    }

    public static boolean intCheck(String value) {
        String point = ".";
        int number = 0;
        if (!value.contains(point)) {
            try {
                number = Integer.valueOf(value);
                if (number > 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean intCheck1000(String value) {
        if (intCheck(value)) {
            int number = Integer.valueOf(value);
            return number > 1000;
        }
        return false;
    }

    public static boolean booleanCheck(String value) {
        if (value.equals("true") || value.equals("false")) {
            return true;
        }
        return false;
    }

    public static boolean reBalanceEnableTypeCheck(String value) {
        return reBalanceEnableTypes.contains(value);
    }

    public static boolean allowReBalanceTypeCheck(String value) {
        return allowReBalanceTypes.contains(value);
    }

    public static boolean attributesTypeCheck(String value) {
        return true;
    }

    public static boolean allocationEnableTypeCheck(String value) {
        return allocationEnableTypes.contains(value);
    }

    public static boolean masterBlockTypeCheck(String value) {
        return masterBlockTypes.contains(value);
    }

    public static boolean defaultTypeCheck(String value) {
        return false;
    }

    public static boolean bandwidthCheck(String proStoreSize) {
        BigDecimal bandwidth = null;
        try {
            String lowerSValue = proStoreSize.toLowerCase().trim();
            String bandwidthUnit = StringUtils.right(lowerSValue, 2);
            if (ClusterDynamicConfigTypeCheckFunUtil.bandwidthUnits.contains(bandwidthUnit)) {
                bandwidth = new BigDecimal(StringUtils.left(lowerSValue, lowerSValue.length() - 2).trim());
            } else {
                bandwidthUnit = StringUtils.right(lowerSValue, 1);
                if (ClusterDynamicConfigTypeCheckFunUtil.bandwidthUnits.contains(bandwidthUnit)) {
                    bandwidth = new BigDecimal(StringUtils.left(lowerSValue, lowerSValue.length() - 1).trim());
                }
            }
        } catch (Exception e) {
            //pass
        }
        return bandwidth != null;
    }

    public static boolean stringListCheck(String value) {
        return true;
    }
}
