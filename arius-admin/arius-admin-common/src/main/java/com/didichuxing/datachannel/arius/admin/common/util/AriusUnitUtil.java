package com.didichuxing.datachannel.arius.admin.common.util;

/**
 * 单位转换工具类.
 *
 * @ClassName AriusUnitUtil
 * @Author gyp
 * @Date 2022/8/27
 * @Version 1.0
 */
public class AriusUnitUtil {

    public static String TIME = "time";
    public static String SIZE = "size";
    public static String COMMON = "common";

    /**
     *  将其他单位转换为基础单位
     * @param value
     * @param unit
     * @return
     */
    public static long unitChange(long value, String unit, String unitStyle) {
        switch (unitStyle) {
            case "time":
                value = AriusDateUtils.getUnitTime(value, unit.toLowerCase());
                break;
            case "size":
                value = SizeUtil.getUnitSize(value + unit.toLowerCase());
                break;
            case "common":
            default:
                break;
        }
        return value;
    }
}