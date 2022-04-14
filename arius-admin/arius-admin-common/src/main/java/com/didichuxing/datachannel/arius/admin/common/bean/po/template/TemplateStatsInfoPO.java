package com.didichuxing.datachannel.arius.admin.common.bean.po.template;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 索引的基本统计信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateStatsInfoPO {
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 索引健康分
     */
    private double indexHealthDegree;
    /**
     * 索引存储容量
     */
    private double store;
    /**
     * 索引qutoa
     */
    private double qutoa;
    /**
     * 索引成本
     */
    private double cost;
    /**
     * 索引昨日访问均量
     */
    private double   accessCountPreDay;
    /**
     * 索引文档数
     */
    private long   docNu;
    /**
     * 索引昨日峰值写入tps
     */
    private double   writeTps;
    /**
     * 索引对应的topic
     */
    private List<String> topics = new ArrayList<>();
    /**
     * 索引存储容量
     */
    private double storeBytes;
}
