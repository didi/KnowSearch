package com.didichuxing.datachannel.arius.admin.common.util;

import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.arius.admin.common.constant.template.TemplateDeployStatusEnum;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateWithPhyTemplates;

/**
 * @author d06679
 * @date 2019/4/3
 */
public class TemplateUtils {

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
}
