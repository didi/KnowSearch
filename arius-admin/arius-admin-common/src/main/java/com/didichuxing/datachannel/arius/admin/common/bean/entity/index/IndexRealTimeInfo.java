package com.didichuxing.datachannel.arius.admin.common.bean.entity.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexRealTimeInfo {

    /**
     * 模板名称
     */
    private String template;

    /**
     * 集群名称
     */
    private String cluster;

    /**
     * 开始时间
     */
    private Date   start;

    /**
     * 结束时间
     */
    private Date   end;

    /**
     * 平均查询率
     */
    private double avgSearchQueryTotalRate;

    /**
     * 平均写入率
     */
    private double avgIndexingIndexTotalRate;

    /**
     * 平均查询时长
     */
    private double avgIndicesSearchQueryTime;

    @Override
    public String toString() {
        return "IndexInfo" + ": template=" + template + ": cluster=" + cluster + ": start=" + start.toString()
               + ": end=" + end.toString() + ": avgSearchQueryTotalRate=" + avgSearchQueryTotalRate
               + ", avgIndexingIndexTotalRate=" + avgIndexingIndexTotalRate + ", avgIndicesSearchQueryTime="
               + avgIndicesSearchQueryTime;
    }
}
