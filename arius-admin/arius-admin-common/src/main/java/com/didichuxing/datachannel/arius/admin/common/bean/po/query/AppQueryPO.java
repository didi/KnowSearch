package com.didichuxing.datachannel.arius.admin.common.bean.po.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获取AppId查询信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppQueryPO {

    /**
     * 模板名称
     */
    private String  indiceSample;
    /**
     * dsl查询模版
     */
    private String  dslTemplate;
    /**
     * dsl查询时间 yyyy-MM-dd HH:mm:ss
     */
    private String  timeStamp;
    /**
     * 模版MD5
     */
    private List<String> dslTemplateMd5;
    /**
     * 查询耗时
     */
    private Integer totalCost;
}
