package com.didichuxing.datachannel.arius.admin.common.bean.entity.access;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/3/4 下午8:07
 * @modified By D10865
 *
 * 某个索引模板历史查询次数统计请求参数
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateAccessHistory {
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 索引模板名称
     */
    private String templateName;

    /**
     * 是否有效
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isValid() {
        return !(StringUtils.isBlank(templateName) || StringUtils.isBlank(clusterName));
    }

}
