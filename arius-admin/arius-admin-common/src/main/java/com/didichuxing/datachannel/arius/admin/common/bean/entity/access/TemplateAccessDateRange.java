package com.didichuxing.datachannel.arius.admin.common.bean.entity.access;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: D10865
 * @description:
 * @date: Create on 2019/3/4 下午4:29
 * @modified By D10865
 *
 * 索引模板维度查询次数统计请求参数
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class TemplateAccessDateRange {

    /**
     * 统计起始时间
     */
    private String startDate;
    /**
     * 统计结束时间
     */
    private String endDate;
    /**
     * 是否有效
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isValid() {
        return !(StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate));
    }
}