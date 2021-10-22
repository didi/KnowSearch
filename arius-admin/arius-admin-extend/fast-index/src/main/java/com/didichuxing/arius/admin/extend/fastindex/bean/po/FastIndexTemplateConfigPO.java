package com.didichuxing.arius.admin.extend.fastindex.bean.po;

import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class FastIndexTemplateConfigPO extends BaseESPO {
    private String name;

    /**
     * reducer数目系数， reducerNum=shardNum*expandfactor
     */
    private int expanfactor = -1;

    /**
     * 最近replicaDay天索引，配置多副本，标签业务
     */
    private int replicaDay = -1;

    /**
     * 数据转化配置
     */
    private String transformType = "normal";

    /**
     * 废弃
     */
    private boolean smallData = false;

    /**
     * 主键中类型是int的字段名
     */
    private String intFilterKeys = null;

    /**
     * 数字类型转化成string写入ES
     */
    private boolean longToStr = false;

    /**
     * 数据类型，如果hive数据为null，则写入数字0
     */
    private boolean longNullToZero = false;

    /**
     * 去除字符串2遍的中括号
     */
    private boolean removeBracket = false;

    /**
     * 是否调用标签系统的预发环境
     */
    private boolean isPassengerPre = false;

    /**
     * hive为null的之后，写入es的数据也是null
     */
    private boolean null2Null = false;

    /**
     * hive中该字段的内容指定当前数据属于哪个es的shard
     */
    private String shardField = null;

    /**
     * 将string解析成json的array写入es
     */
    private String strToArray = null;

    /**
     * 是否需要写入source，如果为false，则会再导入过程中修改索引的mapping配置
     */
    private boolean needSource = true;

    /**
     * 空间索引的geo字段名，配置则开始空间索引功能
     */
    private String spatialGeo = null;

    /**
     * 空间索引的cityId字段名
     */
    private String spatialCityId = null;

    /**
     * 数据加载过程中，是否停止索引写入
     */
    private boolean blockWriter = false;

    /**
     * 当前索引需要使用哪个版本的ES安装包导入数据
     */
    private String esVersion = null;

    /**
     * 写入过程中是否需要答应日志
     */
    private boolean printWriteLog = true;

    /**
     * 是否开始DCDR功能
     */
    private boolean enableDCDR = false;

    /**
     * 写es批次
     */
    private Integer batchSize = -1;

    @Override
    public String getKey() {
        return name;
    }
}
