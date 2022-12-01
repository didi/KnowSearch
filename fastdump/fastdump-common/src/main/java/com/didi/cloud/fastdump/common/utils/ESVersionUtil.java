package com.didi.cloud.fastdump.common.utils;

import static java.util.regex.Pattern.compile;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.didi.cloud.fastdump.common.enums.ESClusterVersionEnum;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class ESVersionUtil {
    public static final String BIG_VERSION_PREFIX_PATTERN = "^\\d*.\\d*";
    public static final String VERSION_PREFIX_PATTERN = "^\\d*.\\d*.\\d*";

    private ESVersionUtil() {
    }

    /**
     * 判断是不是标准的es版本号，如：6.6.1.1000 或者 6.6.1
     * @param version
     * @return
     */
    public static boolean isValid(String version) {
        if (StringUtils.isBlank(version)) {
            return false;
        }

        String[] vers = version.split("\\.");
        if (null == vers) {
            return false;
        }

        for (String ver : vers) {
            if (!ver.chars().allMatch(Character::isDigit)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取当前版本的es版本号
     * @param version 版本名称
     * @return 版本长度
     */
    public static int getVersionLength(String version) {
        if (StringUtils.isBlank(version)) {
            return 0;
        }

        return version.split("\\.").length;
    }

    /**
     * 判断下version1是不是比version2的版本高
     * @param version1
     * @param version2
     * @return
     */
    public static boolean isHigher(String version1, String version2) {
        if (!isValid(version1) || !isValid(version2)) {
            return false;
        }

        String[] vers1 = version1.split("\\.");
        String[] vers2 = version2.split("\\.");

        //小版本兼容
        List<String> vers1List = Lists.newArrayList(vers1);
        List<String> vers2List = Lists.newArrayList(vers2);

        int vers1Size = vers1List.size();
        int vers2Size = vers2List.size();

        if (vers1Size > vers2Size) {
            for (int i = 0; i < vers1Size - vers2Size; i++) {
                vers2List.add("0");
            }
        }

        if (vers2Size > vers1Size) {
            for (int i = 0; i < vers2Size - vers1Size; i++) {
                vers1List.add("0");
            }
        }
        int ver1Count = 0, ver2Count = 0;
        for (int i = 0; i < vers1List.size(); i++) {
            int number = i == (vers1List.size() - 1) ? 1 : (100000000 / (int) Math.pow(100, i));
            ver1Count = ver1Count + Integer.parseInt(vers1List.get(i)) * number;
            ver2Count = ver2Count + Integer.parseInt(vers2List.get(i)) * number;
        }
        return ver1Count > ver2Count;
    }
    
    /**
     * 比较大版本一致性 小版本之间是属于相同的版本：7.6.0==7.6.1 大版本之前的版本是不同的：7.6.0！=7.5.0
     *
     * @param esVersion1 7.6.x
     * @param esVersion2 7.6.x
     * @return boolean    true
     */
    public static Boolean compareBigVersionConsistency(String esVersion1, String esVersion2) {
        if (StringUtils.isBlank(esVersion1) || StringUtils.isBlank(esVersion2)) {
            return Boolean.FALSE;
        }
        if (StringUtils.equals(esVersion1, esVersion2)) {
            return Boolean.TRUE;
        }
        return StringUtils.equals(getESBigVersion(esVersion1), getESBigVersion(esVersion2));
    }
    
    /**
     * 获取es的大版本前缀
     *
     * @param esVersion
     * @return {@link String}
     */
    public static String getESBigVersion(String esVersion) {
        Pattern pattern = compile(BIG_VERSION_PREFIX_PATTERN);
        final Matcher matcher = pattern.matcher(esVersion);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    /**
     * 获取es原生版本
     * @param esVersion
     * @return
     */
    public static String getNativeESBigVersion(String esVersion) {
        Pattern pattern = compile(VERSION_PREFIX_PATTERN);
        final Matcher matcher = pattern.matcher(esVersion);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public static boolean checkSupport(String esVersion) {
        for (ESClusterVersionEnum state : ESClusterVersionEnum.values()) {
            if (Objects.equals(state.getVersion(), esVersion)) { return true;}
        }
        return false;
    }

    public static boolean supportMultipleType(String esVersion) {
        return isHigher(ESClusterVersionEnum.ES_7_0_0.getVersion(), esVersion);
    }
}