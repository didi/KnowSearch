package com.didichuxing.datachannel.arius.admin.common.bean.entity.access;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/3/4 下午5:34
 * @modified By D10865
 *
 * 某个索引模板具体索引查询次数统计请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateAccessDetail {
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 索引模板名称
     */
    private String templateName;
    /**
     * 统计日期
     */
    private String countDate;
    /**
     * 是否有效
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isValid() {
        if (StringUtils.isBlank(templateName) || StringUtils.isBlank(countDate) || StringUtils.isBlank(clusterName)) {
            return false;
        }

        return true;
    }
}
