package com.didi.cloud.fastdump.common.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class CommonUtils {

    protected static final Logger LOGGER        = LoggerFactory.getLogger(CommonUtils.class);

    private static final String REGEX  = ",";

    private CommonUtils() {
    }

    /**
     * 获取MD5值
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 保留指定的小数位 四舍五入
     * @param data    需要转换的数据
     * @param decimal 保留小数的位数 默认是2
     * @return
     */
    public static double formatDouble(double data, int decimal) {
        if (decimal < 0) {
            decimal = 2;
        }

        BigDecimal b = BigDecimal.valueOf(data);
        return b.setScale(decimal, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double divideIntAndFormatDouble(int v1, int v2, int scale, int multiply) {
        BigDecimal v1Decimal = new BigDecimal(v1);
        BigDecimal v2Decimal = new BigDecimal(v2);
        BigDecimal bigDecimal = new BigDecimal(multiply);

        BigDecimal greenDivide = v1Decimal.divide(v2Decimal, scale, 1);
        return greenDivide.multiply(bigDecimal).doubleValue();
    }

    public static double divideDoubleAndFormatDouble(double v1, double v2, int scale, int multiply) {
        BigDecimal v1Decimal = new BigDecimal(v1);
        BigDecimal v2Decimal = new BigDecimal(v2);
        BigDecimal bigDecimal = new BigDecimal(multiply);
  
        BigDecimal greenDivide = v2==0?new BigDecimal(0):v1Decimal.divide(v2Decimal, scale, 1);
        return greenDivide.multiply(bigDecimal).doubleValue();
    }

    public static String toHex(byte[] bytes) {

        final char[] hexDigits = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
            ret.append(hexDigits[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    public static String strList2String(List<String> strList) {
        if (strList == null || strList.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String elem : strList) {
            if (!StringUtils.hasText(elem)) {
                continue;
            }
            sb.append(elem).append(REGEX);
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : sb.toString();
    }

    public static List<String> string2StrList(String str) {
        if (!StringUtils.hasText(str)) {
            return new ArrayList<>();
        }
        List<String> strList = new ArrayList<>();
        for (String elem : str.split(REGEX)) {
            if (!StringUtils.hasText(elem)) {
                continue;
            }
            strList.add(elem);
        }
        return strList;
    }

    public static Long monitorTimestamp2min(Long timestamp) {
        return timestamp - timestamp % 60000;
    }

    /**
     * 字符串追加
     *
     * @param items
     * @return
     */
    public static String strConcat(List<String> items) {
        StringBuilder stringBuilder = new StringBuilder(128);
        boolean isFirstItem = true;

        for (String item : items) {
            if (isFirstItem) {
                stringBuilder.append(String.format("\"%s\"", item));
                isFirstItem = false;
            } else {
                stringBuilder.append(",").append(String.format("\"%s\"", item));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * 判断是否为合法IP
     * @return the ip
     */
    public static boolean checkIp(String addr) {
        if (addr.length() < 7 || addr.length() > 15 || "".equals(addr)) {
            return false;
        }

        String rexp1 = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\\.";
        String rexp2 = "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\\.";
        String rexp3 = "(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";

        Pattern pat = Pattern.compile(rexp1 + rexp2 + rexp2 + rexp3);
        Matcher mat = pat.matcher(addr);

        return mat.find();

    }

    /**
     * 生成固定长度的随机字符串
     */
    public static String randomString(int length) {
        char[] value = new char[length];
        for (int i = 0; i < length; i++) {
            value[i] = randomWritableChar();
        }
        return new String(value);
    }

    /**
     * 随机生成单个随机字符
     */
    public static char randomWritableChar() {
        Random random = new Random();
        return (char) (33 + random.nextInt(94));
    }

    public static String getUniqueKey(String... arg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arg.length; i++) {
            if (i == (arg.length - 1)) {
                sb.append(arg[i]);
            } else {
                sb.append(arg[i]).append("@");
            }
        }
        return sb.toString();
    }
     public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * 避免模糊查询把查询条件中的"% _"当作通配符处理（造成结果是全量查询）
     */
    public static String sqlFuzzyQueryTransfer(String str){
        if(str.contains("%")){
            str = str.replaceAll("%", "\\\\%");
        }
        if(str.contains("_")){
            str = str.replaceAll("_","\\\\_");
        }
        return str;
    }

}