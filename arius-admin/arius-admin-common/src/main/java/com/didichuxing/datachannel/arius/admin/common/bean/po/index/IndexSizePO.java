package com.didichuxing.datachannel.arius.admin.common.bean.po.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: D10865
 * @description: 索引历史大小，用于mapping优化计算得到优化成果
 * @date: Create on 2019/1/15 下午7:44
 * @modified By D10865
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IndexSizePO extends BaseESPO {
    /**
     * 索引名称，会去除末尾版本号
     */
    private String indexName;
    /**
     * 索引模板名称
     */
    private String templateName;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 索引日期
     */
    private String date;
    /**
     * 主shard大小，单位字节
     */
    private Long primaryStoreSize;
    /**
     * 单位化主shard磁盘大小
     */
    private String unitStoreSize;
    /**
     * 文档个数
     */
    private Long docsCount;
    /**
     * 流入流量
     */
    private String sourceUnitSize;
    /**
     * 流入流量 字节
     */
    private Long sourceSize;
    /**
     * 压缩比 primaryStoreSize/sourceSize
     */
    private Double compressionRatio;
    /**
     * 写入日期
     */
    private String sinkDate;
    /**
     * 索引大小，包括副本(如果有的话)，单位是字节
     */
    private Long totalStoreSize;
    /**
     * 单位化索引大小，包括副本(如果有的话)
     */
    private String unitTotalStoreSize;

    public boolean existCR() {
        if(compressionRatio==null) {
            return false;
        }

        if(compressionRatio<=0) {
            return false;
        }

        return true;
    }


    /**
     * 是否为有效的日期
     *
     * @return
     */
    @JSONField(serialize = false)
    public boolean isVaildDate() {
        if (StringUtils.isBlank(date)) {
            return true;
        }
        char[] chars = date.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            if (!Character.isDigit(chars[i]) && chars[i] != '-') {
                return false;
            }
        }

        return true;
    }

    @JSONField(serialize = false)
    @Override
    public String getKey() {
        // 以索引名称命名会出现双写的索引覆盖问题
        return String.format("%s_%s", clusterName, indexName);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
