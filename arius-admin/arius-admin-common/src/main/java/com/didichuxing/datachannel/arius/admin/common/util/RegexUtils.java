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
    
    private static final String ROLLOVER_ERROR_PATTERN = "index \\[\\w*-\\w*/\\w*] already exists";
    
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
     * 它接受一个 ESAlreadyExistsException 并从异常返回错误消息
     *
     * @param e 异常对象
     * @return 来自异常的错误消息。
     */
    public static String matchExceptionErrorByESAlreadyExistsException(ESAlreadyExistsException e) {
        final Pattern compile = compile(ROLLOVER_ERROR_PATTERN);
        final Matcher matcher = compile.matcher(e.getMessage());
        String error = "";
        if (matcher.find()) {
            error = matcher.group();
        }
        return error;
    }
    
}