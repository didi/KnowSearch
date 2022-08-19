package com.didichuxing.datachannel.arius.admin.common.util;

import static java.util.regex.Pattern.compile;

import java.util.regex.Pattern;

/**
 * regex utils 此类包含用于处理正则表达式的静态方法。
 *
 * @author shizeying
 * @date 2022/08/17
 */
public final class RegexUtils {
    private RegexUtils() {
    }
    
    
    /**
     * 它检查索引名称是否以连字符结尾，后跟一个或多个数字
     *
     * @param indexName 要检查的索引的名称。
     * @return 一个布尔值。
     */
    public static boolean checkEndWithHyphenNumbers(String indexName) {
        Pattern regex = compile("-\\d+$");
        return regex.matcher(indexName).find();
    }
    
    
    
}