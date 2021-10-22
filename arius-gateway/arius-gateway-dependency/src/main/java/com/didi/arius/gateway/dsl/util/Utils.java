package com.didi.arius.gateway.dsl.util;

import org.apache.commons.lang.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/7 下午3:55
 * @Modified By
 */
public class Utils {

    /**
     * 在日志文件中，打印异常堆栈
     * @param e
     * @return
     */
    public static String logExceptionStack(Throwable e) {
        StringWriter errorsWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(errorsWriter));
        return getPrefix(errorsWriter.toString());
    }

    /**
     * 获取MD5值
     *
     * @param str
     * @return
     */
    public static String getMD5WithVersion(String str, String version) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes("utf-8"));
            return version.concat(toHex(bytes));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取错误前4096个字符
     * @param str
     * @return
     */
    public static String getPrefix(String str) {
        if (null == str) {
            return "";
        }
        String content = str.length() > 4096 ? str.substring(0, 4095) + "..." : str;
        //对回车符进行替换
        if (content.contains("\n")) {
            //如果结尾含有一个回车符
            int first = content.indexOf("\n");
            int end = content.lastIndexOf("\n");
            if (first == end) {
                content = content.replaceAll("\r|\n*", " ");
            } else {
                content = content.replaceAll("[\\t\\n\\r]", " ");
            }
        }
        return content;
    }

    /**
     * 添加非空元素
     *
     * @param set
     * @param item
     */
    public static void addSetItem(Set<String> set, String item) {
        if (StringUtils.isNotBlank(item)) {
            set.add(item);
        }
    }

    /**
     * 添加非空元素
     *
     * @param set
     * @param items
     */
    public static void addSetItemWithCommSplit(Set<String> set, String items) {
        if (StringUtils.isNotBlank(items)) {
            for (String item : StringUtils.splitByWholeSeparatorPreserveAllTokens(items, ",")) {
                set.add(item);
            }
        }
    }

    /**
     *  去除结尾的一个逗号
     *
     * @param str
     * @return
     */
    public static String trimEndComma(String str) {
        if (StringUtils.isNotBlank(str) && str.charAt(str.length() - 1) == ',') {
            str = str.substring(0, str.length() -1);
        }
        return str;
    }

    /**
     * 获取最大long值
     *
     * @param longList
     * @return
     */
    public static Long getMaxLong(List<Long> longList) {
        if (longList == null || longList.size() == 0) {
            return 0L;
        }
        return Collections.max(longList);
    }

    /**
     * 获取第一个string
     *
     * @param strList
     * @return
     */
    public static String getFirst(List<String> strList) {
        if (strList == null || strList.size() == 0) {
            return "";
        }
        return strList.get(0);
    }


    private static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

}
