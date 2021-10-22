package com.didichuxing.datachannel.arius.admin.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.tunnel.util.log.ILog;
import com.didichuxing.tunnel.util.log.LogFactory;

/**
 * @author d06679
 * @date 2019/4/3
 */
public class IndexNameFactory {

    private static final ILog    LOGGER             = LogFactory.getLog(IndexNameFactory.class);

    private static final Pattern INDEX_NAME_PATTERN = Pattern.compile("(.*)(_v[1-9]\\d*)(.*)");

    public static List<String> appendSuffix(List<String> indices, String suffix) {
        if (indices == null) {
            return null;
        }

        return indices.stream().map(index -> index + suffix).collect(Collectors.toList());

    }

    /**
     * 生成带版本号的索引名字
     * @param expression 表达式
     * @param dateFormat 时间格式
     * @param days 天数
     * @param version  版本号
     * @return
     */
    public static String get(String expression, String dateFormat, Integer days, Integer version) {
        String indexName = getNoVersion(expression, dateFormat, days);
        if (version > 0) {
            indexName = indexName + "_v" + version;
        }
        return indexName;
    }

    /**
     * 生成某一天的索引
     * @param expression 表达式
     * @param dateFormat 时间格式
     * @param days 天数, 负数表示是之前某一天的索引,正数表示是之后某一天的索引
     * @return 索引名字
     */
    public static String getNoVersion(String expression, String dateFormat, Integer days) {

        String result = "";

        if (!expression.endsWith("*")) {
            result = expression;
        } else {

            if (StringUtils.isBlank(dateFormat)) {
                LOGGER.warn("class=IndexNameFactory||method=getNoVersion||expression={}||msg=illegal date format",
                    expression);
                dateFormat = "_yyyy-MM-dd";
            }

            Date now = new Date();
            String indexDate = "";
            if (days >= 0) {
                indexDate = AriusDateUtils.date2Str(AriusDateUtils.getAfterDays(now, days), dateFormat);
            } else {
                indexDate = AriusDateUtils.date2Str(AriusDateUtils.getBeforeDays(now, -1 * days), dateFormat);
            }
            result = expression.replace("*", "") + indexDate;
        }

        return result;
    }

    /**
     * 滚动索引去除版本号
     * @param indexName 索引名字
     * @return 无版本号的索引名字  如果不匹配 返回空字符串
     */
    public static String genIndexNameClear(String indexName, String expression) {
        if (indexName.equals(expression)) {
            return indexName;
        }

        if (expression.endsWith("*")) {
            expression = expression.replace("*", "");
        }

        if (indexName.equals(expression)) {
            return indexName;
        }

        Matcher m = INDEX_NAME_PATTERN.matcher(indexName);
        if (!m.find() || StringUtils.isNotBlank(m.group(3))) {
            return indexName;
        }
        return m.group(1);
    }

    /**
     * 校验是否与当前表示是匹配
     * @param noVersionIndexName 索引名字
     * @param expression 表达式
     * @param dateFormat 时间格式
     * @return 无版本号的索引名字  如果不匹配 返回空字符串
     */
    public static boolean noVersionIndexMatchExpression(String noVersionIndexName, String expression,
                                                        String dateFormat) {
        if (expression.endsWith("*")) {
            return noVersionIndexName.length() == (expression.length() - 1 + dateFormat.length());
        }
        return expression.equals(noVersionIndexName);
    }

    /**
     * 校验是否与当前表示是匹配
     * @param indexName 索引名字
     * @param expression 表达式
     * @param dateFormat 时间格式
     * @return 无版本号的索引名字  如果不匹配 返回空字符串
     */
    public static boolean indexMatchExpression(String indexName, String expression, String dateFormat) {
        return noVersionIndexMatchExpression(genIndexNameClear(indexName, expression), expression, dateFormat);
    }

    /**
     * 获取索引所在的时间
     * @param indexNameNoVersion 没有版本号的索引名字
     * @param expression expression
     * @param dateFormat dateFormat
     * @return 时间
     */
    public static Date genIndexTimeByIndexName(String indexNameNoVersion, String expression, String dateFormat) {
        String dateFormatyyyy = dateFormat.replace("Y", "y");

        if (dateFormatyyyy.contains("y")) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(dateFormatyyyy);
                return format.parse(indexNameNoVersion.substring(expression.length() - 1));
            } catch (Exception e) {
                LOGGER.warn("genIndexTimeByIndexName error||expression={}||dateFormat{}||indexNameNoVersion={}",
                    expression, dateFormatyyyy, indexNameNoVersion);
            }
        } else {

            Date now = new Date();

            //单独处理没有年份的场景
            //按着同一年计算  如果索引时间晚于当前时间5天则是去年的索引
            String indexTimeStr = indexNameNoVersion.substring(expression.length() - 1);

            //大部分场景都是在同一年的
            dateFormatyyyy = "yyyy" + dateFormatyyyy;
            indexTimeStr = AriusDateUtils.date2Str(now, "yyyy") + indexTimeStr;

            try {
                SimpleDateFormat format = new SimpleDateFormat(dateFormatyyyy);
                Date indexTime = format.parse(indexTimeStr);

                //如果索引时间不会晚于未来5天,则认为是与当前时间同一年的索引
                if (indexTime.before(AriusDateUtils.getAfterDays(now, 4))) {
                    return indexTime;
                } else {
                    return AriusDateUtils.getBeforeMonths(indexTime, 12);
                }

            } catch (Exception e) {
                LOGGER.warn("genIndexTimeByIndexName error||expression={}||dateFormat{}||indexNameNoVersion={}",
                    expression, dateFormatyyyy, indexNameNoVersion);
            }
        }

        return null;
    }
}
