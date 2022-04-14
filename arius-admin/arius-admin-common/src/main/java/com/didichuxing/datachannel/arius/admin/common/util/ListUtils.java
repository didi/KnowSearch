package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * @author arthur
 * @date 2019/7/30.
 */
public class ListUtils {

    private ListUtils(){}

    // 分隔符
    private static final String SEPARATOR = ",";

    public static List<Integer> string2IntList(String str) {

        List<Integer> intList = new ArrayList<>();
        if (StringUtils.isBlank(str)) {
            return intList;
        }

        for (String elem : str.split(SEPARATOR)) {
            if (StringUtils.isBlank(elem)) {
                continue;
            }
            intList.add(Integer.valueOf(elem.trim()));
        }
        return intList;
    }

    public static List<Long> string2LongList(String str) {
        List<Long> longList = new ArrayList<>();
        if (StringUtils.isBlank(str)) {
            return longList;
        }

        for (String elem : str.split(SEPARATOR)) {
            if (StringUtils.isBlank(elem)) {
                continue;
            }
            longList.add(Long.valueOf(elem.trim()));
        }
        return longList;
    }

    public static Set<String> string2StrSet(String str) {
        List<String> strList = string2StrList(str);
        return new HashSet<>(strList);
    }

    public static List<String> string2StrList(String str) {
        List<String> strList = new ArrayList<>();
        if (StringUtils.isBlank(str)) {
            return strList;
        }

        for (String elem : str.split(SEPARATOR)) {
            if (StringUtils.isBlank(elem)) {
                continue;
            }
            strList.add(elem.trim());
        }
        return strList;
    }

    public static String longList2String(List<Long> longList) {
        if (longList == null || longList.isEmpty()) {
            return "";
        }

        longList = longList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return StringUtils.join(longList, SEPARATOR);
    }

    public static String intList2String(List<Integer> intList) {
        if (intList == null || intList.isEmpty()) {
            return "";
        }

        intList = intList.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return StringUtils.join(intList, SEPARATOR);
    }

    public static String strList2String(List<String> strList) {
        if (strList == null || strList.isEmpty()) {
            return "";
        }

        strList = strList.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());

        return StringUtils.join(strList, SEPARATOR);
    }

    public static String strSet2String(Set<String> strSet) {
        if (strSet == null || strSet.isEmpty()) {
            return "";
        }

        strSet = strSet.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        return StringUtils.join(strSet, SEPARATOR);
    }

    public static String[] strList2StringArray(List<String> strList) {
        String[] stringArr = new String[strList.size()];
        for (int i = 0; i < strList.size(); i++) {
            stringArr[i] = strList.get(i);
        }

        return stringArr;
    }
}
