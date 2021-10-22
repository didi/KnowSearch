package com.didichuxing.datachannel.arius.admin.common.bean.entity.weekly;

import com.didichuxing.datachannel.arius.admin.common.bean.entity.BaseEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: CT17534
 * @date: 2020-03-15 19:56
 */
@Data
public class AppQuery extends BaseEntity {
    /**
     * 模板名称
     */
    private String       indiceSample;

    /**
     * dsl查询模版
     */
    private String       dslTemplate;

    /**
     * dsl查询时间
     */
    private String       timeStamp;

    /**
     * 模版MD5
     */
    private List<String> dslTemplateMd5;

    /**
     * 查询耗时
     */
    private Integer      totalCost;

}
