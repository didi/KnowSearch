package com.didichuxing.datachannel.arius.admin.common.util;

import static java.util.regex.Pattern.compile;

import com.didiglobal.logi.elasticsearch.client.model.exception.ESAlreadyExistsException;
import java.util.regex.Matcher;
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
    
    
    /**
     * 它接受一个 ESAlreadyExistsException 并返回一个包含已存在的索引名称的字符串
     *
     * @param e 抛出的异常。
     * @return 来自异常的错误消息。
     */
    public static String matchAlreadyExistsErrorByESAlreadyExistsException(ESAlreadyExistsException e) {
        final Pattern compile = compile("index \\[.+\\d+/\\w+] already exists");
        final Matcher matcher = compile.matcher(e.getMessage());
        String error = "";
        if (matcher.find()) {
            final String group = matcher.group();
            final Pattern pattern = compile("\"index \\[.+\\d+/\\w+] already exists$");
            final Matcher m = pattern.matcher(group);
            if (m.find()) {
                error = m.group().replace("\"","");
            }
        }
        return error;
    }
    
}