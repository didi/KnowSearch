package com.didichuxing.datachannel.arius.admin.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;
import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployStatusEnum;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;

/**
 * @author d06679
 * @date 2019/4/3
 */
public class TemplateUtils {

    private static final ILog LOGGER        = LogFactory.getLog(TemplateUtils.class);
    private static final String REGEX_PATTERN_DAY   = "[0-9]{4}[-][0-9]{1,2}[-][0-9]{1,2}";
    private static final String REGEX_PATTERN_MONTH = "[0-9]{4}[-][0-9]{1,2}";
    private static final String REGEX_PATTERN_YEAR = "[0-9]{4}";

    /**
     * 兼容匹配平台升级版本后的索引
     * template6_v1, template6_v2
     */
    private static final String REGEX_PATTERN_NO_PARTITION  = "[v][0-9]{1,3}";

    private TemplateUtils() {
    }

    /**
     * 模板是否是按天创建
     * @param dateFormat 时间格式
     * @return true/false
     */
    public static boolean isSaveByDay(String dateFormat) {
        if (StringUtils.isBlank(dateFormat)) {
            return false;
        }

        return dateFormat.toLowerCase().contains("dd");
    }

    /**
     * 模板是否是按天创建
     * @param dateFormat 时间格式
     * @return true/false
     */
    public static boolean isSaveByMonth(String dateFormat) {
        if (StringUtils.isBlank(dateFormat)) {
            return false;
        }

        if (isSaveByDay(dateFormat)) {
            return false;
        }

        return dateFormat.toLowerCase().contains("mm");
    }

    /**
     * 无滚动  就一个索引判断
     * @param expression 表达式
     * @return true/false
     */
    public static boolean isOnly1Index(String expression) {
        return !expression.endsWith("*");
    }

    /**
     * 生成部署信息
     * @param logicWithPhysical
     * @return
     */
    public static Integer genDeployStatus(IndexTemplateWithPhyTemplates logicWithPhysical) {
        if (!logicWithPhysical.hasPhysicals()) {
            return TemplateDeployStatusEnum.MASTER_SLAVE_OFFLINE.getCode();
        }

        if (logicWithPhysical.getMasterPhyTemplate() != null && logicWithPhysical.getSlavePhyTemplate() != null) {
            return TemplateDeployStatusEnum.MASTER_SLAVE_ONLINE.getCode();
        }

        if (logicWithPhysical.getMasterPhyTemplate() != null) {
            return TemplateDeployStatusEnum.ONLY_MASTER_ONLINE.getCode();
        }

        return TemplateDeployStatusEnum.NO_MASTER_ONLINE.getCode();
    }

    /**
     * 根据索引名称匹配平台模板名称
     * 目前平台仅支持2020-10-22、2020-10、2020 等三种时间后缀分区索引
     * @param indexName     索引名称
     * @return              模板名称,  null 代表不匹配平台模板
     */
    public  static String getMatchTemplateNameByIndexName(String indexName) {
        if (AriusObjUtils.isBlank(indexName)) { return null;}
        String[] temp;

        try {
            // 尝试匹配2020-10-22
            Pattern patternOfDay = Pattern.compile(REGEX_PATTERN_DAY);
            Matcher matcherOfDay = patternOfDay.matcher(indexName);
            if (matcherOfDay.find()) {
                temp = indexName.split(matcherOfDay.group(0));
                if (temp.length == 0) { return null;}
    
                // 获取索引前缀
                String template = temp[0];
                if (AriusObjUtils.isBlank(template)) { return null;}
                return template.substring(0, template.length() - 1);
            }

            // 尝试匹配2020-10
            Pattern patternOfMonth = Pattern.compile(REGEX_PATTERN_MONTH);
            Matcher matcherOfMonth = patternOfMonth.matcher(indexName);
            if (matcherOfMonth.find()) {
                temp = indexName.split(matcherOfMonth.group(0));
                if (temp.length == 0) { return null;}
    
                // 获取索引前缀
                String template = temp[0];
                if (AriusObjUtils.isBlank(template)) { return null;}
                return template.substring(0, template.length() - 1);
            }

            // 尝试匹配2020
            Pattern patternOfYear = Pattern.compile(REGEX_PATTERN_YEAR);
            Matcher matcherOfYear = patternOfYear.matcher(indexName);
            if (matcherOfYear.find()) {
                temp = indexName.split(matcherOfYear.group(0));
                if (temp.length == 0) { return null;}

                // 获取索引前缀
                String template = temp[0];
                if (AriusObjUtils.isBlank(template)) { return null;}
                return template.substring(0, template.length() - 1);
            }

            Pattern patternOfNoPartition = Pattern.compile(REGEX_PATTERN_NO_PARTITION);
            Matcher matcherOfNoPartition = patternOfNoPartition.matcher(indexName);
            if (matcherOfNoPartition.find()) {
                temp = indexName.split(matcherOfNoPartition.group(0));
                if (temp.length == 0) { return null;}

                // 获取索引前缀
                String template = temp[0];
                if (AriusObjUtils.isBlank(template)) { return null;}
                return template.substring(0, template.length() - 1);
            }

        } catch (Exception e) {
            LOGGER.error("class=TemplateUtils||method=getMatchTemplateNameByIndexName||errMsg={}", e.getMessage(), e);
        }

        return indexName;
    }
}
